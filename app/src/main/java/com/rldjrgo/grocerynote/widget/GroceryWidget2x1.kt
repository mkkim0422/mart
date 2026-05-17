package com.rldjrgo.grocerynote.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.rldjrgo.grocerynote.widget.common.SmallContent
import com.rldjrgo.grocerynote.widget.common.WidgetCard
import com.rldjrgo.grocerynote.widget.common.widgetDataFlow
import com.rldjrgo.grocerynote.widget.components.WidgetEmptyState

/**
 * 2x1 — ultra-compact one-line glance. Renders the SAME per-mart remaining-count
 * rows as the Small widget ([SmallContent]); the shorter footprint just shows the
 * top mart(s) that fit. Tap anywhere → opens the app.
 */
class GroceryWidget2x1 : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val data by widgetDataFlow(context).collectAsState(initial = null)
            val d = data
            if (d == null) {
                WidgetCard { WidgetEmptyState(title = "", hint = "불러오는 중…", compact = true) }
            } else {
                SmallContent(d, compact = true)
            }
        }
    }
}

class GroceryWidget2x1Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GroceryWidget2x1()
}
