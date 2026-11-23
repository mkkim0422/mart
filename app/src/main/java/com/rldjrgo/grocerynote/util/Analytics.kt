package com.rldjrgo.grocerynote.util

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

/** Fire-and-forget Firebase Analytics wrapper. Safe to call before init. */
object Analytics {

    private val fa: FirebaseAnalytics by lazy { Firebase.analytics }

    // Seed marts (CLAUDE.md §9). User-named marts must NOT be sent raw to
    // Analytics — only a coarse default/custom class, so a personally-named
    // mart never leaves the device as-is.
    private val seedStores = setOf("쿠팡", "다이소")
    private fun storeClass(name: String) = if (name in seedStores) "default" else "custom"

    fun storeAdded(name: String) = log("store_added", "store_type" to storeClass(name))
    fun itemAdded(storeName: String) = log("item_added", "store_type" to storeClass(storeName))
    fun itemCompleted(storeName: String, fromWidget: Boolean = false) =
        log(if (fromWidget) "item_completed_widget" else "item_completed", "store_type" to storeClass(storeName))
    fun widgetUsed() = log("widget_used")
    /** "쿠팡에서 찾기" tapped. [target] is the shop-app label (StoreSearch.appFor), never the user's mart name. */
    fun storeSearch(target: String) = log("store_search", "target" to target)
    /** Reminder armed — tells whether the (now ⋮-menu-only) feature is still used. */
    fun reminderSet() = log("reminder_set")
    /** Multi-line shared text (카톡 등) split into items. [count] bucketed, never the text. */
    fun sharedItemsAdded(count: Int) =
        log("shared_items_added", "count" to if (count >= 10) "10+" else count.toString())
    fun itemsReordered() = log("items_reordered")
    /** A received list link (1.5.0) opened the app. */
    fun listLinkOpened(count: Int) =
        log("list_link_opened", "count" to if (count >= 10) "10+" else count.toString())
    /** "요리별 재료" tab: a dish was opened. Dish names are our own bundled table, not user text. */
    fun recipeOpened(name: String) = log("recipe_opened", "recipe" to name)
    fun recipeItemsAdded(count: Int) =
        log("recipe_items_added", "count" to if (count >= 10) "10+" else count.toString())
    /** "만드는 법 보기" → YouTube search. Tells whether in-app cooking steps (phase 2) are worth building. */
    fun recipeHowTo(name: String) = log("recipe_howto", "recipe" to name)
    /**
     * A dish search that found nothing — the one place typed text leaves the device
     * (owner decision 2026-11-19: it tells us which dishes to add to the table).
     * Only letters/Hangul, max 20 chars, never numbers. Must be declared in the privacy
     * policy and Play "Data safety" (App activity → in-app search history) before release.
     */
    fun recipeSearchMiss(query: String) {
        val q = query.filter { it.isLetter() || it == ' ' }.trim().take(20)
        if (q.length >= 2) log("recipe_search_miss", "query" to q)
    }
    fun adRemoved() = log("ad_removed")
    fun onboardingCompleted() = log("onboarding_completed")
    /** 리텐션 알림(1.7.0): 보냄 / 눌러서 열림 — 종류별 열림률로 약한 알림을 뺀다. */
    fun nudgeSent(type: String) = log("nudge_sent", "type" to type)
    fun nudgeOpened(type: String) = log("nudge_opened", "type" to type)
    /** 알림 권한 결과. where = onboarding / onboarding_skip */
    fun notifPermission(granted: Boolean, where: String) =
        log("notif_permission", "granted" to granted.toString(), "where" to where)

    private fun log(name: String, vararg params: Pair<String, String>) {
        runCatching {
            val b = Bundle().apply {
                params.forEach { (k, v) -> putString(k, v) }
            }
            fa.logEvent(name, b)
        }
    }
}
