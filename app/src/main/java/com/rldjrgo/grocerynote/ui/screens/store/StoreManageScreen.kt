package com.rldjrgo.grocerynote.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.domain.model.emoji
import com.rldjrgo.grocerynote.ui.components.UndoSnackbarHost
import com.rldjrgo.grocerynote.ui.screens.home.ConfirmDialog
import com.rldjrgo.grocerynote.ui.screens.home.components.AddStoreSheet
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun StoreManageScreen(
    onClose: () -> Unit,
    viewModel: StoreManageViewModel = hiltViewModel(),
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddStore by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Store?>(null) }
    var deleteTarget by remember { mutableStateOf<StoreRow?>(null) }

    // Local working copy so drag reorder is instant. We merge VM updates instead
    // of replacing wholesale: keep the current local order for stores that still
    // exist (so a just-finished drag doesn't snap back before the DB write lands),
    // pick up field changes (rename/color/emoji/count), drop deleted, append new.
    var rows by remember { mutableStateOf(state.rows) }
    var dragging by remember { mutableStateOf(false) }
    LaunchedEffect(state.rows, dragging) {
        if (dragging) return@LaunchedEffect
        val byId = state.rows.associateBy { it.store.id }
        val kept = rows.mapNotNull { local -> byId[local.store.id] }
        val keptIds = kept.mapTo(HashSet()) { it.store.id }
        val added = state.rows.filter { it.store.id !in keptIds }
        rows = kept + added
    }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        rows = rows.toMutableList().apply { add(to.index, removeAt(from.index)) }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()
    LaunchedEffect(deleted) {
        val d = deleted ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "✓ '${d.name}' 삭제됨",
            actionLabel = "되돌리기",
            withDismissAction = false,
            duration = SnackbarDuration.Short,
        )
        when (result) {
            SnackbarResult.ActionPerformed -> viewModel.undoDelete(d.id)
            SnackbarResult.Dismissed -> viewModel.finalizeDelete(d.id)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.bgPrimary)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Top bar — ✕ close · "마트 관리" · "완료"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "닫기",
                    tint = colors.textSecondary,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onClose),
                )
                Text(
                    text = "마트 관리",
                    style = typo.headingM,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "완료",
                    style = typo.title,
                    color = colors.brandPrimary,
                    modifier = Modifier
                        .clickable(onClick = onClose)
                        .padding(8.dp),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.divider),
            )

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                items(rows, key = { it.store.id }) { row ->
                    ReorderableItem(reorderState, key = row.store.id) { isDragging ->
                        StoreManageRow(
                            row = row,
                            isDragging = isDragging,
                            dragHandle = {
                                // Grab the = handle and move up/down immediately
                                // (no long press). 40dp box = comfortable touch target.
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .draggableHandle(
                                            onDragStarted = { dragging = true },
                                            onDragStopped = {
                                                dragging = false
                                                viewModel.persistOrder(rows.map { it.store.id })
                                            },
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DragHandle,
                                        contentDescription = "순서 변경 (위아래로 드래그)",
                                        tint = Color(0xFFB0B0B0),
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                            onEdit = { editTarget = row.store },
                            onDelete = { deleteTarget = row },
                        )
                    }
                }
            }

            // + 마트 추가하기 (full-width Toss blue)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(52.dp)
                    .background(colors.brandPrimary, RoundedCornerShape(12.dp))
                    .clickable { showAddStore = true },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+ 마트 추가하기",
                    style = typo.title,
                    color = Color.White,
                )
            }
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
        UndoSnackbarHost(snackbarHostState)
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

    // 수정 — same sheet UI as the add screen, pre-filled with current values.
    editTarget?.let { s ->
        AddStoreSheet(
            title = "마트 수정",
            confirmLabel = "저장",
            initialName = s.name,
            initialColor = s.color,
            initialIconKey = s.iconKey,
            onAdd = { name, color, iconKey ->
                viewModel.updateStore(s.id, name, color, iconKey)
                editTarget = null
            },
            onDismiss = { editTarget = null },
        )
    }

    deleteTarget?.let { row ->
        val msg = if (row.itemCount > 0) {
            "이 마트의 항목 ${row.itemCount}개가 함께 삭제됩니다. 정말 삭제할까요?"
        } else {
            "정말 삭제할까요?"
        }
        ConfirmDialog(
            title = "'${row.store.name}' 삭제",
            message = msg,
            confirmLabel = "삭제",
            destructive = true,
            onConfirm = {
                viewModel.requestDelete(row.store.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null },
        )
    }
}

@Composable
private fun StoreManageRow(
    row: StoreRow,
    isDragging: Boolean,
    dragHandle: @Composable () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    var menuOpen by remember { mutableStateOf(false) }
    val store = row.store

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(if (isDragging) colors.bgTertiary else colors.bgPrimary)
            .padding(horizontal = 20.dp),
    ) {
        dragHandle()
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(store.color, CircleShape),
        )
        Spacer(Modifier.width(10.dp))
        Text(text = store.emoji(), fontSize = 16.sp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = store.name,
                style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                color = colors.textPrimary,
                maxLines = 1,
            )
            Text(
                text = "${row.itemCount}개 항목",
                style = typo.caption.copy(fontSize = 12.sp),
                color = colors.textTertiary,
            )
        }
        Box {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "메뉴",
                tint = colors.textSecondary,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { menuOpen = true },
            )
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text("수정", style = typo.body, color = colors.textPrimary) },
                    onClick = { menuOpen = false; onEdit() },
                )
                DropdownMenuItem(
                    text = { Text("삭제", style = typo.body, color = colors.danger) },
                    onClick = { menuOpen = false; onDelete() },
                )
            }
        }
    }
}
