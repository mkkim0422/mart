package com.rldjrgo.grocerynote.widget.components

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
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
import com.rldjrgo.grocerynote.widget.actions.CheckItemAction
import com.rldjrgo.grocerynote.widget.actions.OpenStoreAction
import com.rldjrgo.grocerynote.widget.checkboxBorderProvider
import com.rldjrgo.grocerynote.widget.textPrimaryProvider

@Composable
fun WidgetItemRow(
    storeId: Long,
    itemId: Long,
    name: String,
    checkboxSize: Dp = 18.dp,
    nameSize: TextUnit = 13.sp,
) {
    val checkAction: Action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        actionRunCallback<CheckItemAction>(
            actionParametersOf(CheckItemAction.ItemIdKey to itemId),
        )
    } else {
        OpenStoreAction.forStore(storeId = storeId, itemId = itemId)
    }
    val rowOpen: Action = OpenStoreAction.forStore(storeId = storeId, itemId = itemId)

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .size(checkboxSize)
                .background(ColorProvider(day = androidx.compose.ui.graphics.Color.Transparent, night = androidx.compose.ui.graphics.Color.Transparent))
                .cornerRadius(4.dp)
                .clickable(checkAction),
            contentAlignment = Alignment.Center,
        ) {
            // Hollow checkbox: a smaller bordered box. Glance has no Border modifier, so we
            // simulate the border with a checkboxBorder-colored background and a slightly
            // smaller inner background of the surrounding card.
            Box(
                modifier = GlanceModifier
                    .size(checkboxSize)
                    .background(checkboxBorderProvider())
                    .cornerRadius(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(checkboxSize - 3.dp)
                        .background(com.rldjrgo.grocerynote.widget.cardBgProvider())
                        .cornerRadius(3.dp),
                ) {}
            }
        }
        Spacer(GlanceModifier.width(10.dp))
        Text(
            text = name,
            style = TextStyle(
                color = textPrimaryProvider(),
                fontSize = nameSize,
                fontWeight = FontWeight.Normal,
            ),
            modifier = GlanceModifier.defaultWeight().clickable(rowOpen),
            maxLines = 1,
        )
    }
}
