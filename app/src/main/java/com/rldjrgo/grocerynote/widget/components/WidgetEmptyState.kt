package com.rldjrgo.grocerynote.widget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.rldjrgo.grocerynote.widget.actions.OpenStoreAction
import com.rldjrgo.grocerynote.widget.textPrimaryProvider
import com.rldjrgo.grocerynote.widget.textTertiaryProvider

@Composable
fun WidgetEmptyState(
    title: String,
    hint: String,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable(OpenStoreAction.forStore(storeId = -1L)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = TextStyle(
                    color = textPrimaryProvider(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Text(
                text = hint,
                style = TextStyle(
                    color = textTertiaryProvider(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                ),
                modifier = GlanceModifier.padding(top = 4.dp),
            )
        }
    }
}
