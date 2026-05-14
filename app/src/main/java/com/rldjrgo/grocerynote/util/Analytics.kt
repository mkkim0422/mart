package com.rldjrgo.grocerynote.util

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

/** Fire-and-forget Firebase Analytics wrapper. Safe to call before init. */
object Analytics {

    private val fa: FirebaseAnalytics by lazy { Firebase.analytics }

    fun storeAdded(name: String) = log("store_added", "store_name" to name)
    fun itemAdded(storeName: String) = log("item_added", "store_name" to storeName)
    fun itemCompleted(storeName: String, fromWidget: Boolean = false) =
        log(if (fromWidget) "item_completed_widget" else "item_completed", "store_name" to storeName)
    fun widgetUsed() = log("widget_used")
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
