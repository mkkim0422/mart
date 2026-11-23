package com.rldjrgo.grocerynote.notify

import com.rldjrgo.grocerynote.data.local.NudgePrefs
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.util.StoreSearch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** 알림 1건의 내용 + 탭했을 때 열 곳 + 보냈다고 기록할 키. 순수 데이터(안드로이드 의존 없음). */
data class Nudge(
    val type: NudgeType,
    val title: String,
    val text: String,
    /** 탭 → 이 마트가 선택된 홈. */
    val storeId: Long? = null,
    /** 탭 → 추가 시트 바로 열기(첫 목록 유도). */
    val openAdd: Boolean = false,
    /** 탭 → 요리별 재료 탭에서 이 요리 열기(시즌). */
    val recipeName: String? = null,
    /** SEASON: SeasonCalendar 키. */
    val seasonKey: String? = null,
)

/** 판단에 필요한 그 순간의 스냅샷. 리시버가 DB·DataStore에서 모아서 넘긴다. */
data class NudgeSnapshot(
    val stores: List<Store>,
    /** 완료 포함 전체 항목(첫 목록 판단에 완료분도 필요). */
    val items: List<Item>,
    val nowMillis: Long,
    /** PackageManager.firstInstallTime. */
    val firstInstallMillis: Long,
    val prefs: NudgePrefs,
    /** 요리 이름 → 재료(main) 수. 시즌 알림 제목의 "N가지"에 쓴다(없으면 숫자 생략). */
    val recipeMainCounts: Map<String, Int> = emptyMap(),
    val zone: ZoneId = ZoneId.systemDefault(),
)

/**
 * "지금 이 슬롯에 보낼 알림 1개"를 고른다. 규칙(2026-11-22 확정):
 *  - 하루 최대 1건. 예외: 시즌 알림은 그날 다른 알림이 나갔어도 1건 더 (사용자 합의 "날짜형 있는 날만 2건").
 *  - 아침: 시즌 > 첫 목록 > 남은 목록.  저녁: 배송 마감만.
 *  - 남은 목록: 생성 후 5일 지난 미완료 항목이 있는 마트, 마트별 5일에 1번.
 *  - 배송 마감: 마트 이름이 컬리/쿠팡으로 매핑되고 미완료 항목 ≥1, 마트별 3일에 1번.
 *  - 첫 목록: 설치 20시간 후, 항목(완료 포함)이 0개, 평생 1번.
 */
object NudgeEngine {

    const val STALE_AFTER_DAYS = 5
    const val STALE_COOLDOWN_DAYS = 5
    const val DELIVERY_COOLDOWN_DAYS = 3
    const val FIRST_LIST_AFTER_HOURS = 20
    private val DELIVERY_APPS = setOf("컬리", "쿠팡")

    fun decide(slot: NudgeSlot, s: NudgeSnapshot): Nudge? {
        val p = s.prefs
        if (!p.enabled) return null
        val today = epochDay(s.nowMillis, s.zone)
        val sentToday = p.lastSentDay == today

        return when (slot) {
            NudgeSlot.MORNING -> {
                // 시즌은 하루 상한의 예외(단, 오늘 이미 시즌을 보냈으면 끝).
                season(s, today)?.takeIf { !(sentToday && p.lastSentWasSeason) }
                    ?: if (sentToday) null else (firstList(s) ?: stale(s, today))
            }
            // 아침에 시즌 알림이 나간 날은 저녁 배송 알림 1건을 더 허용(합의: 날짜형 있는 날만 2건).
            NudgeSlot.EVENING -> if (sentToday && !p.lastSentWasSeason) null else delivery(s, today)
        }
    }

    private fun season(s: NudgeSnapshot, today: Long): Nudge? {
        if (!s.prefs.seasonEnabled) return null
        val date = LocalDate.ofEpochDay(today)
        val e = SeasonCalendar.due(date, s.prefs.seasonSent) ?: return null
        // "설 차례상 재료 19가지, 빠진 거 없는지 확인해요" — 숫자는 요리표의 실제 재료 수(사용자 선택 2026-11-22).
        val n = s.recipeMainCounts[e.recipeName] ?: 0
        val title = if (n > 0) "${e.label} ${e.noun} ${n}가지, 빠진 거 없는지 확인해요" else "${e.label} ${e.noun}, 빠진 거 없는지 확인해요"
        val dday = java.time.temporal.ChronoUnit.DAYS.between(date, e.date)
        val text = if (e.countdown && dday > 0) "${e.label.substringBefore(' ')}까지 ${dday}일 · 눌러서 마트 목록에 담기" else "눌러서 마트 목록에 담기"
        return Nudge(NudgeType.SEASON, title, text, recipeName = e.recipeName, seasonKey = e.key)
    }

    private fun firstList(s: NudgeSnapshot): Nudge? {
        if (s.prefs.firstListSent) return null
        if (s.items.isNotEmpty()) return null
        val age = s.nowMillis - s.firstInstallMillis
        if (age < TimeUnit.HOURS.toMillis(FIRST_LIST_AFTER_HOURS.toLong())) return null
        return Nudge(
            NudgeType.FIRST_LIST,
            title = "오늘 뭐 사야 하죠? 지금 적어 두세요",
            text = "이름만 적으면 끝, 10초",
            openAdd = true,
        )
    }

    private fun stale(s: NudgeSnapshot, today: Long): Nudge? {
        if (!s.prefs.staleEnabled) return null
        val cutoff = s.nowMillis - TimeUnit.DAYS.toMillis(STALE_AFTER_DAYS.toLong())
        val active = s.items.filter { !it.isCompleted }
        val candidates = s.stores
            .filter { !it.isArchived }
            .filter { st -> (s.prefs.staleLastSent[st.id] ?: Long.MIN_VALUE) <= today - STALE_COOLDOWN_DAYS }
            .mapNotNull { st ->
                val mine = active.filter { it.storeId == st.id }
                val stale = mine.filter { it.createdAt <= cutoff }
                if (stale.isEmpty()) null else Triple(st, mine, stale)
            }
        val (store, mine, stale) = candidates.maxByOrNull { it.third.size } ?: return null
        // 항목 이름이 제목 맨 앞 — "목록 N개가 기다려요" 식 주어는 어색하다는 사용자 지적(2026-11-22).
        // 온라인 마트도 있으니 "갈 때" 같은 말은 쓰지 않는다.
        val oldest = stale.minByOrNull { it.createdAt }!!
        val days = ((s.nowMillis - oldest.createdAt) / TimeUnit.DAYS.toMillis(1)).toInt()
        return Nudge(
            NudgeType.STALE_LIST,
            title = "${oldest.name}, 아직 안 사셨죠?",
            text = "${store.name}에서 살 것 ${mine.size}개 · ${days}일째 그대로예요",
            storeId = store.id,
        )
    }

    private fun delivery(s: NudgeSnapshot, today: Long): Nudge? {
        if (!s.prefs.deliveryEnabled) return null
        val active = s.items.filter { !it.isCompleted }
        val candidates = s.stores
            .filter { !it.isArchived && it.shopAppEnabled }
            .filter { st -> StoreSearch.appFor(st.name)?.label in DELIVERY_APPS }
            .filter { st -> (s.prefs.deliveryLastSent[st.id] ?: Long.MIN_VALUE) <= today - DELIVERY_COOLDOWN_DAYS }
            .map { st -> st to active.filter { it.storeId == st.id } }
            .filter { it.second.isNotEmpty() }
        val (store, mine) = candidates.maxByOrNull { it.second.size } ?: return null
        // "오늘 쿠팡 주문 마감 전이에요" (사용자 선택 2026-11-22). 마감 시각은 지역·배송권마다 달라 숫자를 안 쓴다.
        val first = mine.first().name
        val rest = mine.size - 1
        return Nudge(
            NudgeType.DELIVERY,
            title = "오늘 ${store.name} 주문 마감 전이에요",
            text = if (rest > 0) "$first 외 ${rest}개 담아 두셨어요" else "$first 담아 두셨어요",
            storeId = store.id,
        )
    }


    fun epochDay(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().toEpochDay()
}
