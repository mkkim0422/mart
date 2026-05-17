package com.rldjrgo.grocerynote.widget.common

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.rldjrgo.grocerynote.di.WidgetEntryPoint
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.domain.model.emoji
import com.rldjrgo.grocerynote.widget.actions.OpenStoreAction
import com.rldjrgo.grocerynote.widget.cardBgProvider
import com.rldjrgo.grocerynote.widget.dividerProvider
import com.rldjrgo.grocerynote.widget.textTertiaryProvider
import com.rldjrgo.grocerynote.widget.components.WidgetEmptyState
import com.rldjrgo.grocerynote.widget.components.WidgetItemRow
import com.rldjrgo.grocerynote.widget.components.WidgetStoreCountRow
import com.rldjrgo.grocerynote.widget.components.WidgetStoreHeader
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

/** Snapshot of what every widget renders: active stores (item-count desc) + their active items. */
data class WidgetData(
    val stores: List<Store>,
    val itemsByStore: Map<Long, List<Item>>,
    /** User-chosen stores for the Large widget, in order. Empty = auto. */
    val largeStoreIds: List<Long> = emptyList(),
)

private fun buildWidgetData(
    stores: List<Store>,
    allItems: List<Item>,
    largeIds: List<Long>,
): WidgetData {
    val activeItems = allItems.filter { !it.isCompleted }
    val grouped = activeItems.groupBy { it.storeId }
    val itemsByStore: Map<Long, List<Item>> = stores.associate { store ->
        store.id to grouped[store.id].orEmpty()
            .sortedWith(compareBy({ it.displayOrder }, { it.id }))
    }
    val sorted = stores.sortedWith(
        compareByDescending<Store> { itemsByStore[it.id]?.size ?: 0 }
            .thenBy { it.displayOrder }
    )
    Log.d(
        "WidgetUpdater",
        "buildWidgetData stores=${stores.size} activeItems=${activeItems.size} " +
            "byStore=${itemsByStore.mapValues { it.value.size }}",
    )
    return WidgetData(sorted, itemsByStore, largeIds)
}

/**
 * Reactive data for every widget. Subscribed INSIDE provideContent via
 * collectAsState — while the widget session is alive, any DB change re-emits
 * and Glance recomposes (no reliance on update() re-running provideGlance,
 * which Glance does not reliably do).
 */
fun widgetDataFlow(context: Context): Flow<WidgetData> {
    val entry = EntryPointAccessors.fromApplication(
        context.applicationContext,
        WidgetEntryPoint::class.java,
    )
    return combine(
        entry.storeRepository().observeActiveStores(),
        entry.itemRepository().observeAllItems(),
        entry.settingsDataStore().largeWidgetStoreIds,
    ) { stores, allItems, largeIds ->
        buildWidgetData(stores, allItems, largeIds)
    }.catch { emit(WidgetData(emptyList(), emptyMap())) }
}

@Composable
fun WidgetCard(content: @Composable () -> Unit) {
    // Tapping ANYWHERE on the widget card → open the app. The per-mart "+"
    // buttons sit on top with their own click (add-item for that mart).
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(cardBgProvider())
            .cornerRadius(22.dp)
            .clickable(OpenStoreAction.forStore(storeId = -1L))
            .padding(8.dp),
    ) { content() }
}

private fun WidgetData.totalItems(): Int = itemsByStore.values.sumOf { it.size }

@Composable
private fun EmptyOr(
    data: WidgetData,
    compact: Boolean = false,
    body: @Composable () -> Unit,
) {
    when {
        compact && (data.stores.isEmpty() || data.totalItems() == 0) -> WidgetCard {
            // 2x1: too short for a 3-line illustration → small cart + one hint.
            WidgetEmptyState(title = "", hint = "탭해서 추가", compact = true)
        }
        data.stores.isEmpty() -> WidgetCard {
            WidgetEmptyState(title = "마트를 추가해주세요", hint = "탭하면 앱이 열려요")
        }
        data.totalItems() == 0 -> WidgetCard {
            WidgetEmptyState(title = "항목 추가", hint = "탭해서 추가하세요")
        }
        else -> WidgetCard { body() }
    }
}

// ── Small (2x2) — per-mart remaining counts ("어디 들를지" at a glance) ──
// [compact] = the 2x1 widget: identical row content, only the empty state shrinks.
@Composable
fun SmallContent(data: WidgetData, compact: Boolean = false) = EmptyOr(data, compact) {
    // 2x1 (compact) is only ~1 cell tall → 2 rows fit; a 3rd would clip. Show 2.
    val shown = data.stores.take(if (compact) 2 else 4)
    Column(modifier = GlanceModifier.fillMaxSize()) {
        shown.forEach { store ->
            WidgetStoreCountRow(
                storeId = store.id,
                storeName = store.name,
                storeColor = store.color,
                storeEmoji = store.emoji(),
                count = data.itemsByStore[store.id]?.size ?: 0,
            )
        }
        if (!compact && data.stores.size > shown.size) {
            Text(
                text = "외 ${data.stores.size - shown.size}개 마트",
                style = TextStyle(
                    color = textTertiaryProvider(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                ),
                modifier = GlanceModifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

// ── Medium (4x2): 1 mart = full width, else top-2 side-by-side ──
@Composable
fun MediumContent(data: WidgetData) = EmptyOr(data) {
    // data.stores is already item-count-desc. Active = has ≥1 item.
    // Every mart the user added items to stays visible (no dominant-hide).
    val active = data.stores.filter { (data.itemsByStore[it.id]?.size ?: 0) > 0 }
    val single = active.size <= 1
    if (single) {
        val s = active.firstOrNull() ?: data.stores.first()
        MartColumn(
            store = s,
            items = data.itemsByStore[s.id].orEmpty(),
            maxItems = 8,
            modifier = GlanceModifier.fillMaxSize(),
            areaPadding = 6.dp,
        )
    } else {
        val t = active.take(2)
        Row(modifier = GlanceModifier.fillMaxSize()) {
            MartColumn(t[0], data.itemsByStore[t[0].id].orEmpty(), 4, modifier = GlanceModifier.defaultWeight().fillMaxHeight(), areaPadding = 12.dp)
            Box(modifier = GlanceModifier.width(1.dp).fillMaxHeight().background(dividerProvider())) {}
            MartColumn(t[1], data.itemsByStore[t[1].id].orEmpty(), 4, modifier = GlanceModifier.defaultWeight().fillMaxHeight(), areaPadding = 12.dp)
        }
    }
}

// ── Large (4x4): up to 4 marts — 1 / 2 / (1 top + 2) / 2x2 ──
@Composable
fun LargeContent(data: WidgetData) = EmptyOr(data) {
    fun cnt(s: Store) = data.itemsByStore[s.id]?.size ?: 0
    val base = if (data.largeStoreIds.isNotEmpty()) {
        data.largeStoreIds.mapNotNull { id -> data.stores.find { it.id == id } }
    } else {
        data.stores.filter { cnt(it) > 0 }
    }
    // Every mart with items stays visible (no dominant-hide), capped at 4.
    val active = base.ifEmpty { data.stores }.take(4)

    @Composable
    fun fullCol(s: Store) {
        MartColumn(
            s, data.itemsByStore[s.id].orEmpty(), 12,
            modifier = GlanceModifier.fillMaxSize(), areaPadding = 8.dp, itemNameSize = 14.sp,
        )
    }

    when {
        active.size == 1 -> fullCol(active[0])
        active.size == 2 -> Row(modifier = GlanceModifier.fillMaxSize()) {
            MartColumn(active[0], data.itemsByStore[active[0].id].orEmpty(), 6, modifier = GlanceModifier.defaultWeight().fillMaxHeight(), areaPadding = 10.dp)
            Box(modifier = GlanceModifier.width(1.dp).fillMaxHeight().background(dividerProvider())) {}
            MartColumn(active[1], data.itemsByStore[active[1].id].orEmpty(), 6, modifier = GlanceModifier.defaultWeight().fillMaxHeight(), areaPadding = 10.dp)
        }
        active.size == 3 -> Column(modifier = GlanceModifier.fillMaxSize()) {
            MartColumn(active[0], data.itemsByStore[active[0].id].orEmpty(), 4, modifier = GlanceModifier.fillMaxWidth().defaultWeight(), areaPadding = 8.dp)
            Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(dividerProvider())) {}
            GridRow(active[1], active[2], data, 3, GlanceModifier.defaultWeight().fillMaxWidth())
        }
        else -> Column(modifier = GlanceModifier.fillMaxSize()) {
            GridRow(active[0], active[1], data, 3, GlanceModifier.defaultWeight().fillMaxWidth())
            Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(dividerProvider())) {}
            GridRow(active[2], active[3], data, 3, GlanceModifier.defaultWeight().fillMaxWidth())
        }
    }
}

@Composable
private fun GridRow(
    left: Store,
    right: Store,
    data: WidgetData,
    per: Int,
    modifier: GlanceModifier,
) {
    Row(modifier = modifier) {
        MartColumn(left, data.itemsByStore[left.id].orEmpty(), per, modifier = GlanceModifier.defaultWeight().fillMaxHeight(), areaPadding = 8.dp)
        Box(modifier = GlanceModifier.width(1.dp).fillMaxHeight().background(dividerProvider())) {}
        MartColumn(right, data.itemsByStore[right.id].orEmpty(), per, modifier = GlanceModifier.defaultWeight().fillMaxHeight(), areaPadding = 8.dp)
    }
}

@Composable
private fun MartColumn(
    store: Store,
    items: List<Item>,
    maxItems: Int,
    modifier: GlanceModifier = GlanceModifier,
    areaPadding: androidx.compose.ui.unit.Dp = 2.dp,
    itemCheckboxSize: androidx.compose.ui.unit.Dp = 16.dp,
    itemNameSize: androidx.compose.ui.unit.TextUnit = 14.sp,
) {
    // Header fixed on top; the item list is a SCROLLABLE LazyColumn so EVERY
    // item is reachable. (A plain Column clipped the list to whatever fit the
    // widget height → user saw only 2 of 5 with no "more" hint. maxItems is no
    // longer a hard cap — scrolling handles overflow.)
    // The whole column is clickable → open app. Empty space, the header text
    // and (for an empty mart) the list area all fall through to this so the
    // user never taps a "dead" zone. The "+" button and item rows have their
    // own clicks (RemoteViews: one click per view, innermost wins).
    Column(
        modifier = modifier
            .padding(areaPadding)
            .clickable(OpenStoreAction.forStore(storeId = -1L)),
    ) {
        WidgetStoreHeader(
            storeId = store.id,
            storeName = store.name,
            storeColor = store.color,
            storeEmoji = store.emoji(),
            itemCount = items.size,
        )
        when {
            items.isEmpty() -> {
                // No list → explicit clickable filler so the empty area opens
                // the app (a bare ListView swallows taps, breaking fall-through).
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .clickable(OpenStoreAction.forStore(storeId = -1L)),
                ) {}
            }
            items.size <= maxItems -> {
                // Fits without scrolling → plain Column. The leftover space is
                // then the (clickable) parent Column's, not a ListView's, so
                // tapping the blank area below the list opens the app.
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    items.forEach { i ->
                        WidgetItemRow(
                            storeId = store.id,
                            itemId = i.id,
                            name = i.name,
                            checkboxSize = itemCheckboxSize,
                            nameSize = itemNameSize,
                        )
                    }
                }
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .clickable(OpenStoreAction.forStore(storeId = -1L)),
                ) {}
            }
            else -> {
                // Overflow → scrollable LazyColumn (content fills, no dead zone).
                LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    items(items, itemId = { it.id }) { i ->
                        WidgetItemRow(
                            storeId = store.id,
                            itemId = i.id,
                            name = i.name,
                            checkboxSize = itemCheckboxSize,
                            nameSize = itemNameSize,
                        )
                    }
                }
            }
        }
    }
}
