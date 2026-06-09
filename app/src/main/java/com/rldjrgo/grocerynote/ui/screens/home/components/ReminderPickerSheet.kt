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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Bottom sheet to pick a one-shot reminder date + time for a single item.
 * Date opens a Material3 DatePickerDialog, time a TimePicker dialog. "설정" returns
 * the chosen epoch millis; "알림 끄기" (only when one already exists) clears it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPickerSheet(
    itemName: String,
    initialAtMillis: Long?,
    onConfirm: (Long) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Default: existing reminder, else one hour from now.
    val base = initialAtMillis ?: (System.currentTimeMillis() + 60 * 60 * 1000L)
    val baseCal = remember { Calendar.getInstance().apply { timeInMillis = base } }

    var year by remember { mutableIntStateOf(baseCal.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(baseCal.get(Calendar.MONTH)) } // 0-based
    var day by remember { mutableIntStateOf(baseCal.get(Calendar.DAY_OF_MONTH)) }
    var hour by remember { mutableIntStateOf(baseCal.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(baseCal.get(Calendar.MINUTE)) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateLabel = remember(year, month, day) {
        val c = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0) }
        SimpleDateFormat("yyyy. M. d (E)", Locale.KOREA).format(c.time)
    }
    val timeLabel = remember(hour, minute) {
        val c = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute) }
        SimpleDateFormat("a h:mm", Locale.KOREA).format(c.time)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bgPrimary,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text("🔔  '$itemName' 알림", style = typo.headingM, color = colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                "정한 날짜·시간에 한 번 알려드려요",
                style = typo.bodyS,
                color = colors.textTertiary,
            )
            Spacer(Modifier.height(20.dp))

            PickerField(label = "날짜", value = dateLabel) { showDatePicker = true }
            Spacer(Modifier.height(12.dp))
            PickerField(label = "시간", value = timeLabel) { showTimePicker = true }

            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(colors.brandPrimary, RoundedCornerShape(12.dp))
                    .clickable {
                        onConfirm(localEpoch(year, month, day, hour, minute))
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text("설정", style = typo.title, color = colors.bgPrimary)
            }
            if (initialAtMillis != null) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable { onClear() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("알림 끄기", style = typo.title, color = colors.danger)
                }
            }
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }

    if (showDatePicker) {
        val initialUtc = remember {
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear(); set(year, month, day)
            }.timeInMillis
        }
        val state = rememberDatePickerState(initialSelectedDateMillis = initialUtc)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { utc ->
                        val c = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utc }
                        year = c.get(Calendar.YEAR)
                        month = c.get(Calendar.MONTH)
                        day = c.get(Calendar.DAY_OF_MONTH)
                    }
                    showDatePicker = false
                }) { Text("확인", color = colors.brandPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소", color = colors.textSecondary)
                }
            },
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = false,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = colors.bgPrimary,
            confirmButton = {
                TextButton(onClick = {
                    hour = state.hour
                    minute = state.minute
                    showTimePicker = false
                }) { Text("확인", color = colors.brandPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("취소", color = colors.textSecondary)
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = state)
                }
            },
        )
    }
}

@Composable
private fun PickerField(label: String, value: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = typo.body, color = colors.textSecondary, modifier = Modifier.width(48.dp))
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .border(1.dp, colors.divider, RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                value,
                style = typo.body.copy(fontWeight = FontWeight.Medium),
                color = colors.textPrimary,
            )
        }
    }
}

/** Local-time epoch millis for the chosen calendar fields (seconds zeroed). */
private fun localEpoch(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
    Calendar.getInstance().apply {
        set(year, month, day, hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
