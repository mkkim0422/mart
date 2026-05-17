package com.rldjrgo.grocerynote.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.domain.model.iconKeyToEmoji
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.ui.theme.MartPalette

private val MART_PRESETS = listOf(
    "이마트", "홈플러스", "롯데마트", "다이소",
    "쿠팡", "마켓컬리", "편의점", "시장", "약국",
)

private val ICONS = listOf(
    "🛒", "🏪", "🥬", "🥚", "🍞", "🧺", "🛍",
    "📦", "🚀", "🥕", "🍎", "💊", "🌸", "🧴", "🧻",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoreSheet(
    onAdd: (name: String, color: Color, iconKey: String) -> Unit,
    onDismiss: () -> Unit,
    title: String = "새 마트 추가",
    confirmLabel: String = "추가하기",
    initialName: String = "",
    initialColor: Color = MartPalette.Blue,
    initialIconKey: String = "",
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(initialName) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var selectedIcon by remember {
        mutableStateOf(if (initialIconKey.isBlank()) ICONS.first() else iconKeyToEmoji(initialIconKey))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bgPrimary,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .imePadding(),
        ) {
            Text(
                text = title,
                style = typo.headingM,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(16.dp))
            // Name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(colors.bgPrimary, RoundedCornerShape(12.dp))
                    .border(1.5.dp, colors.brandPrimary, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = typo.body.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.brandPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    decorationBox = { inner ->
                        if (name.isEmpty()) {
                            Text(
                                text = "마트 이름 (예: 이마트)",
                                style = typo.body,
                                color = colors.textTertiary,
                            )
                        }
                        inner()
                    },
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("추천", style = typo.bodyS, color = colors.textTertiary)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MART_PRESETS) { preset ->
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .background(colors.bgTertiary, RoundedCornerShape(8.dp))
                            .clickable { name = preset }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(preset, style = typo.bodyS, color = colors.textSecondary)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("아이콘", style = typo.bodyS, color = colors.textTertiary)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ICONS) { icon ->
                    val selected = icon == selectedIcon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.bgTertiary, RoundedCornerShape(10.dp))
                            .border(
                                width = if (selected) 2.dp else 0.dp,
                                color = if (selected) colors.brandPrimary else Color.Transparent,
                                shape = RoundedCornerShape(10.dp),
                            )
                            .clickable { selectedIcon = icon },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(icon, style = typo.title)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("컬러", style = typo.bodyS, color = colors.textTertiary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MartPalette.all.forEach { c ->
                    val selected = c.value == selectedColor.value
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(c, CircleShape)
                            .border(
                                width = if (selected) 2.dp else 0.dp,
                                color = if (selected) colors.textPrimary else Color.Transparent,
                                shape = CircleShape,
                            )
                            .clickable { selectedColor = c },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(
                        if (name.isNotBlank()) colors.brandPrimary else colors.bgTertiary,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = name.isNotBlank()) {
                        onAdd(name.trim(), selectedColor, iconKeyFor(selectedIcon))
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = confirmLabel,
                    style = typo.title,
                    color = if (name.isNotBlank()) colors.bgPrimary else colors.textDisabled,
                )
            }
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}

private fun iconKeyFor(emoji: String): String = "emoji:$emoji"
