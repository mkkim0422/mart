package com.rldjrgo.grocerynote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.ui.theme.AppTheme

/** "오전 9시" 식 표기. */
fun hourLabel(hour: Int): String {
    val h = ((hour % 24) + 24) % 24
    val ampm = if (h < 12) "오전" else "오후"
    val h12 = when (h % 12) { 0 -> 12; else -> h % 12 }
    return "$ampm ${h12}시"
}

/**
 * 알림 시간 고르기 — 아침·저녁을 한 시트에서(설정 줄을 하나로 줄임, 2026-11-23). 시·분 피커 대신
 * 정시 칩(초심플): 아침 6~11시, 저녁 16~21시. 누르면 바로 적용되고 시트는 열려 있다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NudgeTimeSheet(
    morningHour: Int,
    eveningHour: Int,
    onPickMorning: (Int) -> Unit,
    onPickEvening: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.bgPrimary,
        dragHandle = null,
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp).navigationBarsPadding()) {
            Text("알림 시간", style = typo.headingM, color = colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text("아침이나 저녁, 정한 시각쯤 하루 1번만 와요", style = typo.bodyS, color = colors.textTertiary)
            Spacer(Modifier.height(20.dp))
            HourChips("아침", 6..11, morningHour, onPickMorning)
            Spacer(Modifier.height(20.dp))
            HourChips("저녁", 16..21, eveningHour, onPickEvening)
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(colors.brandPrimary, RoundedCornerShape(12.dp))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) { Text("확인", style = typo.title, color = androidx.compose.ui.graphics.Color.White) }
        }
    }
}

@Composable
private fun HourChips(label: String, hours: IntRange, selected: Int, onPick: (Int) -> Unit) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Text(label, style = typo.bodyS.copy(fontWeight = FontWeight.Medium), color = colors.textSecondary)
    Spacer(Modifier.height(8.dp))
    hours.chunked(3).forEach { rowHours ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            rowHours.forEach { h ->
                val on = h == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(
                            if (on) colors.brandPrimarySoft else colors.bgTertiary,
                            RoundedCornerShape(12.dp),
                        )
                        .clickable { onPick(h) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        hourLabel(h),
                        style = typo.body.copy(fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal),
                        color = if (on) colors.brandPrimary else colors.textPrimary,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
