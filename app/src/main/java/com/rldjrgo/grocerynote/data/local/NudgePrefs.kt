package com.rldjrgo.grocerynote.data.local

/**
 * 리텐션 알림 설정 + 발송 이력 (DataStore). 기본값 = 전부 켬, 아침 9시 · 저녁 18시 (2026-11-22 확정).
 */
data class NudgePrefs(
    val enabled: Boolean = true,
    val staleEnabled: Boolean = true,
    val deliveryEnabled: Boolean = true,
    val seasonEnabled: Boolean = true,
    /** 0~23. */
    val morningHour: Int = DEFAULT_MORNING_HOUR,
    val eveningHour: Int = DEFAULT_EVENING_HOUR,
    /** 마지막으로 어떤 알림이든 보낸 날(epoch day, 로컬). 하루 1건 상한용. */
    val lastSentDay: Long = -1L,
    /** 그날 보낸 알림이 시즌형이었는지 — 시즌 알림은 다른 알림과 같은 날 1건 더 허용. */
    val lastSentWasSeason: Boolean = false,
    val firstListSent: Boolean = false,
    /** storeId → 마지막 발송 epoch day. */
    val staleLastSent: Map<Long, Long> = emptyMap(),
    val deliveryLastSent: Map<Long, Long> = emptyMap(),
    /** SeasonCalendar 키. */
    val seasonSent: Set<String> = emptySet(),
    /** 온보딩에서 알림 권한을 한 번이라도 물었는지(거부 후 다시 안 묻기). */
    val permissionAsked: Boolean = false,
) {
    companion object {
        const val DEFAULT_MORNING_HOUR = 9
        const val DEFAULT_EVENING_HOUR = 18
    }
}
