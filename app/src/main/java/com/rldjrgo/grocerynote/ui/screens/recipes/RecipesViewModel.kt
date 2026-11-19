package com.rldjrgo.grocerynote.ui.screens.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import com.rldjrgo.grocerynote.data.recipes.Recipe
import com.rldjrgo.grocerynote.data.recipes.RecipeBook
import com.rldjrgo.grocerynote.data.recipes.normalizeRecipeQuery
import com.rldjrgo.grocerynote.data.repository.ItemRepository
import com.rldjrgo.grocerynote.data.repository.StoreRepository
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.util.Analytics
import com.rldjrgo.grocerynote.util.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipesUiState(
    val loading: Boolean = true,
    /** Every dish, in file order (grouped by [Recipe.category]). */
    val recipes: List<Recipe> = emptyList(),
    val query: String = "",
    /** Dishes matching [query] (empty query → empty; the screen shows the full list instead). */
    val results: List<Recipe> = emptyList(),
    val recent: List<Recipe> = emptyList(),
    /** Everyday dishes offered as one-tap pills so nobody has to type at the mart. */
    val popular: List<Recipe> = emptyList(),
    val opened: Recipe? = null,
    /** Ingredient names the user ticked on the opened dish. Nothing is pre-ticked. */
    val picked: Set<String> = emptySet(),
    val stores: List<Store> = emptyList(),
    val selectedStoreId: Long? = null,
    /**
     * Names already on a shopping list (lowercased) → ids of the marts holding them.
     * Only a name on the SELECTED mart is greyed out — the same thing may well be
     * wanted at another mart too (user 2026-11-19).
     */
    val existing: Map<String, Set<Long>> = emptyMap(),
) {
    /** True when [name] is already on the selected mart's list (→ can't be added again). */
    fun onSelectedMart(name: String): Boolean =
        selectedStoreId != null && existing[RecipesViewModel.itemKey(name)]?.contains(selectedStoreId) == true

    /** Other marts (not the selected one) that already hold [name] — shown as a note only. */
    fun otherMartsWith(name: String): List<String> =
        existing[RecipesViewModel.itemKey(name)].orEmpty()
            .filter { it != selectedStoreId }
            .mapNotNull { id -> stores.firstOrNull { it.id == id }?.name }
}

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val book: RecipeBook,
    private val storeRepo: StoreRepository,
    private val itemRepo: ItemRepository,
    private val settings: SettingsDataStore,
    private val widgetUpdater: WidgetUpdater,
) : ViewModel() {

    private data class Local(
        val recipes: List<Recipe>? = null,
        val query: String = "",
        val opened: Recipe? = null,
        val picked: Set<String> = emptySet(),
        val storeId: Long? = null,
    )

    private val local = MutableStateFlow(Local())

    val uiState: StateFlow<RecipesUiState> = combine(
        local,
        storeRepo.observeActiveStores(),
        itemRepo.observeAllItems(),
        settings.recentRecipes,
    ) { l, stores, items, recentNames ->
        val recipes = l.recipes.orEmpty()
        val storeIds = stores.mapTo(HashSet()) { it.id }
        val existing = HashMap<String, MutableSet<Long>>()
        items.forEach {
            if (!it.isCompleted && it.storeId in storeIds) existing.getOrPut(itemKey(it.name)) { HashSet() } += it.storeId
        }
        val selected = l.storeId?.takeIf { it in storeIds } ?: stores.firstOrNull()?.id
        val byName = recipes.associateBy { it.name }
        RecipesUiState(
            loading = l.recipes == null,
            recipes = recipes,
            query = l.query,
            results = search(recipes, l.query),
            recent = recentNames.mapNotNull { byName[it] },
            popular = POPULAR.mapNotNull { byName[it] }.filter { it.name !in recentNames },
            opened = l.opened,
            picked = l.picked.filterNot { existing[itemKey(it)]?.contains(selected) == true }.toSet(),
            stores = stores,
            selectedStoreId = selected,
            existing = existing,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipesUiState())

    init {
        viewModelScope.launch {
            val recipes = book.load()
            local.update { it.copy(recipes = recipes) }
        }
        viewModelScope.launch {
            val last = settings.recipeLastStoreId.first()
            if (last > 0) local.update { if (it.storeId == null) it.copy(storeId = last) else it }
        }
    }

    private var missJob: Job? = null
    private val reportedMisses = HashSet<String>()

    fun setQuery(q: String) {
        local.update { it.copy(query = q) }
        // Typing pauses for 2s on something we don't have → remember it (once per text)
        // so the table can grow. Not sent per keystroke.
        missJob?.cancel()
        missJob = viewModelScope.launch {
            delay(2_000)
            val recipes = local.value.recipes ?: return@launch
            val text = local.value.query.trim()
            if (text.length >= 2 && search(recipes, text).isEmpty() && reportedMisses.add(text)) {
                Analytics.recipeSearchMiss(text)
            }
        }
    }

    fun open(recipe: Recipe) {
        local.update { it.copy(opened = recipe, picked = emptySet()) }
        Analytics.recipeOpened(recipe.name)
        viewModelScope.launch { settings.pushRecentRecipe(recipe.name) }
    }

    fun close() = local.update { it.copy(opened = null, picked = emptySet()) }

    fun toggle(item: String) = local.update {
        it.copy(picked = if (item in it.picked) it.picked - item else it.picked + item)
    }

    /** "전체 선택" on a group: tick everything not already on a list; tap again to clear the group. */
    fun toggleAll(items: List<String>) {
        val state = uiState.value
        val free = items.filterNot(state::onSelectedMart)
        local.update {
            it.copy(picked = if (it.picked.containsAll(free)) it.picked - free.toSet() else it.picked + free)
        }
    }

    fun selectStore(id: Long) = local.update { it.copy(storeId = id) }

    /** Add the ticked names to the selected mart. [onDone] gets (count, mart name). */
    fun addPicked(onDone: (Int, String) -> Unit) {
        val state = uiState.value
        val store = state.stores.firstOrNull { it.id == state.selectedStoreId } ?: return
        // Keep the dish's own order (재료 → 넣어도 좋은 것 → 양념 → 준비물), not tap order.
        val order = state.opened?.let { it.main + it.optional + it.pantry + it.supplies }.orEmpty()
        val names = order.filter { it in state.picked && !state.onSelectedMart(it) }
        if (names.isEmpty()) return
        viewModelScope.launch {
            names.forEach { itemRepo.addItem(store.id, it) }
            settings.setRecipeLastStoreId(store.id)
            local.update { it.copy(picked = emptySet(), storeId = store.id) }
            widgetUpdater.updateAll()
            Analytics.recipeItemsAdded(names.size)
            onDone(names.size, store.name)
        }
    }

    private fun search(recipes: List<Recipe>, query: String): List<Recipe> {
        val q = normalizeRecipeQuery(query)
        if (q.isEmpty()) return emptyList()
        // Name-prefix hits first, then any name/alias containing the text.
        val (starts, rest) = recipes
            .filter { r -> r.searchKeys.any { it.contains(q) } }
            .partition { it.searchKeys.first().startsWith(q) }
        return starts + rest
    }

    companion object {
        /** Must match names in assets/recipes.txt (missing ones are simply skipped). */
        private val POPULAR = listOf(
            "김치찌개", "된장찌개", "미역국", "제육볶음", "카레라이스", "김밥",
            "떡볶이", "닭볶음탕", "소불고기", "잡채", "계란말이", "삼겹살 구이",
        )

        /** Same rule the lists use to call two names "the same item". */
        fun itemKey(name: String): String = name.trim().lowercase()
    }
}
