package com.rldjrgo.grocerynote.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

/**
 * Light + Dark color tokens for the widget.
 *
 * We no longer use Glance's day/night [ColorProvider] (which defers the light/dark
 * decision to the LAUNCHER). Some launchers — notably One UI — render the widget in
 * dark even when the system is in light mode ("폰은 라이트인데 위젯만 다크"). Instead we
 * resolve `isDark` ONCE in [BaseGroceryWidget.provideGlance] (from the app's
 * DarkModePref, Auto→system night) and expose it via [LocalWidgetDark]; every color
 * below is baked as a STATIC provider in-process, so the launcher can't override the
 * theme and the widget always matches the app.
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

/**
 * Whether the widget renders in dark theme. Provided by [BaseGroceryWidget]'s
 * `CompositionLocalProvider`; defaults to light so a stray read never crashes.
 */
val LocalWidgetDark = staticCompositionLocalOf { false }

private fun sp(c: Color): ColorProvider = androidx.glance.unit.ColorProvider(c)

@Composable
private fun pick(day: Color, night: Color): ColorProvider =
    sp(if (LocalWidgetDark.current) night else day)

@Composable fun bgProvider(): ColorProvider = pick(WidgetColors.LightBg, WidgetColors.DarkBg)
@Composable fun cardBgProvider(): ColorProvider = pick(WidgetColors.LightCardBg, WidgetColors.DarkCardBg)
@Composable fun dividerProvider(): ColorProvider = pick(WidgetColors.LightDivider, WidgetColors.DarkDivider)
@Composable fun textPrimaryProvider(): ColorProvider = pick(WidgetColors.LightTextPrimary, WidgetColors.DarkTextPrimary)
@Composable fun textSecondaryProvider(): ColorProvider = pick(WidgetColors.LightTextSecondary, WidgetColors.DarkTextSecondary)
@Composable fun textTertiaryProvider(): ColorProvider = pick(WidgetColors.LightTextTertiary, WidgetColors.DarkTextTertiary)
@Composable fun checkboxBorderProvider(): ColorProvider = pick(WidgetColors.LightCheckboxBorder, WidgetColors.DarkCheckboxBorder)
@Composable fun brandProvider(): ColorProvider = pick(WidgetColors.LightBrand, WidgetColors.DarkBrand)
fun staticProvider(c: Color): ColorProvider = androidx.glance.unit.ColorProvider(c)

/**
 * Soft tint of a mart color for the widget header icon-box / count pill.
 * Day alpha 0.15, night 0.25 (slightly stronger so it reads on dark card bg).
 */
@Composable
fun martSoftProvider(c: Color): ColorProvider =
    sp(c.copy(alpha = if (LocalWidgetDark.current) 0.25f else 0.15f))
