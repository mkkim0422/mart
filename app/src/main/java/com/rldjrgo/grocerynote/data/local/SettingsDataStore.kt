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
enum class WidgetRefresh { Immediate, Min5, Min30 }

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val HasSeenOnboarding = booleanPreferencesKey("has_seen_onboarding")
        val DarkMode = stringPreferencesKey("dark_mode")
        val WidgetRefresh = stringPreferencesKey("widget_refresh")
        val IsAdRemoved = booleanPreferencesKey("is_ad_removed")
        val OnboardingCompletedCount = intPreferencesKey("onboarding_completed_count")
    }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.HasSeenOnboarding] ?: false }

    val darkMode: Flow<DarkModePref> = context.dataStore.data
        .map { runCatching { DarkModePref.valueOf(it[Keys.DarkMode] ?: "Auto") }.getOrDefault(DarkModePref.Auto) }

    val widgetRefresh: Flow<WidgetRefresh> = context.dataStore.data
        .map { runCatching { WidgetRefresh.valueOf(it[Keys.WidgetRefresh] ?: "Immediate") }.getOrDefault(WidgetRefresh.Immediate) }

    val isAdRemoved: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.IsAdRemoved] ?: false }

    suspend fun setOnboardingSeen() {
        context.dataStore.edit { it[Keys.HasSeenOnboarding] = true }
    }

    suspend fun setDarkMode(pref: DarkModePref) {
        context.dataStore.edit { it[Keys.DarkMode] = pref.name }
    }

    suspend fun setWidgetRefresh(pref: WidgetRefresh) {
        context.dataStore.edit { it[Keys.WidgetRefresh] = pref.name }
    }

    suspend fun setAdRemoved(removed: Boolean) {
        context.dataStore.edit { it[Keys.IsAdRemoved] = removed }
    }
}
