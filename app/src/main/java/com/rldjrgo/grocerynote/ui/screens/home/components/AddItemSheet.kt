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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemSheet(
    storeName: String,
    recentItemNames: List<String>,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(80)
        focusRequester.requestFocus()
    }

    val submit = {
        val name = text.trim()
        if (name.isNotEmpty()) {
            onAdd(name)
            text = ""  // continuous-add mode
        }
    }

    val suggestions = remember(text, recentItemNames) {
        val q = text.trim()
        if (q.isEmpty()) emptyList() else recentItemNames
            .filter { it.startsWith(q, ignoreCase = true) && it != q }
            .take(5)
    }
    val chips = remember(recentItemNames) { recentItemNames.take(10) }

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
                text = "${storeName}에 추가",
                style = typo.headingM,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(colors.bgPrimary, RoundedCornerShape(12.dp))
                    .border(1.5.dp, colors.brandPrimary, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = typo.body.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.brandPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    decorationBox = { inner ->
                        if (text.isEmpty()) {
                            Text(
                                text = "무엇을 살까요?",
                                style = typo.body,
                                color = colors.textTertiary,
                            )
                        }
                        inner()
                    },
                )
                Text(
                    text = "추가",
                    style = typo.title,
                    color = if (text.isNotBlank()) colors.brandPrimary else colors.textDisabled,
                    modifier = Modifier.clickable(enabled = text.isNotBlank()) { submit() },
                )
            }
            if (suggestions.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bgSecondary, RoundedCornerShape(12.dp))
                        .padding(vertical = 4.dp),
                ) {
                    suggestions.forEach { s ->
                        Text(
                            text = s,
                            style = typo.body,
                            color = colors.textPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { text = s }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                }
            }
            if (chips.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "자주 사는 항목",
                    style = typo.bodyS,
                    color = colors.textTertiary,
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(chips) { name ->
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .background(colors.bgTertiary, RoundedCornerShape(8.dp))
                                .clickable { onAdd(name) }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = name,
                                style = typo.bodyS,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(colors.bgTertiary, RoundedCornerShape(12.dp))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "완료",
                    style = typo.title,
                    color = colors.textSecondary,
                )
            }
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}
