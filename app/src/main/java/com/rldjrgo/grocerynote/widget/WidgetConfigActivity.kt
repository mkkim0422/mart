package com.rldjrgo.grocerynote.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.rldjrgo.grocerynote.data.repository.ItemRepository
import com.rldjrgo.grocerynote.data.repository.StoreRepository
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.domain.model.emoji
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.widget.actions.WidgetStateKeys
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * 목록 위젯 "어떤 마트를 보여줄까요?" — launched by the launcher when the widget is
 * placed from the system picker (where it isn't optional) and on long-press → 설정.
 * Backing out keeps the widget as 전체 마트: RESULT_OK is set up front, because a
 * CANCELED result makes the launcher delete a freshly placed widget.
 */
@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    @Inject lateinit var storeRepo: StoreRepository
    @Inject lateinit var itemRepo: ItemRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))

        enableEdgeToEdge()
        lifecycleScope.launch {
            val current = currentMart(this@WidgetConfigActivity, appWidgetId)
            setContent {
                AppTheme(darkTheme = isSystemInDarkTheme()) {
                    val stores by storeRepo.observeActiveStores()
                        .collectAsStateWithLifecycle(initialValue = emptyList())
                    val counts by itemRepo.observeActiveCounts()
                        .collectAsStateWithLifecycle(initialValue = emptyMap())
                    MartPicker(
                        stores = stores,
                        counts = counts,
                        initial = current,
                        onConfirm = { martId ->
                            lifecycleScope.launch {
                                applyWidgetMart(this@WidgetConfigActivity, appWidgetId, martId)
                                finish()
                            }
                        },
                        onCancel = { finish() },
                    )
                }
            }
        }
    }
}

/** Save which mart list widget [appWidgetId] shows and redraw it. */
suspend fun applyWidgetMart(context: Context, appWidgetId: Int, martId: Long) {
    val glanceId = runCatching { GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId) }.getOrNull() ?: return
    updateAppWidgetState(context, glanceId) { it[WidgetStateKeys.MartId] = martId }
    GroceryWidgetMedium().update(context, glanceId)
}

private suspend fun currentMart(context: Context, appWidgetId: Int): Long {
    val glanceId = runCatching { GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId) }.getOrNull()
        ?: return WidgetStateKeys.ALL_MARTS
    val prefs: Preferences = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
    return prefs[WidgetStateKeys.MartId] ?: WidgetStateKeys.ALL_MARTS
}

/**
 * Radio list: 전체 마트 + each mart. Shared with the in-app widget picker
 * (WidgetSizePickerSheet) so both places ask the same question the same way.
 */
@Composable
fun MartPicker(
    stores: List<Store>,
    /** Null = don't show item counts (caller has none at hand). */
    counts: Map<Long, Int>?,
    initial: Long,
    onConfirm: (Long) -> Unit,
    onCancel: (() -> Unit)?,
    modifier: Modifier = Modifier.fillMaxSize().safeDrawingPadding(),
    confirmLabel: String = "완료",
    /** Small gray line above the confirm button (in-app picker: how the pin popup works). */
    footnote: String? = null,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    var picked by remember(initial) { mutableLongStateOf(initial) }
    Column(modifier = modifier.background(colors.bgPrimary).padding(20.dp)) {
        Text("어떤 마트를 보여줄까요?", style = typo.headingL, color = colors.textPrimary)
        Spacer(Modifier.height(6.dp))
        Text("나중에 위젯을 길게 눌러 바꿀 수 있어요", style = typo.bodyS, color = colors.textTertiary)
        Spacer(Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            item {
                MartOption(
                    selected = picked == WidgetStateKeys.ALL_MARTS,
                    title = "전체 마트",
                    sub = "마트별로 이어서 보여줘요",
                    badge = null,
                    onClick = { picked = WidgetStateKeys.ALL_MARTS },
                )
            }
            items(stores, key = { it.id }) { s ->
                MartOption(
                    selected = picked == s.id,
                    title = s.name,
                    sub = counts?.let { "${it[s.id] ?: 0}개" },
                    badge = s.emoji() to s.color,
                    onClick = { picked = s.id },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        if (footnote != null) {
            Text(footnote, style = typo.bodyS, color = colors.textTertiary)
            Spacer(Modifier.height(12.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(colors.brandPrimary, RoundedCornerShape(12.dp))
                .clickable { onConfirm(picked) },
            contentAlignment = Alignment.Center,
        ) {
            Text(confirmLabel, style = typo.title, color = Color.White)
        }
        if (onCancel != null) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(colors.bgTertiary, RoundedCornerShape(12.dp))
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center,
            ) {
                Text("취소", style = typo.title, color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun MartOption(
    selected: Boolean,
    title: String,
    sub: String?,
    badge: Pair<String, Color>?,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(
                    width = if (selected) 7.dp else 2.dp,
                    color = if (selected) colors.brandPrimary else colors.textDisabled,
                    shape = CircleShape,
                ),
        )
        Spacer(Modifier.width(14.dp))
        if (badge != null) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(badge.second.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) { Text(badge.first, style = typo.bodyS) }
            Spacer(Modifier.width(10.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = typo.title, color = colors.textPrimary)
            if (sub != null) Text(sub, style = typo.caption, color = colors.textTertiary)
        }
    }
}
