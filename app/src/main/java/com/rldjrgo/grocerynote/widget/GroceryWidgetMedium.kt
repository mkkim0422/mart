package com.rldjrgo.grocerynote.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Medium (4x2) initial size — the recommended default. Adapts via [BaseGroceryWidget]. */
class GroceryWidgetMedium : BaseGroceryWidget()

class GroceryWidgetMediumReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GroceryWidgetMedium()
}
