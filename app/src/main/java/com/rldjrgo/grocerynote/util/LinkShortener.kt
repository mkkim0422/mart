package com.rldjrgo.grocerynote.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Shortens the shared list link (1.5.0) — user: the base64 link was still too long to
 * paste into a chat. is.gd first, tinyurl second, both key-free; each keeps the `#…`
 * list part intact through its 301 (checked 2026-11-13). Any failure / no network →
 * the long link, so sharing never breaks. If a service shuts down, swap it here.
 *
 * The list leaves the phone only here, only when the user taps share — it's a grocery
 * list the user is about to post in a chat anyway.
 */
object LinkShortener {

    private val services = listOf(
        "https://is.gd/create.php?format=simple&url=",
        "https://tinyurl.com/api-create.php?url=",
    )

    suspend fun shorten(longUrl: String): String = withContext(Dispatchers.IO) {
        val q = URLEncoder.encode(longUrl, "UTF-8")
        for (api in services) {
            val short = runCatching {
                val conn = URL(api + q).openConnection() as HttpURLConnection
                try {
                    conn.connectTimeout = 2_500
                    conn.readTimeout = 2_500
                    if (conn.responseCode != 200) return@runCatching null
                    conn.inputStream.bufferedReader().use { it.readText() }.trim()
                } finally {
                    conn.disconnect()
                }
            }.getOrNull()
            if (short != null && short.startsWith("https://") && short.length < 60) return@withContext short
        }
        longUrl
    }
}
