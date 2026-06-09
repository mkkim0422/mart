package com.rldjrgo.grocerynote.ui.screens.home

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
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
import com.rldjrgo.grocerynote.ui.screens.home.components.ReminderPickerSheet
import com.rldjrgo.grocerynote.ui.screens.home.components.ShareRequestDialog
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
    var showShareRequest by remember { mutableStateOf(false) }
    var showWidgetSizePicker by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Item?>(null) }
    var moveTarget by remember { mutableStateOf<Item?>(null) }
    var deleteTarget by remember { mutableStateOf<Item?>(null) }
    var reminderTarget by remember { mutableStateOf<Item?>(null) }
    // Captured while the POST_NOTIFICATIONS dialog is up (itemId to epochMillis).
    var pendingReminder by remember { mutableStateOf<Pair<Long, Long>?>(null) }

    val selectedStore = state.stores.firstOrNull { it.id == state.selectedStoreId }
    val fabColor = selectedStore?.color ?: colors.brandPrimary

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Android 13+ runtime notification permission. On grant, apply the reminder we
    // captured before showing the dialog; on denial, point the user to settings.
    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val pend = pendingReminder
        pendingReminder = null
        when {
            granted && pend != null -> {
                viewModel.setReminder(pend.first, pend.second)
                scope.launch { snackbarHostState.showSnackbar("🔔 알림이 설정되었어요") }
            }
            !granted -> scope.launch {
                snackbarHostState.showSnackbar("설정 > 알림에서 마트노트 알림을 켜주세요")
            }
        }
    }
    // Share FAB debounce — guard against double-taps so the system share sheet
    // is not requested twice (causes flicker on some launchers).
    var lastShareAt by remember { mutableLongStateOf(0L) }
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
        PageTitle(title = "마트노트")
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
                    text = "위젯을 추가하면 홈화면에서 한눈에 확인",
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
            EmptyStores(onAddStoreClick = { showAddStore = true }, modifier = Modifier.weight(1f))
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
                    .weight(1f)
                    .fillMaxWidth()
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
                        when {
                            // Bootstrap settle (initial id not a real store yet,
                            // or data still resolving): just fade — NO slide, so
                            // the screen doesn't swipe left once on first launch.
                            ii < 0 || ti < 0 -> fadeIn() togetherWith fadeOut()
                            ti >= ii ->
                                (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut())
                            else ->
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
                            onSetReminder = { reminderTarget = it },
                        )
                    }
                }
                // FAB stack — share (small, mart-tinted, only when a mart is
                // selected) above the primary "추가" extended FAB.
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 18.dp, bottom = 18.dp),
                ) {
                    if (selectedStore != null) {
                        SmallFloatingActionButton(
                            onClick = onClick@{
                                // 1초 연타 방지 — 다이얼로그가 모달이라 자체적으로
                                // 중복 호출이 막히지만, 다이얼로그 띄우는 비용도
                                // 두 번 들지 않게 가드.
                                val now = System.currentTimeMillis()
                                if (now - lastShareAt < 1_000L) return@onClick
                                lastShareAt = now

                                // 빈 리스트 가드는 다이얼로그 띄우기 전에 체크 — 부탁
                                // 문구 입력하게 한 뒤 "공유할 게 없다"고 알리면 흐름이
                                // 어색하므로 여기서 끊는다.
                                if (state.activeItems.isEmpty()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("공유할 항목이 없습니다.")
                                    }
                                    return@onClick
                                }
                                showShareRequest = true
                            },
                            containerColor = colors.bgPrimary,
                            contentColor = fabColor,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 4.dp,
                                focusedElevation = 4.dp,
                                hoveredElevation = 4.dp,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "장보기 리스트 공유",
                            )
                        }
                    }
                    // Extended FAB — rounded rectangle filled with the current mart
                    // color (Toss-blue fallback when no mart is selected).
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
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
        }
        AdBanner()
    }
        UndoSnackbarHost(snackbarHostState)
    }

    if (showAddItem && selectedStore != null) {
        AddItemSheet(
            stores = state.stores,
            selectedStoreId = selectedStore.id,
            onSelectStore = viewModel::selectStore,
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

    if (showShareRequest && selectedStore != null) {
        ShareRequestDialog(
            storeName = selectedStore.name,
            storeColor = selectedStore.color,
            onShare = { note ->
                // 다이얼로그 안에서 받은 사용자 입력을 헤더로 얹어 텍스트 빌드 →
                // 시스템 공유 시트 호출. 다이얼로그는 즉시 닫는다 (시트가 떠 있는
                // 동안 다이얼로그가 뒤에 남아있을 필요 없음).
                showShareRequest = false
                val body = buildShareText(
                    storeName = selectedStore.name,
                    items = state.activeItems,
                    requestNote = note,
                )
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                try {
                    context.startActivity(
                        Intent.createChooser(sendIntent, "장보기 리스트 공유")
                    )
                } catch (_: ActivityNotFoundException) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "공유할 수 있는 앱을 찾을 수 없습니다."
                        )
                    }
                }
            },
            onDismiss = { showShareRequest = false },
        )
    }

    if (showWidgetSizePicker) {
        WidgetSizePickerSheet(
            onPick = viewModel::pinWidget,
            onDismiss = { showWidgetSizePicker = false },
        )
    }

    reminderTarget?.let { target ->
        ReminderPickerSheet(
            itemName = target.name,
            initialAtMillis = target.reminderAt,
            onConfirm = { atMillis ->
                when {
                    atMillis <= System.currentTimeMillis() -> scope.launch {
                        snackbarHostState.showSnackbar("이미 지난 시간이에요. 다시 선택해주세요")
                    }
                    // Android 13+: must hold POST_NOTIFICATIONS before the alarm fires.
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) != PackageManager.PERMISSION_GRANTED -> {
                        pendingReminder = target.id to atMillis
                        reminderTarget = null
                        notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    else -> {
                        viewModel.setReminder(target.id, atMillis)
                        reminderTarget = null
                        scope.launch { snackbarHostState.showSnackbar("🔔 알림이 설정되었어요") }
                    }
                }
            },
            onClear = {
                viewModel.clearReminder(target.id)
                reminderTarget = null
                scope.launch { snackbarHostState.showSnackbar("알림을 껐어요") }
            },
            onDismiss = { reminderTarget = null },
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

/**
 * 공유용 텍스트 빌더.
 *
 * 사용자가 부탁 문구를 적었으면 맨 위에 한 줄로 얹고 빈 줄로 띄움. 아이템
 * 이름은 trim + `\n` 치환으로 한 줄 보장. 푸터는 마트노트 워터마크.
 *
 * 향후 확장 자리 (사용자 요청, 2026-05-26):
 *   - 받는 사람 OS에 따라 마트노트 설치 링크 첨부.
 *     예) installLinks: SharePlatforms? = null,
 *         SharePlatforms.Both → Android(Play) + iOS(App Store) 두 줄
 *         SharePlatforms.AndroidOnly → Play 링크만
 *     iOS 앱이 아직 존재하지 않으므로([[handoff-master]] §1) iOS 분기는 앱
 *     출시 후 다시 결정. 시그니처만 열어둠 — 기본값 null이라 호출부 변경 없음.
 */
private fun buildShareText(
    storeName: String,
    items: List<Item>,
    requestNote: String,
    // installLinks: SharePlatforms? = null,   // 향후 확장 자리
): String = buildString {
    val note = requestNote.trim().replace('\n', ' ')
    if (note.isNotEmpty()) {
        append(note)
        append("\n\n")
    }
    append("🛒 [")
    append(storeName)
    append("] 장보기 리스트\n\n")
    items.forEach { item ->
        val clean = item.name.trim().replace('\n', ' ')
        append("• ")
        append(clean)
        append('\n')
    }
    append("\n📝 마트노트에서 보냄")
    // 향후 자리: if (installLinks != null) { append("\n👉 ...") }
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
