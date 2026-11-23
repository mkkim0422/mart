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
import com.rldjrgo.grocerynote.notify.NudgeSlot
import com.rldjrgo.grocerynote.notify.NudgeType

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "grocery_prefs")

enum class DarkModePref { Auto, On, Off }

private const val RECENT_RECIPES_MAX = 8

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
        val HasSeenSearchHint = booleanPreferencesKey("has_seen_search_hint")
        val CompletedCount = intPreferencesKey("completed_count")
        val ReviewAsked = booleanPreferencesKey("review_asked")
        val HasSeenMartPicker = booleanPreferencesKey("has_seen_mart_picker")
        val RecentRecipes = stringPreferencesKey("recent_recipes")
        val RecipeLastStoreId = longPreferencesKey("recipe_last_store_id")
        val HasOpenedRecipes = booleanPreferencesKey("has_opened_recipes")
        // 리텐션 알림(넛지) — 1.7.0
        val NudgeEnabled = booleanPreferencesKey("nudge_enabled")
        val NudgeStale = booleanPreferencesKey("nudge_stale")
        val NudgeDelivery = booleanPreferencesKey("nudge_delivery")
        val NudgeSeason = booleanPreferencesKey("nudge_season")
        val NudgeMorningHour = intPreferencesKey("nudge_morning_hour")
        val NudgeEveningHour = intPreferencesKey("nudge_evening_hour")
        val NudgeLastSentDay = longPreferencesKey("nudge_last_sent_day")
        val NudgeLastSentWasSeason = booleanPreferencesKey("nudge_last_sent_was_season")
        val NudgeFirstListSent = booleanPreferencesKey("nudge_first_list_sent")
        val NudgeStaleLastSent = stringPreferencesKey("nudge_stale_last_sent")
        val NudgeDeliveryLastSent = stringPreferencesKey("nudge_delivery_last_sent")
        val NudgeSeasonSent = stringPreferencesKey("nudge_season_sent")
        val NotifPermAsked = booleanPreferencesKey("notif_perm_asked")
    }

    /** "요리별 재료" tab: last opened dishes, newest first (names, max [RECENT_RECIPES_MAX]). */
    val recentRecipes: Flow<List<String>> = context.dataStore.data
        .map { prefs -> (prefs[Keys.RecentRecipes] ?: "").split("\n").filter { it.isNotBlank() } }

    suspend fun pushRecentRecipe(name: String) {
        context.dataStore.edit { prefs ->
            val cur = (prefs[Keys.RecentRecipes] ?: "").split("\n").filter { it.isNotBlank() && it != name }
            prefs[Keys.RecentRecipes] = (listOf(name) + cur).take(RECENT_RECIPES_MAX).joinToString("\n")
        }
    }

    /** "요리별 재료" tab opened at least once → its "new" dot on the bottom bar goes away for good. */
    val hasOpenedRecipes: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.HasOpenedRecipes] ?: false }

    suspend fun setRecipesOpened() {
        context.dataStore.edit { it[Keys.HasOpenedRecipes] = true }
    }

    /** Mart the user last added recipe ingredients to (0 = none yet). */
    val recipeLastStoreId: Flow<Long> = context.dataStore.data
        .map { it[Keys.RecipeLastStoreId] ?: 0L }

    suspend fun setRecipeLastStoreId(id: Long) {
        context.dataStore.edit { it[Keys.RecipeLastStoreId] = id }
    }

    /** First-run "자주 가는 곳을 골라 주세요" sheet shown/handled (1.5.1). */
    val hasSeenMartPicker: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.HasSeenMartPicker] ?: false }

    suspend fun setMartPickerSeen() {
        context.dataStore.edit { it[Keys.HasSeenMartPicker] = true }
    }

    /** In-app review (2026-11-13): count in-app completes; ask once at the Nth. */
    suspend fun bumpCompletedCount(): Int {
        var n = 0
        context.dataStore.edit { n = (it[Keys.CompletedCount] ?: 0) + 1; it[Keys.CompletedCount] = n }
        return n
    }

    val reviewAsked: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.ReviewAsked] ?: false }

    suspend fun setReviewAsked() {
        context.dataStore.edit { it[Keys.ReviewAsked] = true }
    }

    /** One-time tip on online-mart tabs ("🔍 누르면 쿠팡에서 바로 검색"); cleared on dismiss or first use. */
    val hasSeenSearchHint: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.HasSeenSearchHint] ?: false }

    suspend fun setSearchHintSeen() {
        context.dataStore.edit { it[Keys.HasSeenSearchHint] = true }
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

    // ── 리텐션 알림(넛지) 설정 + 발송 이력 (1.7.0) ─────────────────────────────────
    val nudgePrefs: Flow<NudgePrefs> = context.dataStore.data.map { p ->
        NudgePrefs(
            enabled = p[Keys.NudgeEnabled] ?: true,
            staleEnabled = p[Keys.NudgeStale] ?: true,
            deliveryEnabled = p[Keys.NudgeDelivery] ?: true,
            seasonEnabled = p[Keys.NudgeSeason] ?: true,
            morningHour = p[Keys.NudgeMorningHour] ?: NudgePrefs.DEFAULT_MORNING_HOUR,
            eveningHour = p[Keys.NudgeEveningHour] ?: NudgePrefs.DEFAULT_EVENING_HOUR,
            lastSentDay = p[Keys.NudgeLastSentDay] ?: -1L,
            lastSentWasSeason = p[Keys.NudgeLastSentWasSeason] ?: false,
            firstListSent = p[Keys.NudgeFirstListSent] ?: false,
            staleLastSent = parseDayMap(p[Keys.NudgeStaleLastSent]),
            deliveryLastSent = parseDayMap(p[Keys.NudgeDeliveryLastSent]),
            seasonSent = (p[Keys.NudgeSeasonSent] ?: "").split(",").filter { it.isNotBlank() }.toSet(),
            permissionAsked = p[Keys.NotifPermAsked] ?: false,
        )
    }

    suspend fun setNudgeEnabled(on: Boolean) {
        context.dataStore.edit { it[Keys.NudgeEnabled] = on }
    }

    suspend fun setNudgeTypeEnabled(type: NudgeType, on: Boolean) {
        val key = when (type) {
            NudgeType.STALE_LIST -> Keys.NudgeStale
            NudgeType.DELIVERY -> Keys.NudgeDelivery
            NudgeType.SEASON -> Keys.NudgeSeason
            NudgeType.FIRST_LIST -> return // 평생 1회짜리, 토글 없음
        }
        context.dataStore.edit { it[key] = on }
    }

    suspend fun setNudgeHour(slot: NudgeSlot, hour: Int) {
        val key = if (slot == NudgeSlot.MORNING) Keys.NudgeMorningHour else Keys.NudgeEveningHour
        context.dataStore.edit { it[key] = hour.coerceIn(0, 23) }
    }

    suspend fun setNotifPermissionAsked() {
        context.dataStore.edit { it[Keys.NotifPermAsked] = true }
    }

    /** 알림 1건을 보낸 직후 호출 — 하루 상한·종류별 쿨다운·시즌 중복 방지 이력. */
    suspend fun recordNudgeSent(type: NudgeType, today: Long, storeId: Long?, seasonKey: String?) {
        context.dataStore.edit { p ->
            p[Keys.NudgeLastSentDay] = today
            p[Keys.NudgeLastSentWasSeason] = type == NudgeType.SEASON
            when (type) {
                NudgeType.FIRST_LIST -> p[Keys.NudgeFirstListSent] = true
                NudgeType.STALE_LIST -> if (storeId != null) {
                    p[Keys.NudgeStaleLastSent] = encodeDayMap(parseDayMap(p[Keys.NudgeStaleLastSent]) + (storeId to today))
                }
                NudgeType.DELIVERY -> if (storeId != null) {
                    p[Keys.NudgeDeliveryLastSent] = encodeDayMap(parseDayMap(p[Keys.NudgeDeliveryLastSent]) + (storeId to today))
                }
                NudgeType.SEASON -> if (seasonKey != null) {
                    val cur = (p[Keys.NudgeSeasonSent] ?: "").split(",").filter { it.isNotBlank() }.toMutableSet()
                    cur += seasonKey
                    // 지난 연도 키는 버려서 무한히 안 자라게 (키 끝이 연도).
                    val thisYear = java.time.LocalDate.ofEpochDay(today).year
                    p[Keys.NudgeSeasonSent] = cur.filter { k -> (k.substringAfterLast('-').toIntOrNull() ?: thisYear) >= thisYear - 1 }
                        .joinToString(",")
                }
            }
        }
    }

    /** "storeId:epochDay,storeId:epochDay" ↔ Map. 마트가 몇 개 안 되니 문자열로 충분. */
    private fun parseDayMap(raw: String?): Map<Long, Long> =
        (raw ?: "").split(",").mapNotNull { e ->
            val parts = e.split(":")
            if (parts.size != 2) return@mapNotNull null
            val id = parts[0].toLongOrNull() ?: return@mapNotNull null
            val day = parts[1].toLongOrNull() ?: return@mapNotNull null
            id to day
        }.toMap()

    private fun encodeDayMap(m: Map<Long, Long>): String =
        m.entries.joinToString(",") { "${it.key}:${it.value}" }
}
