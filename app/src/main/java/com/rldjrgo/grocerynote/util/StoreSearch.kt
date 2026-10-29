package com.rldjrgo.grocerynote.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

/**
 * "쇼핑몰 앱 바로가기" (2026-10-28).
 *
 * The mart's NAME decides which shopping app it maps to (쿠팡 → 쿠팡 앱,
 * 이마트 → 이마트 앱 …). Marts with no matching app (시장, 약국…) get nothing.
 * Each mart has its own on/off switch at the top of its list
 * (`stores.shop_app_enabled`, default ON).
 *
 * Tapping 🔍 on an item:
 *  1. Apps with a search link open on that item's results. Verified on S24+ (2026-10-28):
 *     쿠팡, 컬리, 11번가, 옥션, 테무 (results), G마켓 (search box pre-filled).
 *     From APK analysis, not yet seen on a device: 홈플러스, SSG.
 *  2. Others: the app opens on its first screen (no search link found).
 *  3. App not installed → its Play Store page. Never a website.
 *
 * No <queries> needed: we only startActivity and catch the failure; we never
 * ask PackageManager what is installed.
 *
 * Package ids verified on Google Play (KR) 2026-10-28.
 */
object StoreSearch {

    class ShopApp(
        /** UI label: "쿠팡 앱 바로가기", "쿠팡에서 찾기". */
        val label: String,
        val packageName: String,
        /** Mart-name words that map to this app. */
        private val nameHints: List<String>,
        /** Case-sensitive hints (short Latin names like "CU" that would over-match). */
        private val exactHints: List<String> = emptyList(),
        /** In-app search link, only where the app publishes one. */
        private val searchLink: ((String) -> String)? = null,
    ) {
        /** True when tapping 🔍 lands on the item's search results (not just the app's home). */
        val searchesItem: Boolean get() = searchLink != null

        internal fun matches(martName: String): Boolean =
            nameHints.any { martName.contains(it, ignoreCase = true) } ||
                exactHints.any { martName.contains(it) }

        internal fun intents(query: String): List<Intent> {
            val q = URLEncoder.encode(query.trim(), "UTF-8")
            return listOfNotNull(
                // CLEAR_TASK: if the shop app is already running, many (컬리, 11번가…) just bring
                // their old screen to the front and ignore the new keyword (user saw "우유" instead
                // of their item). Clearing the shop app's task makes it open on the new search.
                searchLink?.let {
                    Intent(Intent.ACTION_VIEW, Uri.parse(it(q))).setPackage(packageName)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                },
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(packageName),
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")),
                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")),
            )
        }
    }

    /** URL-encode a whole URL to pass it as a query value. */
    private fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")

    /** First match wins, so more specific names ("이마트24", "이마트몰") come before "이마트". */
    private val apps: List<ShopApp> = listOf(
        ShopApp("쿠팡", "com.coupang.mobile", listOf("쿠팡"), searchLink = { q -> "coupang://search?q=$q" }),
        ShopApp("컬리", "com.dbs.kurly.m2", listOf("컬리"), searchLink = { q -> "kurly://search?keyword=$q" }),
        ShopApp("이마트24", "kr.co.emart24.everse", listOf("이마트24")),
        ShopApp("이마트에브리데이", "com.emarteveryday", listOf("이마트에브리데이", "이마트 에브리데이")),
        ShopApp("이마트몰", "kr.co.emart.emartmall", listOf("이마트몰")),
        ShopApp("이마트", "com.emart.today", listOf("이마트")),
        // ssg://execute params become a Bundle for the main screen; "landingUrl" is the
        // key its code reads (APK strings). Unverified on device: worst case the app opens on home.
        ShopApp("SSG", "kr.co.ssg", listOf("쓱", "SSG"), searchLink = { q ->
            "ssg://execute?landingUrl=" + enc("https://m.ssg.com/search.ssg?query=$q")
        }),
        // App manifest registers https://mfront.homeplus.co.kr/search → sent to the app package, it opens in-app.
        ShopApp("홈플러스", "com.socialapps.homeplus", listOf("홈플러스"), searchLink = { q -> "https://mfront.homeplus.co.kr/search?keyword=$q" }),
        ShopApp("롯데마트", "com.lottemart.lmscp", listOf("롯데마트", "롯데슈퍼")),
        ShopApp("롯데ON", "com.lotte", listOf("롯데온", "롯데ON")),
        ShopApp("다이소몰", "com.uxlayer.wipoint", listOf("다이소")),
        ShopApp("오아시스", "com.gaeasoft.oasis", listOf("오아시스")),
        ShopApp("하나로마트", "com.nonghyup.hanaromartapp", listOf("하나로", "농협")),
        ShopApp("킴스클럽", "kr.co.elandmall.kimsclub", listOf("킴스클럽")),
        ShopApp("우리동네GS", "com.gsr.gs25", listOf("GS25", "GS더프레시", "지에스")),
        ShopApp("포켓CU", "com.bgfcu.membership", listOf("씨유"), exactHints = listOf("CU")),
        ShopApp("배민", "com.sampleapp", listOf("배민", "배달의민족", "B마트")),
        ShopApp("올리브영", "com.oliveyoung", listOf("올리브영")),
        // elevenst://loadurl?url= opens a URL inside the app (string in the app's own code);
        // the search page is the one the app itself uses (search.11st.co.kr/MW/search?searchKeyword=).
        ShopApp("11번가", "com.elevenst", listOf("11번가"), searchLink = { q ->
            "elevenst://loadurl?url=" + enc("https://search.11st.co.kr/MW/search?searchKeyword=$q")
        }),
        // Opens G마켓's search box with the keyword filled in (one tap to results) — the closest
        // link found; openwindow/webview variants only showed the home screen (S24+, 2026-10-28).
        ShopApp("G마켓", "com.ebay.kr.gmarket", listOf("G마켓", "지마켓"), searchLink = { q ->
            "gmarket://search?type=page&keyword=$q"
        }),
        // auction://openwindow.v2?targeturl= opens a URL in-app → search results (verified S24+).
        ShopApp("옥션", "com.ebay.kr.auction", listOf("옥션"), searchLink = { q ->
            "auction://openwindow.v2?targeturl=" + enc("https://m.auction.co.kr/n/search?keyword=$q")
        }),
        ShopApp("무신사", "com.musinsa.store", listOf("무신사")),
        ShopApp("테무", "com.einnovation.temu", listOf("테무", "temu"), searchLink = { q ->
            "temu://com.einnovation.temu/search_result.html?search_key=$q"
        }),
        ShopApp("알리", "com.alibaba.aliexpresshd", listOf("알리", "aliexpress")),
    )

    /** The shopping app this mart name maps to, or null (no toggle, no 🔍). */
    fun appFor(martName: String): ShopApp? = apps.firstOrNull { it.matches(martName) }

    /**
     * Open [itemName] in [app]: search results (쿠팡·컬리) → app home → Play Store.
     * Returns false only when not even the Play Store could open.
     */
    fun open(context: Context, app: ShopApp, itemName: String): Boolean =
        // NEW_TASK always. (Tried without it: 쿠팡 puts its own home under the
        // search screen regardless, so back = 쿠팡 home either way — not ours to fix.)
        app.intents(itemName).any { intent ->
            runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess
        }
}
