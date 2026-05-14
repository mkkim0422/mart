package com.rldjrgo.grocerynote.util

import android.content.Context
import com.rldjrgo.grocerynote.widget.GroceryWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Fan-out helper: re-render every placed instance of GroceryWidget. Called from
 * every ViewModel mutation that affects what the widget shows.
 */
object WidgetUpdater {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun requestUpdate(context: Context) {
        scope.launch {
            runCatching {
                val mgr = GlanceAppWidgetManager(context)
                val ids = mgr.getGlanceIds(GroceryWidget::class.java)
                ids.forEach { id -> GroceryWidget().update(context, id) }
            }
        }
    }
}
