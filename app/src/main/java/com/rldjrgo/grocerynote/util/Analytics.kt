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
    fun adRemoved() = log("ad_removed")
    fun onboardingCompleted() = log("onboarding_completed")

    private fun log(name: String, vararg params: Pair<String, String>) {
        runCatching {
            val b = Bundle().apply {
                params.forEach { (k, v) -> putString(k, v) }
            }
            fa.logEvent(name, b)
        }
    }
}
