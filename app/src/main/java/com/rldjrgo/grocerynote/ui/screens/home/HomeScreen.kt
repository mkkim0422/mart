package com.rldjrgo.grocerynote.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.ui.components.AdBanner
import com.rldjrgo.grocerynote.ui.screens.home.components.AddItemSheet
import com.rldjrgo.grocerynote.ui.screens.home.components.AddStoreSheet
import com.rldjrgo.grocerynote.ui.screens.home.components.EmptyItems
import com.rldjrgo.grocerynote.ui.screens.home.components.EmptyStores
import com.rldjrgo.grocerynote.ui.screens.home.components.ItemList
import com.rldjrgo.grocerynote.ui.screens.home.components.StoreTabBar
import com.rldjrgo.grocerynote.ui.theme.AppTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    val typo = AppTheme.typography

    var showAddItem by remember { mutableStateOf(false) }
    var showAddStore by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Item?>(null) }
    var moveTarget by remember { mutableStateOf<Item?>(null) }
    var deleteTarget by remember { mutableStateOf<Item?>(null) }

    val selectedStore = state.stores.firstOrNull { it.id == state.selectedStoreId }
    val itemCounts = remember(state.stores, state.activeItems, state.selectedStoreId) {
        // We only have active items for the selected store. Show that count; others read "" (handled).
        buildMap<Long, Int> {
            state.selectedStoreId?.let { put(it, state.activeItems.size) }
        }
    }

    // Forward widget deep links into the ViewModel.
    val deepLink by com.rldjrgo.grocerynote.DeepLinkBus.flow.collectAsStateWithLifecycle()
    LaunchedEffect(deepLink) {
        val payload = deepLink ?: return@LaunchedEffect
        viewModel.handleDeepLink(payload.storeId, payload.itemId)
        com.rldjrgo.grocerynote.DeepLinkBus.flow.value = null
    }

    // Clear highlight after first paint.
    LaunchedEffect(state.highlightItemId) {
        if (state.highlightItemId != null) {
            kotlinx.coroutines.delay(2_500)
            viewModel.clearHighlight()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary),
    ) {
        // TopAppBar (custom — Toss style, no shadow)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "장보기 메모",
                style = typo.headingM,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
        }
        if (state.stores.isEmpty() && !state.isLoading) {
            EmptyStores(onAddStoreClick = { showAddStore = true })
        } else {
            StoreTabBar(
                stores = state.stores,
                selectedStoreId = state.selectedStoreId,
                itemCounts = itemCounts,
                onStoreClick = viewModel::selectStore,
                onAddStoreClick = { showAddStore = true },
            )
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.activeItems.isEmpty()) {
                    EmptyItems(onAddClick = { showAddItem = true })
                } else {
                    ItemList(
                        items = state.activeItems,
                        highlightItemId = state.highlightItemId,
                        onCompleteAnimDone = viewModel::completeItem,
                        onRename = { renameTarget = it },
                        onMove = { moveTarget = it },
                        onDelete = { deleteTarget = it },
                        onAddInline = { showAddItem = true },
                    )
                }
                FloatingActionButton(
                    onClick = { showAddItem = true },
                    containerColor = colors.brandPrimary,
                    contentColor = colors.bgPrimary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 4.dp,
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(56.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "항목 추가",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
        AdBanner()
    }

    if (showAddItem && selectedStore != null) {
        AddItemSheet(
            storeName = selectedStore.name,
            recentItemNames = state.recentItemNames,
            onAdd = viewModel::addItem,
            onDismiss = { showAddItem = false },
        )
    }
    if (showAddStore) {
        AddStoreSheet(
            onAdd = { name, color, iconKey ->
                viewModel.addStore(name, color, iconKey)
                showAddStore = false
            },
            onDismiss = { showAddStore = false },
        )
    }

    renameTarget?.let { target ->
        RenameItemDialog(
            initial = target.name,
            onConfirm = { newName ->
                viewModel.updateItemName(target.id, newName)
                renameTarget = null
            },
            onDismiss = { renameTarget = null },
        )
    }

    moveTarget?.let { target ->
        MoveItemDialog(
            currentStoreId = target.storeId,
            stores = state.stores,
            onPick = { newStoreId ->
                viewModel.moveItem(target.id, newStoreId)
                moveTarget = null
            },
            onDismiss = { moveTarget = null },
        )
    }

    deleteTarget?.let { target ->
        ConfirmDialog(
            title = "삭제할까요?",
            message = "'${target.name}'을(를) 삭제합니다.",
            confirmLabel = "삭제",
            destructive = true,
            onConfirm = {
                viewModel.deleteItem(target.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null },
        )
    }
}

@Composable
private fun RenameItemDialog(
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.bgPrimary,
        title = { Text("이름 수정", style = typo.headingM, color = colors.textPrimary) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.5.dp, colors.brandPrimary, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    textStyle = typo.body.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.brandPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Text(
                "수정",
                style = typo.title,
                color = colors.brandPrimary,
                modifier = Modifier.clickable { onConfirm(text) }.padding(12.dp),
            )
        },
        dismissButton = {
            Text(
                "취소",
                style = typo.title,
                color = colors.textSecondary,
                modifier = Modifier.clickable(onClick = onDismiss).padding(12.dp),
            )
        },
    )
}

@Composable
private fun MoveItemDialog(
    currentStoreId: Long,
    stores: List<com.rldjrgo.grocerynote.domain.model.Store>,
    onPick: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.bgPrimary,
        title = { Text("다른 마트로 이동", style = typo.headingM, color = colors.textPrimary) },
        text = {
            Column {
                stores.filter { it.id != currentStoreId }.forEach { s ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(s.id) }
                            .padding(vertical = 12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(s.color, CircleShape),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(s.name, style = typo.body, color = colors.textPrimary)
                    }
                }
                if (stores.size <= 1) {
                    Text(
                        "이동할 다른 마트가 없어요.",
                        style = typo.bodyS,
                        color = colors.textTertiary,
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Text(
                "취소",
                style = typo.title,
                color = colors.textSecondary,
                modifier = Modifier.clickable(onClick = onDismiss).padding(12.dp),
            )
        },
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.bgPrimary,
        title = { Text(title, style = typo.headingM, color = colors.textPrimary) },
        text = { Text(message, style = typo.body, color = colors.textSecondary) },
        confirmButton = {
            Text(
                confirmLabel,
                style = typo.title,
                color = if (destructive) colors.danger else colors.brandPrimary,
                modifier = Modifier.clickable(onClick = onConfirm).padding(12.dp),
            )
        },
        dismissButton = {
            Text(
                "취소",
                style = typo.title,
                color = colors.textSecondary,
                modifier = Modifier.clickable(onClick = onDismiss).padding(12.dp),
            )
        },
    )
}
