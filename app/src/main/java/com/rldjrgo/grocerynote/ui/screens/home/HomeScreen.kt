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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.emoji
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import com.rldjrgo.grocerynote.ui.components.AdBanner
import com.rldjrgo.grocerynote.ui.components.PageTitle
import com.rldjrgo.grocerynote.ui.components.UndoSnackbarHost
import com.rldjrgo.grocerynote.ui.components.WidgetSizePickerSheet
import com.rldjrgo.grocerynote.ui.components.swipeBetweenTabs
import com.rldjrgo.grocerynote.ui.screens.home.components.AddItemSheet
import com.rldjrgo.grocerynote.ui.screens.home.components.AddStoreSheet
import com.rldjrgo.grocerynote.ui.screens.home.components.EmptyItems
import com.rldjrgo.grocerynote.ui.screens.home.components.EmptyStores
import com.rldjrgo.grocerynote.ui.screens.home.components.ItemList
import com.rldjrgo.grocerynote.ui.screens.home.components.StoreTabBar
import com.rldjrgo.grocerynote.ui.theme.AppTheme

@Composable
fun HomeScreen(
    onManageStores: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    val typo = AppTheme.typography

    var showAddItem by remember { mutableStateOf(false) }
    var sharedDraft by remember { mutableStateOf("") }
    var showAddStore by remember { mutableStateOf(false) }
    var showWidgetSizePicker by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Item?>(null) }
    var moveTarget by remember { mutableStateOf<Item?>(null) }
    var deleteTarget by remember { mutableStateOf<Item?>(null) }

    val selectedStore = state.stores.firstOrNull { it.id == state.selectedStoreId }
    val fabColor = selectedStore?.color ?: colors.brandPrimary

    val snackbarHostState = remember { SnackbarHostState() }
    val undo by viewModel.undoEvent.collectAsStateWithLifecycle()
    val newlyAddedStoreId by viewModel.newlyAddedStoreId.collectAsStateWithLifecycle()
    LaunchedEffect(undo) {
        val u = undo ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "✓ ${u.name} 구매 완료",
            actionLabel = "되돌리기",
            withDismissAction = false,
            duration = SnackbarDuration.Short,
        )
        when (result) {
            SnackbarResult.ActionPerformed -> viewModel.undoComplete(u.itemId)
            SnackbarResult.Dismissed -> viewModel.consumeUndoEvent()
        }
    }

    // Forward widget deep links into the ViewModel.
    val deepLink by com.rldjrgo.grocerynote.DeepLinkBus.flow.collectAsStateWithLifecycle()
    LaunchedEffect(deepLink) {
        val payload = deepLink ?: return@LaunchedEffect
        viewModel.handleDeepLink(payload.storeId, payload.itemId)
        if (payload.openAddItem) {
            sharedDraft = payload.sharedText ?: ""
            showAddItem = true
        }
        com.rldjrgo.grocerynote.DeepLinkBus.flow.value = null
    }

    // Clear highlight after first paint.
    LaunchedEffect(state.highlightItemId) {
        if (state.highlightItemId != null) {
            kotlinx.coroutines.delay(1_500)
            viewModel.clearHighlight()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
            .statusBarsPadding(),
    ) {
        // Screen title (shared PageTitle — kept in sync across all screens)
        PageTitle(title = "구매예정")
        if (state.showWidgetBanner) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.brandPrimarySoft)
                    .height(56.dp)
                    .padding(horizontal = 14.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.TipsAndUpdates,
                    contentDescription = null,
                    tint = colors.brandPrimary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "위젯을 추가하면 홈화면에서 바로 체크",
                    style = typo.bodyS,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.brandPrimary)
                        .clickable { showWidgetSizePicker = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "추가",
                        style = typo.bodyS.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                    )
                }
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "닫기",
                    tint = colors.textTertiary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { viewModel.dismissWidgetBanner() },
                )
            }
        }
        if (state.stores.isEmpty() && !state.isLoading) {
            EmptyStores(onAddStoreClick = { showAddStore = true })
        } else {
            StoreTabBar(
                stores = state.stores,
                selectedStoreId = state.selectedStoreId,
                itemCounts = state.itemCounts,
                onStoreClick = viewModel::selectStore,
                onAddStoreClick = { showAddStore = true },
                onManageStoresClick = onManageStores,
                newlyAddedStoreId = newlyAddedStoreId,
            )
            val storesL = state.stores
            val curIdx = storesL.indexOfFirst { it.id == state.selectedStoreId }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .swipeBetweenTabs(
                        onNext = {
                            if (curIdx in 0 until storesL.lastIndex) {
                                viewModel.selectStore(storesL[curIdx + 1].id)
                            }
                        },
                        onPrev = {
                            if (curIdx > 0) viewModel.selectStore(storesL[curIdx - 1].id)
                        },
                    ),
            ) {
                AnimatedContent(
                    targetState = state.selectedStoreId,
                    transitionSpec = {
                        val ti = storesL.indexOfFirst { it.id == targetState }
                        val ii = storesL.indexOfFirst { it.id == initialState }
                        if (ti >= ii) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "martSwitch",
                    modifier = Modifier.fillMaxSize(),
                ) { _ ->
                    if (state.activeItems.isEmpty()) {
                        EmptyItems(onAddClick = { showAddItem = true })
                    } else {
                        ItemList(
                            items = state.activeItems,
                            storeColor = selectedStore?.color ?: colors.brandPrimary,
                            highlightItemId = state.highlightItemId,
                            onCompleteAnimDone = viewModel::completeItem,
                            onRename = { renameTarget = it },
                            onMove = { moveTarget = it },
                            onDelete = { deleteTarget = it },
                        )
                    }
                }
                // Extended FAB — rounded rectangle filled with the current mart
                // color (Toss-blue fallback when no mart is selected).
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 18.dp, bottom = 18.dp)
                        .height(56.dp)
                        .shadow(6.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(fabColor)
                        .clickable { showAddItem = true }
                        .padding(horizontal = 22.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "항목 추가",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "추가",
                        style = typo.body.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Color.White,
                    )
                }
            }
        }
        AdBanner()
    }
        UndoSnackbarHost(snackbarHostState)
    }

    if (showAddItem && selectedStore != null) {
        AddItemSheet(
            storeName = selectedStore.name,
            storeColor = selectedStore.color,
            storeEmoji = selectedStore.emoji(),
            recentItemNames = state.recentItemNames,
            onAdd = viewModel::addItem,
            onDeleteRecent = viewModel::deleteFrequentItem,
            onDismiss = { showAddItem = false; sharedDraft = "" },
            initialText = sharedDraft,
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

    if (showWidgetSizePicker) {
        WidgetSizePickerSheet(
            onPick = viewModel::pinWidget,
            onDismiss = { showWidgetSizePicker = false },
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
