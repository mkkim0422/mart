package com.rldjrgo.grocerynote.ui.components

import androidx.compose.ui.graphics.Color
import com.rldjrgo.grocerynote.ui.theme.MartPalette

/**
 * One-tap marts (2026-11-13, 1.5.1): the "추천" chips in 마트 추가 and the first-run
 * "자주 가는 곳을 골라 주세요" sheet. Tapping sets name + color + emoji together.
 * 쿠팡/다이소 aren't listed — they're the seed marts; anything the user already has is
 * hidden by the callers. 메모 / 준비물 are deliberately non-mart lists so people find out
 * the app (and its widget) works for any short list — kept to two so it still reads as
 * a shopping app.
 */
data class MartPreset(val name: String, val color: Color, val emoji: String) {
    val iconKey: String get() = "emoji:$emoji"
}

val MART_PRESETS: List<MartPreset> = listOf(
    MartPreset("이마트", MartPalette.Yellow, "🛒"),
    MartPreset("홈플러스", MartPalette.Red, "🛒"),
    MartPreset("롯데마트", MartPalette.Orange, "🛒"),
    MartPreset("코스트코", MartPalette.Blue, "📦"),
    MartPreset("마켓컬리", MartPalette.Purple, "🥬"),
    MartPreset("올리브영", MartPalette.Green, "🧴"),
    MartPreset("편의점", MartPalette.Orange, "🏪"),
    MartPreset("시장", MartPalette.Green, "🧺"),
    MartPreset("약국", MartPalette.Pink, "💊"),
    MartPreset("메모", MartPalette.Gray, "📝"),
    MartPreset("준비물", MartPalette.Pink, "✏️"),
)

/** Presets the user doesn't have yet (name match, ignoring spaces). */
fun presetsExcluding(existingNames: Collection<String>): List<MartPreset> {
    val have = existingNames.map { it.replace(" ", "") }.toSet()
    return MART_PRESETS.filter { it.name.replace(" ", "") !in have }
}
