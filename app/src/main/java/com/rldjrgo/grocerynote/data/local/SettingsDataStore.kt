package com.rldjrgo.grocerynote.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "grocery_prefs")

enum class DarkModePref { Auto, On, Off }

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val HasSeenOnboarding = booleanPreferencesKey("has_seen_onboarding")
        val DarkMode = stringPreferencesKey("dark_mode")
        val IsAdRemoved = booleanPreferencesKey("is_ad_removed")
        val OnboardingCompletedCount = intPreferencesKey("onboarding_completed_count")
        val HasAddedWidget = booleanPreferencesKey("has_added_widget")
        val HasDismissedWidgetBanner = booleanPreferencesKey("has_dismissed_widget_banner")
        val WidgetNudgeLastShownAt = longPreferencesKey("widget_nudge_last_shown_at")
        val LargeWidgetStoreIds = stringPreferencesKey("large_widget_store_ids")
        val HasSeenVoiceIntro = booleanPreferencesKey("has_seen_voice_intro")
    }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.HasSeenOnboarding] ?: false }

    val darkMode: Flow<DarkModePref> = context.dataStore.data
        .map { runCatching { DarkModePref.valueOf(it[Keys.DarkMode] ?: "Off") }.getOrDefault(DarkModePref.Off) }

    val isAdRemoved: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.IsAdRemoved] ?: false }

    val hasAddedWidget: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.HasAddedWidget] ?: false }

    val hasDismissedWidgetBanner: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.HasDismissedWidgetBanner] ?: false }

    /** 위젯 유도 시트를 마지막으로 보여준 시각 (epoch millis, 0 = 아직 안 봄).
     *  위젯 미설치 상태면 15일 간격으로 재노출된다. */
    val widgetNudgeLastShownAt: Flow<Long> = context.dataStore.data
        .map { it[Keys.WidgetNudgeLastShownAt] ?: 0L }

    /** First-time voice-add intro sheet: shown once, then suppressed. */
    val hasSeenVoiceIntro: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.HasSeenVoiceIntro] ?: false }

    /** Stores chosen for the Large widget, in display order. Empty = auto (top by display order). */
    val largeWidgetStoreIds: Flow<List<Long>> = context.dataStore.data
        .map { prefs ->
            (prefs[Keys.LargeWidgetStoreIds] ?: "")
                .split(",")
                .mapNotNull { it.trim().toLongOrNull() }
        }

    suspend fun setOnboardingSeen() {
        context.dataStore.edit { it[Keys.HasSeenOnboarding] = true }
    }

    suspend fun setDarkMode(pref: DarkModePref) {
        context.dataStore.edit { it[Keys.DarkMode] = pref.name }
    }

    suspend fun setAdRemoved(removed: Boolean) {
        context.dataStore.edit { it[Keys.IsAdRemoved] = removed }
    }

    suspend fun setHasAddedWidget(added: Boolean) {
        context.dataStore.edit { it[Keys.HasAddedWidget] = added }
    }

    suspend fun setHasDismissedWidgetBanner(dismissed: Boolean) {
        context.dataStore.edit { it[Keys.HasDismissedWidgetBanner] = dismissed }
    }

    suspend fun setWidgetNudgeShownAt(atMillis: Long) {
        context.dataStore.edit { it[Keys.WidgetNudgeLastShownAt] = atMillis }
    }

    suspend fun setVoiceIntroSeen() {
        context.dataStore.edit { it[Keys.HasSeenVoiceIntro] = true }
    }

    suspend fun setLargeWidgetStoreIds(ids: List<Long>) {
        context.dataStore.edit { it[Keys.LargeWidgetStoreIds] = ids.joinToString(",") }
    }
}
