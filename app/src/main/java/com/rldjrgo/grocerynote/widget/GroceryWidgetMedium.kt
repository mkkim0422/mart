package com.rldjrgo.grocerynote.widget

import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.currentState
import com.rldjrgo.grocerynote.widget.actions.WidgetStateKeys
import com.rldjrgo.grocerynote.widget.common.ListContent
import com.rldjrgo.grocerynote.widget.common.WidgetData

/**
 * 목록 위젯 — one mart (or all marts stacked) as ONE scrollable list with ○ to
 * check in place. The mart is chosen when the widget is placed (in-app picker or
 * [WidgetConfigActivity]) and changed via long-press → 설정. Class name kept from
 * the old "Medium (4x2)" widget so placed Medium widgets carry over (as 전체).
 * The layout just fills whatever size it's given → [SizeMode.Single].
 */
class GroceryWidgetMedium : BaseGroceryWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

    @Composable
    override fun Content(data: WidgetData) {
        val prefs = currentState<Preferences>()
        ListContent(
            data = data,
            martId = prefs[WidgetStateKeys.MartId] ?: WidgetStateKeys.ALL_MARTS,
            checking = prefs[WidgetStateKeys.Checking].orEmpty().mapNotNullTo(HashSet()) { it.toLongOrNull() },
        )
    }
}

class GroceryWidgetMediumReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GroceryWidgetMedium()
}
