package com.rldjrgo.grocerynote.util

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import com.rldjrgo.grocerynote.data.repository.ItemRepository
import com.rldjrgo.grocerynote.data.repository.StoreRepository
import com.rldjrgo.grocerynote.di.ApplicationScope
import com.rldjrgo.grocerynote.widget.GroceryWidget2x1
import com.rldjrgo.grocerynote.widget.GroceryWidgetLarge
import com.rldjrgo.grocerynote.widget.GroceryWidgetMedium
import com.rldjrgo.grocerynote.widget.GroceryWidgetSmall
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "WidgetUpdater"

/**
 * Keeps every placed widget (Small/Medium/Large) in sync with the DB.
 *
 * Primary mechanism (reliable): a process-lifetime subscription to the items +
 * stores Flows — any DB write re-renders the widgets, so we never depend on each
 * ViewModel remembering to call us. [updateAll] stays as an explicit 2nd path.
 *
 * Render uses `GlanceAppWidget.updateAll(context)` (the high-level API that
 * recomposes ALL instances) instead of manual glanceId enumeration which could
 * silently return 0, and renders twice (Glance background-caches).
 */
@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val settings: SettingsDataStore,
    private val itemRepo: ItemRepository,
    private val storeRepo: StoreRepository,
) {
    private val renderMutex = Mutex()
    private val started = AtomicBoolean(false)

    // All render triggers (DB change + explicit ViewModel calls) funnel here.
    // A burst of fast adds collapses into ONE render via the short debounce —
    // no per-call mutex pile-up, so the count never lags behind the app.
    private val trigger = MutableSharedFlow<Unit>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** Called once from App.onCreate. */
    fun start() {
        if (!started.compareAndSet(false, true)) return
        Log.d(TAG, "start(): subscribing")
        // DB change → poke the trigger.
        applicationScope.launch(Dispatchers.IO) {
            combine(
                itemRepo.observeAllItems(),
                storeRepo.observeActiveStores(),
            ) { items, stores -> items.size to stores.size }
                .drop(1) // initial emission: widget already rendered on placement
                .collect {
                    Log.d(TAG, "DB changed → trigger")
                    trigger.tryEmit(Unit)
                }
        }
        // Single coalesced render pipeline (80ms quiet window).
        applicationScope.launch(Dispatchers.IO) {
            trigger
                .debounce(120)
                .collect { renderAll("coalesced") }
        }
    }

    /** Explicit trigger (ViewModels, CheckItemAction). Cheap, non-blocking. */
    fun updateAll() {
        Log.d(TAG, "updateAll() called")
        trigger.tryEmit(Unit)
    }

    private data class Target(
        val widget: GlanceAppWidget,
        val widgetClass: Class<out GlanceAppWidget>,
        val name: String,
    )

    private fun targets(): List<Target> = listOf(
        Target(GroceryWidget2x1(), GroceryWidget2x1::class.java, "2x1"),
        Target(GroceryWidgetSmall(), GroceryWidgetSmall::class.java, "Small"),
        Target(GroceryWidgetMedium(), GroceryWidgetMedium::class.java, "Medium"),
        Target(GroceryWidgetLarge(), GroceryWidgetLarge::class.java, "Large"),
    )

    /**
     * ONE clean pass: `GlanceAppWidget.updateAll(context)` per class — the
     * documented API that restarts each placed instance's Glance session. The
     * widget itself subscribes to widgetDataFlow via collectAsState, so it also
     * auto-refreshes while alive; this updateAll is the wake/fallback path.
     * Mutex + debounce keep bursts to a single sequential render.
     */
    private suspend fun renderAll(reason: String) {
        renderMutex.withLock {
            Log.d(TAG, "renderAll[$reason]: begin")
            var anyPlaced = false
            targets().forEach { t ->
                runCatching {
                    val ids = GlanceAppWidgetManager(context).getGlanceIds(t.widgetClass)
                    if (ids.isNotEmpty()) anyPlaced = true
                    Log.d(TAG, "$reason ${t.name} ids=${ids.size} → updateAll")
                    t.widget.updateAll(context)
                }.onFailure { Log.e(TAG, "$reason ${t.name} failed", it) }
            }
            if (anyPlaced) runCatching { settings.setHasAddedWidget(true) }
            Log.d(TAG, "renderAll[$reason]: done (anyPlaced=$anyPlaced)")
        }
    }
}
