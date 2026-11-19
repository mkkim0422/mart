package com.rldjrgo.grocerynote.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import com.rldjrgo.grocerynote.di.ApplicationScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Fired by the launcher once the user confirms pinning the widget. */
@AndroidEntryPoint
class WidgetPinSuccessReceiver : BroadcastReceiver() {

    @Inject lateinit var settings: SettingsDataStore
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        // Fired the moment the user taps "추가" and the widget is actually
        // placed. Send them to the home screen now so they immediately see the
        // freshly-placed widget and can drag it where they want — no manual
        // "go to home screen" step. Race-free: this runs *after* the pin, not
        // before/with the requestPinAppWidget call.
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
        // The widget pins small (2x2) — say it can grow now, while they're looking at
        // it; the same line inside the picker sheet goes unread (user 2026-11-17).
        runCatching {
            Toast.makeText(
                context.applicationContext,
                "위젯을 꾹 누르면 크기를 바꿀 수 있어요",
                Toast.LENGTH_LONG,
            ).show()
        }
        // 목록 위젯 pinned from the in-app picker: apply the mart chosen there.
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val martId = if (intent.hasExtra(EXTRA_MART_ID)) intent.getLongExtra(EXTRA_MART_ID, 0L) else null
        val pending = goAsync()
        appScope.launch {
            try {
                if (martId != null && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    applyWidgetMart(context, appWidgetId, martId)
                }
                settings.setHasAddedWidget(true)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_MART_ID = "widget_mart_id"
    }
}
