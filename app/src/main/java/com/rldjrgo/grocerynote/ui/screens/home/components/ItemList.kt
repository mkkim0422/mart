package com.rldjrgo.grocerynote.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun ItemList(
    items: List<Item>,
    storeColor: Color,
    highlightItemId: Long?,
    onCompleteAnimDone: (Long, String) -> Unit,
    onRename: (Item) -> Unit,
    onMove: (Item) -> Unit,
    onDelete: (Item) -> Unit,
    onSetReminder: (Item) -> Unit,
    modifier: Modifier = Modifier,
    searchLabel: String? = null,
    onSearch: (Item) -> Unit = {},
    /** One-time tip shown above the list on online-mart tabs; null = hidden. */
    searchHint: String? = null,
    onSearchHintDismiss: () -> Unit = {},
    /** Long-press-drag reorder finished → persist this top-to-bottom id order. */
    onReorder: (List<Long>) -> Unit = {},
) {
    val colors = AppTheme.colors
    val haptics = LocalHapticFeedback.current

    // Local working copy so the drag is instant (same merge pattern as
    // StoreTabBar): keep local order for surviving ids so a just-finished drag
    // doesn't snap back before the DB write lands; drop removed, append new.
    var localItems by remember { mutableStateOf(items) }
    var dragging by remember { mutableStateOf(false) }
    LaunchedEffect(items, dragging) {
        if (dragging) return@LaunchedEffect
        val byId = items.associateBy { it.id }
        val kept = localItems.mapNotNull { byId[it.id] }
        val keptIds = kept.mapTo(HashSet()) { it.id }
        localItems = kept + items.filter { it.id !in keptIds }
    }

    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        // Keys, not indices — the optional hint card shifts indices by one.
        val fromIdx = localItems.indexOfFirst { it.id == from.key }
        val toIdx = localItems.indexOfFirst { it.id == to.key }
        if (fromIdx >= 0 && toIdx >= 0) {
            localItems = localItems.toMutableList().apply { add(toIdx, removeAt(fromIdx)) }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgPrimary),
        // Bottom inset clears the floating 음성추가/추가 FAB row (56dp + 18dp margin)
        // so the last items can scroll above the buttons instead of hiding behind them.
        contentPadding = PaddingValues(top = 8.dp, bottom = 92.dp, start = 0.dp, end = 0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        if (searchHint != null) {
            item(key = "search_hint") {
                SearchHintCard(
                    text = searchHint,
                    tint = storeColor,
                    onDismiss = onSearchHintDismiss,
                )
            }
        }
        items(localItems, key = { it.id }) { item ->
            ReorderableItem(reorderState, key = item.id) { isDragging ->
                ItemRow(
                    item = item,
                    storeColor = storeColor,
                    highlighted = item.id == highlightItemId,
                    onCompleteAnimDone = { onCompleteAnimDone(item.id, item.name) },
                    onRename = { onRename(item) },
                    onMove = { onMove(item) },
                    onDelete = { onDelete(item) },
                    onSetReminder = { onSetReminder(item) },
                    searchLabel = searchLabel,
                    onSearch = { onSearch(item) },
                    // Hold a row → lift (haptic) → drag up/down to reorder.
                    dragModifier = Modifier
                        .graphicsLayer {
                            if (isDragging) {
                                shadowElevation = 8.dp.toPx()
                                scaleX = 1.02f
                                scaleY = 1.02f
                            }
                        }
                        .longPressDraggableHandle(
                            onDragStarted = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                dragging = true
                            },
                            onDragStopped = {
                                dragging = false
                                onReorder(localItems.map { it.id })
                            },
                        ),
                )
            }
        }
    }
}

/** Mart-tinted one-liner: "🔍 누르면 쿠팡 앱에서 바로 검색돼요" + ✕. */
@Composable
private fun SearchHintCard(
    text: String,
    tint: Color,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .background(tint.copy(alpha = if (colors.isDark) 0.18f else 0.10f), RoundedCornerShape(12.dp))
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = text,
            style = typo.bodyS,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "닫기",
                tint = colors.textTertiary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
fun EmptyItems(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.ShoppingBag,
            contentDescription = null,
            tint = colors.textDisabled,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "아직 살 게 없네요",
            style = typo.headingM,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "오른쪽 아래 + 버튼을 눌러 추가하세요",
            style = typo.bodyS,
            color = colors.textTertiary,
        )
    }
}

@Composable
fun EmptyStores(
    onAddStoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Storefront,
            contentDescription = null,
            tint = colors.textDisabled,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "첫 번째 마트를 만들어보세요",
            style = typo.headingM,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "쿠팡, 다이소처럼 마트별로 따로 정리할 수 있어요",
            style = typo.bodyS,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(52.dp)
                .background(colors.brandPrimary, RoundedCornerShape(12.dp))
                .clickable(onClick = onAddStoreClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+ 마트 추가하기",
                style = typo.title,
                color = colors.bgPrimary,
            )
        }
    }
}
