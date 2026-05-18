package com.rldjrgo.grocerynote.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Mini (2x1) initial size. After placement it adapts across all 5 layouts via
 * [BaseGroceryWidget] (SizeMode.Responsive). Class/package kept for back-compat.
 */
class GroceryWidget2x1 : BaseGroceryWidget()

class GroceryWidget2x1Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GroceryWidget2x1()
}
