package com.rldjrgo.grocerynote.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Long (2x4) initial size — one mart, full-height item list. Adapts across all
 * 5 layouts via [BaseGroceryWidget] when resized.
 */
class GroceryWidgetLong : BaseGroceryWidget()

class GroceryWidgetLongReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GroceryWidgetLong()
}
