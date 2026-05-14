package com.rldjrgo.grocerynote.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * App font family. Defaults to system sans (Roboto/Noto on Korean devices).
 *
 * To switch to Pretendard:
 *   1. Drop Pretendard-Regular/Medium/SemiBold/Bold .ttf into `app/src/main/res/font/`
 *      with names: pretendard_regular.ttf, pretendard_medium.ttf,
 *                  pretendard_semibold.ttf, pretendard_bold.ttf
 *   2. Replace [AppFontFamily] below with:
 *        FontFamily(
 *          Font(R.font.pretendard_regular, FontWeight.Normal),
 *          Font(R.font.pretendard_medium, FontWeight.Medium),
 *          Font(R.font.pretendard_semibold, FontWeight.SemiBold),
 *          Font(R.font.pretendard_bold, FontWeight.Bold),
 *        )
 */
val AppFontFamily: FontFamily = FontFamily.Default

@Immutable
data class AppTypography(
    val displayL: TextStyle,
    val headingL: TextStyle,
    val headingM: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val bodyS: TextStyle,
    val caption: TextStyle,
    val micro: TextStyle,
)

private fun toss(sizeSp: Int, weight: FontWeight): TextStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = weight,
    fontSize = sizeSp.sp,
    letterSpacing = (-0.02).em,
    lineHeight = (sizeSp * 1.5f).sp,
)

internal val DefaultAppTypography = AppTypography(
    displayL = toss(24, FontWeight.Bold),
    headingL = toss(20, FontWeight.Bold),
    headingM = toss(17, FontWeight.SemiBold),
    title = toss(16, FontWeight.Medium),
    body = toss(15, FontWeight.Normal),
    bodyS = toss(13, FontWeight.Normal),
    caption = toss(12, FontWeight.Normal),
    micro = toss(11, FontWeight.Medium),
)

val LocalAppTypography = compositionLocalOf<AppTypography> { error("AppTypography not provided") }
