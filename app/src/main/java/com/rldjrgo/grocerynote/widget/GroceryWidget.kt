package com.rldjrgo.grocerynote.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.rldjrgo.grocerynote.di.WidgetEntryPoint
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.widget.components.WidgetEmptyState
import com.rldjrgo.grocerynote.widget.components.WidgetItemRow
import com.rldjrgo.grocerynote.widget.components.WidgetStoreHeader
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

/**
 * Responsive grocery widget — Small (2x2), Medium (4x2 default), Large (4x4).
 *
 * Per project budget: `updatePeriodMillis = 0` (configured in widget XML). All updates
 * are user-triggered via [WidgetUpdater] from the app or [CheckItemAction] from a check tap.
 */
class GroceryWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(WidgetSizes.Small, WidgetSizes.Medium, WidgetSizes.Large)
    )

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Pull data eagerly from Room via Hilt EntryPoint. We read the current snapshot,
        // not a long-lived Flow, because Glance regenerates the entire tree on update().
        val entry = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        )
        val stores = entry.storeRepository().observeActiveStores().first()
        val itemsByStore = stores.associate { store ->
            store.id to entry.itemRepository().observeActiveItems(store.id).first()
        }
        // Sort: marts with more items come first; ties keep displayOrder.
        val sorted = stores.sortedWith(
            compareByDescending<Store> { itemsByStore[it.id]?.size ?: 0 }
                .thenBy { it.displayOrder }
        )

        provideContent {
            val size = LocalSize.current
            WidgetContent(size, sorted, itemsByStore)
        }
    }

    @Composable
    private fun WidgetContent(
        size: DpSize,
        stores: List<Store>,
        itemsByStore: Map<Long, List<Item>>,
    ) {
        if (stores.isEmpty()) {
            WidgetCard {
                WidgetEmptyState(
                    title = "마트를 추가해주세요",
                    hint = "탭하면 앱이 열려요",
                )
            }
            return
        }
        val totalItems = itemsByStore.values.sumOf { it.size }
        if (totalItems == 0) {
            WidgetCard {
                WidgetEmptyState(
                    title = "추가할 항목이 없어요",
                    hint = "탭해서 추가하세요",
                )
            }
            return
        }
        WidgetCard {
            when {
                size.width >= WidgetSizes.Large.width && size.height >= WidgetSizes.Large.height ->
                    LargeLayout(stores, itemsByStore)
                size.width >= WidgetSizes.Medium.width ->
                    MediumLayout(stores, itemsByStore)
                else ->
                    SmallLayout(stores.first(), itemsByStore[stores.first().id].orEmpty())
            }
        }
    }

    @Composable
    private fun WidgetCard(content: @Composable () -> Unit) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(cardBgProvider())
                .cornerRadius(16.dp)
                .padding(8.dp),
        ) { content() }
    }

    // ── Small (2x2) ────────────────────────────────────────────────
    @Composable
    private fun SmallLayout(store: Store, items: List<Item>) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            WidgetStoreHeader(
                storeId = store.id,
                storeName = store.name,
                storeColor = store.color,
                itemCount = items.size,
                dotSize = 6.dp,
                nameSize = 13.sp,
                countSize = 11.sp,
            )
            Spacer(GlanceModifier.height(4.dp))
            val shown = items.take(4)
            shown.forEach { item ->
                WidgetItemRow(
                    storeId = store.id,
                    itemId = item.id,
                    name = item.name,
                    checkboxSize = 16.dp,
                    nameSize = 12.sp,
                )
            }
            if (items.size > shown.size) {
                Text(
                    text = "외 ${items.size - shown.size}개",
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

    // ── Medium (4x2) — default ─────────────────────────────────────
    @Composable
    private fun MediumLayout(stores: List<Store>, itemsByStore: Map<Long, List<Item>>) {
        val firstTwo = stores.take(2).filter { (itemsByStore[it.id]?.size ?: 0) > 0 }
        if (firstTwo.size <= 1) {
            // Single mart enlarged
            val store = firstTwo.firstOrNull() ?: stores.first()
            val items = itemsByStore[store.id].orEmpty()
            Column(modifier = GlanceModifier.fillMaxSize()) {
                WidgetStoreHeader(store.id, store.name, store.color, items.size)
                Spacer(GlanceModifier.height(4.dp))
                items.take(5).forEach { i ->
                    WidgetItemRow(storeId = store.id, itemId = i.id, name = i.name)
                }
            }
        } else {
            Row(modifier = GlanceModifier.fillMaxSize()) {
                MartColumn(firstTwo[0], itemsByStore[firstTwo[0].id].orEmpty(), 3, modifier = GlanceModifier.defaultWeight().fillMaxHeight())
                Box(
                    modifier = GlanceModifier
                        .width(0.5.dp)
                        .fillMaxHeight()
                        .background(dividerProvider()),
                ) {}
                MartColumn(firstTwo[1], itemsByStore[firstTwo[1].id].orEmpty(), 3, modifier = GlanceModifier.defaultWeight().fillMaxHeight())
            }
        }
    }

    // ── Large (4x4) ────────────────────────────────────────────────
    @Composable
    private fun LargeLayout(stores: List<Store>, itemsByStore: Map<Long, List<Item>>) {
        val shown = stores.take(5).filter { (itemsByStore[it.id]?.size ?: 0) > 0 }.take(5)
        Column(modifier = GlanceModifier.fillMaxSize()) {
            shown.forEachIndexed { idx, store ->
                MartColumn(
                    store = store,
                    items = itemsByStore[store.id].orEmpty(),
                    maxItems = 4,
                    modifier = GlanceModifier.fillMaxWidth(),
                    itemCheckboxSize = 20.dp,
                    itemNameSize = 14.sp,
                )
                if (idx < shown.lastIndex) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(dividerProvider()),
                    ) {}
                }
            }
        }
    }

    @Composable
    private fun MartColumn(
        store: Store,
        items: List<Item>,
        maxItems: Int,
        modifier: GlanceModifier = GlanceModifier,
        itemCheckboxSize: androidx.compose.ui.unit.Dp = 18.dp,
        itemNameSize: androidx.compose.ui.unit.TextUnit = 13.sp,
    ) {
        Column(modifier = modifier.padding(2.dp)) {
            WidgetStoreHeader(
                storeId = store.id,
                storeName = store.name,
                storeColor = store.color,
                itemCount = items.size,
            )
            Spacer(GlanceModifier.height(2.dp))
            items.take(maxItems).forEach { i ->
                WidgetItemRow(
                    storeId = store.id,
                    itemId = i.id,
                    name = i.name,
                    checkboxSize = itemCheckboxSize,
                    nameSize = itemNameSize,
                )
            }
            if (items.size > maxItems) {
                Text(
                    text = "외 ${items.size - maxItems}개",
                    style = TextStyle(
                        color = textTertiaryProvider(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    modifier = GlanceModifier.padding(horizontal = 12.dp, vertical = 2.dp),
                )
            }
        }
    }
}
