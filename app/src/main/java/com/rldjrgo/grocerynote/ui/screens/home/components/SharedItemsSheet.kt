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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.domain.model.emoji
import com.rldjrgo.grocerynote.ui.theme.AppTheme

/** The sender's mart, offered as "＋ 다이소 새로 만들기" when the receiver has no mart by that name. */
data class NewMart(val name: String, val color: Color, val emoji: String)

/** storeId passed to onAdd when the user keeps "새로 만들기" → caller creates [NewMart] first. */
const val NEW_MART_ID = -1L

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
    /** Sender's mart name from the link (title "받은 다이소 장보기 N개"). */
    senderStoreName: String? = null,
    /** Non-null when the receiver has no mart named like the sender's → preselected "새로 만들기". */
    newMart: NewMart? = null,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var storeId by remember { mutableStateOf(if (newMart != null) NEW_MART_ID else initialStoreId) }
    val creating = newMart != null && storeId == NEW_MART_ID
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
                text = when {
                    fromLink && !senderStoreName.isNullOrBlank() -> "받은 $senderStoreName 장보기 ${names.size}개"
                    fromLink -> "받은 장보기 리스트 ${names.size}개"
                    else -> "받은 글에서 ${names.size}개를 찾았어요"
                },
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
                if (newMart != null) {
                    item(key = "new") {
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .background(if (creating) newMart.color else Color.Transparent, RoundedCornerShape(18.dp))
                                .then(
                                    if (creating) Modifier
                                    else Modifier.dashedBorder(newMart.color, 18.dp),
                                )
                                .clickable { storeId = NEW_MART_ID }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "＋ ${newMart.emoji} ${newMart.name} 새로 만들기",
                                style = typo.bodyS,
                                color = if (creating) Color.White else newMart.color,
                            )
                        }
                    }
                }
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
            if (creating) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "${newMart!!.name} 마트를 새로 만들고 여기에 담아요",
                    style = typo.caption,
                    color = newMart.color,
                    modifier = Modifier
                        .background(newMart.color.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
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
                        when {
                            count <= 0 -> colors.bgTertiary
                            creating -> newMart!!.color
                            else -> colors.brandPrimary
                        },
                        RoundedCornerShape(12.dp),
                    )
                    .clickable(enabled = count > 0) {
                        onAdd(storeId, names.filterIndexed { i, _ -> i !in unchecked })
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when {
                        count <= 0 -> "담을 항목을 골라주세요"
                        creating -> "${newMart!!.name} 만들고 ${count}개 담기"
                        else -> "${count}개 담기"
                    },
                    style = typo.title,
                    color = if (count > 0) Color.White else colors.textDisabled,
                )
            }
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}

/** Dashed pill outline for the "새로 만들기" chip when it isn't selected. */
private fun Modifier.dashedBorder(color: Color, radius: androidx.compose.ui.unit.Dp): Modifier =
    drawBehind {
        val stroke = 1.5.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
            size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = stroke,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())),
            ),
        )
    }
