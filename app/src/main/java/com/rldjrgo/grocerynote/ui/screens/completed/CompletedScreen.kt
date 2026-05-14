package com.rldjrgo.grocerynote.ui.screens.completed

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.ui.components.AdBanner
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.ui.theme.Corners
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CompletedScreen(
    viewModel: CompletedViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    val typo = AppTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary),
    ) {
        // TopBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("완료 목록", style = typo.headingL, color = colors.textPrimary)
        }

        // Filter chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "all") {
                FilterChip(
                    label = "전체",
                    color = null,
                    selected = state.filterStoreId == null,
                    onClick = { viewModel.setFilter(null) },
                )
            }
            items(state.stores, key = { it.id }) { store ->
                FilterChip(
                    label = store.name,
                    color = store.color,
                    selected = state.filterStoreId == store.id,
                    onClick = { viewModel.setFilter(store.id) },
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (state.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "아직 완료한 항목이 없어요",
                    style = typo.body,
                    color = colors.textTertiary,
                )
            }
            return
        }

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

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
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

@Composable
private fun FilterChip(
    label: String,
    color: androidx.compose.ui.graphics.Color?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(32.dp)
            .background(
                if (selected) colors.brandPrimarySoft else colors.bgTertiary,
                RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
    ) {
        if (color != null) {
            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = typo.bodyS,
            color = if (selected) colors.brandPrimary else colors.textSecondary,
        )
    }
}

@Composable
private fun CompletedRow(
    item: Item,
    store: Store?,
    onReactivate: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 20.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(colors.bgTertiary, Corners.small)
                .clickable(onClick = onReactivate),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(14.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = typo.body.copy(textDecoration = TextDecoration.LineThrough),
                color = colors.textTertiary,
                maxLines = 1,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp),
            ) {
                if (store != null) {
                    Box(modifier = Modifier.size(6.dp).background(store.color, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text(store.name, style = typo.micro, color = colors.textTertiary)
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = formatHourMin(item.completedAt),
                    style = typo.micro,
                    color = colors.textTertiary,
                )
            }
        }
        // Reactivate (left) and Delete (right) inline icons — sat in place of swipe to keep things simple.
        Row {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "되돌리기",
                tint = colors.success,
                modifier = Modifier
                    .size(20.dp)
                    .clip(Corners.small)
                    .clickable(onClick = onReactivate),
            )
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "삭제",
                tint = colors.danger,
                modifier = Modifier
                    .size(20.dp)
                    .clip(Corners.small)
                    .clickable(onClick = onDelete),
            )
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

private fun formatHourMin(ts: Long?): String {
    if (ts == null) return ""
    return SimpleDateFormat("HH:mm", Locale.KOREA).format(Date(ts))
}
