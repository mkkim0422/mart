package com.rldjrgo.grocerynote

import android.app.Application
import android.content.res.Configuration
import com.google.android.gms.ads.MobileAds
import com.rldjrgo.grocerynote.di.WidgetEntryPoint
import com.rldjrgo.grocerynote.util.WidgetUpdater
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class App : Application() {
    private var widgetUpdater: WidgetUpdater? = null
    private var lastNightMode: Int = Configuration.UI_MODE_NIGHT_UNDEFINED

    override fun onCreate() {
        super.onCreate()
        // Initialize AdMob off the main thread — first call does network I/O.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching { MobileAds.initialize(this@App) {} }
        }
        // Firebase / Crashlytics auto-initialize via google-services plugin.

        lastNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        // Start the widget auto-refresher (process-lifetime DB subscription).
        runCatching {
            EntryPointAccessors
                .fromApplication(this, WidgetEntryPoint::class.java)
                .widgetUpdater()
                .also { widgetUpdater = it }
                .start()
        }
    }

    /**
     * The widget bakes light/dark in-process from the phone's night mode (see
     * BaseGroceryWidget), so the launcher can't flip it by itself. Re-render when
     * the phone theme changes while our process is alive; if the process is dead
     * the next app open / DB write catches up (same as any other refresh).
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val night = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (night != lastNightMode) {
            lastNightMode = night
            widgetUpdater?.updateAll()
        }
    }
}
