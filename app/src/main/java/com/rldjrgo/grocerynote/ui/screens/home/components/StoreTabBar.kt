package com.rldjrgo.grocerynote.ui.screens.home.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.ui.theme.AppTheme

@Composable
fun StoreTabBar(
    stores: List<Store>,
    selectedStoreId: Long?,
    itemCounts: Map<Long, Int>,
    onStoreClick: (Long) -> Unit,
    onAddStoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Column(modifier = modifier.fillMaxWidth().background(colors.bgPrimary)) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(stores, key = { it.id }) { store ->
                val selected = store.id == selectedStoreId
                val count = itemCounts[store.id] ?: 0
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onStoreClick(store.id) }
                        .height(48.dp)
                        .padding(horizontal = 16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(store.color, CircleShape),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = store.name,
                            style = typo.headingM,
                            color = if (selected) colors.textPrimary else colors.textTertiary,
                        )
                        if (count > 0) {
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = count.toString(),
                                style = typo.bodyS,
                                color = colors.textTertiary,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(if (selected) 24.dp else 0.dp)
                            .background(colors.brandPrimary),
                    )
                }
            }
            item(key = "add-store") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = onAddStoreClick)
                        .height(48.dp)
                        .padding(horizontal = 16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "마트 추가",
                        tint = colors.textTertiary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "추가",
                        style = typo.title,
                        color = colors.textTertiary,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.divider),
        )
    }
}
