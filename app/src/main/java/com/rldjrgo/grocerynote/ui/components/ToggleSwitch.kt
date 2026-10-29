package com.rldjrgo.grocerynote.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.ui.theme.AppTheme

/** 44×26 pill toggle, 20dp knob, 0.2s slide. ON = Toss-blue (global accent). Shared by 설정 and 마트 편집. */
@Composable
fun ToggleSwitch(checked: Boolean, onToggle: () -> Unit) {
    val colors = AppTheme.colors
    val trackOff = if (colors.isDark) Color(0xFF444444) else Color(0xFFD5D2CB)
    val knobX by animateDpAsState(
        targetValue = if (checked) 21.dp else 3.dp,
        animationSpec = tween(200),
        label = "knobX",
    )
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (checked) colors.brandPrimary else trackOff)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobX)
                .size(20.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}
