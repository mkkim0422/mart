package com.rldjrgo.grocerynote.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.ui.theme.AppTheme

private const val MAX_SELECTION = 4

/**
 * Pick up to 4 marts (ordered) to show in the Large widget.
 * Empty selection = auto (top by display order).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetStoreSelectionSheet(
    stores: List<Store>,
    initiallySelected: List<Long>,
    onSave: (List<Long>) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selected = remember {
        mutableStateListOf<Long>().apply { addAll(initiallySelected.filter { id -> stores.any { it.id == id } }) }
    }
    var overflow by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bgPrimary,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = "노출순서설정",
                style = typo.headingM.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "위젯에 표시되는 우선순위 설정 (최대 4개)",
                style = typo.bodyS,
                color = Color(0xFF6B6B6B),
            )
            if (overflow) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "최대 4개까지 선택할 수 있어요",
                    style = typo.bodyS,
                    color = colors.danger,
                )
            }
            Spacer(Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                items(stores, key = { it.id }) { store ->
                    val isChecked = selected.contains(store.id)
                    val order = selected.indexOf(store.id) + 1
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) {
                                    selected.remove(store.id)
                                    overflow = false
                                } else if (selected.size >= MAX_SELECTION) {
                                    overflow = true
                                } else {
                                    selected.add(store.id)
                                    overflow = false
                                }
                            }
                            .padding(vertical = 8.dp),
                    ) {
                        Box(modifier = Modifier.size(10.dp).background(store.color, CircleShape))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = store.name,
                            style = typo.body.copy(fontWeight = FontWeight.Medium),
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        if (isChecked && order > 0) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(store.color.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = order.toString(),
                                    style = typo.caption.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = store.color,
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { want ->
                                if (!want) {
                                    selected.remove(store.id); overflow = false
                                } else if (selected.size >= MAX_SELECTION) {
                                    overflow = true
                                } else {
                                    selected.add(store.id); overflow = false
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = store.color),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(colors.brandPrimary, RoundedCornerShape(12.dp))
                    .clickable { onSave(selected.toList()) },
                contentAlignment = Alignment.Center,
            ) {
                Text("저장", style = typo.title, color = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { onSave(emptyList()) },
                contentAlignment = Alignment.Center,
            ) {
                Text("자동(항목 많은 순)으로 두기", style = typo.bodyS, color = colors.textSecondary)
            }
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}
