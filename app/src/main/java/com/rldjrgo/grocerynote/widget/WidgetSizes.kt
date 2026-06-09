package com.rldjrgo.grocerynote.widget

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * Breakpoints for [androidx.glance.appwidget.SizeMode.Responsive]. Every placed
 * widget adapts between these 5 layouts as the user resizes it on the home screen.
 * Glance picks the largest entry that fits, so these are exact match keys.
 */
object WidgetSizes {
    val TwoByOne = DpSize(110.dp, 40.dp)   // Mini   2x1 — one-line counts
    val Small = DpSize(110.dp, 110.dp)     // Small  2x2 — per-mart counts
    val Long = DpSize(110.dp, 250.dp)      // Long   2x4 — 1 mart + items
    val Medium = DpSize(250.dp, 110.dp)    // Medium 4x2 ★ default — 2 marts + items
    val Large = DpSize(250.dp, 250.dp)     // Large  4x4 — up to 4 marts + items
    // XLarge: 사용자가 위젯을 더 크게 키웠을 때 자동 활성화되는 가상 단계.
    // 별도 위젯 클래스/picker 항목은 없음 — 어댑티브 리사이즈로만 들어간다.
    // 320×320 은 Large(250) 보다 한 셀 가량 크고 xml maxResize(480) 보다 작아,
    // 사용자가 정사각형에 가깝게 키우는 도중 자연스럽게 점프한다.
    val XLarge = DpSize(320.dp, 320.dp)    // XLarge ≥5셀 — up to 6 marts + items

    /** The set handed to SizeMode.Responsive (order irrelevant; match is by fit). */
    val responsiveSet = setOf(TwoByOne, Small, Long, Medium, Large, XLarge)
}
