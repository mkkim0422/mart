package com.rldjrgo.grocerynote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.ui.navigation.Routes
import com.rldjrgo.grocerynote.ui.theme.AppTheme

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val items = listOf(
    NavItem(Routes.HOME, "활성", Icons.AutoMirrored.Outlined.List),
    NavItem(Routes.COMPLETED, "완료", Icons.Outlined.CheckCircle),
    NavItem(Routes.SETTINGS, "설정", Icons.Outlined.Settings),
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.bgPrimary)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.divider),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val tint = if (selected) colors.brandPrimary else colors.textTertiary
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { if (!selected) onNavigate(item.route) }
                        .padding(vertical = 6.dp),
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = item.label,
                        color = tint,
                        style = AppTheme.typography.micro,
                    )
                }
            }
        }
    }
}
