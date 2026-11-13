package com.rldjrgo.grocerynote.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.domain.model.emoji
import com.rldjrgo.grocerynote.ui.theme.AppTheme

/**
 * "받은 글에서 N개를 찾았어요" — shown when several lines are shared into the app
 * (카톡 나에게 보내기 메모, 가족이 보낸 리스트). Every line starts checked; the user
 * picks the mart and taps once to add them all.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedItemsSheet(
    names: List<String>,
    stores: List<Store>,
    initialStoreId: Long,
    onAdd: (storeId: Long, names: List<String>) -> Unit,
    onDismiss: () -> Unit,
    /** True when the names came from a received list link (1.5.0), not pasted text. */
    fromLink: Boolean = false,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var storeId by remember { mutableStateOf(initialStoreId) }
    val unchecked = remember { mutableStateListOf<Int>() }
    val count = names.size - unchecked.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bgPrimary,
        dragHandle = null,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = if (fromLink) "받은 장보기 리스트 ${names.size}개" else "받은 글에서 ${names.size}개를 찾았어요",
                style = typo.headingM,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "담을 마트를 고르고 한 번에 넣으세요",
                style = typo.bodyS,
                color = colors.textTertiary,
            )
            Spacer(Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(stores, key = { it.id }) { store ->
                    val selected = store.id == storeId
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .background(
                                if (selected) store.color else colors.bgTertiary,
                                RoundedCornerShape(18.dp),
                            )
                            .clickable { storeId = store.id }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${store.emoji()} ${store.name}",
                            style = typo.bodyS,
                            color = if (selected) Color.White else colors.textSecondary,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp)) {
                items(names.size) { i ->
                    val checked = i !in unchecked
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable { if (checked) unchecked.add(i) else unchecked.remove(i) },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(
                                    if (checked) colors.brandPrimary else Color.Transparent,
                                    RoundedCornerShape(6.dp),
                                )
                                .border(
                                    1.5.dp,
                                    if (checked) colors.brandPrimary else colors.textDisabled,
                                    RoundedCornerShape(6.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (checked) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = names[i],
                            style = typo.body,
                            color = if (checked) colors.textPrimary else colors.textTertiary,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(
                        if (count > 0) colors.brandPrimary else colors.bgTertiary,
                        RoundedCornerShape(12.dp),
                    )
                    .clickable(enabled = count > 0) {
                        onAdd(storeId, names.filterIndexed { i, _ -> i !in unchecked })
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (count > 0) "${count}개 담기" else "담을 항목을 골라주세요",
                    style = typo.title,
                    color = if (count > 0) Color.White else colors.textDisabled,
                )
            }
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}
