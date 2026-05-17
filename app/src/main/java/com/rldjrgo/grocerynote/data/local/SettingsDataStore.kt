package com.rldjrgo.grocerynote.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
        val LargeWidgetStoreIds = stringPreferencesKey("large_widget_store_ids")
    }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.HasSeenOnboarding] ?: false }

    val darkMode: Flow<DarkModePref> = context.dataStore.data
        .map { runCatching { DarkModePref.valueOf(it[Keys.DarkMode] ?: "Auto") }.getOrDefault(DarkModePref.Auto) }

    val isAdRemoved: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.IsAdRemoved] ?: false }

    val hasAddedWidget: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.HasAddedWidget] ?: false }

    val hasDismissedWidgetBanner: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.HasDismissedWidgetBanner] ?: false }

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

    suspend fun setLargeWidgetStoreIds(ids: List<Long>) {
        context.dataStore.edit { it[Keys.LargeWidgetStoreIds] = ids.joinToString(",") }
    }
}
