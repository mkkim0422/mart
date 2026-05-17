package com.rldjrgo.grocerynote

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.rldjrgo.grocerynote.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize AdMob off the main thread — first call does network I/O.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching { MobileAds.initialize(this@App) {} }
        }
        // Firebase / Crashlytics auto-initialize via google-services plugin.

        // Start the widget auto-refresher (process-lifetime DB subscription).
        runCatching {
            EntryPointAccessors
                .fromApplication(this, WidgetEntryPoint::class.java)
                .widgetUpdater()
                .start()
        }
    }
}
