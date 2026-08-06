package com.rldjrgo.grocerynote.ui.screens.home

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import com.rldjrgo.grocerynote.data.repository.ItemRepository
import com.rldjrgo.grocerynote.data.repository.StoreRepository
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.reminder.ReminderScheduler
import com.rldjrgo.grocerynote.util.WidgetPinHelper
import com.rldjrgo.grocerynote.util.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val stores: List<Store> = emptyList(),
    val selectedStoreId: Long? = null,
    val activeItems: List<Item> = emptyList(),
    val itemCounts: Map<Long, Int> = emptyMap(),
    val recentItemNames: List<String> = emptyList(),
    val highlightItemId: Long? = null,
    val showWidgetBanner: Boolean = false,
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val storeRepo: StoreRepository,
    private val itemRepo: ItemRepository,
    private val widgetUpdater: WidgetUpdater,
    private val settings: SettingsDataStore,
    private val widgetPin: WidgetPinHelper,
    private val reminderScheduler: ReminderScheduler,
) : AndroidViewModel(application) {

    /** Emitted right after an item is completed so the UI can show an undo Snackbar. */
    data class UndoInfo(val itemId: Long, val name: String, val token: Long)

    private val _undoEvent = MutableStateFlow<UndoInfo?>(null)
    val undoEvent: StateFlow<UndoInfo?> = _undoEvent

    /** Set briefly after addStore so the tab strip can auto-scroll + pulse the new pill. */
    private val _newlyAddedStoreId = MutableStateFlow<Long?>(null)
    val newlyAddedStoreId: StateFlow<Long?> = _newlyAddedStoreId

    private val selectedStoreId = MutableStateFlow<Long?>(null)
    private val highlightItemId = MutableStateFlow<Long?>(null)
    private val recentNames = MutableStateFlow<List<String>>(emptyList())

    private val storesFlow = storeRepo.observeActiveStores()
    private val activeItemsFlow = selectedStoreId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else itemRepo.observeActiveItems(id)
    }
    private val countsFlow = itemRepo.observeActiveCounts()
    private val bannerFlow = combine(
        settings.hasAddedWidget,
        settings.hasDismissedWidgetBanner,
    ) { added, dismissed -> !added && !dismissed }

    val uiState: StateFlow<HomeUiState> = combine(
        storesFlow,
        selectedStoreId,
        activeItemsFlow,
        recentNames,
        highlightItemId,
    ) { stores, selId, items, names, hl ->
        HomeUiState(
            stores = stores,
            selectedStoreId = selId ?: stores.firstOrNull()?.id,
            activeItems = items,
            recentItemNames = names,
            highlightItemId = hl,
            isLoading = false,
        )
    }.combine(countsFlow) { state, counts ->
        state.copy(itemCounts = counts)
    }.combine(bannerFlow) { state, showBanner ->
        state.copy(showWidgetBanner = showBanner)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    init {
        refreshRecentNames()
        // Keep selectedStoreId valid. Critical after "전체 삭제": the old store ids
        // are gone and replaced by new ones — if we kept the stale id, addItem()
        // would insert with a non-existent store_id and the FK constraint would
        // reject it (item silently never saved → widget/app stay empty).
        viewModelScope.launch {
            storesFlow.collect { stores ->
                val cur = selectedStoreId.value
                if (stores.isNotEmpty() && (cur == null || stores.none { it.id == cur })) {
                    selectedStoreId.value = stores.first().id
                }
            }
        }
        // "자주 사는 항목" is per-mart → refresh whenever the selected store changes.
        viewModelScope.launch {
            selectedStoreId.collect { refreshRecentNames() }
        }
    }

    fun selectStore(id: Long) {
        selectedStoreId.value = id
    }

    /** Called by MainActivity when the widget deep-links into the app. */
    fun handleDeepLink(storeId: Long?, itemId: Long?) {
        storeId?.let { selectedStoreId.value = it }
        highlightItemId.value = itemId
    }

    fun clearHighlight() {
        highlightItemId.value = null
    }

    fun pinWidget(size: com.rldjrgo.grocerynote.util.WidgetSize): Boolean =
        widgetPin.pinWidget(size)

    fun dismissWidgetBanner() {
        viewModelScope.launch { settings.setHasDismissedWidgetBanner(true) }
    }

    fun addItem(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val stores = storesFlow.first()
            if (stores.isEmpty()) return@launch
            val cur = selectedStoreId.value
            // Never trust a stale id (post-wipe it points at a deleted store → FK fail).
            val sid = if (cur != null && stores.any { it.id == cur }) cur else stores.first().id
            if (selectedStoreId.value != sid) selectedStoreId.value = sid
            itemRepo.addItem(sid, name)
            refreshRecentNames()
            widgetUpdater.updateAll()
        }
    }

    fun completeItem(itemId: Long, itemName: String) {
        viewModelScope.launch {
            // A pending reminder on a now-bought item is pointless → drop it.
            itemRepo.setReminder(itemId, null)
            reminderScheduler.cancel(itemId)
            itemRepo.completeItem(itemId)
            _undoEvent.value = UndoInfo(itemId, itemName, System.currentTimeMillis())
            widgetUpdater.updateAll()
        }
    }

    /** Set a one-shot purchase reminder at [atMillis] (epoch). Caller ensures the
     *  POST_NOTIFICATIONS permission (13+) and that [atMillis] is in the future. */
    fun setReminder(itemId: Long, atMillis: Long) {
        viewModelScope.launch {
            itemRepo.setReminder(itemId, atMillis)
            reminderScheduler.schedule(itemId, atMillis)
            widgetUpdater.updateAll()
        }
    }

    fun clearReminder(itemId: Long) {
        viewModelScope.launch {
            itemRepo.setReminder(itemId, null)
            reminderScheduler.cancel(itemId)
            widgetUpdater.updateAll()
        }
    }

    /** Snackbar [되돌리기] → put the item back into the active list. */
    fun undoComplete(itemId: Long) {
        viewModelScope.launch {
            itemRepo.reactivateItem(itemId)
            _undoEvent.value = null
            widgetUpdater.updateAll()
        }
    }

    fun consumeUndoEvent() {
        _undoEvent.value = null
    }

    fun updateItemName(itemId: Long, newName: String) {
        viewModelScope.launch {
            itemRepo.renameItem(itemId, newName)
            refreshRecentNames()
            widgetUpdater.updateAll()
        }
    }

    fun moveItem(itemId: Long, newStoreId: Long) {
        viewModelScope.launch {
            itemRepo.moveItemToStore(itemId, newStoreId)
            widgetUpdater.updateAll()
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            reminderScheduler.cancel(itemId)
            itemRepo.deleteItem(itemId)
            widgetUpdater.updateAll()
        }
    }

    fun addStore(name: String, color: Color, iconKey: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newId = storeRepo.addStore(name, color, iconKey)
            selectedStoreId.value = newId
            _newlyAddedStoreId.value = newId
            widgetUpdater.updateAll()
            kotlinx.coroutines.delay(1_500)
            if (_newlyAddedStoreId.value == newId) _newlyAddedStoreId.value = null
        }
    }

    /** Persist a tab-strip drag reorder (jiggle edit mode). */
    fun reorderStores(orderedIds: List<Long>) {
        viewModelScope.launch {
            storeRepo.reorder(orderedIds)
            widgetUpdater.updateAll()
        }
    }

    fun deleteStore(storeId: Long) {
        viewModelScope.launch {
            storeRepo.deleteStore(storeId)
            if (selectedStoreId.value == storeId) selectedStoreId.value = null
            widgetUpdater.updateAll()
        }
    }

    private fun refreshRecentNames() {
        viewModelScope.launch {
            val sid = selectedStoreId.value ?: storesFlow.first().firstOrNull()?.id
            recentNames.value =
                if (sid == null) emptyList() else itemRepo.recentItemNamesByStore(sid, 20)
        }
    }

    /** Delete a "자주 사는 항목" entry from the current mart. */
    fun deleteFrequentItem(name: String) {
        viewModelScope.launch {
            val sid = selectedStoreId.value ?: return@launch
            itemRepo.deleteByStoreAndName(sid, name)
            refreshRecentNames()
            widgetUpdater.updateAll()
        }
    }
}
