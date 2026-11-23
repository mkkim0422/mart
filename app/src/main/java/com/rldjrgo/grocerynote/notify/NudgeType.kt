package com.rldjrgo.grocerynote.notify

/**
 * 리텐션용 로컬 알림("넛지") 종류 — 1.7.0 1차 (2026-11-22 사용자 확정, 메모리 martnote-push-notifications-plan).
 *
 * 종류마다 알림 채널을 따로 둬서 폰 설정에서 종류별로 끌 수 있고, 앱 설정에도 종류별 토글이 있다.
 * 서버/FCM 없음 — 전부 폰 안에서 [NudgeScheduler]가 하루 2번(아침·저녁) 깨우고 [NudgeEngine]이
 * "오늘 보낼 것 1개"만 고른다 (CLAUDE.md "푸시 남발 금지" = 하루 최대 1건).
 */
enum class NudgeType(
    val channelId: String,
    val channelName: String,
    val channelDesc: String,
    /** Firebase Analytics 파라미터 값. */
    val key: String,
) {
    /** D-15: 설치 후 하루가 지나도 항목이 하나도 없을 때, 딱 한 번. */
    FIRST_LIST("nudge_start", "시작 안내", "처음 목록을 적을 때까지 한 번만", "first_list"),

    /** A-3: 5일 넘게 남아 있는 미완료 항목이 있는 마트. */
    STALE_LIST("nudge_list", "남은 목록", "오래 남아 있는 장보기 목록", "stale_list"),

    /** A-6: 컬리·쿠팡 탭에 항목이 있으면 저녁에 "오늘 주문하면 내일 새벽". */
    DELIVERY("nudge_delivery", "배송 마감", "컬리·쿠팡 목록이 있으면 저녁에", "delivery"),

    /** B-11·12: 설·추석 D-7, 김장·캠핑·소풍 시즌 — 요리별 재료의 체크리스트로 연결. */
    SEASON("nudge_season", "명절·시즌 장보기", "설·추석·김장 일주일 전", "season"),
}

/** 하루 두 번 깨우는 슬롯. 아침 = 목록·시즌·첫 목록, 저녁 = 배송 마감. */
enum class NudgeSlot { MORNING, EVENING }
