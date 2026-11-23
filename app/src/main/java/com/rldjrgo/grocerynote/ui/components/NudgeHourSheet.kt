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
 * 알림 시간 고르기 — 시·분 피커 대신 정시 목록(초심플). [hours] 범위만 보여 준다
 * (아침 6~11시, 저녁 16~21시).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NudgeHourSheet(
    title: String,
    hours: IntRange,
    selected: Int,
    onPick: (Int) -> Unit,
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
            Text(title, style = typo.headingM, color = colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text("정한 시각쯤 하루 1번만 와요", style = typo.bodyS, color = colors.textTertiary)
            Spacer(Modifier.height(16.dp))
            hours.forEach { h ->
                val on = h == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(
                            if (on) colors.brandPrimarySoft else colors.bgPrimary,
                            RoundedCornerShape(12.dp),
                        )
                        .clickable { onPick(h) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        hourLabel(h),
                        style = typo.body.copy(fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal),
                        color = if (on) colors.brandPrimary else colors.textPrimary,
                    )
                    if (on) Box { Text("✓", style = typo.body, color = colors.brandPrimary) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
