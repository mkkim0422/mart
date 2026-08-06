package com.rldjrgo.grocerynote.ui.screens.home.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.MoreVert
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.domain.model.emoji
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * Pill-style mart tabs (design spec). Selected pill is filled with the mart
 * color; unselected pills are white cards with a hairline border. Global
 * Toss-blue stays elsewhere — the pill fill is the per-mart color pool.
 *
 * iOS-style jiggle edit mode: long-press a pill → haptic + every pill wiggles
 * and grows an ✕ badge; keep holding and drag sideways to reorder (persisted
 * on drop). Tap ✕ → delete confirm (owned by HomeScreen). Tap any pill /
 * back / 추가 / 관리 to exit edit mode.
 */
@Composable
fun StoreTabBar(
    stores: List<Store>,
    selectedStoreId: Long?,
    itemCounts: Map<Long, Int>,
    onStoreClick: (Long) -> Unit,
    onAddStoreClick: () -> Unit,
    onManageStoresClick: () -> Unit,
    onDeleteStoreClick: (Store) -> Unit = {},
    onReorderStores: (List<Long>) -> Unit = {},
    // Jiggle edit mode is hoisted so HomeScreen can also exit it when the user
    // taps anywhere outside the tab strip (list body, FABs, …).
    editMode: Boolean = false,
    onEditModeChange: (Boolean) -> Unit = {},
    newlyAddedStoreId: Long? = null,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dark = colors.isDark
    val pillBg = if (dark) Color(0xFF2A2A2A) else Color(0xFFFFFFFF)
    val dashColor = if (dark) Color(0xFF444444) else Color(0xFFD5D2CB)
    val badgeOffBg = if (dark) Color(0xFF222222) else Color(0xFFF0EEE9)
    val badgeOffText = Color(0xFF6B6B6B)

    val haptics = LocalHapticFeedback.current
    BackHandler(enabled = editMode) { onEditModeChange(false) }
    // Lifting the finger right after the long-press ALSO registers as a click
    // on the same pill, which would instantly exit the just-entered edit mode.
    // Swallow that one release-click; auto-expire so a real tap is never eaten.
    var suppressNextClick by remember { mutableStateOf(false) }
    LaunchedEffect(suppressNextClick) {
        if (suppressNextClick) {
            kotlinx.coroutines.delay(500)
            suppressNextClick = false
        }
    }

    // Local working copy so drag reorder is instant (same merge pattern as
    // StoreManageScreen): keep local order for surviving ids so a just-finished
    // drag doesn't snap back before the DB write lands; drop deleted, append new.
    var localStores by remember { mutableStateOf(stores) }
    var dragging by remember { mutableStateOf(false) }
    LaunchedEffect(stores, dragging) {
        if (dragging) return@LaunchedEffect
        val byId = stores.associateBy { it.id }
        val kept = localStores.mapNotNull { byId[it.id] }
        val keptIds = kept.mapTo(HashSet()) { it.id }
        localStores = kept + stores.filter { it.id !in keptIds }
    }

    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        // Trailing "추가"/"관리" items are not reorderable — never move past them.
        if (from.index < localStores.size && to.index < localStores.size) {
            localStores = localStores.toMutableList()
                .apply { add(to.index, removeAt(from.index)) }
        }
    }

    // Guarantee the first mart is fully visible on entry — the page slide-in
    // animation could otherwise leave the list scrolled a few px to the right.
    LaunchedEffect(stores.isNotEmpty()) {
        if (stores.isNotEmpty()) listState.scrollToItem(0)
    }
    // Newly added mart sits at the end → scroll so it (and the "+ 추가" button) shows.
    LaunchedEffect(newlyAddedStoreId) {
        if (newlyAddedStoreId != null && localStores.isNotEmpty()) {
            listState.animateScrollToItem(localStores.size)
        }
    }
    // Follow the selected mart: swiping to an off-screen tab must bring its pill
    // into view so the user can see which mart they're on. Keyed on the id ONLY —
    // keying on the stores list too made a drag-drop scroll-jump: persisting the
    // new order re-emits stores, which re-ran this and yanked the strip back to
    // the selected pill (= where the drag started). Never follow while editing.
    LaunchedEffect(selectedStoreId) {
        if (dragging || editMode) return@LaunchedEffect
        val idx = localStores.indexOfFirst { it.id == selectedStoreId }
        if (idx >= 0) listState.animateScrollToItem(idx)
    }

    Column(modifier = modifier.fillMaxWidth().background(colors.bgPrimary)) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 14.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        ) {
            items(localStores, key = { it.id }) { store ->
                ReorderableItem(reorderState, key = store.id) { isDragging ->
                    val selected = store.id == selectedStoreId
                    val isNew = store.id == newlyAddedStoreId
                    val count = itemCounts[store.id] ?: 0
                    // Jiggle rotation only exists while editing (no idle animation
                    // cost); the pill being dragged stays straight. Per-pill start
                    // offset desyncs the wiggle like iOS home icons.
                    val jiggle = if (editMode && !isDragging) {
                        val transition = rememberInfiniteTransition(label = "jiggle")
                        transition.animateFloat(
                            initialValue = -1.5f,
                            targetValue = 1.5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(160, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse,
                                initialStartOffset = StartOffset(((store.id % 5) * 26).toInt()),
                            ),
                            label = "jiggleAngle",
                        ).value
                    } else 0f
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                // In edit mode reserve headroom so the ✕ badge can
                                // overlap the pill's top-end corner without clipping.
                                .then(
                                    if (editMode) Modifier.padding(top = 8.dp, end = 8.dp)
                                    else Modifier
                                )
                                .graphicsLayer {
                                    rotationZ = jiggle
                                    if (isDragging) {
                                        scaleX = 1.06f
                                        scaleY = 1.06f
                                    }
                                }
                                .shadow(
                                    elevation = if (isDragging) 6.dp else 0.dp,
                                    shape = RoundedCornerShape(19.dp),
                                )
                                .height(38.dp)
                                .clip(RoundedCornerShape(19.dp))
                                .background(if (selected) store.color else pillBg)
                                .then(
                                    when {
                                        isNew -> Modifier.border(2.dp, colors.brandPrimary, RoundedCornerShape(19.dp))
                                        selected -> Modifier
                                        else -> Modifier.border(1.dp, colors.divider, RoundedCornerShape(19.dp))
                                    }
                                )
                                // One continuous gesture: long-press (haptic, enter
                                // edit mode) → keep holding → drag sideways to move.
                                .longPressDraggableHandle(
                                    onDragStarted = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onEditModeChange(true)
                                        dragging = true
                                        suppressNextClick = true
                                    },
                                    onDragStopped = {
                                        dragging = false
                                        onReorderStores(localStores.map { it.id })
                                    },
                                )
                                .clickable {
                                    when {
                                        suppressNextClick -> suppressNextClick = false
                                        editMode -> onEditModeChange(false)
                                        else -> onStoreClick(store.id)
                                    }
                                }
                                .padding(horizontal = 14.dp),
                        ) {
                            Text(text = store.emoji(), style = AppTheme.typography.body.copy(fontSize = 16.sp))
                            Text(
                                text = store.name,
                                style = AppTheme.typography.body.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = if (selected) Color.White else colors.textPrimary,
                            )
                            Box(
                                modifier = Modifier
                                    .padding(start = 2.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(
                                        if (selected) Color.White.copy(alpha = 0.25f) else badgeOffBg
                                    )
                                    .padding(horizontal = 6.dp, vertical = 1.dp),
                            ) {
                                Text(
                                    text = count.toString(),
                                    style = AppTheme.typography.body.copy(fontSize = 11.sp),
                                    color = if (selected) Color.White else badgeOffText,
                                )
                            }
                        }
                        // iOS-style delete badge — quiet neutral circle (no loud
                        // red): pill-colored bg, hairline border, thin ✕.
                        if (editMode && !isDragging) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .shadow(2.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(pillBg)
                                    .border(1.dp, colors.divider, CircleShape)
                                    .clickable { onDeleteStoreClick(store) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "${store.name} 삭제",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(11.dp),
                                )
                            }
                        }
                    }
                }
            }
            item(key = "add-store") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(38.dp)
                        .drawBehind {
                            drawRoundRect(
                                color = dashColor,
                                cornerRadius = CornerRadius(19.dp.toPx()),
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(9f, 6f), 0f,
                                    ),
                                ),
                            )
                        }
                        .clickable { onEditModeChange(false); onAddStoreClick() }
                        .padding(horizontal = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "마트 추가",
                        tint = colors.textTertiary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "추가",
                        style = AppTheme.typography.body.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = colors.textTertiary,
                    )
                }
            }
            item(key = "manage-stores") {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.dp, colors.divider, CircleShape)
                        .clickable { onEditModeChange(false); onManageStoresClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "마트 관리",
                        tint = Color(0xFF6B6B6B),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.divider),
        )
    }
}
