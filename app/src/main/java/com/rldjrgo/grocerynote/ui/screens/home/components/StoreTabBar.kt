package com.rldjrgo.grocerynote.ui.screens.home.components

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
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.domain.model.emoji
import com.rldjrgo.grocerynote.ui.theme.AppTheme

/**
 * Pill-style mart tabs (design spec). Selected pill is filled with the mart
 * color; unselected pills are white cards with a hairline border. Global
 * Toss-blue stays elsewhere — the pill fill is the per-mart color pool.
 */
@Composable
fun StoreTabBar(
    stores: List<Store>,
    selectedStoreId: Long?,
    itemCounts: Map<Long, Int>,
    onStoreClick: (Long) -> Unit,
    onAddStoreClick: () -> Unit,
    onManageStoresClick: () -> Unit,
    newlyAddedStoreId: Long? = null,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dark = colors.isDark
    val pillBg = if (dark) Color(0xFF2A2A2A) else Color(0xFFFFFFFF)
    val dashColor = if (dark) Color(0xFF444444) else Color(0xFFD5D2CB)
    val badgeOffBg = if (dark) Color(0xFF222222) else Color(0xFFF0EEE9)
    val badgeOffText = Color(0xFF6B6B6B)

    val listState = rememberLazyListState()
    // Guarantee the first mart (이마트) is fully visible on entry — the page slide-in
    // animation could otherwise leave the list scrolled a few px to the right.
    LaunchedEffect(stores.isNotEmpty()) {
        if (stores.isNotEmpty()) listState.scrollToItem(0)
    }
    // Newly added mart sits at the end → scroll so it (and the "+ 추가" button) shows.
    LaunchedEffect(newlyAddedStoreId) {
        if (newlyAddedStoreId != null && stores.isNotEmpty()) {
            listState.animateScrollToItem(stores.size)
        }
    }
    // Follow the selected mart: swiping to an off-screen tab must bring its pill
    // into view so the user can see which mart they're on.
    LaunchedEffect(selectedStoreId, stores) {
        val idx = stores.indexOfFirst { it.id == selectedStoreId }
        if (idx >= 0) listState.animateScrollToItem(idx)
    }

    Column(modifier = modifier.fillMaxWidth().background(colors.bgPrimary)) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        ) {
            items(stores, key = { it.id }) { store ->
                val selected = store.id == selectedStoreId
                val isNew = store.id == newlyAddedStoreId
                val count = itemCounts[store.id] ?: 0
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
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
                        .clickable { onStoreClick(store.id) }
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
                        .clickable(onClick = onAddStoreClick)
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
                        .clickable(onClick = onManageStoresClick),
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
