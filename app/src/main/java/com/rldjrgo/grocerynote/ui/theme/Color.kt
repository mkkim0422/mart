package com.rldjrgo.grocerynote.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Toss Light palette ─────────────────────────────────────────────
object TossLight {
    val BgPrimary = Color(0xFFFAFAF7)   // warm off-white (design spec)
    val BgSecondary = Color(0xFFF9FAFB)
    val BgTertiary = Color(0xFFF2F4F6)
    val Divider = Color(0xFFF0EEE9)     // warm hairline (design spec)
    val TextPrimary = Color(0xFF191F28)
    val TextSecondary = Color(0xFF4E5968)
    val TextTertiary = Color(0xFFB0B0B0) // warmer tertiary (design spec)
    val TextDisabled = Color(0xFFC9CDD2)
    val BrandPrimary = Color(0xFF3182F6)
    val BrandPrimaryHover = Color(0xFF1B64DA)
    val BrandPrimarySoft = Color(0xFFE8F3FF)
    val Success = Color(0xFF2BA471)
    val Danger = Color(0xFFF04452)
    val Warning = Color(0xFFFFA000)
}

// ── Toss Dark palette ──────────────────────────────────────────────
object TossDark {
    val BgPrimary = Color(0xFF17171B)
    val BgSecondary = Color(0xFF1F1F23)
    val BgTertiary = Color(0xFF2B2D31)
    val Divider = Color(0xFF3A3D42)
    val TextPrimary = Color(0xFFF2F4F6)
    val TextSecondary = Color(0xFFA8B0BA)
    val TextTertiary = Color(0xFF6B7684)
    val TextDisabled = Color(0xFF4E5968)
    val BrandPrimary = Color(0xFF4592FF)
    val BrandPrimaryHover = Color(0xFF6BA8FF)
    val BrandPrimarySoft = Color(0xFF1A2D4D)
    val Success = Color(0xFF3FBE85)
    val Danger = Color(0xFFFF5C68)
    val Warning = Color(0xFFFFB73D)
}

// ── Mart palette (8) ───────────────────────────────────────────────
object MartPalette {
    val Blue = Color(0xFF3182F6)     // 쿠팡 추천
    val Yellow = Color(0xFFFFB800)   // 이마트 추천
    val Red = Color(0xFFF04452)      // 다이소 추천
    val Green = Color(0xFF2BA471)    // 홈플러스 추천
    val Purple = Color(0xFF8B5CF6)   // 마켓컬리 추천
    val Pink = Color(0xFFF564A9)     // 베이커리
    val Orange = Color(0xFFFF8A3D)   // 시장
    val Gray = Color(0xFF6B7684)     // 기타

    val all: List<Color> = listOf(Blue, Yellow, Red, Green, Purple, Pink, Orange, Gray)
}

/**
 * Soft tint of a mart color, used for badge/pill/icon-box backgrounds.
 * Dark mode uses a slightly stronger alpha so the tint stays visible on dark bg.
 * The text drawn on top keeps the original mart color (enough contrast either way).
 */
fun Color.soft(isDark: Boolean = false): Color = copy(alpha = if (isDark) 0.25f else 0.15f)

@Immutable
data class AppColors(
    val bgPrimary: Color,
    val bgSecondary: Color,
    val bgTertiary: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val brandPrimary: Color,
    val brandPrimaryHover: Color,
    val brandPrimarySoft: Color,
    val success: Color,
    val danger: Color,
    val warning: Color,
    val isDark: Boolean,
)

internal val LightAppColors = AppColors(
    bgPrimary = TossLight.BgPrimary,
    bgSecondary = TossLight.BgSecondary,
    bgTertiary = TossLight.BgTertiary,
    divider = TossLight.Divider,
    textPrimary = TossLight.TextPrimary,
    textSecondary = TossLight.TextSecondary,
    textTertiary = TossLight.TextTertiary,
    textDisabled = TossLight.TextDisabled,
    brandPrimary = TossLight.BrandPrimary,
    brandPrimaryHover = TossLight.BrandPrimaryHover,
    brandPrimarySoft = TossLight.BrandPrimarySoft,
    success = TossLight.Success,
    danger = TossLight.Danger,
    warning = TossLight.Warning,
    isDark = false,
)

internal val DarkAppColors = AppColors(
    bgPrimary = TossDark.BgPrimary,
    bgSecondary = TossDark.BgSecondary,
    bgTertiary = TossDark.BgTertiary,
    divider = TossDark.Divider,
    textPrimary = TossDark.TextPrimary,
    textSecondary = TossDark.TextSecondary,
    textTertiary = TossDark.TextTertiary,
    textDisabled = TossDark.TextDisabled,
    brandPrimary = TossDark.BrandPrimary,
    brandPrimaryHover = TossDark.BrandPrimaryHover,
    brandPrimarySoft = TossDark.BrandPrimarySoft,
    success = TossDark.Success,
    danger = TossDark.Danger,
    warning = TossDark.Warning,
    isDark = true,
)

val LocalAppColors = compositionLocalOf<AppColors> { error("AppColors not provided") }
