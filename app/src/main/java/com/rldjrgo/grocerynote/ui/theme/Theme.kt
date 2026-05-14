package com.rldjrgo.grocerynote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * App-wide Toss-style theme. Material3 ColorScheme is provided so that Material components
 * (Surface, etc.) get sane defaults, but our own [LocalAppColors] is the source of truth for
 * brand and semantic colors.
 *
 * Material You dynamic color is intentionally NOT used (brand consistency).
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val typography = DefaultAppTypography
    val shapes = AppShapes()

    val materialColorScheme = if (darkTheme) {
        darkColorScheme(
            primary = appColors.brandPrimary,
            onPrimary = appColors.bgPrimary,
            background = appColors.bgPrimary,
            onBackground = appColors.textPrimary,
            surface = appColors.bgPrimary,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.bgSecondary,
            onSurfaceVariant = appColors.textSecondary,
            outline = appColors.divider,
            error = appColors.danger,
            onError = appColors.bgPrimary,
        )
    } else {
        lightColorScheme(
            primary = appColors.brandPrimary,
            onPrimary = appColors.bgPrimary,
            background = appColors.bgPrimary,
            onBackground = appColors.textPrimary,
            surface = appColors.bgPrimary,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.bgSecondary,
            onSurfaceVariant = appColors.textSecondary,
            outline = appColors.divider,
            error = appColors.danger,
            onError = appColors.bgPrimary,
        )
    }

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalAppTypography provides typography,
        LocalAppShapes provides shapes,
        LocalContentColor provides appColors.textPrimary,
        LocalTextStyle provides typography.body.copy(color = appColors.textPrimary),
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content,
        )
    }
}

/** Convenience accessors. */
object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
    val typography: AppTypography
        @Composable @ReadOnlyComposable get() = LocalAppTypography.current
    val shapes: AppShapes
        @Composable @ReadOnlyComposable get() = LocalAppShapes.current
}
