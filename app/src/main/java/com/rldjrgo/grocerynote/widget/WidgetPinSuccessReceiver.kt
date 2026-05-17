package com.rldjrgo.grocerynote.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import com.rldjrgo.grocerynote.di.ApplicationScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Fired by the launcher once the user confirms pinning the widget. */
@AndroidEntryPoint
class WidgetPinSuccessReceiver : BroadcastReceiver() {

    @Inject lateinit var settings: SettingsDataStore
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        appScope.launch {
            try {
                settings.setHasAddedWidget(true)
            } finally {
                pending.finish()
            }
        }
    }
}
