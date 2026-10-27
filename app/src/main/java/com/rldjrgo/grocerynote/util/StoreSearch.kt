package com.rldjrgo.grocerynote.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

/**
 * "쿠팡에서 찾기" — for ONLINE marts, an item can be opened as a search in that
 * mart's own app (2026-10-27).
 *
 * Nothing leaves the device except the search the user asked to open: no
 * server, no account. Order of attempts (verified on-device 2026-10-28, S24+):
 *  1. The mart app's own search link, sent to its package explicitly
 *     (쿠팡 `coupang://search?q=`, 컬리 `kurly://search?keyword=` — both open the
 *     app straight on the results page).
 *  2. The mart's web search in a browser, where the mart allows it
 *     (컬리 works; 쿠팡's site answers "사용권한이 없습니다" to browsers, so it has none).
 *  3. The mart app's Play Store page, so the user can install it.
 *
 * The app links are the marts' own public schemes; if a mart ever changes
 * them step 2/3 still gets the user somewhere useful.
 *
 * Which marts count as online is decided by NAME (contains), so a user-renamed
 * "쿠팡 로켓" or "마켓컬리" still qualifies. Extend [targets] to add more.
 */
object StoreSearch {

    class Target(
        /** Short label for UI: "쿠팡에서 찾기". */
        val label: String,
        /** The mart's Android app. */
        val packageName: String,
        private val appLink: (String) -> String,
        private val webLink: ((String) -> String)?,
    ) {
        fun appUri(query: String): Uri = Uri.parse(appLink(enc(query)))
        fun webUri(query: String): Uri? = webLink?.let { Uri.parse(it(enc(query))) }
        fun storeUri(): Uri = Uri.parse("market://details?id=$packageName")
        private fun enc(q: String) = URLEncoder.encode(q.trim(), "UTF-8")
    }

    private val targets: List<Pair<String, Target>> = listOf(
        "쿠팡" to Target(
            label = "쿠팡",
            packageName = "com.coupang.mobile",
            appLink = { q -> "coupang://search?q=$q" },
            webLink = null,
        ),
        "컬리" to Target(
            label = "컬리",
            packageName = "com.dbs.kurly.m2",
            appLink = { q -> "kurly://search?keyword=$q" },
            webLink = { q -> "https://www.kurly.com/search?sword=$q" },
        ),
    )

    /** The online-mart target for this mart name, or null for offline marts (이마트/다이소/시장…). */
    fun targetFor(storeName: String): Target? =
        targets.firstOrNull { (key, _) -> storeName.contains(key) }?.second

    /**
     * Open [itemName] as a search in the mart's app (or browser / Play Store).
     * Returns false when the mart isn't an online target or nothing could open.
     */
    fun open(context: Context, storeName: String, itemName: String): Boolean {
        val target = targetFor(storeName) ?: return false
        val attempts = listOfNotNull(
            Intent(Intent.ACTION_VIEW, target.appUri(itemName)).setPackage(target.packageName),
            target.webUri(itemName)?.let { Intent(Intent.ACTION_VIEW, it) },
            Intent(Intent.ACTION_VIEW, target.storeUri()),
        )
        // NEW_TASK always. (Tried without it: 쿠팡 puts its own home under the
        // search screen regardless, so back = 쿠팡 home either way — not ours to fix.)
        return attempts.any { intent ->
            runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess
        }
    }
}
