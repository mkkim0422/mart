package com.rldjrgo.grocerynote.ui.screens.home.components

import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.ui.theme.soft

/**
 * Bottom "add item" sheet.
 *
 * Implemented as a custom [Dialog] (NOT Material3 ModalBottomSheet): the dialog
 * window is full-screen with SOFT_INPUT_ADJUST_RESIZE + decorFitsSystemWindows
 * = false, and the panel is bottom-aligned with `imePadding()`. There is NO
 * separate sheet slide animation, so the ONLY motion is the keyboard's own IME
 * inset animation pushing the panel up — one smooth motion, never the old
 * "panel → keyboard covers → panel jumps above keyboard" three-step stutter.
 */
@Composable
fun AddItemSheet(
    storeName: String,
    storeColor: Color,
    storeEmoji: String,
    recentItemNames: List<String>,
    onAdd: (String) -> Unit,
    onDeleteRecent: (String) -> Unit,
    onDismiss: () -> Unit,
    initialText: String = "",
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    var text by remember { mutableStateOf(initialText) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var lastAdded by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<String?>(null) }
    val scrimClick = remember { MutableInteractionSource() }
    val panelClick = remember { MutableInteractionSource() }

    LaunchedEffect(lastAdded) {
        if (lastAdded != null) {
            kotlinx.coroutines.delay(1500)
            lastAdded = null
        }
    }

    val submit: () -> Unit = {
        val name = text.trim()
        android.util.Log.d("GroceryAdd", "submit() invoked: name='$name', willAdd=${name.isNotEmpty()}")
        if (name.isNotEmpty()) {
            onAdd(name)
            lastAdded = name
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

    val handleColor = if (colors.isDark) Color(0xFF444444) else Color(0xFFD5D2CB)
    val fieldBg = if (colors.isDark) Color(0xFF1F1F1F) else Color(0xFFFAFAF7)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnClickOutside = false,
        ),
    ) {
        // Configure the dialog's window: full-screen, resizes with the IME so
        // `imePadding()` tracks the keyboard smoothly (single motion).
        val view = LocalView.current
        SideEffect {
            (view.parent as? DialogWindowProvider)?.window?.let { w ->
                w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                w.setGravity(Gravity.BOTTOM)
                w.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                w.setDimAmount(0.45f)
            }
        }
        // Pop the keyboard immediately (next frame, once the field is attached).
        LaunchedEffect(Unit) {
            withFrameNanos {}
            runCatching { focusRequester.requestFocus() }
            keyboard?.show()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Scrim: tap above the panel to dismiss (window dim draws the shade).
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = scrimClick,
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
            // Panel — bottom-aligned, rises with the keyboard via imePadding().
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .imePadding()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(colors.bgPrimary)
                    // Absorb taps so they don't fall through to the scrim.
                    .clickable(
                        interactionSource = panelClick,
                        indication = null,
                        onClick = {},
                    )
                    .padding(horizontal = 20.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .background(handleColor, RoundedCornerShape(2.dp)),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 12.dp, bottom = 16.dp),
                ) {
                    Text(
                        text = "$storeEmoji ${storeName}에 추가",
                        style = typo.headingM.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (text.isNotBlank()) submit()
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "닫기",
                            tint = Color(0xFF6B6B6B),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                Spacer(Modifier.height(16.dp))
                // Inline confirmation toast — slides down 1.5s after each add.
                AnimatedVisibility(
                    visible = lastAdded != null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .background(storeColor.soft(colors.isDark), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "✓ ‘${lastAdded}’ 추가됨 — 계속 입력하세요",
                            style = typo.bodyS.copy(fontWeight = FontWeight.Medium),
                            color = storeColor,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(fieldBg, RoundedCornerShape(14.dp))
                        .border(
                            width = 1.5.dp,
                            color = if (text.isBlank()) colors.divider else storeColor,
                            shape = RoundedCornerShape(14.dp),
                        )
                        .padding(start = 16.dp, end = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = storeColor,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        textStyle = typo.body.copy(color = colors.textPrimary, fontSize = 17.sp),
                        cursorBrush = SolidColor(storeColor),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        decorationBox = { inner ->
                            if (text.isEmpty()) {
                                Text(
                                    text = "무엇을 살까요?",
                                    style = typo.body.copy(fontSize = 17.sp),
                                    color = colors.textTertiary,
                                )
                            }
                            inner()
                        },
                    )
                    // "추가" button — only when there's something to add, in the
                    // mart color so it's obvious which mart this goes to.
                    if (text.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .background(storeColor, RoundedCornerShape(10.dp))
                                .clickable {
                                    android.util.Log.d("GroceryAdd", "추가 button clicked")
                                    submit()
                                }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "추가",
                                style = typo.title.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = Color.White,
                            )
                        }
                    }
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
                        text = "최근 등록 상품",
                        style = typo.bodyS,
                        color = colors.textTertiary,
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(chips) { name ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .height(36.dp)
                                    .background(colors.bgTertiary, RoundedCornerShape(8.dp)),
                            ) {
                                Text(
                                    text = name,
                                    style = typo.bodyS,
                                    color = colors.textSecondary,
                                    modifier = Modifier
                                        .clickable {
                                            onAdd(name)
                                            lastAdded = name
                                        }
                                        .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                                )
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "삭제",
                                    tint = colors.textTertiary,
                                    modifier = Modifier
                                        .clickable { deleteTarget = name }
                                        .padding(end = 10.dp)
                                        .size(14.dp),
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
                        .clickable {
                            // Commit any in-progress typing before closing.
                            if (text.isNotBlank()) submit()
                            onDismiss()
                        },
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

    deleteTarget?.let { target ->
        com.rldjrgo.grocerynote.ui.screens.home.ConfirmDialog(
            title = "최근 등록 상품에서 삭제",
            message = "‘$target’ 을(를) 이 마트의 최근 등록 상품에서 삭제할까요?",
            confirmLabel = "삭제",
            destructive = true,
            onConfirm = {
                onDeleteRecent(target)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null },
        )
    }
}
