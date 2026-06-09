package com.rldjrgo.grocerynote.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-shot per-item purchase reminders.
 *
 * Uses an **inexact** alarm ([AlarmManager.setAndAllowWhileIdle]) on purpose:
 * a shopping reminder tolerates a few minutes of slack, and inexact alarms need
 * no SCHEDULE_EXACT_ALARM/USE_EXACT_ALARM permission (Play-policy + battery
 * friendly — see the 2026-06-09 reminder decision). The alarm fires
 * [ReminderReceiver], which posts the notification.
 *
 * The PendingIntent request code is the item id, so re-scheduling the same item
 * replaces its previous alarm (one reminder per item — CLAUDE.md "초심플"). The
 * alarm carries only the item id; [ReminderReceiver] looks up the current mart +
 * item name from the DB at fire time, so a rename still shows the right text.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    init {
        ensureChannel(context)
    }

    /** Arm (or re-arm) the reminder for [itemId] at [atMillis] (epoch). */
    fun schedule(itemId: Long, atMillis: Long) {
        val am = context.getSystemService<AlarmManager>() ?: return
        am.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            atMillis,
            buildPendingIntent(itemId, create = true)!!,
        )
    }

    /** Cancel any pending alarm for [itemId]. Safe to call when none is armed. */
    fun cancel(itemId: Long) {
        val am = context.getSystemService<AlarmManager>() ?: return
        val pi = buildPendingIntent(itemId, create = false)
        if (pi != null) {
            am.cancel(pi)
            pi.cancel()
        }
    }

    private fun buildPendingIntent(itemId: Long, create: Boolean): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_ITEM_ID, itemId)
        }
        var flags = PendingIntent.FLAG_IMMUTABLE
        flags = flags or if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE
        return PendingIntent.getBroadcast(context, itemId.toInt(), intent, flags)
    }

    companion object {
        const val CHANNEL_ID = "item_reminders"
        const val ACTION_FIRE = "com.rldjrgo.grocerynote.action.REMINDER_FIRE"
        const val EXTRA_ITEM_ID = "extra_item_id"

        /** Idempotent — safe to call before every notify. minSdk 26 → channel always exists. */
        fun ensureChannel(context: Context) {
            val nm = context.getSystemService<NotificationManager>() ?: return
            if (nm.getNotificationChannel(CHANNEL_ID) != null) return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "장보기 알림",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "정한 시간에 살 항목을 알려줘요"
            }
            nm.createNotificationChannel(channel)
        }
    }
}
