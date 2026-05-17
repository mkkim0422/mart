package com.rldjrgo.grocerynote.widget.common

/**
 * Widget layout decisions. The key rule (per spec): if the busiest mart utterly
 * dominates (≥4× the 2nd), give it the WHOLE widget instead of wasting half the
 * space on a near-empty mart.
 */
object WidgetLayoutCalculator {

    private const val DOMINANT_RATIO = 4

    /** True → render only the top mart full-width (it dwarfs the rest). */
    fun isDominant(topCount: Int, secondCount: Int): Boolean =
        secondCount <= 0 || topCount >= secondCount * DOMINANT_RATIO

    /** Soft per-mart item hint (the list is a scrollable LazyColumn, never hard-clipped). */
    fun mediumItemsPerStore(storeCount: Int): Int = if (storeCount <= 1) 8 else 4

    fun largeItemsPerStore(storeCount: Int): Int = when {
        storeCount <= 1 -> 12
        storeCount == 2 -> 6
        storeCount == 3 -> 4
        else -> 3
    }
}
