package com.rldjrgo.grocerynote.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Large (4x4) initial size. Adapts across all 5 layouts via [BaseGroceryWidget]. */
class GroceryWidgetLarge : BaseGroceryWidget()

class GroceryWidgetLargeReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GroceryWidgetLarge()
}
