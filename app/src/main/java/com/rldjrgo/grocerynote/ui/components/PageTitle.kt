package com.rldjrgo.grocerynote.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.ui.theme.AppTheme

/**
 * The one screen-title used by every top-level screen (Home / Completed /
 * Settings). Change the size/spacing here and all screens stay in sync.
 *
 * Size = the existing Completed/Settings heading ([AppTypography.headingL] —
 * 20sp Bold, -0.02em, Pretendard-ready), primary text color (dark-mode
 * aware), left aligned, padding h20 / top16 / bottom12.
 *
 * @param title    required heading text
 * @param subtitle optional one-line caption under the title
 * @param trailing optional right-aligned slot (e.g. an action icon)
 */
@Composable
fun PageTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                // Same size as the previous Completed/Settings title.
                style = typo.headingL,
                color = colors.textPrimary,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = typo.bodyS,
                    color = colors.textTertiary,
                )
            }
        }
        if (trailing != null) trailing()
    }
}
