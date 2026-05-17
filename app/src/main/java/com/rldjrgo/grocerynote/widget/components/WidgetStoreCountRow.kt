package com.rldjrgo.grocerynote.widget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.rldjrgo.grocerynote.widget.martSoftProvider
import com.rldjrgo.grocerynote.widget.staticProvider
import com.rldjrgo.grocerynote.widget.textPrimaryProvider

/**
 * Small-widget row: a mart's emoji badge + name + remaining-count pill.
 * No own click — taps fall through to the widget card (→ opens the app).
 */
@Composable
fun WidgetStoreCountRow(
    storeId: Long,
    storeName: String,
    storeColor: Color,
    storeEmoji: String,
    count: Int,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .size(20.dp)
                .background(martSoftProvider(storeColor))
                .cornerRadius(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = storeEmoji, style = TextStyle(fontSize = 11.sp))
        }
        Spacer(GlanceModifier.width(8.dp))
        Text(
            text = storeName,
            style = TextStyle(
                color = textPrimaryProvider(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1,
        )
        if (count > 0) {
            Box(
                modifier = GlanceModifier
                    .background(martSoftProvider(storeColor))
                    .cornerRadius(8.dp)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    text = count.toString(),
                    style = TextStyle(
                        color = staticProvider(storeColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        } else {
            Text(
                text = "—",
                style = TextStyle(
                    color = staticProvider(Color(0xFFC9CDD2)),
                    fontSize = 11.sp,
                ),
            )
        }
    }
}
