package com.rldjrgo.grocerynote.ui.screens.recipes

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rldjrgo.grocerynote.data.recipes.Recipe
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.domain.model.emoji
import com.rldjrgo.grocerynote.ui.components.AdBanner
import com.rldjrgo.grocerynote.ui.components.PageTitle
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.util.Analytics

private const val YOUTUBE_PACKAGE = "com.google.android.youtube"

/**
 * "만드는 법 보기": open YouTube on the search results for [query].
 * The YouTube app's own search intent is used first — the https "results?search_query="
 * link only landed on YouTube's home/blank screen on a Galaxy S24+ (user report
 * 2026-11-19). No YouTube app → the same search in the browser.
 */
private fun openHowTo(context: android.content.Context, query: String) {
    val inApp = Intent(Intent.ACTION_SEARCH)
        .setPackage(YOUTUBE_PACKAGE)
        .putExtra("query", query)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val web = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://m.youtube.com/results?search_query=" + Uri.encode(query)),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(inApp) }
        .recoverCatching { context.startActivity(web) }
        .onFailure { Toast.makeText(context, "열 수 있는 앱이 없어요", Toast.LENGTH_SHORT).show() }
}

/** Occasions (김장·캠핑…) aren't a single dish, so no "만드는 법 보기" there. */
private const val OCCASION_CATEGORY = "행사·모임"

/**
 * "요리별 재료" tab — pick a dish, see what to buy, tick what you need and add it
 * to a mart. Nothing is pre-ticked (we can't know what's at home), names already
 * on a list are greyed out, and there are no amounts.
 */
@Composable
fun RecipesScreen(viewModel: RecipesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    val opened = state.opened

    BackHandler(enabled = opened != null) { viewModel.close() }

    // 시즌 알림(설·추석·김장) 탭 → 그 체크리스트를 바로 연다.
    val deepLink by com.rldjrgo.grocerynote.DeepLinkBus.flow.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(deepLink) {
        val name = deepLink?.recipeName ?: return@LaunchedEffect
        com.rldjrgo.grocerynote.DeepLinkBus.flow.value = null
        viewModel.openByName(name)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
            .statusBarsPadding(),
    ) {
        if (opened == null) {
            RecipeList(state, viewModel)
        } else {
            RecipeDetail(opened, state, viewModel)
        }
    }
}

// ───────────────────────── list ─────────────────────────

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.RecipeList(
    state: RecipesUiState,
    viewModel: RecipesViewModel,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val focus = LocalFocusManager.current
    val open: (Recipe) -> Unit = { focus.clearFocus(); viewModel.open(it) }

    // Same rule as the other tabs: the screen title is the tab's own name.
    PageTitle(title = "요리별 재료", subtitle = "요리를 고르면 살 재료가 나와요")
    SearchField(state.query, viewModel::setQuery)

    val grouped = remember(state.recipes) { state.recipes.groupBy { it.category } }

    LazyColumn(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
    ) {
        if (state.query.isNotBlank()) {
            if (state.results.isEmpty() && !state.loading) {
                item(key = "none") {
                    Text(
                        "아직 없는 요리예요",
                        style = typo.body,
                        color = colors.textTertiary,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
            items(state.results.chunked(2), key = { "r-" + it.first().name }) { row -> RecipeRow(row, open) }
        } else {
            if (state.recent.isNotEmpty()) {
                item(key = "recent-h") { SectionHeader("최근 본 요리", null) }
                item(key = "recent") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.recent, key = { it.name }) { r ->
                            Pill(text = "${r.emoji} ${r.name}", onClick = { open(r) })
                        }
                    }
                }
            }
            if (state.popular.isNotEmpty()) {
                item(key = "popular-h") { SectionHeader("자주 찾는 요리", null) }
                item(key = "popular") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.popular, key = { it.name }) { r ->
                            Pill(text = "${r.emoji} ${r.name}", onClick = { open(r) })
                        }
                    }
                }
            }
            grouped.forEach { (category, list) ->
                item(key = "h-$category") { SectionHeader(category, "${list.size}") }
                items(list.chunked(2), key = { "c-" + it.first().name }) { row -> RecipeRow(row, open) }
            }
        }
    }
    AdBanner()
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val focus = LocalFocusManager.current
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.bgTertiary)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.textTertiary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text("요리 이름 (예: 김치찌개)", style = typo.body, color = colors.textTertiary)
            }
            BasicTextField(
                value = query,
                onValueChange = onChange,
                singleLine = true,
                textStyle = typo.body.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.brandPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "지우기",
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp).clickable { onChange("") },
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: String?) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = typo.title, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
        if (count != null) {
            Spacer(Modifier.width(6.dp))
            Text(count, style = typo.bodyS, color = colors.textTertiary)
        }
    }
}

@Composable
private fun RecipeRow(row: List<Recipe>, onOpen: (Recipe) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        row.forEach { r -> RecipeCard(r, Modifier.weight(1f)) { onOpen(r) } }
        if (row.size == 1) Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun RecipeCard(recipe: Recipe, modifier: Modifier, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Row(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(0.5.dp, colors.divider, RoundedCornerShape(12.dp))
            .background(colors.bgSecondary)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(recipe.emoji, style = typo.title)
        Spacer(Modifier.width(8.dp))
        Text(
            recipe.name,
            style = typo.body,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Pill(text: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        text,
        style = AppTheme.typography.bodyS,
        color = colors.textSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.bgTertiary)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

// ───────────────────────── detail ─────────────────────────

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.RecipeDetail(
    recipe: Recipe,
    state: RecipesUiState,
    viewModel: RecipesViewModel,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val context = LocalContext.current
    val store = state.stores.firstOrNull { it.id == state.selectedStoreId }
    val count = state.picked.size

    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "뒤로",
            tint = colors.textPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable { viewModel.close() }
                .padding(12.dp)
                .size(24.dp),
        )
        Text(
            "${recipe.emoji} ${recipe.name}",
            style = typo.headingL,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp),
    ) {
        // Right under the dish name: dish → how it's made → what to buy. Top of the page
        // is "about the dish", the bottom bar is "about buying" (user 2026-11-19). The old
        // "살 것만 누르세요" line here just repeated the button's own "살 것을 눌러 주세요".
        if (recipe.category != OCCASION_CATEGORY) {
            val shape = RoundedCornerShape(999.dp)
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .clip(shape)
                    .border(1.dp, colors.divider, shape)
                    .background(colors.bgPrimary)
                    .clickable {
                        Analytics.recipeHowTo(recipe.name)
                        // "황금레시피" is how people actually title/search cooking videos (user 2026-11-19).
                        openHowTo(context, "${recipe.name} 황금레시피")
                    }
                    .padding(start = 8.dp, end = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color(0xFFFF0000),
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(2.dp))
                Text("만드는 법 보기", style = typo.body, fontWeight = FontWeight.Medium, color = colors.textPrimary)
            }
        }
        IngredientGroup("재료", null, recipe.main, state, viewModel, selectAll = true)
        IngredientGroup("넣어도 좋은 것", null, recipe.optional, state, viewModel)
        IngredientGroup("양념", "집에 없는 것만", recipe.pantry, state, viewModel)
        IngredientGroup("준비물", null, recipe.supplies, state, viewModel, selectAll = true)
    }

    // Where to put them + the one button.
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(colors.divider))
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp)) {
        if (state.stores.size > 1 && store != null) {
            StorePicker(state.stores, store, viewModel::selectStore)
            Spacer(Modifier.height(12.dp))
        }
        val enabled = count > 0 && store != null
        Row(
            modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (enabled) colors.brandPrimary else colors.bgTertiary)
                    .clickable(enabled = enabled) {
                        viewModel.addPicked { n, martName ->
                            Toast.makeText(context, "${martName}에 ${n}개 담았어요", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when {
                        store == null -> "마트를 먼저 만들어 주세요"
                        count == 0 -> "살 것을 눌러 주세요"
                        else -> "${store.name}에 ${count}개 담기"
                    },
                    style = typo.title,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) androidx.compose.ui.graphics.Color.White else colors.textDisabled,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IngredientGroup(
    title: String,
    hint: String?,
    items: List<String>,
    state: RecipesUiState,
    viewModel: RecipesViewModel,
    selectAll: Boolean = false,
) {
    if (items.isEmpty()) return
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = typo.title, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
        if (hint != null) {
            Spacer(Modifier.width(8.dp))
            Text(hint, style = typo.bodyS, color = colors.textTertiary)
        }
        Spacer(Modifier.weight(1f))
        if (selectAll) {
            val free = items.filterNot(state::onSelectedMart)
            if (free.size > 1) {
                Text(
                    if (state.picked.containsAll(free)) "선택 해제" else "전체 선택",
                    style = typo.bodyS,
                    color = colors.brandPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.toggleAll(items) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
            }
        }
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { name ->
            val here = state.onSelectedMart(name)
            val storeName = state.stores.firstOrNull { it.id == state.selectedStoreId }?.name
            IngredientChip(
                name = name,
                selected = name in state.picked,
                onListAt = if (here) storeName else null,
                alsoAt = if (here) null else state.otherMartsWith(name).firstOrNull(),
                onClick = { viewModel.toggle(name) },
            )
        }
    }
}

@Composable
private fun IngredientChip(name: String, selected: Boolean, onListAt: String?, alsoAt: String?, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val shape = RoundedCornerShape(999.dp)
    val disabled = onListAt != null
    val bg = when {
        disabled -> colors.bgTertiary
        selected -> colors.brandPrimarySoft
        else -> colors.bgPrimary
    }
    val border = when {
        disabled -> colors.bgTertiary
        selected -> colors.brandPrimary
        else -> colors.divider
    }
    Row(
        modifier = Modifier
            .heightIn(min = 40.dp)
            .clip(shape)
            .border(1.dp, border, shape)
            .background(bg)
            .clickable(enabled = !disabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected && !disabled) {
            Text("✓ ", style = typo.body, fontWeight = FontWeight.Bold, color = colors.brandPrimary)
        }
        Text(
            name,
            style = typo.body,
            fontWeight = if (selected && !disabled) FontWeight.Medium else FontWeight.Normal,
            color = when {
                disabled -> colors.textDisabled
                selected -> colors.brandPrimary
                else -> colors.textPrimary
            },
        )
        if (disabled) {
            Text(" · ${onListAt}에 있음", style = typo.caption, color = colors.textTertiary)
        } else if (alsoAt != null) {
            // On another mart's list: still selectable, just so the user knows.
            Text(" · ${alsoAt}에도 있음", style = typo.caption, color = colors.textTertiary)
        }
    }
}

/** Marts shown as chips before the rest fold into "+N". */
private const val STORE_CHIPS_MAX = 5

/**
 * "담을 곳" — every mart as a chip, wrapping onto the next line (never a sideways
 * scroll: in this narrow strip it hid marts off-screen — user 2026-11-19). A pure
 * dropdown was tried the same day and dropped: two taps and hidden choices for the
 * usual 2–4 marts. Only when there are more than [STORE_CHIPS_MAX] + 1 marts do the
 * extra ones fold into a "+N" chip that unfolds them in place.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StorePicker(stores: List<Store>, selected: Store, onSelect: (Long) -> Unit) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    var expanded by remember { mutableStateOf(false) }
    val many = stores.size > STORE_CHIPS_MAX + 1
    // Tab order; while folded, the chosen mart always stays visible.
    val shown = if (!many || expanded) stores else {
        val head = stores.take(STORE_CHIPS_MAX)
        if (head.any { it.id == selected.id }) head else head.dropLast(1) + selected
    }
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        shown.forEach { s -> StoreChip(s, selected = s.id == selected.id) { onSelect(s.id) } }
        if (many) {
            // "+N" unfolds the rest right here as more chips (no pop-up list — user
            // 2026-11-19); "접기" folds them back.
            val shape = RoundedCornerShape(999.dp)
            Text(
                if (expanded) "접기" else "+${stores.size - shown.size}",
                style = typo.bodyS,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
                modifier = Modifier
                    .clip(shape)
                    .background(colors.bgTertiary)
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun StoreChip(store: Store, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(999.dp)
    Text(
        "${store.emoji()} ${store.name}",
        style = AppTheme.typography.bodyS,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (selected) colors.textPrimary else colors.textSecondary,
        maxLines = 1,
        modifier = Modifier
            .clip(shape)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) store.color else colors.divider, shape)
            .background(if (selected) store.color.copy(alpha = 0.10f) else colors.bgPrimary)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}
