package com.rldjrgo.grocerynote.widget.components

import androidx.compose.runtime.Composable
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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.rldjrgo.grocerynote.widget.actions.OpenStoreAction
import com.rldjrgo.grocerynote.widget.checkboxBorderProvider
import com.rldjrgo.grocerynote.widget.textPrimaryProvider

/**
 * Simple read-only row: dot + item name. Has its own "open app" click because
 * inside a Glance LazyColumn (RemoteViews ListView) the parent card click does
 * NOT reach list rows — without this, tapping items would do nothing.
 */
@Composable
fun WidgetItemRow(
    storeId: Long,
    itemId: Long,
    name: String,
    checkboxSize: Dp = 16.dp,
    nameSize: TextUnit = 14.sp,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .clickable(OpenStoreAction.forStore(storeId = -1L))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .size(6.dp)
                .background(checkboxBorderProvider())
                .cornerRadius(3.dp),
        ) {}
        Spacer(GlanceModifier.width(10.dp))
        Text(
            text = name,
            style = TextStyle(
                color = textPrimaryProvider(),
                fontSize = nameSize,
                fontWeight = FontWeight.Normal,
            ),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1,
        )
    }
}
