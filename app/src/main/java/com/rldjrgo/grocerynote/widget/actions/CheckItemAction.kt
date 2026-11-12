package com.rldjrgo.grocerynote.widget.actions

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.rldjrgo.grocerynote.di.WidgetEntryPoint
import com.rldjrgo.grocerynote.util.Analytics
import com.rldjrgo.grocerynote.widget.GroceryWidgetMedium
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay

/** Per-widget Glance state (PreferencesGlanceStateDefinition). */
object WidgetStateKeys {
    /** List widget: which mart it shows. Missing / [ALL_MARTS] = every mart, stacked. */
    val MartId = longPreferencesKey("mart_id")
    const val ALL_MARTS = -1L

    /** Items tapped in THIS widget that are mid-check (✓ + strikethrough shown, not yet completed). */
    val Checking = stringSetPreferencesKey("checking_ids")
}

/**
 * Widget ○ tap → complete the item without opening the app (re-added 2026-11-12,
 * user decision: Keep & co. let you check from the widget; display-only was a
 * reason to leave). Shows a short ✓ + strikethrough first so the tap is felt —
 * widgets can't animate — then completes in the DB, which drops the row from
 * every widget via the live data flow.
 */
class CheckItemAction : ActionCallback {

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val itemId = parameters[ItemIdKey] ?: return
        val entry = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
        val repo = entry.itemRepository()
        val item = repo.getItem(itemId) ?: return
        if (item.isCompleted) return

        setChecking(context, glanceId, itemId, on = true)
        delay(FEEDBACK_MS)

        // Same side effects as the in-app 완료 (HomeViewModel.completeItem).
        repo.setReminder(itemId, null)
        entry.reminderScheduler().cancel(itemId)
        repo.completeItem(itemId)
        setChecking(context, glanceId, itemId, on = false)
        val storeName = entry.storeRepository().getStore(item.storeId)?.name.orEmpty()
        Analytics.itemCompleted(storeName, fromWidget = true)
        entry.widgetUpdater().updateAll()
    }

    private suspend fun setChecking(context: Context, glanceId: GlanceId, itemId: Long, on: Boolean) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val cur = prefs[WidgetStateKeys.Checking].orEmpty()
            prefs[WidgetStateKeys.Checking] = if (on) cur + itemId.toString() else cur - itemId.toString()
        }
        GroceryWidgetMedium().update(context, glanceId)
    }

    companion object {
        val ItemIdKey: ActionParameters.Key<Long> = ActionParameters.Key("itemId")
        private const val FEEDBACK_MS = 600L
    }
}
