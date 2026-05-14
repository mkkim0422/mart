package com.rldjrgo.grocerynote.widget.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.compose.runtime.Composable
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.rldjrgo.grocerynote.widget.actions.OpenStoreAction
import com.rldjrgo.grocerynote.widget.textSecondaryProvider
import com.rldjrgo.grocerynote.widget.textTertiaryProvider

@Composable
fun WidgetStoreHeader(
    storeId: Long,
    storeName: String,
    storeColor: Color,
    itemCount: Int,
    dotSize: Dp = 8.dp,
    nameSize: TextUnit = 14.sp,
    countSize: TextUnit = 12.sp,
) {
    val tintedBg = storeColor.copy(alpha = 0.08f)
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(36.dp)
            .background(ColorProvider(day = tintedBg, night = tintedBg))
            .cornerRadius(10.dp)
            .clickable(OpenStoreAction.forStore(storeId))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .size(dotSize)
                .background(ColorProvider(day = storeColor, night = storeColor))
                .cornerRadius(dotSize / 2),
        ) {}
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = storeName,
            style = TextStyle(
                color = textSecondaryProvider(),
                fontSize = nameSize,
                fontWeight = FontWeight.Medium,
            ),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1,
        )
        Text(
            text = itemCount.toString(),
            style = TextStyle(
                color = textTertiaryProvider(),
                fontSize = countSize,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

/** Unused helper — kept in case mart color contrast against light/dark bg needs adjustment. */
internal fun Color.isLight(): Boolean = luminance() > 0.5f
