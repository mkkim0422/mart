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
import com.rldjrgo.grocerynote.widget.common.LargeContent
import com.rldjrgo.grocerynote.widget.common.WidgetCard
import com.rldjrgo.grocerynote.widget.common.widgetDataFlow
import com.rldjrgo.grocerynote.widget.components.WidgetEmptyState

/** Large (4x4) — up to 4 marts (most items first / user-selected). */
class GroceryWidgetLarge : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val data by widgetDataFlow(context).collectAsState(initial = null)
            val d = data
            if (d == null) {
                WidgetCard { WidgetEmptyState("불러오는 중…", "잠시만요") }
            } else {
                LargeContent(d)
            }
        }
    }
}

class GroceryWidgetLargeReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GroceryWidgetLarge()
}
