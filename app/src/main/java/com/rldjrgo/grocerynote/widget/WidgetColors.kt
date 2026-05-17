package com.rldjrgo.grocerynote.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

/**
 * Light + Dark color tokens for the widget.
 *
 * Glance 1.1.1 puts the public factories in different packages:
 * - Interface: `androidx.glance.unit.ColorProvider`
 * - Static (single color): `androidx.glance.unit.ColorProvider(color)` — top-level fun
 * - Day/Night: `androidx.glance.color.ColorProvider(day, night)` — top-level fun
 *   (lives in DayNightColorProvidersKt). Called via FQN to avoid name collision
 *   with the interface above.
 */
object WidgetColors {
    val LightBg = Color(0xFFFFFFFF)
    val LightCardBg = Color(0xFFFFFFFF)
    val LightDivider = Color(0xFFF0EEE9)
    val LightTextPrimary = Color(0xFF191F28)
    val LightTextSecondary = Color(0xFF4E5968)
    val LightTextTertiary = Color(0xFF8B95A1)
    val LightCheckboxBorder = Color(0xFFD5D2CB)
    val LightBrand = Color(0xFF3182F6)

    val DarkBg = Color(0xFF262626)
    val DarkCardBg = Color(0xFF262626)
    val DarkDivider = Color(0xFF333333)
    val DarkTextPrimary = Color(0xFFF2F4F6)
    val DarkTextSecondary = Color(0xFFA8B0BA)
    val DarkTextTertiary = Color(0xFF6B7684)
    val DarkCheckboxBorder = Color(0xFF555555)
    val DarkBrand = Color(0xFF4592FF)
}

private fun dn(day: Color, night: Color): ColorProvider =
    androidx.glance.color.ColorProvider(day = day, night = night)

fun bgProvider(): ColorProvider = dn(WidgetColors.LightBg, WidgetColors.DarkBg)
fun cardBgProvider(): ColorProvider = dn(WidgetColors.LightCardBg, WidgetColors.DarkCardBg)
fun dividerProvider(): ColorProvider = dn(WidgetColors.LightDivider, WidgetColors.DarkDivider)
fun textPrimaryProvider(): ColorProvider = dn(WidgetColors.LightTextPrimary, WidgetColors.DarkTextPrimary)
fun textSecondaryProvider(): ColorProvider = dn(WidgetColors.LightTextSecondary, WidgetColors.DarkTextSecondary)
fun textTertiaryProvider(): ColorProvider = dn(WidgetColors.LightTextTertiary, WidgetColors.DarkTextTertiary)
fun checkboxBorderProvider(): ColorProvider = dn(WidgetColors.LightCheckboxBorder, WidgetColors.DarkCheckboxBorder)
fun brandProvider(): ColorProvider = dn(WidgetColors.LightBrand, WidgetColors.DarkBrand)
fun staticProvider(c: Color): ColorProvider = androidx.glance.unit.ColorProvider(c)

/**
 * Soft tint of a mart color for the widget header icon-box / count pill.
 * Day alpha 0.15, night 0.22 (slightly stronger so it reads on dark card bg).
 */
fun martSoftProvider(c: Color): ColorProvider =
    dn(c.copy(alpha = 0.15f), c.copy(alpha = 0.25f))
