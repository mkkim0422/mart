package com.rldjrgo.grocerynote.ui.screens.home.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.ui.theme.Corners
import com.rldjrgo.grocerynote.util.formatReminderShort
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Completion animation V2 (in-app only). Must stay in sync with CLAUDE.md §11.
private const val STRIKE_DURATION_MS = 1000   // left→right strikethrough sweep + total length
private const val CHECK_FADE_IN_MS = 250      // green ✓ fade + scale-in

@Composable
fun ItemRow(
    item: Item,
    storeColor: Color,
    highlighted: Boolean,
    onCompleteAnimDone: () -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    onSetReminder: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var menuOpen by remember { mutableStateOf(false) }
    // Latched the moment "완료" is tapped; survives recomposition so the
    // animation always plays to 1000ms even under fast consecutive completes.
    var triggered by remember(item.id) { mutableStateOf(item.isCompleted) }

    // strike: 0→1 sweeps the strikethrough left→right. check: 0→1 fades+scales the ✓.
    val strike = remember(item.id) { Animatable(if (item.isCompleted) 1f else 0f) }
    val check = remember(item.id) { Animatable(if (item.isCompleted) 1f else 0f) }

    // Developer-options "Animation off" (ANIMATOR_DURATION_SCALE = 0) → complete instantly.
    val animatorsOff = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }

    val complete: () -> Unit = {
        if (!triggered) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            triggered = true
        }
    }

    LaunchedEffect(triggered) {
        if (!triggered || item.isCompleted) return@LaunchedEffect
        if (animatorsOff) {
            strike.snapTo(1f)
            check.snapTo(1f)
            onCompleteAnimDone()
            return@LaunchedEffect
        }
        launch { strike.animateTo(1f, tween(STRIKE_DURATION_MS, easing = FastOutSlowInEasing)) }
        launch { check.animateTo(1f, tween(CHECK_FADE_IN_MS)) }
        delay(STRIKE_DURATION_MS.toLong())
        // Row is dropped by the active-items Flow + the [되돌리기] Snackbar shows.
        onCompleteAnimDone()
    }

    val lineColor = colors.textPrimary.copy(alpha = 0.7f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                if (highlighted) storeColor.copy(alpha = if (colors.isDark) 0.25f else 0.15f)
                else colors.bgPrimary,
            )
            .semantics { contentDescription = if (triggered) "완료됨" else item.name }
            .padding(horizontal = 20.dp),
    ) {
        // Actual rendered text width (the Text node fills the weight, so
        // size.width ≠ glyph width). The strikethrough must only span the
        // text itself, not the empty space up to the "완료" button.
        var textRightPx by remember(item.id) { mutableStateOf(0f) }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = typo.body,
                color = colors.textPrimary,
                onTextLayout = { result ->
                    textRightPx = (0 until result.lineCount)
                        .maxOfOrNull { result.getLineRight(it) }
                        ?: result.size.width.toFloat()
                },
                modifier = Modifier
                    .drawWithContent {
                        drawContent()
                        val p = strike.value
                        if (p > 0f && textRightPx > 0f) {
                            val y = size.height / 2f
                            val end = textRightPx.coerceAtMost(size.width) * p
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, y),
                                end = Offset(end, y),
                                strokeWidth = 2.dp.toPx(),
                            )
                        }
                    },
            )
            // Second line: the reminder chip (only when a reminder is set). Tap it to
            // re-open the picker. Hidden while the completion animation runs.
            val reminderAt = item.reminderAt
            if (reminderAt != null && !triggered) {
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onSetReminder),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = storeColor,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = formatReminderShort(reminderAt),
                        style = typo.caption.copy(fontWeight = FontWeight.Medium),
                        color = storeColor,
                    )
                }
            }
        }
        // Reminder bell — one tap opens the date/time picker. Outline = none set,
        // filled (mart color) = a reminder is armed. Disabled during the complete anim.
        Spacer(Modifier.width(6.dp))
        val hasReminder = item.reminderAt != null
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(enabled = !triggered, onClick = onSetReminder),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (hasReminder) Icons.Filled.Notifications
                else Icons.Outlined.Notifications,
                contentDescription = if (hasReminder) "알림 변경" else "알림 설정",
                tint = if (hasReminder) storeColor else colors.textTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(6.dp))
        // "완료" affordance. Before tap: bordered button. After tap: a green
        // circle + white ✓ fades/scales in (0→250ms) and holds until 1000ms.
        Box(
            modifier = Modifier.size(width = 56.dp, height = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (triggered) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer {
                            alpha = check.value
                            val s = 0.6f + 0.4f * check.value
                            scaleX = s
                            scaleY = s
                        }
                        .background(colors.success, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "완료됨",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.5.dp, colors.divider, Corners.small)
                        .clickable(onClick = complete),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "완료",
                        style = typo.caption.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
                        color = Color(0xFF6B6B6B),
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Box {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "더보기",
                tint = colors.textTertiary,
                modifier = Modifier
                    .size(20.dp)
                    // Disabled while the completion animation is running.
                    .clickable(enabled = !triggered) { menuOpen = true },
            )
            DropdownMenu(
                expanded = menuOpen && !triggered,
                onDismissRequest = { menuOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text("이름 수정", style = typo.body, color = colors.textPrimary) },
                    onClick = { menuOpen = false; onRename() },
                )
                DropdownMenuItem(
                    text = { Text("다른 마트로 이동", style = typo.body, color = colors.textPrimary) },
                    onClick = { menuOpen = false; onMove() },
                )
                DropdownMenuItem(
                    text = { Text("삭제", style = typo.body, color = colors.danger) },
                    onClick = { menuOpen = false; onDelete() },
                )
            }
        }
    }
}
