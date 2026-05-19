package com.rldjrgo.grocerynote.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.ui.theme.AppTheme

/**
 * Material "undo" Snackbar, pinned ABOVE the extended "+추가" FAB so the two
 * never overlap. The FAB sits ~(banner ≈50 + 18 margin + 56 height) ≈ 124-134dp
 * up from the bottom; 140dp clears it with a small gap.
 * Dark pill (#191F28 / dark #2B2D31), white text, Toss-blue [되돌리기] action
 * — the action stays global Toss-blue regardless of mart color.
 */
@Composable
fun BoxScope.UndoSnackbarHost(hostState: SnackbarHostState) {
    val dark = AppTheme.colors.isDark
    SnackbarHost(
        hostState = hostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(start = 16.dp, end = 16.dp, bottom = 140.dp),
    ) { data ->
        Snackbar(
            snackbarData = data,
            shape = RoundedCornerShape(12.dp),
            containerColor = if (dark) Color(0xFF2B2D31) else Color(0xFF191F28),
            contentColor = Color.White,
            actionColor = Color(0xFF3182F6),
        )
    }
}
