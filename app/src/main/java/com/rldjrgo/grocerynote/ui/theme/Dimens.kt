package com.rldjrgo.grocerynote.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Toss-style spacing scale: 4 / 8 / 12 / 16 / 20 / 24 / 32 / 40 / 48 dp */
object Spacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
    val xxxxl: Dp = 40.dp
    val huge: Dp = 48.dp
    val screenHorizontal: Dp = 20.dp
    val cardInner: Dp = 16.dp
    val componentGap: Dp = 12.dp
}

/** Corner radii. Toss tends slightly larger. */
object Corners {
    val small = RoundedCornerShape(8.dp)   // chip / badge
    val medium = RoundedCornerShape(12.dp) // input / button
    val large = RoundedCornerShape(16.dp)  // card / modal
    val xlarge = RoundedCornerShape(20.dp) // sheet / FAB-ish
    val full = RoundedCornerShape(50)
}

/** Component metric defaults. */
object ComponentSize {
    val buttonHeight: Dp = 52.dp
    val inputHeight: Dp = 52.dp
    val checkbox: Dp = 22.dp
    val tabBarHeight: Dp = 48.dp
    val fab: Dp = 56.dp
    val martColorDot: Dp = 8.dp
}

@Immutable
data class AppShapes(
    val small: RoundedCornerShape = Corners.small,
    val medium: RoundedCornerShape = Corners.medium,
    val large: RoundedCornerShape = Corners.large,
    val xlarge: RoundedCornerShape = Corners.xlarge,
)

val LocalAppShapes = compositionLocalOf<AppShapes> { AppShapes() }
