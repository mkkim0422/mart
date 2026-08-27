package com.rldjrgo.grocerynote.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.rldjrgo.grocerynote.widget.GroceryWidget2x1Receiver
import com.rldjrgo.grocerynote.widget.GroceryWidgetLargeReceiver
import com.rldjrgo.grocerynote.widget.GroceryWidgetLongReceiver
import com.rldjrgo.grocerynote.widget.GroceryWidgetMediumReceiver
import com.rldjrgo.grocerynote.widget.GroceryWidgetSmallReceiver
import com.rldjrgo.grocerynote.widget.WidgetPinSuccessReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class WidgetSize { TWO_BY_ONE, SMALL, LONG, MEDIUM, LARGE }

/** Requests the launcher to pin a widget of the chosen size. */
@Singleton
class WidgetPinHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun isSupported(): Boolean =
        AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported

    /** 5개 사이즈 중 하나라도 홈화면에 실제로 놓여 있으면 true. */
    fun anyWidgetPlaced(): Boolean = try {
        val manager = AppWidgetManager.getInstance(context)
        listOf(
            GroceryWidget2x1Receiver::class.java,
            GroceryWidgetSmallReceiver::class.java,
            GroceryWidgetLongReceiver::class.java,
            GroceryWidgetMediumReceiver::class.java,
            GroceryWidgetLargeReceiver::class.java,
        ).any { manager.getAppWidgetIds(ComponentName(context, it)).isNotEmpty() }
    } catch (e: Exception) {
        false
    }

    /** @return true if a pin request was shown, false if the launcher doesn't support it. */
    fun pinWidget(size: WidgetSize): Boolean {
        return try {
            val manager = AppWidgetManager.getInstance(context)
            if (!manager.isRequestPinAppWidgetSupported) return false
            val receiver = when (size) {
                WidgetSize.TWO_BY_ONE -> GroceryWidget2x1Receiver::class.java
                WidgetSize.SMALL -> GroceryWidgetSmallReceiver::class.java
                WidgetSize.LONG -> GroceryWidgetLongReceiver::class.java
                WidgetSize.MEDIUM -> GroceryWidgetMediumReceiver::class.java
                WidgetSize.LARGE -> GroceryWidgetLargeReceiver::class.java
            }
            val provider = ComponentName(context, receiver)
            val callback = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, WidgetPinSuccessReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            manager.requestPinAppWidget(provider, null, callback)
        } catch (e: Exception) {
            false
        }
    }

    /** Back-compat: pin the recommended Medium widget. */
    fun pinMediumWidget(): Boolean = pinWidget(WidgetSize.MEDIUM)
}
