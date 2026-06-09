package com.rldjrgo.grocerynote.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rldjrgo.grocerynote.di.ReminderEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * `setAndAllowWhileIdle` alarms are wiped on reboot, so re-arm every still-pending
 * item reminder. Reminders whose time already passed during downtime are dropped
 * (reminder_at cleared) rather than fired late.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Only the post-unlock BOOT_COMPLETED — the DB lives in credential-encrypted
        // storage, so we must not touch it during direct-boot.
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pending = goAsync()
        val entry = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReminderEntryPoint::class.java,
        )
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repo = entry.itemRepository()
                val scheduler = entry.reminderScheduler()
                val now = System.currentTimeMillis()
                repo.itemsWithReminder().forEach { item ->
                    val at = item.reminderAt ?: return@forEach
                    if (at > now) {
                        scheduler.schedule(item.id, at)
                    } else {
                        // Missed during downtime → drop rather than fire late.
                        repo.setReminder(item.id, null)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
