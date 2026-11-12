package com.rldjrgo.grocerynote.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SharedTextParserTest {

    @Test
    fun linesBecomeItems() {
        assertEquals(listOf("우유", "계란", "두부"), SharedTextParser.parse("우유\n계란\n\n두부\n"))
    }

    @Test
    fun singleLineCommaList() {
        assertEquals(listOf("우유", "계란", "두부"), SharedTextParser.parse("우유, 계란,두부"))
    }

    @Test
    fun bulletsNumbersAndKakaoPrefixStripped() {
        val raw = "- 우유\n• 계란\n1. 두부\n2) 대파\n☐ 양파\n[엄마] [오후 3:12] 라면"
        assertEquals(listOf("우유", "계란", "두부", "대파", "양파", "라면"), SharedTextParser.parse(raw))
    }

    @Test
    fun martNoteShareRoundTrip() {
        val raw = "이것 좀 사다줘\n\n🛒 [쿠팡] 장보기 리스트\n\n• 우유\n• 계란\n\n📝 마트노트에서 보냄\n" +
            "👉 갤럭시(안드로이드)라면 마트노트 받기: https://play.google.com/store/apps/details?id=com.rldjrgo.grocerynote"
        assertEquals(listOf("이것 좀 사다줘", "우유", "계란"), SharedTextParser.parse(raw))
    }

    @Test
    fun longSentencesAndDuplicatesDropped() {
        val raw = "우유\n오늘 퇴근길에 마트 들러서 이것저것 좀 사오면 좋겠는데 시간 되면 부탁해\n우유"
        assertEquals(listOf("우유"), SharedTextParser.parse(raw))
    }
}
