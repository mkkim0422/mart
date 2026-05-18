package com.rldjrgo.grocerynote.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Small (2x2) initial size. Adapts across all 5 layouts via [BaseGroceryWidget]. */
class GroceryWidgetSmall : BaseGroceryWidget()

class GroceryWidgetSmallReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GroceryWidgetSmall()
}
