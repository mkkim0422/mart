package com.rldjrgo.grocerynote.widget.components

import androidx.compose.runtime.Composable
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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.rldjrgo.grocerynote.widget.actions.OpenStoreAction
import com.rldjrgo.grocerynote.widget.dividerProvider
import com.rldjrgo.grocerynote.widget.martSoftProvider
import com.rldjrgo.grocerynote.widget.staticProvider
import com.rldjrgo.grocerynote.widget.textPrimaryProvider

/**
 * Mart header — strong per-mart color so the widget is recognizable at a glance:
 * a tinted rounded-square emoji badge + mart name + a colored count pill,
 * with a 1dp bottom divider.
 */
@Composable
fun WidgetStoreHeader(
    storeId: Long,
    storeName: String,
    storeColor: Color,
    storeEmoji: String,
    itemCount: Int,
    iconBoxSize: Dp = 24.dp,
    emojiSize: TextUnit = 14.sp,
    nameSize: TextUnit = 13.sp,
) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left area (emoji + name + count) — explicit click → open the app
            // with THIS mart preselected. Glance/RemoteViews doesn't reliably
            // bubble taps from non-clickable children up to a clickable parent
            // Column, so each "category area" row needs its own click anchored
            // to store.id (the parent MartColumn.clickable alone isn't enough).
            Row(
                modifier = GlanceModifier
                    .defaultWeight()
                    .clickable(OpenStoreAction.forStore(storeId = storeId)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(iconBoxSize)
                        .background(martSoftProvider(storeColor))
                        .cornerRadius(7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = storeEmoji,
                        style = TextStyle(fontSize = emojiSize),
                    )
                }
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = storeName,
                    style = TextStyle(
                        color = textPrimaryProvider(),
                        fontSize = nameSize,
                        fontWeight = FontWeight.Medium,
                    ),
                    modifier = GlanceModifier.defaultWeight(),
                    maxLines = 1,
                )
                Box(
                    modifier = GlanceModifier
                        .background(martSoftProvider(storeColor))
                        .cornerRadius(10.dp)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = itemCount.toString(),
                        style = TextStyle(
                            color = staticProvider(storeColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }
            Spacer(GlanceModifier.width(8.dp))
            // Per-mart quick "+" → opens the add-item sheet for this mart.
            Box(
                modifier = GlanceModifier
                    .size(32.dp)
                    .background(martSoftProvider(storeColor))
                    .cornerRadius(9.dp)
                    .clickable(OpenStoreAction.addToStore(storeId)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+",
                    style = TextStyle(
                        color = staticProvider(storeColor),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(dividerProvider()),
        ) {}
        Spacer(GlanceModifier.height(8.dp))
    }
}

/** Unused helper — kept in case mart color contrast against light/dark bg needs adjustment. */
internal fun Color.isLight(): Boolean = luminance() > 0.5f
