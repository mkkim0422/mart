package com.rldjrgo.grocerynote.widget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.rldjrgo.grocerynote.R
import com.rldjrgo.grocerynote.widget.actions.CheckItemAction
import com.rldjrgo.grocerynote.widget.actions.OpenStoreAction
import com.rldjrgo.grocerynote.widget.textPrimaryProvider
import com.rldjrgo.grocerynote.widget.textTertiaryProvider

/**
 * List-widget row: ○ + item name.
 * - ○ (32dp touch target) → [CheckItemAction]: completes the item in place.
 * - name → open the app on that mart (same as before).
 * Each part has its own click: inside a Glance LazyColumn (RemoteViews ListView)
 * a parent click never reaches list rows.
 */
@Composable
fun WidgetItemRow(
    storeId: Long,
    itemId: Long,
    name: String,
    checking: Boolean,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .size(36.dp)
                .clickable(
                    actionRunCallback<CheckItemAction>(
                        actionParametersOf(CheckItemAction.ItemIdKey to itemId),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(
                    if (checking) R.drawable.widget_check_on else R.drawable.widget_check_off,
                ),
                contentDescription = if (checking) "완료됨" else "$name 완료",
                modifier = GlanceModifier.size(20.dp),
            )
        }
        Text(
            text = name,
            style = TextStyle(
                color = if (checking) textTertiaryProvider() else textPrimaryProvider(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textDecoration = if (checking) TextDecoration.LineThrough else TextDecoration.None,
            ),
            maxLines = 1,
            modifier = GlanceModifier
                .defaultWeight()
                .clickable(OpenStoreAction.forStore(storeId = storeId))
                .padding(start = 2.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        )
    }
}
