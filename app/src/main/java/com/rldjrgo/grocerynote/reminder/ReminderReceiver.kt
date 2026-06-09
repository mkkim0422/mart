package com.rldjrgo.grocerynote.reminder

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rldjrgo.grocerynote.MainActivity
import com.rldjrgo.grocerynote.R
import com.rldjrgo.grocerynote.di.ReminderEntryPoint
import com.rldjrgo.grocerynote.ui.navigation.Routes
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Fired by [ReminderScheduler]'s alarm. Looks up the current mart + item name from
 * the DB (so a rename still shows correctly), posts the "이거 살 시간이예요!"
 * notification, then clears the item's reminder_at (one-shot → the in-app/widget 🔔
 * disappears).
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_FIRE) return
        val itemId = intent.getLongExtra(ReminderScheduler.EXTRA_ITEM_ID, -1L)
        if (itemId <= 0L) return

        val pending = goAsync()
        val appContext = context.applicationContext
        val entry = EntryPointAccessors.fromApplication(appContext, ReminderEntryPoint::class.java)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val item = entry.itemRepository().getItem(itemId)
                // Item deleted, or reminder already cleared/canceled → don't notify.
                if (item == null || item.reminderAt == null) return@launch
                val storeName = entry.storeRepository().getStore(item.storeId)?.name.orEmpty()

                showNotification(appContext, itemId, item.storeId, storeName, item.name)

                entry.itemRepository().setReminder(itemId, null)
                entry.widgetUpdater().updateAll()
            } finally {
                pending.finish()
            }
        }
    }

    private fun showNotification(
        context: Context,
        itemId: Long,
        storeId: Long,
        storeName: String,
        itemName: String,
    ) {
        ReminderScheduler.ensureChannel(context)

        // Android 13+: silently skip if the user never granted POST_NOTIFICATIONS.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Tap → open the app on Home with that mart preselected + item highlighted.
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(Routes.HOME_DEEPLINK_STORE_ARG, storeId)
            putExtra(Routes.HOME_DEEPLINK_ITEM_ARG, itemId)
        }
        val contentPI = PendingIntent.getActivity(
            context,
            itemId.toInt(),
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val title = if (storeName.isBlank()) itemName else "$storeName · $itemName"
        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle(title)
            .setContentText("이거 살 시간이예요! 🛒")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentPI)
            .build()

        NotificationManagerCompat.from(context).notify(itemId.toInt(), notification)
    }
}
