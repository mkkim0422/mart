package com.rldjrgo.grocerynote.notify

import androidx.compose.ui.graphics.Color
import com.rldjrgo.grocerynote.data.local.NudgePrefs
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.Store
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NudgeEngineTest {
    private val zone = ZoneId.of("Asia/Seoul")
    private fun at(y: Int, m: Int, d: Int, h: Int = 9) =
        LocalDateTime.of(y, m, d, h, 0).atZone(zone).toInstant().toEpochMilli()
    private fun day(y: Int, m: Int, d: Int) = LocalDate.of(y, m, d).toEpochDay()
    private val oneDay = 86_400_000L
    private val oneHour = 3_600_000L

    private fun store(id: Long, name: String) = Store(id, name, Color.Blue, "box", id.toInt(), false, 0L)
    private fun item(id: Long, storeId: Long, name: String, created: Long, done: Boolean = false) =
        Item(id, storeId, name, done, if (done) created else null, 0, created)
    private val coupang = store(1, "쿠팡")
    private val daiso = store(2, "다이소")

    private fun snap(
        now: Long,
        items: List<Item> = emptyList(),
        prefs: NudgePrefs = NudgePrefs(),
        install: Long = now - 30 * oneDay,
    ) = NudgeSnapshot(listOf(coupang, daiso), items, now, install, prefs, mapOf("차례상" to 19), zone)

    @Test fun firstList_after20h_noItems() {
        val now = at(2026, 12, 2)
        assertNull(NudgeEngine.decide(NudgeSlot.MORNING, snap(now, install = now - 10 * oneHour)))
        assertEquals(
            NudgeType.FIRST_LIST,
            NudgeEngine.decide(NudgeSlot.MORNING, snap(now, install = now - 25 * oneHour))?.type,
        )
        assertNull(NudgeEngine.decide(NudgeSlot.MORNING, snap(now, prefs = NudgePrefs(firstListSent = true))))
    }

    @Test fun stale_itemNameFirst_and_cooldown() {
        val now = at(2026, 12, 2)
        val items = listOf(item(1, 1, "우유", now - 6 * oneDay), item(2, 1, "계란", now - 1 * oneDay))
        val n = NudgeEngine.decide(NudgeSlot.MORNING, snap(now, items))!!
        assertEquals(NudgeType.STALE_LIST, n.type)
        assertEquals("우유, 아직 안 사셨죠?", n.title)
        assertEquals("쿠팡에서 살 것 2개 · 6일째 그대로예요", n.text)
        assertEquals(1L, n.storeId)
        // 4일 전엔 아직 아님 / 쿨다운 5일 / 종류 토글
        assertNull(NudgeEngine.decide(NudgeSlot.MORNING, snap(now, listOf(item(1, 1, "우유", now - 4 * oneDay)))))
        val today = day(2026, 12, 2)
        assertNull(
            NudgeEngine.decide(NudgeSlot.MORNING, snap(now, items, NudgePrefs(staleLastSent = mapOf(1L to today - 3)))),
        )
        assertNull(NudgeEngine.decide(NudgeSlot.MORNING, snap(now, items, NudgePrefs(staleEnabled = false))))
    }

    @Test fun delivery_onlyCoupangOrKurly_evening() {
        val now = at(2026, 12, 2, 18)
        val three = listOf(item(1, 1, "우유", now), item(2, 1, "두부", now), item(3, 1, "라면", now))
        val n = NudgeEngine.decide(NudgeSlot.EVENING, snap(now, three))!!
        assertEquals("오늘 쿠팡 주문 마감 전이에요", n.title)
        assertEquals("우유 외 2개 담아 두셨어요", n.text)
        // 다이소 목록만 있으면 없음, 완료된 항목은 제외
        assertNull(NudgeEngine.decide(NudgeSlot.EVENING, snap(now, listOf(item(1, 2, "수세미", now)))))
        assertNull(NudgeEngine.decide(NudgeSlot.EVENING, snap(now, listOf(item(1, 1, "우유", now, done = true)))))
    }

    @Test fun onePerDay_exceptSeasonDay() {
        val now = at(2026, 12, 2, 18)
        val today = day(2026, 12, 2)
        val items = listOf(item(1, 1, "우유", now))
        assertNull(NudgeEngine.decide(NudgeSlot.EVENING, snap(now, items, NudgePrefs(lastSentDay = today))))
        assertEquals(
            NudgeType.DELIVERY,
            NudgeEngine.decide(
                NudgeSlot.EVENING,
                snap(now, items, NudgePrefs(lastSentDay = today, lastSentWasSeason = true)),
            )?.type,
        )
        assertNull(NudgeEngine.decide(NudgeSlot.MORNING, snap(now, items, NudgePrefs(enabled = false))))
    }

    @Test fun season_seollal2027_text_and_once() {
        val now = at(2027, 1, 31)
        val n = NudgeEngine.decide(NudgeSlot.MORNING, snap(now))!!
        assertEquals(NudgeType.SEASON, n.type)
        assertEquals("설 차례상 재료 19가지, 빠진 거 없는지 확인해요", n.title)
        assertEquals("설까지 7일 · 눌러서 마트 목록에 담기", n.text)
        assertEquals("차례상", n.recipeName)
        assertEquals("seollal-2027", n.seasonKey)
        assertNull(
            NudgeEngine.decide(
                NudgeSlot.MORNING,
                snap(now, prefs = NudgePrefs(seasonSent = setOf("seollal-2027"), firstListSent = true)),
            ),
        )
    }

    @Test fun seasonCalendar_dates_and_window() {
        // KASI 음력 1/1, 8/15 (korean-lunar-calendar로 대조, 2026-11-22)
        val expect = mapOf(
            "seollal-2027" to LocalDate.of(2027, 2, 7), "seollal-2028" to LocalDate.of(2028, 1, 27),
            "seollal-2029" to LocalDate.of(2029, 2, 13), "seollal-2030" to LocalDate.of(2030, 2, 3),
            "chuseok-2027" to LocalDate.of(2027, 9, 15), "chuseok-2028" to LocalDate.of(2028, 10, 3),
            "chuseok-2029" to LocalDate.of(2029, 9, 22), "chuseok-2030" to LocalDate.of(2030, 9, 12),
        )
        val all = (2027..2030).flatMap { SeasonCalendar.eventsFor(it) }.associate { it.key to it.date }
        expect.forEach { (k, d) -> assertEquals(k, d, all[k]) }
        // 창: D-7부터 3일. D-8, D-4는 아님.
        assertNull(SeasonCalendar.due(LocalDate.of(2027, 1, 30), emptySet()))
        assertEquals("seollal-2027", SeasonCalendar.due(LocalDate.of(2027, 2, 2), emptySet())?.key)
        assertNull(SeasonCalendar.due(LocalDate.of(2027, 2, 3), emptySet()))
        // 2031년엔 표가 없어 설·추석은 조용히 없음
        assertNull(SeasonCalendar.due(LocalDate.of(2031, 1, 16), emptySet()))
    }
}
