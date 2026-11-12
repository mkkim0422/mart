package com.rldjrgo.grocerynote.widget.common

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.rldjrgo.grocerynote.R
import com.rldjrgo.grocerynote.di.WidgetEntryPoint
import com.rldjrgo.grocerynote.widget.WidgetSizes
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.domain.model.emoji
import com.rldjrgo.grocerynote.widget.actions.OpenStoreAction
import com.rldjrgo.grocerynote.widget.cardBgProvider
import com.rldjrgo.grocerynote.widget.textTertiaryProvider
import com.rldjrgo.grocerynote.widget.components.WidgetEmptyState
import com.rldjrgo.grocerynote.widget.components.WidgetItemRow
import com.rldjrgo.grocerynote.widget.components.WidgetStoreCountRow
import com.rldjrgo.grocerynote.widget.components.WidgetStoreHeader
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.retryWhen

/** Snapshot of what every widget renders: active stores (item-count desc) + their active items. */
data class WidgetData(
    val stores: List<Store>,
    val itemsByStore: Map<Long, List<Item>>,
    /** 노출순서설정 (Settings): marts the user put first, in order. Empty = auto. */
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
    val counted = stores.sortedWith(
        compareByDescending<Store> { itemsByStore[it.id]?.size ?: 0 }
            .thenBy { it.displayOrder }
    )
    // 노출순서설정 applies to EVERY widget size: user-picked marts come first in
    // their chosen order, the rest keep item-count-desc. (Stable sort — before
    // this, only Large/Long read the setting and Medium/Small ignored it.)
    val sorted = if (largeIds.isEmpty()) counted else {
        val pos = largeIds.withIndex().associate { (i, id) -> id to i }
        counted.sortedBy { pos[it.id] ?: Int.MAX_VALUE }
    }
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
    }
        // A transient Room/DataStore error used to complete the flow with an
        // EMPTY snapshot — inside a live widget session that (a) replaced good
        // data with "추가할 항목이 없어요" and (b) killed the collector, so every
        // later in-app change was silently ignored until the session died.
        // Retry a few times instead; if it still fails, log and keep the last
        // good frame (provideGlance's initial load has its own fallback).
        .retryWhen { cause, attempt ->
            Log.w("WidgetUpdater", "widgetDataFlow failed (attempt $attempt)", cause)
            if (attempt < 3) { delay(300L * (attempt + 1)); true } else false
        }
        .catch { Log.e("WidgetUpdater", "widgetDataFlow gave up — keeping last frame", it) }
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

@Composable
private fun EmptyOr(
    data: WidgetData,
    compact: Boolean = false,
    body: @Composable () -> Unit,
) {
    // No items yet is NOT an empty widget any more: the mart rows/columns render as
    // usual (count "—", "아직 적은 게 없어요"); tapping a mart opens it. The old
    // "항목 추가 / 탭해서 추가하세요" card said the same thing twice.
    // Only "no marts at all" still gets the placeholder.
    when {
        compact && data.stores.isEmpty() -> WidgetCard {
            // 2x1: too short for a 3-line illustration → small cart + one hint.
            WidgetEmptyState(title = "", hint = "마트를 추가해주세요", compact = true)
        }
        data.stores.isEmpty() -> WidgetCard {
            WidgetEmptyState(title = "마트를 추가해주세요", hint = "탭하면 앱이 열려요")
        }
        else -> WidgetCard { body() }
    }
}

// ── Count widget (2x1 · 2x2, taller → more rows) — per-mart remaining counts ──
// "어디 들를지" at a glance. Row tap → that mart in the app; "+" (not on 2x1,
// too short) → add-item sheet for the first mart shown.
// [size] is the EXACT widget size (SizeMode.Exact): rows are fitted to the real
// height so the "+" never covers the last row's count pill.
@Composable
fun CountContent(size: DpSize, data: WidgetData) {
    val compact = size.height < WidgetSizes.CountCompactBelow
    EmptyOr(data, compact) {
        val rows = if (compact) 2 else
            ((size.height - CARD_PAD_V - FAB_ZONE) / COUNT_ROW_H).toInt().coerceAtLeast(2)
        val shown = data.stores.take(rows)
        Box(modifier = GlanceModifier.fillMaxSize()) {
            // 2x1 strip: center the 2 rows (One UI cells are tall — top-aligned rows
            // left a blank band underneath). With "+", rows stay on top above it.
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = if (compact) Alignment.CenterVertically else Alignment.Top,
            ) {
                shown.forEach { store ->
                    WidgetStoreCountRow(
                        storeId = store.id,
                        storeName = store.name,
                        storeColor = store.color,
                        storeEmoji = store.emoji(),
                        count = data.itemsByStore[store.id]?.size ?: 0,
                    )
                }
            }
            if (!compact) AddFab(null)
        }
    }
}

// ── List widget (2x2 and up) — ONE scrollable list, ○ checks in place ──
// [martId] = the mart picked when the widget was placed (long-press → 설정 to
// change); ALL_MARTS = every mart stacked in tab order, each under its header.
// Never two lists side by side: one widget may hold only ONE LazyColumn — 2+
// collection views made Glance fail to inflate (blank, dead widget, 2026-05).
@Composable
fun ListContent(data: WidgetData, martId: Long, checking: Set<Long>) = EmptyOr(data) {
    val single = data.stores.find { it.id == martId }
    val marts = if (single != null) listOf(single) else listOrder(data)
    Box(modifier = GlanceModifier.fillMaxSize()) {
        Column(modifier = GlanceModifier.fillMaxSize().padding(horizontal = 4.dp)) {
            if (single != null) {
                // One mart: header stays put, only the items scroll.
                Spacer(GlanceModifier.height(6.dp))
                StoreHeader(single, data)
            }
            LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                marts.forEachIndexed { index, store ->
                    if (single == null) {
                        item(itemId = -store.id) {
                            Column(modifier = GlanceModifier.fillMaxWidth()) {
                                Spacer(GlanceModifier.height(if (index == 0) 6.dp else 14.dp))
                                StoreHeader(store, data)
                            }
                        }
                    }
                    val items = data.itemsByStore[store.id].orEmpty()
                    if (items.isEmpty()) {
                        item(itemId = EMPTY_ROW_ID_BASE - store.id) { EmptyMartRow(store.id) }
                    } else {
                        items(items, itemId = { it.id }) { i ->
                            WidgetItemRow(
                                storeId = store.id,
                                itemId = i.id,
                                name = i.name,
                                checking = i.id in checking,
                            )
                        }
                    }
                }
                // Room under the last row so it can scroll clear of the "+" button.
                item(itemId = Long.MIN_VALUE) { Spacer(GlanceModifier.height(44.dp)) }
            }
        }
        AddFab(single?.id)
    }
}

/** 노출순서설정 marts first (user's order), then the rest in app tab order — stable while checking. */
private fun listOrder(data: WidgetData): List<Store> {
    val picked = data.largeStoreIds.mapNotNull { id -> data.stores.find { it.id == id } }
    val pickedIds = picked.mapTo(HashSet()) { it.id }
    return picked + data.stores.filter { it.id !in pickedIds }.sortedBy { it.displayOrder }
}

private const val EMPTY_ROW_ID_BASE = -1_000_000_000L
private val CARD_PAD_V = 16.dp     // WidgetCard padding, top + bottom
private val FAB_ZONE = 40.dp       // "+" (36dp) + gap
private val COUNT_ROW_H = 32.dp    // WidgetStoreCountRow: 20dp badge + 2×6dp padding

@Composable
private fun StoreHeader(store: Store, data: WidgetData) {
    Box(modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        WidgetStoreHeader(
            storeId = store.id,
            storeName = store.name,
            storeColor = store.color,
            storeEmoji = store.emoji(),
            itemCount = data.itemsByStore[store.id]?.size ?: 0,
        )
    }
}

@Composable
private fun EmptyMartRow(storeId: Long) {
    Text(
        text = "아직 적은 게 없어요",
        maxLines = 1,
        style = TextStyle(color = textTertiaryProvider(), fontSize = 12.sp),
        modifier = GlanceModifier
            .fillMaxWidth()
            .clickable(OpenStoreAction.addToStore(storeId))
            .padding(start = 12.dp, top = 6.dp, bottom = 10.dp),
    )
}

/**
 * The one "+" per widget, bottom-right, over the content. One-mart list widget →
 * add-item sheet for that mart; otherwise (전체 / count widget, [storeId] null) →
 * just open the app — guessing "the first mart" opened 쿠팡 when the user meant
 * another mart (user 2026-11-12).
 */
@Composable
private fun AddFab(storeId: Long?) {
    Box(
        modifier = GlanceModifier.fillMaxSize().padding(4.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Image(
            provider = ImageProvider(R.drawable.widget_fab),
            contentDescription = "추가",
            modifier = GlanceModifier
                .size(36.dp)
                .clickable(
                    if (storeId != null) OpenStoreAction.addToStore(storeId)
                    else OpenStoreAction.forStore(-1L),
                ),
        )
    }
}
