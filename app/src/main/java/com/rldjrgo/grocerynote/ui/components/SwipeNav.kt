package com.rldjrgo.grocerynote.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Edge-bounce state for [swipeBetweenTabs]: when the user drags past the first
 * or last tab (nowhere to go), the content follows the finger with rubber-band
 * resistance and springs back on release — "this is the end" feedback without
 * opening anything. Create with [rememberTabSwipeBounce] and apply [offsetPx]
 * to the content that should nudge (e.g. `Modifier.offset { IntOffset(...) }`).
 */
@Stable
class TabSwipeBounce internal constructor(
    internal val anim: Animatable<Float, AnimationVector1D>,
) {
    val offsetPx: Float get() = anim.value
}

@Composable
fun rememberTabSwipeBounce(): TabSwipeBounce =
    remember { TabSwipeBounce(Animatable(0f)) }

/**
 * Horizontal content swipe: drag left → [onNext], drag right → [onPrev].
 * Used to move between mart tabs (active screen) / filters (completed screen).
 *
 * Uses [detectHorizontalDragGestures], so vertical list scrolling is unaffected
 * and a child that consumes the horizontal drag (e.g. a per-row SwipeToDismiss)
 * takes precedence over this.
 *
 * [hasNext]/[hasPrev] + [bounce]: when there is no tab in the drag direction,
 * the bounce offset rubber-bands instead (see [TabSwipeBounce]).
 *
 * Composable on purpose: the callbacks/flags are read via [rememberUpdatedState]
 * so the pointerInput never restarts on recomposition (a restart mid-drag used
 * to cancel the gesture).
 */
@Composable
fun Modifier.swipeBetweenTabs(
    onNext: () -> Unit,
    onPrev: () -> Unit,
    hasNext: Boolean = true,
    hasPrev: Boolean = true,
    bounce: TabSwipeBounce? = null,
): Modifier {
    val curOnNext = rememberUpdatedState(onNext)
    val curOnPrev = rememberUpdatedState(onPrev)
    val curHasNext = rememberUpdatedState(hasNext)
    val curHasPrev = rememberUpdatedState(hasPrev)
    val scope = rememberCoroutineScope()
    return pointerInput(Unit) {
        val threshold = 72.dp.toPx()
        val maxBounce = 40.dp.toPx()
        var total = 0f
        detectHorizontalDragGestures(
            onDragStart = { total = 0f },
            onHorizontalDrag = { change, dragAmount ->
                total += dragAmount
                change.consume()
                if (bounce != null) {
                    // Dragging into a wall (no tab in that direction) → the
                    // content follows the finger, resisted and capped.
                    val atWall = (total < 0f && !curHasNext.value) ||
                        (total > 0f && !curHasPrev.value)
                    val target =
                        if (atWall) (total * 0.25f).coerceIn(-maxBounce, maxBounce)
                        else 0f
                    scope.launch { bounce.anim.snapTo(target) }
                }
            },
            onDragEnd = {
                if (bounce != null) scope.launch {
                    bounce.anim.animateTo(
                        0f,
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    )
                }
                when {
                    total <= -threshold -> curOnNext.value()
                    total >= threshold -> curOnPrev.value()
                }
            },
            onDragCancel = {
                total = 0f
                if (bounce != null) scope.launch { bounce.anim.animateTo(0f, spring()) }
            },
        )
    }
}
