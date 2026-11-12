package com.rldjrgo.grocerynote.widget

import androidx.compose.ui.unit.dp

/**
 * Widget size rules (2 widgets since 2026-11-12: count + list).
 * Both use SizeMode.Exact / Single, so there are no Responsive breakpoints any more.
 */
object WidgetSizes {
    /** Count widget shorter than this = the 2x1 strip: 2 rows, no "+". */
    val CountCompactBelow = 100.dp
}
