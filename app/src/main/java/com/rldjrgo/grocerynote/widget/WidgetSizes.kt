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

    /** The set handed to SizeMode.Responsive (order irrelevant; match is by fit). */
    val responsiveSet = setOf(TwoByOne, Small, Long, Medium, Large)
}
