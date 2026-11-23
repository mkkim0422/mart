package com.rldjrgo.grocerynote.notify

import java.time.LocalDate
import java.time.MonthDay

/**
 * 명절·시즌 알림 날짜표 (B-11·B-12). 서버가 없어 앱에 박아 둔다.
 *
 * 설·추석(음력 1/1, 8/15)의 양력 날짜는 2026-11-22에 파이썬 `korean-lunar-calendar`(KASI 자료 기반)로 계산해 대조함.
 *  - **2028 설은 1/27(KST)** — 처음에 timeanddate 요약(1/26)을 그대로 넣었다가 정밀검사에서 잡음. 중국 춘절(1/26)과 하루 다름.
 *  - 2027 설은 **2/7(KST)** — 중국 춘절(2/6)보다 하루 늦다(삭 시각이 KST로 자정을 넘김). Wikipedia
 *    Chinese New Year 표와 다르니 그쪽을 베끼지 말 것.
 *  - 2030 설은 timeanddate 요약이 2/4만 보여 줘 불명확 → 천문 삭(2/3 KST 01:07)으로 2/3 채택.
 *    D-7 알림이라 하루 차이는 영향 없음.
 * 표가 끝나는 2031년부터는 설·추석 알림이 그냥 안 나간다(틀린 날 보내는 것보다 낫다). 연 1회 표 연장 필요.
 *
 * 고정일 시즌(김장 11/15, 캠핑 5/1, 봄소풍 4/20, 가을소풍 10/5)은 **제품 가정**이지 통계 근거가 없다.
 * recipeName 은 assets/recipes.txt 의 요리 이름과 정확히 같아야 한다(알림을 누르면 그 요리가 열림).
 */
data class SeasonEvent(
    /** 중복 발송 방지 키. 예: "seollal-2027", "kimjang-2026". */
    val key: String,
    /** 행사 당일. 알림은 [leadDays] 전부터 3일 창 안에서 한 번. */
    val date: LocalDate,
    val leadDays: Int,
    val recipeName: String,
    /** 제목 앞머리: "설 차례상", "추석 차례상", "김장", "캠핑 준비물", "소풍 도시락". */
    val label: String,
    /** 설·추석처럼 "D-N일"을 본문에 넣을 행사면 true. */
    val countdown: Boolean,
    /** 제목의 "재료 N가지" 자리 낱말. 캠핑은 재료가 아니라 "장볼 것". */
    val noun: String = "재료",
)

object SeasonCalendar {

    private val seollal = mapOf(
        2027 to MonthDay.of(2, 7),
        2028 to MonthDay.of(1, 27),
        2029 to MonthDay.of(2, 13),
        2030 to MonthDay.of(2, 3),
    )
    private val chuseok = mapOf(
        2027 to MonthDay.of(9, 15),
        2028 to MonthDay.of(10, 3),
        2029 to MonthDay.of(9, 22),
        2030 to MonthDay.of(9, 12),
    )

    /** 알림이 나갈 수 있는 창(일). D-7 당일을 놓쳐도(폰 꺼짐 등) 이틀 안엔 보낸다. */
    const val WINDOW_DAYS = 3

    fun eventsFor(year: Int): List<SeasonEvent> = buildList {
        seollal[year]?.let {
            add(
                SeasonEvent(key = "seollal-$year", date = it.atYear(year), leadDays = 7, recipeName = "차례상", label = "설 차례상", countdown = true),
            )
        }
        chuseok[year]?.let {
            add(
                SeasonEvent(key = "chuseok-$year", date = it.atYear(year), leadDays = 7, recipeName = "차례상", label = "추석 차례상", countdown = true),
            )
        }
        add(
            SeasonEvent(key = "kimjang-$year", date = LocalDate.of(year, 11, 15), leadDays = 7, recipeName = "김장", label = "김장", countdown = false),
        )
        add(
            SeasonEvent(key = "camping-$year", date = LocalDate.of(year, 5, 1), leadDays = 7, recipeName = "캠핑 1박 2일", label = "캠핑", countdown = false, noun = "장볼 것"),
        )
        add(
            SeasonEvent(key = "picnic-spring-$year", date = LocalDate.of(year, 4, 20), leadDays = 5, recipeName = "소풍 도시락", label = "봄 소풍 도시락", countdown = false),
        )
        add(
            SeasonEvent(key = "picnic-fall-$year", date = LocalDate.of(year, 10, 5), leadDays = 5, recipeName = "소풍 도시락", label = "가을 소풍 도시락", countdown = false),
        )
    }

    /**
     * 오늘 보낼 수 있는 시즌 행사. 창 = [date - leadDays, date - leadDays + WINDOW_DAYS).
     * 여러 개 겹치면 행사일이 가까운 것 우선. [alreadySent] 에 있는 키는 제외.
     */
    fun due(today: LocalDate, alreadySent: Set<String>): SeasonEvent? =
        (eventsFor(today.year) + eventsFor(today.year + 1))
            .filter { it.key !in alreadySent }
            .filter { e ->
                val start = e.date.minusDays(e.leadDays.toLong())
                !today.isBefore(start) && today.isBefore(start.plusDays(WINDOW_DAYS.toLong()))
            }
            .minByOrNull { it.date }
}
