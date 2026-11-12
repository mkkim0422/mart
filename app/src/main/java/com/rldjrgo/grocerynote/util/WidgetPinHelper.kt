package com.rldjrgo.grocerynote.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rldjrgo.grocerynote.widget.GroceryWidget2x1Receiver
import com.rldjrgo.grocerynote.widget.GroceryWidgetMediumReceiver
import com.rldjrgo.grocerynote.widget.WidgetPinSuccessReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** The 2 widgets (2026-11-12). Enum order = picker order. */
enum class WidgetKind { COUNT, LIST }

/** Requests the launcher to pin a widget. */
@Singleton
class WidgetPinHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun isSupported(): Boolean =
        AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported

    /** 개수/목록 위젯 중 하나라도 홈화면에 실제로 놓여 있으면 true. */
    fun anyWidgetPlaced(): Boolean = try {
        val manager = AppWidgetManager.getInstance(context)
        listOf(
            GroceryWidget2x1Receiver::class.java,
            GroceryWidgetMediumReceiver::class.java,
        ).any { manager.getAppWidgetIds(ComponentName(context, it)).isNotEmpty() }
    } catch (e: Exception) {
        false
    }

    /**
     * @param martId LIST only: the mart picked in-app before pinning (or ALL_MARTS).
     *   Handed to [WidgetPinSuccessReceiver] via the callback, which saves it for the
     *   new widget id the launcher fills in.
     * @return true if a pin request was shown, false if the launcher doesn't support it.
     */
    fun pinWidget(kind: WidgetKind, martId: Long? = null): Boolean {
        return try {
            val manager = AppWidgetManager.getInstance(context)
            if (!manager.isRequestPinAppWidgetSupported) return false
            val receiver = when (kind) {
                WidgetKind.COUNT -> GroceryWidget2x1Receiver::class.java
                WidgetKind.LIST -> GroceryWidgetMediumReceiver::class.java
            }
            val callbackIntent = Intent(context, WidgetPinSuccessReceiver::class.java)
            if (martId != null) callbackIntent.putExtra(WidgetPinSuccessReceiver.EXTRA_MART_ID, martId)
            // MUTABLE: the launcher must be able to add EXTRA_APPWIDGET_ID to it.
            val mutable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            val callback = PendingIntent.getBroadcast(
                context,
                0,
                callbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or mutable,
            )
            manager.requestPinAppWidget(ComponentName(context, receiver), null, callback)
        } catch (e: Exception) {
            false
        }
    }
}
