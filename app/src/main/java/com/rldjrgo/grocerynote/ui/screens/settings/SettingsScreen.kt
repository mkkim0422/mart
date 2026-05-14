package com.rldjrgo.grocerynote.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rldjrgo.grocerynote.data.local.DarkModePref
import com.rldjrgo.grocerynote.data.local.WidgetRefresh
import com.rldjrgo.grocerynote.ui.components.AdBanner
import com.rldjrgo.grocerynote.ui.screens.home.ConfirmDialog
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var confirmWipe1 by remember { mutableStateOf(false) }
    var confirmWipe2 by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val json = viewModel.exportJson()
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(json.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.bgPrimary)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("설정", style = typo.headingL, color = colors.textPrimary)
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item { SectionHeader("일반") }
            item {
                RadioRow(
                    label = "다크 모드",
                    value = when (state.darkMode) {
                        DarkModePref.Auto -> "자동"
                        DarkModePref.On -> "켜기"
                        DarkModePref.Off -> "끄기"
                    },
                ) {
                    DarkModePicker(state.darkMode, viewModel::setDarkMode)
                }
            }
            item {
                RadioRow(
                    label = "위젯 새로고침",
                    value = when (state.widgetRefresh) {
                        WidgetRefresh.Immediate -> "즉시"
                        WidgetRefresh.Min5 -> "5분"
                        WidgetRefresh.Min30 -> "30분"
                    },
                ) {
                    WidgetRefreshPicker(state.widgetRefresh, viewModel::setWidgetRefresh)
                }
            }

            item { SectionHeader("데이터") }
            item {
                ActionRow("데이터 내보내기 (JSON)") {
                    exportLauncher.launch("grocery-note-${System.currentTimeMillis()}.json")
                }
            }
            item {
                ActionRow("전체 삭제", destructive = true) { confirmWipe1 = true }
            }

            item { SectionHeader("결제") }
            item {
                ActionRow(
                    title = if (state.isAdRemoved) "광고 제거됨" else "광고 제거 (₩1,900)",
                    enabled = !state.isAdRemoved,
                ) {
                    // Wired in Phase 6.
                }
            }

            item { SectionHeader("정보") }
            item { ActionRow("버전 ${state.version}", enabled = false) {} }
            item {
                ActionRow("피드백 보내기") {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:help@sphinfo.co.kr")
                        putExtra(Intent.EXTRA_SUBJECT, "[장보기 메모] 피드백")
                    }
                    runCatching { context.startActivity(intent) }
                }
            }
            item {
                ActionRow("별점 주기") {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${context.packageName}")
                    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    runCatching { context.startActivity(intent) }
                }
            }
            item {
                ActionRow("친구에게 추천") {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "장보기 메모 - 마트별로 정리하는 초심플 장보기 앱 https://play.google.com/store/apps/details?id=${context.packageName}"
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

    LaunchedEffect(state.toast) {
        if (state.toast != null) {
            kotlinx.coroutines.delay(1500)
            viewModel.clearToast()
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Text(
        text = title,
        style = typo.bodyS,
        color = colors.textTertiary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

@Composable
private fun ActionRow(
    title: String,
    enabled: Boolean = true,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val textColor = when {
        !enabled -> colors.textTertiary
        destructive -> colors.danger
        else -> colors.textPrimary
    }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 20.dp),
        ) {
            Text(text = title, style = typo.body, color = textColor, modifier = Modifier.weight(1f))
            if (enabled && !destructive) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(colors.divider))
    }
}

@Composable
private fun RadioRow(
    label: String,
    value: String,
    picker: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { expanded = !expanded }
                .padding(horizontal = 20.dp),
        ) {
            Text(label, style = typo.body, color = colors.textPrimary, modifier = Modifier.weight(1f))
            Text(value, style = typo.bodyS, color = colors.textSecondary)
        }
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgSecondary)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) { picker() }
        }
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(colors.divider))
    }
}

@Composable
private fun DarkModePicker(current: DarkModePref, onPick: (DarkModePref) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DarkModePref.values().forEach { p ->
            val label = when (p) { DarkModePref.Auto -> "자동"; DarkModePref.On -> "켜기"; DarkModePref.Off -> "끄기" }
            PickChip(label = label, selected = p == current) { onPick(p) }
        }
    }
}

@Composable
private fun WidgetRefreshPicker(current: WidgetRefresh, onPick: (WidgetRefresh) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WidgetRefresh.values().forEach { p ->
            val label = when (p) { WidgetRefresh.Immediate -> "즉시"; WidgetRefresh.Min5 -> "5분"; WidgetRefresh.Min30 -> "30분" }
            PickChip(label = label, selected = p == current) { onPick(p) }
        }
    }
}

@Composable
private fun PickChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(36.dp)
            .background(
                if (selected) colors.brandPrimarySoft else colors.bgPrimary,
                CircleShape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
    ) {
        Text(
            label,
            style = typo.bodyS,
            color = if (selected) colors.brandPrimary else colors.textSecondary,
        )
    }
}
