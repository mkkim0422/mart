package com.rldjrgo.grocerynote.util

/**
 * Splits text shared into the app (카톡 "나에게 보내기" 메모, 가족이 보낸 마트노트
 * 리스트 등) into item names — one per line, or comma-separated on a single line.
 *
 * NOT a 카톡 대화 import (CLAUDE.md §3): only what the user explicitly shares
 * through the system share sheet, confirmed in [SharedItemsSheet] before saving.
 */
object SharedTextParser {

    /** Longest name kept — anything longer is a sentence, not an item. */
    private const val MAX_NAME = 30
    private const val MAX_ITEMS = 50

    // "[엄마] [오후 3:12] 우유" — prefix KakaoTalk adds when several messages are copied.
    private val kakaoPrefix = Regex("""^\[[^\]]*]\s*\[[^\]]*]\s*""")
    // Bullets / checkboxes / numbering: "- 우유", "• 우유", "☐ 우유", "1. 우유", "2) 우유".
    private val bullet = Regex("""^([-–—•·*▪◦○●□■☐☑✓✔✅]+|\d{1,2}[.)])\s*""")
    private val url = Regex("""https?://\S+""")

    fun parse(raw: String): List<String> {
        val lines = raw.lines().map { it.trim() }.filter { it.isNotEmpty() }
        // A single line "우유, 계란, 두부" is a list too.
        val pieces = if (lines.size == 1) lines[0].split(',', '，', '、') else lines
        return pieces.asSequence()
            .map { it.trim() }
            .filterNot { isChrome(it) }
            .map { kakaoPrefix.replace(it, "") }
            .map { bullet.replace(it, "").trim() }
            .filter { it.isNotEmpty() && it.length <= MAX_NAME }
            .distinct()
            .take(MAX_ITEMS)
            .toList()
    }

    /** Header/footer lines of a list shared from 마트노트 itself, and bare links. */
    private fun isChrome(line: String): Boolean =
        (line.startsWith("🛒") && line.endsWith("장보기 리스트")) ||
            line.startsWith("📝") ||
            line.startsWith("👉") ||
            url.containsMatchIn(line)
}
