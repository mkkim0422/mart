package com.rldjrgo.grocerynote.widget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.rldjrgo.grocerynote.widget.actions.OpenStoreAction
import com.rldjrgo.grocerynote.widget.textPrimaryProvider
import com.rldjrgo.grocerynote.widget.textTertiaryProvider

/**
 * Friendly empty state — cart illustration + message. The whole card is tappable
 * (opens the app).
 *
 * [compact] (2x1): one short line — a small cart + the hint only, so it never
 * overflows the single-cell-tall footprint.
 */
@Composable
fun WidgetEmptyState(
    title: String,
    hint: String,
    compact: Boolean = false,
) {
    if (compact) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
                .clickable(OpenStoreAction.forStore(storeId = -1L)),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🛒", style = TextStyle(fontSize = 15.sp))
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = hint,
                    maxLines = 1,
                    style = TextStyle(
                        color = textPrimaryProvider(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
        return
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable(OpenStoreAction.forStore(storeId = -1L)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🛒", style = TextStyle(fontSize = 36.sp))
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = title,
                maxLines = 1,
                style = TextStyle(
                    color = textPrimaryProvider(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Spacer(GlanceModifier.height(3.dp))
            Text(
                text = hint,
                maxLines = 1,
                style = TextStyle(
                    color = textTertiaryProvider(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
    }
}
