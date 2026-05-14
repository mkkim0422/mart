package com.rldjrgo.grocerynote.ui.screens.devtest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.ui.theme.ComponentSize
import com.rldjrgo.grocerynote.ui.theme.Corners
import com.rldjrgo.grocerynote.ui.theme.Spacing

/**
 * Phase 2 verification screen. Wired temporarily into MainActivity; will be replaced by
 * the real HomeScreen in Phase 3.
 */
@Composable
fun DevTestScreen(
    viewModel: DevTestViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    val typo = AppTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
            .padding(horizontal = Spacing.screenHorizontal),
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            text = "Phase 2 검증",
            style = typo.headingL,
            color = colors.textPrimary,
        )
        Text(
            text = "Room + Hilt + 시딩 + Repository 동작 확인",
            style = typo.bodyS,
            color = colors.textTertiary,
            modifier = Modifier.padding(top = Spacing.xs),
        )

        Spacer(Modifier.height(Spacing.xl))

        // Mart tab strip — selected gets a bottom underline in brand color.
        if (state.stores.isEmpty()) {
            Text(
                text = "마트가 없습니다 (시딩 실패?)",
                style = typo.body,
                color = colors.danger,
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(state.stores, key = { it.id }) { store ->
                    StoreTab(
                        store = store,
                        selected = store.id == state.selectedStoreId,
                        itemCount = if (store.id == state.selectedStoreId) state.activeItems.size else null,
                        onClick = { viewModel.selectStore(store.id) },
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.lg))

        // Quick add input
        state.selectedStoreId?.let { sid ->
            QuickAddRow(onAdd = { name -> viewModel.addItem(sid, name) })
        }

        Spacer(Modifier.height(Spacing.md))

        Text(
            text = "활성 항목 (${state.activeItems.size})",
            style = typo.title,
            color = colors.textSecondary,
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentPadding = PaddingValues(vertical = Spacing.sm),
        ) {
            items(state.activeItems, key = { it.id }) { item ->
                ItemRow(
                    item = item,
                    onToggle = { viewModel.toggleComplete(item) },
                    onDelete = { viewModel.deleteItem(item) },
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.divider),
        )
        Spacer(Modifier.height(Spacing.md))

        Text(
            text = "완료 항목 (${state.completedItems.size})",
            style = typo.title,
            color = colors.textSecondary,
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentPadding = PaddingValues(vertical = Spacing.sm),
        ) {
            items(state.completedItems, key = { it.id }) { item ->
                ItemRow(
                    item = item,
                    onToggle = { viewModel.toggleComplete(item) },
                    onDelete = { viewModel.deleteItem(item) },
                )
            }
        }
    }
}

@Composable
private fun StoreTab(
    store: Store,
    selected: Boolean,
    itemCount: Int?,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(ComponentSize.martColorDot)
                    .background(store.color, CircleShape),
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = store.name + (itemCount?.let { " ($it)" } ?: ""),
                style = typo.headingM,
                color = if (selected) colors.textPrimary else colors.textTertiary,
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(if (selected) 32.dp else 0.dp)
                .background(colors.brandPrimary),
        )
    }
}

@Composable
private fun QuickAddRow(onAdd: (String) -> Unit) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    var text by remember { mutableStateOf("") }
    val submit = {
        if (text.isNotBlank()) {
            onAdd(text)
            text = ""
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(ComponentSize.inputHeight)
            .background(colors.bgSecondary, Corners.medium)
            .border(1.dp, colors.divider, Corners.medium)
            .padding(horizontal = Spacing.lg),
    ) {
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .onKeyEvent { e ->
                    if (e.type == KeyEventType.KeyUp && e.key == Key.Enter) {
                        submit(); true
                    } else false
                },
            textStyle = typo.body.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.brandPrimary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            decorationBox = { inner ->
                if (text.isEmpty()) {
                    Text(
                        text = "항목 이름 입력 후 + 또는 Enter",
                        style = typo.body,
                        color = colors.textTertiary,
                    )
                }
                inner()
            },
        )
        IconButton(
            onClick = submit,
            enabled = text.isNotBlank(),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "추가",
                tint = if (text.isNotBlank()) colors.brandPrimary else colors.textDisabled,
            )
        }
    }
}

@Composable
private fun ItemRow(
    item: Item,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(ComponentSize.checkbox)
                .background(
                    color = if (item.isCompleted) colors.brandPrimary else colors.bgPrimary,
                    shape = Corners.small,
                )
                .border(
                    width = 1.5.dp,
                    color = if (item.isCompleted) colors.brandPrimary else colors.textDisabled,
                    shape = Corners.small,
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            if (item.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.bgPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(Modifier.width(Spacing.md))
        Text(
            text = item.name,
            style = typo.body,
            color = if (item.isCompleted) colors.textTertiary else colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "삭제",
                tint = colors.textTertiary,
            )
        }
    }
}
