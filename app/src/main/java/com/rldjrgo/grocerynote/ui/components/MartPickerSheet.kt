package com.rldjrgo.grocerynote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.ui.theme.AppTheme

/**
 * First run only (1.5.1): "자주 가는 곳을 골라 주세요" — multi-select the presets the user
 * doesn't have yet; "N곳 추가하기" creates them all (color/emoji included). 건너뛰기 keeps
 * just the seed 쿠팡/다이소. Never shown to people who already have items.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MartPickerSheet(
    presets: List<MartPreset>,
    onAdd: (List<MartPreset>) -> Unit,
    onSkip: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val picked = remember { mutableStateListOf<MartPreset>() }

    ModalBottomSheet(
        onDismissRequest = onSkip,
        sheetState = sheetState,
        containerColor = colors.bgPrimary,
        dragHandle = null,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text("자주 가는 곳을 골라 주세요", style = typo.headingM, color = colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                "쿠팡·다이소는 이미 있어요. 장보기 말고 메모·준비물도 따로 적어둘 수 있어요",
                style = typo.bodyS,
                color = colors.textTertiary,
            )
            Spacer(Modifier.height(18.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                presets.forEach { p ->
                    val on = p in picked
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .background(if (on) p.color else colors.bgTertiary, RoundedCornerShape(20.dp))
                            .border(1.dp, if (on) p.color else Color.Transparent, RoundedCornerShape(20.dp))
                            .clickable { if (on) picked.remove(p) else picked.add(p) }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${p.emoji} ${p.name}",
                            style = typo.bodyS.copy(fontWeight = FontWeight.Medium),
                            color = if (on) Color.White else colors.textSecondary,
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(
                        if (picked.isEmpty()) colors.bgTertiary else colors.brandPrimary,
                        RoundedCornerShape(12.dp),
                    )
                    .clickable(enabled = picked.isNotEmpty()) { onAdd(picked.toList()) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (picked.isEmpty()) "골라 주세요" else "${picked.size}곳 추가하기",
                    style = typo.title,
                    color = if (picked.isEmpty()) colors.textDisabled else Color.White,
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(48.dp).clickable(onClick = onSkip),
                contentAlignment = Alignment.Center,
            ) {
                Text("건너뛰기", style = typo.bodyS, color = colors.textTertiary)
            }
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}
