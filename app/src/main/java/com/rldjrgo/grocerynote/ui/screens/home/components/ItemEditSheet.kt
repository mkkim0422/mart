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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.domain.model.emoji
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.util.formatReminderShort

/**
 * "항목 편집" (2026-11-13) — opened by tapping an item's name. Replaces the row's ⋮ menu
 * (알림 · 이름 수정 · 다른 마트로 이동 · 삭제 · ○○에서 찾기 was too much; 찾기 = the row's 🔍).
 * Name and mart are edited in place and applied on 저장; 알림 / 삭제 hand off to the
 * existing reminder sheet / delete confirm.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditSheet(
    item: Item,
    stores: List<Store>,
    onSave: (name: String, storeId: Long) -> Unit,
    onReminder: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember(item.id) { mutableStateOf(item.name) }
    var storeId by remember(item.id) { mutableStateOf(item.storeId) }
    val canSave = name.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bgPrimary,
        dragHandle = null,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp).imePadding()) {
            Text("항목 편집", style = typo.headingM, color = colors.textPrimary)
            Spacer(Modifier.height(14.dp))
            BasicTextField(
                value = name,
                onValueChange = { name = it.replace('\n', ' ').take(100) },
                singleLine = true,
                textStyle = typo.body.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.brandPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (canSave) onSave(name.trim(), storeId) }),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.5.dp, colors.brandPrimary, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) { inner() }
                },
            )
            if (stores.size > 1) {
                Spacer(Modifier.height(16.dp))
                Text("마트", style = typo.caption, color = colors.textTertiary)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(stores, key = { it.id }) { store ->
                        val on = store.id == storeId
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .background(if (on) store.color else colors.bgTertiary, RoundedCornerShape(18.dp))
                                .clickable { storeId = store.id }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${store.emoji()} ${store.name}",
                                style = typo.bodyS,
                                color = if (on) Color.White else colors.textSecondary,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable(onClick = onReminder),
            ) {
                Icon(Icons.Outlined.Notifications, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("알림", style = typo.body, color = colors.textPrimary, modifier = Modifier.weight(1f))
                Text(
                    text = item.reminderAt?.let { formatReminderShort(it) } ?: "설정 안 함",
                    style = typo.bodyS,
                    color = colors.textTertiary,
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(52.dp)
                        .background(colors.danger.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center,
                ) { Text("삭제", style = typo.title, color = colors.danger) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .background(if (canSave) colors.brandPrimary else colors.bgTertiary, RoundedCornerShape(12.dp))
                        .clickable(enabled = canSave) { onSave(name.trim(), storeId) },
                    contentAlignment = Alignment.Center,
                ) { Text("저장", style = typo.title, color = if (canSave) Color.White else colors.textDisabled) }
            }
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}
