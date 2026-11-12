package com.rldjrgo.grocerynote.widget

import androidx.compose.runtime.Composable
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import com.rldjrgo.grocerynote.widget.common.CountContent
import com.rldjrgo.grocerynote.widget.common.WidgetData

/**
 * 개수 위젯 — per-mart remaining counts (2x1 strip or 2x2+). Class name kept from
 * the old "Mini (2x1)" widget so users' placed Mini widgets carry over.
 * Exact size: rows are fitted to the real height (see [CountContent]).
 */
class GroceryWidget2x1 : BaseGroceryWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    @Composable
    override fun Content(data: WidgetData) = CountContent(LocalSize.current, data)
}

class GroceryWidget2x1Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GroceryWidget2x1()
}
