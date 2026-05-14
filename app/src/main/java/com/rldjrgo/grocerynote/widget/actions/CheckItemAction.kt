package com.rldjrgo.grocerynote.widget.actions

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.rldjrgo.grocerynote.di.WidgetEntryPoint
import com.rldjrgo.grocerynote.widget.GroceryWidget
import dagger.hilt.android.EntryPointAccessors

/**
 * API 31+ checkbox tap → complete the item, refresh the widget. Stays under the 5s
 * ActionCallback budget — we run synchronously on the caller's IO context.
 */
class CheckItemAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val itemId = parameters[ItemIdKey] ?: return
        if (itemId <= 0) return
        val entry = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        )
        runCatching { entry.itemRepository().completeItem(itemId) }
        // Re-render this widget.
        GroceryWidget().update(context, glanceId)
    }

    companion object {
        val ItemIdKey: ActionParameters.Key<Long> = ActionParameters.Key("itemId")
    }
}
