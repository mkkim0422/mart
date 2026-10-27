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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.ui.theme.AppTheme

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
) {
    val colors = AppTheme.colors
    LazyColumn(
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
        items(items, key = { it.id }) { item ->
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
            )
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
