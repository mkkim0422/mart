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
import com.rldjrgo.grocerynote.widget.common.WidgetCard
import com.rldjrgo.grocerynote.widget.common.widgetDataFlow
import com.rldjrgo.grocerynote.widget.components.WidgetEmptyState
import dagger.hilt.android.EntryPointAccessors

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
            }.collectAsState(initial = DarkModePref.Off)
            val systemNight = (ctx.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val isDark = when (darkPref) {
                DarkModePref.Auto -> systemNight
                DarkModePref.On -> true
                DarkModePref.Off -> false
            }

            CompositionLocalProvider(LocalWidgetDark provides isDark) {
                val size = LocalSize.current
                val data by widgetDataFlow(context).collectAsState(initial = null)
                val d = data
                if (d == null) {
                    val mini = size.width < WidgetSizes.Medium.width && size.height <= WidgetSizes.Small.height
                    WidgetCard { WidgetEmptyState(title = "", hint = "불러오는 중…", compact = mini) }
                } else {
                    AdaptiveContent(size, d)
                }
            }
        }
    }
}
