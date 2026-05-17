package com.rldjrgo.grocerynote.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Widgets
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rldjrgo.grocerynote.data.local.DarkModePref
import com.rldjrgo.grocerynote.ui.components.AdBanner
import com.rldjrgo.grocerynote.ui.components.PageTitle
import com.rldjrgo.grocerynote.ui.components.WidgetSizePickerSheet
import com.rldjrgo.grocerynote.ui.components.WidgetStoreSelectionSheet
import com.rldjrgo.grocerynote.ui.screens.home.ConfirmDialog
import com.rldjrgo.grocerynote.ui.screens.onboarding.OnboardingScreen
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.ui.theme.soft
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val context = LocalContext.current

    var confirmWipe1 by remember { mutableStateOf(false) }
    var confirmWipe2 by remember { mutableStateOf(false) }
    var showWidgetSizePicker by remember { mutableStateOf(false) }
    var showWidgetStoreSelection by remember { mutableStateOf(false) }
    var showFeedbackBridge by remember { mutableStateOf(false) }
    var showIntroPreview by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(colors.bgPrimary).statusBarsPadding()) {
        PageTitle(title = "설정")
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item { SectionHeader("위젯") }
            item {
                SettingRow(
                    label = "위젯 추가하기",
                    icon = Icons.Outlined.Widgets,
                    onClick = { showWidgetSizePicker = true },
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = colors.brandPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            item {
                SettingRow(
                    label = "노출순서설정",
                    icon = Icons.Outlined.GridView,
                    sub = "위젯에 표시되는 우선순위 설정 (최대 4개)",
                    onClick = { showWidgetStoreSelection = true },
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = colors.textTertiary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            item { SectionHeader("일반") }
            item {
                val on = state.darkMode == DarkModePref.On
                ToggleRow(
                    label = "다크 모드",
                    icon = Icons.Outlined.DarkMode,
                    sub = if (on) "켜짐" else "꺼짐",
                    checked = on,
                    onToggle = {
                        viewModel.setDarkMode(if (on) DarkModePref.Off else DarkModePref.On)
                    },
                )
            }

            item { SectionHeader("데이터") }
            item {
                ActionRow("전체 삭제", icon = Icons.Outlined.DeleteForever, destructive = true) { confirmWipe1 = true }
            }

            // Billing UI hidden until v1.5 (BillingRepository code stays compiled).
            if (com.rldjrgo.grocerynote.BuildConfig.SHOW_BILLING) {
                item { SectionHeader("결제") }
                item {
                    ActionRow(
                        title = if (state.isAdRemoved) "광고 제거됨" else "광고 제거 (₩1,900)",
                        icon = Icons.Outlined.Block,
                        enabled = !state.isAdRemoved,
                    ) {
                        (context as? android.app.Activity)?.let { viewModel.purchaseRemoveAds(it) }
                    }
                }
            }

            item { SectionHeader("정보") }
            item { ActionRow("버전 ${state.version}", icon = Icons.Outlined.Info, enabled = false) {} }
            item {
                ActionRow("인트로 미리보기", icon = Icons.Outlined.Slideshow) {
                    showIntroPreview = true
                }
            }
            item {
                ActionRow("피드백 보내기", icon = Icons.Outlined.Email) {
                    showFeedbackBridge = true
                }
            }
            item {
                ActionRow("별점 주기", icon = Icons.Outlined.Star) {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${context.packageName}")
                    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    runCatching { context.startActivity(intent) }
                }
            }
            item {
                ActionRow("친구에게 추천", icon = Icons.Outlined.Share) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "마트노트 - 마트별로 정리하는 초심플 장보기 앱 https://play.google.com/store/apps/details?id=${context.packageName}"
                        )
                    }
                    runCatching { context.startActivity(Intent.createChooser(intent, "공유")) }
                }
            }
        }
        AdBanner()
    }

    if (confirmWipe1) {
        ConfirmDialog(
            title = "전체 삭제할까요?",
            message = "모든 마트와 항목이 삭제됩니다.",
            confirmLabel = "다음",
            destructive = true,
            onConfirm = { confirmWipe1 = false; confirmWipe2 = true },
            onDismiss = { confirmWipe1 = false },
        )
    }
    if (confirmWipe2) {
        ConfirmDialog(
            title = "정말 삭제할까요?",
            message = "이 작업은 되돌릴 수 없습니다.",
            confirmLabel = "삭제",
            destructive = true,
            onConfirm = {
                viewModel.wipeAllData()
                confirmWipe2 = false
            },
            onDismiss = { confirmWipe2 = false },
        )
    }

    if (showWidgetSizePicker) {
        WidgetSizePickerSheet(
            onPick = viewModel::pinWidget,
            onDismiss = { showWidgetSizePicker = false },
        )
    }

    if (showWidgetStoreSelection) {
        WidgetStoreSelectionSheet(
            stores = state.stores,
            initiallySelected = state.largeWidgetStoreIds,
            onSave = { ids ->
                viewModel.saveLargeWidgetStoreIds(ids)
                showWidgetStoreSelection = false
            },
            onDismiss = { showWidgetStoreSelection = false },
        )
    }

    if (showFeedbackBridge) {
        FeedbackBridgeSheet(
            onSend = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:mkkim850422@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "[마트노트] 의견")
                }
                runCatching { context.startActivity(intent) }
                showFeedbackBridge = false
            },
            onDismiss = { showFeedbackBridge = false },
        )
    }

    if (showIntroPreview) {
        Dialog(
            onDismissRequest = { showIntroPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            OnboardingScreen(onDone = { showIntroPreview = false })
        }
    }

    LaunchedEffect(state.toast) {
        if (state.toast != null) {
            kotlinx.coroutines.delay(1500)
            viewModel.clearToast()
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val typo = AppTheme.typography
    Text(
        text = title,
        style = typo.caption.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
        ),
        color = Color(0xFF6B6B6B),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
    )
}

/** 32dp rounded leading icon box. Danger rows get a red-tinted box + red icon. */
@Composable
private fun LeadingIconBox(icon: ImageVector, destructive: Boolean) {
    val dark = AppTheme.colors.isDark
    val boxBg = when {
        destructive && dark -> AppTheme.colors.danger.soft(true)
        destructive -> Color(0xFFFEE2E4)
        dark -> Color(0xFF333333)
        else -> Color(0xFFF0EEE9)
    }
    val iconTint = when {
        destructive -> Color(0xFFEF4444)
        dark -> Color(0xFFF5F5F5)
        else -> Color(0xFF1A1A1A)
    }
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(boxBg, RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp),
        )
    }
}

/** Shared row: 32dp icon box · label (+optional sub) · trailing slot · 1dp divider. */
@Composable
private fun SettingRow(
    label: String,
    icon: ImageVector,
    sub: String? = null,
    enabled: Boolean = true,
    destructive: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val rowDivider = if (colors.isDark) Color(0xFF333333) else colors.divider
    val labelColor = when {
        !enabled -> colors.textTertiary
        destructive -> colors.danger
        else -> colors.textPrimary
    }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            LeadingIconBox(icon = icon, destructive = destructive)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = typo.body.copy(fontWeight = FontWeight.Medium),
                    color = labelColor,
                )
                if (sub != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(text = sub, style = typo.caption, color = Color(0xFF6B6B6B))
                }
            }
            trailing()
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(rowDivider))
    }
}

@Composable
private fun ActionRow(
    title: String,
    icon: ImageVector,
    enabled: Boolean = true,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    SettingRow(
        label = title,
        icon = icon,
        enabled = enabled,
        destructive = destructive,
        onClick = if (enabled) onClick else null,
    ) {
        if (enabled && !destructive) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    icon: ImageVector,
    sub: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    SettingRow(label = label, icon = icon, sub = sub, onClick = onToggle) {
        ToggleSwitch(checked = checked, onToggle = onToggle)
    }
}

/**
 * Bridge sheet shown before opening the mail client — explains we want
 * real-life app ideas / improvement requests and will act on them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackBridgeSheet(
    onSend: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bgPrimary,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = "의견 보내기",
                style = typo.headingM.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "실생활에서 \"이런 앱 있으면 좋겠다\" 싶은 아이디어나, " +
                    "지금 이 앱에서 불편하거나 개선했으면 하는 점을 편하게 보내주세요.\n\n" +
                    "보내주신 의견은 직접 검토해 앱에 반영해 드릴게요.",
                style = typo.body,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(colors.brandPrimary, RoundedCornerShape(12.dp))
                    .clickable(onClick = onSend),
                contentAlignment = Alignment.Center,
            ) {
                Text("의견 보내기", style = typo.title, color = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}

/** 44×26 pill toggle, 20dp knob, 0.2s slide. ON = Toss-blue (global accent). */
@Composable
private fun ToggleSwitch(checked: Boolean, onToggle: () -> Unit) {
    val colors = AppTheme.colors
    val trackOff = if (colors.isDark) Color(0xFF444444) else Color(0xFFD5D2CB)
    val knobX by animateDpAsState(
        targetValue = if (checked) 21.dp else 3.dp,
        animationSpec = tween(200),
        label = "knobX",
    )
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (checked) colors.brandPrimary else trackOff)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobX)
                .size(20.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

