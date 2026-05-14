package com.rldjrgo.grocerynote.widget.actions

import android.content.Intent
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionStartActivity
import com.rldjrgo.grocerynote.MainActivity
import com.rldjrgo.grocerynote.ui.navigation.Routes

/**
 * Pre-API-31 fallback (and header taps on any API) — opens MainActivity with the
 * target mart preselected and (optionally) the tapped item id for highlight.
 */
object OpenStoreAction {

    fun forStore(storeId: Long, itemId: Long? = null): Action {
        val params = mutableMapOf<ActionParameters.Key<*>, Any>()
        if (storeId > 0) params[StoreIdKey] = storeId
        if (itemId != null && itemId > 0) params[ItemIdKey] = itemId
        return actionStartActivity(
            intent = buildIntent(storeId, itemId),
            parameters = actionParametersOf(*params.entries.map { (k, v) ->
                @Suppress("UNCHECKED_CAST")
                (k as ActionParameters.Key<Any>) to v
            }.toTypedArray()),
        )
    }

    val StoreIdKey: ActionParameters.Key<Long> = ActionParameters.Key("storeId")
    val ItemIdKey: ActionParameters.Key<Long> = ActionParameters.Key("itemId")

    private fun buildIntent(storeId: Long, itemId: Long?): Intent {
        // The activity reads these via DeepLinkBus.consume(intent).
        return Intent().apply {
            // We don't know the package context here — let actionStartActivity resolve via
            // (Glance fills component from the runtime context). Setting component name lazily
            // via class is the safest pattern in Glance docs.
            setClassName(
                /* packageName = */ "com.rldjrgo.grocerynote",
                /* className   = */ MainActivity::class.java.name,
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (storeId > 0) putExtra(Routes.HOME_DEEPLINK_STORE_ARG, storeId)
            if (itemId != null && itemId > 0) putExtra(Routes.HOME_DEEPLINK_ITEM_ARG, itemId)
        }
    }
}
