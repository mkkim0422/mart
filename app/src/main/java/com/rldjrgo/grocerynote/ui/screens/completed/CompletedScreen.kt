package com.rldjrgo.grocerynote.ui.screens.completed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.domain.model.emoji
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import com.rldjrgo.grocerynote.ui.components.AdBanner
import com.rldjrgo.grocerynote.ui.components.PageTitle
import com.rldjrgo.grocerynote.ui.components.UndoSnackbarHost
import com.rldjrgo.grocerynote.ui.components.swipeBetweenTabs
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.ui.theme.soft
import java.util.Calendar

@Composable
fun CompletedScreen(
    viewModel: CompletedViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    val typo = AppTheme.typography

    val snackbarHostState = remember(Unit) { SnackbarHostState() }
    val undo by viewModel.undoEvent.collectAsStateWithLifecycle()
    LaunchedEffect(undo) {
        val u = undo ?: return@LaunchedEffect
        val msg = when (u.kind) {
            CompletedViewModel.UndoKind.Reactivated -> "↩ ‘${u.name}’ 다시 담음"
            CompletedViewModel.UndoKind.Deleted -> "🗑 ‘${u.name}’ 삭제됨"
        }
        val result = snackbarHostState.showSnackbar(
            message = msg,
            actionLabel = "되돌리기",
            withDismissAction = false,
            duration = SnackbarDuration.Short,
        )
        when (result) {
            SnackbarResult.ActionPerformed -> viewModel.undo(u)
            SnackbarResult.Dismissed -> viewModel.consumeUndoEvent()
        }
    }

    // Swipe left/right over the screen to move between filters (전체 + each mart).
    val filterIds = remember(state.stores) { listOf<Long?>(null) + state.stores.map { it.id } }
    val filterIdx = filterIds.indexOf(state.filterStoreId)
    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
            .statusBarsPadding()
            .swipeBetweenTabs(
                onNext = {
                    if (filterIdx in 0 until filterIds.lastIndex) {
                        viewModel.setFilter(filterIds[filterIdx + 1])
                    }
                },
                onPrev = {
                    if (filterIdx > 0) viewModel.setFilter(filterIds[filterIdx - 1])
                },
            ),
    ) {
        // TopBar
        PageTitle(title = "완료")

        // Filter chips — same pill language as the active screen's StoreTabBar.
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "all") {
                FilterPill(
                    label = "전체",
                    emoji = null,
                    color = null,
                    count = null,
                    selected = state.filterStoreId == null,
                    onClick = { viewModel.setFilter(null) },
                )
            }
            items(state.stores, key = { it.id }) { store ->
                FilterPill(
                    label = store.name,
                    emoji = store.emoji(),
                    color = store.color,
                    count = state.completedCounts[store.id] ?: 0,
                    selected = state.filterStoreId == store.id,
                    onClick = { viewModel.setFilter(store.id) },
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (state.items.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "아직 완료한 항목이 없어요",
                    style = typo.body,
                    color = colors.textTertiary,
                )
            }
            AdBanner()
        } else {
        val grouped = remember(state.items) { groupByBucket(state.items) }

        // Bottom summary computed
        val now = Calendar.getInstance()
        val weekStart = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }.timeInMillis
        val monthStart = (now.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val weekCount = state.items.count { (it.completedAt ?: 0) >= weekStart }
        val monthCount = state.items.count { (it.completedAt ?: 0) >= monthStart }

        AnimatedContent(
            targetState = state.filterStoreId,
            transitionSpec = {
                // Clean crossfade only — no horizontal slide, so the body doesn't
                // drift sideways while the fixed filter strip stays put. 220ms.
                val alpha = tween<Float>(220, easing = FastOutSlowInEasing)
                fadeIn(alpha) togetherWith fadeOut(alpha)
            },
            label = "filterSwitch",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { _ ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                grouped.forEach { (bucket, list) ->
                    item(key = "h-$bucket") {
                        Text(
                            text = bucket,
                            style = typo.title,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        )
                    }
                    items(list, key = { "i-${it.id}" }) { item ->
                        CompletedRow(
                            item = item,
                            store = state.storesById[item.storeId],
                            onReactivate = { viewModel.reactivate(item.id) },
                            onDelete = { viewModel.delete(item.id) },
                        )
                    }
                }
            }
        }

        // Summary footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.divider),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("이번 주 ${weekCount}개", style = typo.bodyS, color = colors.textTertiary)
            Text("이번 달 ${monthCount}개", style = typo.bodyS, color = colors.textTertiary)
        }
        AdBanner()
        }
    }
        UndoSnackbarHost(snackbarHostState)
    }
}

@Composable
private fun FilterPill(
    label: String,
    emoji: String?,
    color: androidx.compose.ui.graphics.Color?,
    count: Int?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val dark = colors.isDark
    val pillBg = if (dark) Color(0xFF2A2A2A) else Color(0xFFFFFFFF)
    val selectedBg = color ?: Color(0xFF191F28) // "전체" = 검정
    val badgeOffBg = if (dark) Color(0xFF222222) else Color(0xFFF0EEE9)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(if (selected) selectedBg else pillBg)
            .then(
                if (selected) Modifier
                else Modifier.border(1.dp, colors.divider, RoundedCornerShape(19.dp))
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
    ) {
        if (emoji != null) {
            Text(text = emoji, style = typo.body.copy(fontSize = 16.sp))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
            color = if (selected) Color.White else colors.textPrimary,
        )
        if (count != null) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (selected) Color.White.copy(alpha = 0.25f) else badgeOffBg)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    text = count.toString(),
                    style = typo.body.copy(fontSize = 11.sp),
                    color = if (selected) Color.White else Color(0xFF6B6B6B),
                )
            }
        }
    }
}

@Composable
private fun CompletedRow(
    item: Item,
    store: Store?,
    onReactivate: () -> Unit,
    onDelete: () -> Unit,
) {
    // Swipe-to-delete/reactivate removed: the gesture is unreliable on this
    // device. The ⋮ menu in CompletedRowContent already provides 삭제 +
    // 되돌리기, so the actions stay available without the broken swipe.
    CompletedRowContent(item, store, onReactivate, onDelete)
}

@Composable
private fun CompletedRowContent(
    item: Item,
    store: Store?,
    onReactivate: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    var menuOpen by remember(item.id) { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colors.bgPrimary)
            .padding(horizontal = 20.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = null,
            tint = Color(0xFFC9CDD2),
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = item.name,
            style = typo.body.copy(textDecoration = TextDecoration.LineThrough),
            color = colors.textTertiary,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        if (store != null) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(store.color.soft(colors.isDark), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = "${store.emoji()} ${store.name}",
                    style = typo.micro.copy(fontWeight = FontWeight.SemiBold),
                    color = store.color,
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Box {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "더보기",
                tint = colors.textTertiary,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { menuOpen = true },
            )
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text("다시 구매예정으로", style = typo.body, color = colors.textPrimary) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Replay, null, tint = colors.success, modifier = Modifier.size(20.dp))
                    },
                    onClick = { menuOpen = false; onReactivate() },
                )
                DropdownMenuItem(
                    text = { Text("영구 삭제", style = typo.body, color = colors.danger) },
                    leadingIcon = {
                        Icon(Icons.Outlined.DeleteForever, null, tint = colors.danger, modifier = Modifier.size(20.dp))
                    },
                    onClick = { menuOpen = false; onDelete() },
                )
            }
        }
    }
}

@Composable
private fun <T> remember(key1: Any?, calculation: () -> T): T {
    return androidx.compose.runtime.remember(key1) { calculation() }
}

private fun groupByBucket(items: List<Item>): Map<String, List<Item>> {
    val now = Calendar.getInstance()
    val todayStart = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val yesterdayStart = todayStart - 24L * 60 * 60 * 1000
    val weekStart = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    }.timeInMillis
    val monthStart = (now.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    return items.groupBy { it ->
        val t = it.completedAt ?: 0L
        when {
            t >= todayStart -> "오늘"
            t >= yesterdayStart -> "어제"
            t >= weekStart -> "이번 주"
            t >= monthStart -> "이번 달"
            else -> "이전"
        }
    }.toMap()
}
