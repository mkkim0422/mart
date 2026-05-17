package com.rldjrgo.grocerynote.ui.screens.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.ui.theme.Corners

private const val CHECK_FILL_MS = 150
private const val STRIKE_MS = 300
private const val WAIT_MS = 700
private const val FADE_MS = 300

@Composable
fun ItemRow(
    item: Item,
    storeColor: Color,
    highlighted: Boolean,
    onCompleteAnimDone: () -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val haptic = LocalHapticFeedback.current
    var menuOpen by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(item.isCompleted) }
    val alpha = remember { Animatable(1f) }
    val translateY = remember { Animatable(0f) }

    val textColor by animateColorAsState(
        targetValue = if (checked) colors.textTertiary else colors.textPrimary,
        animationSpec = tween(STRIKE_MS),
        label = "textColor",
    )

    // No delay/animation — tapping "완료" removes the item right away
    // (the list flow recomposes and drops the row instantly).
    val complete: () -> Unit = {
        if (!checked) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            checked = true
            onCompleteAnimDone()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                if (highlighted) storeColor.copy(alpha = if (colors.isDark) 0.25f else 0.15f)
                else colors.bgPrimary,
            )
            .alpha(alpha.value)
            .graphicsLayer { translationY = translateY.value }
            // Row tap does NOT complete — only the explicit "완료" button does.
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = item.name,
            style = typo.body.copy(
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
            ),
            color = textColor,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        // "완료" affordance — clearly a complete action, not a vague selection box.
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 32.dp)
                .background(
                    color = if (checked) storeColor else Color.Transparent,
                    shape = Corners.small,
                )
                .border(
                    width = 1.5.dp,
                    color = if (checked) storeColor else colors.divider,
                    shape = Corners.small,
                )
                .clickable(enabled = !checked, onClick = complete),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "완료",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            } else {
                Text(
                    text = "완료",
                    style = typo.caption.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
                    color = Color(0xFF6B6B6B),
                )
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
                    .clickable { menuOpen = true },
            )
            DropdownMenu(
                expanded = menuOpen,
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
