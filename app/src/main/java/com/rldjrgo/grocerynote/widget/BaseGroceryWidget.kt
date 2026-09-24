package com.rldjrgo.grocerynote.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.rldjrgo.grocerynote.data.local.DarkModePref
import com.rldjrgo.grocerynote.di.WidgetEntryPoint
import com.rldjrgo.grocerynote.widget.common.AdaptiveContent
import com.rldjrgo.grocerynote.widget.common.WidgetData
import com.rldjrgo.grocerynote.widget.common.widgetDataFlow
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

/**
 * Shared base for all 5 widgets. Each placed widget is `SizeMode.Responsive`
 * over the 5 breakpoints, so once on the home screen it auto-adapts between
 * Mini / Small / Long / Medium / Large as the user resizes it.
 *
 * The 5 concrete subclasses still exist on purpose: the add-widget picker pins
 * a specific INITIAL size (via each subclass's xml `targetCell*`), and existing
 * users' placed widgets keep working (class names/packages are unchanged).
 * After placement they all behave identically through [AdaptiveContent].
 */
abstract class BaseGroceryWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(WidgetSizes.responsiveSet)
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load data BEFORE composing. collectAsState(initial = null) used to push
        // a "불러오는 중…" frame to the launcher first; if the process was then
        // killed (boot, launcher restart, memory pressure) before the real data
        // frame landed, that loading card stayed on the home screen until the
        // next updateAll — potentially for a long time, since updatePeriodMillis=0
        // means nothing auto-refreshes. Now the first pushed frame IS the data.
        val entry = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
        // If the DB read fails outright, render the empty card rather than
        // letting the Glance worker crash into the launcher's error layout.
        val initialData = runCatching { widgetDataFlow(context).first() }
            .getOrElse { WidgetData(emptyList(), emptyMap()) }
        val initialDark = runCatching { entry.settingsDataStore().darkMode.first() }
            .getOrDefault(DarkModePref.Off)
        // ONE shared flow per session. Before, widgetDataFlow(context) was
        // rebuilt on every recomposition, so collectAsState re-subscribed to
        // Room ×5 breakpoints each time the data changed.
        val liveData = widgetDataFlow(context)
        provideContent {
            val ctx = LocalContext.current
            // Resolve dark IN-PROCESS (not via Glance day/night → launcher) so One UI
            // can't force the widget dark while the system is light. Honors the app's
            // DarkModePref so the widget matches the in-app theme.
            val darkPref by remember(ctx) {
                EntryPointAccessors
                    .fromApplication(ctx.applicationContext, WidgetEntryPoint::class.java)
                    .settingsDataStore()
                    .darkMode
            }.collectAsState(initial = initialDark)
            val systemNight = (ctx.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val isDark = when (darkPref) {
                DarkModePref.Auto -> systemNight
                DarkModePref.On -> true
                DarkModePref.Off -> false
            }

            CompositionLocalProvider(LocalWidgetDark provides isDark) {
                val size = LocalSize.current
                val data by liveData.collectAsState(initial = initialData)
                AdaptiveContent(size, data)
            }
        }
    }
}
