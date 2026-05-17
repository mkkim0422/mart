package com.rldjrgo.grocerynote.ui.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Horizontal content swipe: drag left → [onNext], drag right → [onPrev].
 * Used to move between mart tabs (active screen) / filters (completed screen).
 *
 * Uses [detectHorizontalDragGestures], so vertical list scrolling is unaffected
 * and a child that consumes the horizontal drag (e.g. a per-row SwipeToDismiss)
 * takes precedence over this.
 */
fun Modifier.swipeBetweenTabs(
    onNext: () -> Unit,
    onPrev: () -> Unit,
): Modifier = pointerInput(onNext, onPrev) {
    val threshold = 72.dp.toPx()
    var total = 0f
    detectHorizontalDragGestures(
        onDragStart = { total = 0f },
        onHorizontalDrag = { change, dragAmount ->
            total += dragAmount
            change.consume()
        },
        onDragEnd = {
            when {
                total <= -threshold -> onNext()
                total >= threshold -> onPrev()
            }
        },
        onDragCancel = { total = 0f },
    )
}
