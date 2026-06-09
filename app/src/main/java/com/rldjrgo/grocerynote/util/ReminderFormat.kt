package com.rldjrgo.grocerynote.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Short label for a reminder time, shown under the item name and in the picker:
 * "오늘 18:00" / "내일 09:00" / "6/10(수) 18:00".
 */
fun formatReminderShort(atMillis: Long, now: Long = System.currentTimeMillis()): String {
    val time = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date(atMillis))
    return when (dayDiff(now, atMillis)) {
        0 -> "오늘 $time"
        1 -> "내일 $time"
        else -> {
            val date = SimpleDateFormat("M/d(E)", Locale.KOREA).format(Date(atMillis))
            "$date $time"
        }
    }
}

/** Whole-calendar-day difference (target − base), local time. */
private fun dayDiff(baseMillis: Long, targetMillis: Long): Int {
    val base = midnight(baseMillis)
    val target = midnight(targetMillis)
    return ((target - base) / 86_400_000L).toInt()
}

private fun midnight(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis
