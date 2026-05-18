package com.rldjrgo.grocerynote.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.util.WidgetSize

// Warm sample palette — the preview always shows the light design sample.
private val PvCard = Color(0xFFFAFAF7)
private val PvDivider = Color(0xFFF0EEE9)
private val PvText = Color(0xFF191F28)
private val PvSub = Color(0xFFB0B0B0)

private data class Mart(val emoji: String, val name: String, val accent: Color, val soft: Color)

private val EMart = Mart("🛒", "이마트", Color(0xFFFFB800), Color(0xFFFFF6DB))
private val Daiso = Mart("🏪", "다이소", Color(0xFFF04452), Color(0xFFFFE3E8))
private val Coupang = Mart("📦", "쿠팡", Color(0xFF3182F6), Color(0xFFE1F3FA))
// Short name on purpose — the Large 2-column grid is narrow; a 4-char name
// wrapped to 2 lines and broke the layout. Kept ≤3 chars like every other
// sample mart so all previews render consistently.
private val Kurly = Mart("🛍", "컬리", Color(0xFF8B5CF6), Color(0xFFEDDCF4))

/**
 * Pick Small / Medium (recommended) / Large with a faithful mini-render of the
 * real widget. Picking a size fires the system "add widget to home" dialog via
 * [onPick] (requestPinAppWidget) so the widget is actually placed. If the
 * launcher doesn't support pinning, falls back to the manual guide.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSizePickerSheet(
    onPick: (WidgetSize) -> Boolean,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val context = LocalContext.current
    // skipPartiallyExpanded: full-height only. confirmValueChange blocking
    // Hidden disables the drag-down / swipe-to-close gesture — the sheet can
    // only be closed via the explicit X button.
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden },
    )
    // Non-null only when the launcher can't pin → show manual fallback guide.
    var manualFallback by remember { mutableStateOf<WidgetSize?>(null) }

    fun choose(size: WidgetSize) {
        // On Samsung One UI / most launchers, requestPinAppWidget shows its
        // "홈 화면에 추가" confirm popup *over this app* — the app MUST stay in
        // the foreground. Jumping to HOME here suppresses that popup (the app
        // just bounces out with nothing shown). So: stay in-app on success;
        // only fall back to the manual guide when the launcher can't pin.
        if (onPick(size)) onDismiss() else manualFallback = size
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bgPrimary,
        // No "ㅡ" drag handle at all → there is no draggable strip to grab,
        // so the sheet can ONLY be closed via the ✕ button.
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            val fallback = manualFallback
            if (fallback == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "위젯 사이즈 선택",
                        style = typo.headingM.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "닫기",
                            tint = colors.textTertiary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "크기를 고르면 ‘홈 화면에 추가’ 팝업이 떠요.\n‘추가’를 누르면 홈 화면으로 이동돼요. 위젯을 길게 눌러 원하는 자리로 옮기세요.",
                    style = typo.bodyS,
                    color = Color(0xFF6B6B6B),
                )
                Spacer(Modifier.height(20.dp))

                WidgetSizeOption(WidgetSize.TWO_BY_ONE, "Mini", "2개 마트 표기", false) { choose(WidgetSize.TWO_BY_ONE) }
                Spacer(Modifier.height(12.dp))
                WidgetSizeOption(WidgetSize.SMALL, "Small", "3개 마트 표기", false) { choose(WidgetSize.SMALL) }
                Spacer(Modifier.height(12.dp))
                WidgetSizeOption(WidgetSize.LONG, "Long", "1개 마트 및 세부항목", false) { choose(WidgetSize.LONG) }
                Spacer(Modifier.height(12.dp))
                WidgetSizeOption(WidgetSize.MEDIUM, "Medium", "2개 마트 및 세부항목", false) { choose(WidgetSize.MEDIUM) }
                Spacer(Modifier.height(12.dp))
                WidgetSizeOption(WidgetSize.LARGE, "Large", "4개 마트 및 세부항목", false) { choose(WidgetSize.LARGE) }
            } else {
                ManualAddGuide(
                    size = fallback,
                    onBack = { manualFallback = null },
                    onGoHome = {
                        context.startActivity(
                            Intent(Intent.ACTION_MAIN)
                                .addCategory(Intent.CATEGORY_HOME)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                        onDismiss()
                    },
                )
            }
            Spacer(Modifier.height(12.dp))
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun WidgetSizeOption(
    size: WidgetSize,
    title: String,
    subtitle: String,
    recommended: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (recommended) 2.dp else 1.dp,
                color = if (recommended) colors.brandPrimary else colors.divider,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        WidgetSamplePreview(size)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = typo.body.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary,
                )
                if (recommended) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(colors.brandPrimarySoft, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "추천",
                            style = typo.caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                            color = colors.brandPrimary,
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(text = subtitle, style = typo.bodyS, color = Color(0xFF6B6B6B))
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ── Faithful mini-render of the actual widget ──────────────────────
@Composable
private fun WidgetSamplePreview(size: WidgetSize) {
    when (size) {
        WidgetSize.TWO_BY_ONE -> SampleCard(108.dp, 56.dp) {
            CountRow(Coupang, 3)
            CountRow(Daiso, 2)
        }
        WidgetSize.SMALL -> SampleCard(108.dp, 108.dp) {
            CountRow(EMart, 4)
            CountRow(Daiso, 3)
            CountRow(Coupang, 1)
        }
        WidgetSize.LONG -> SampleCard(108.dp, 208.dp) {
            MartHeaderMini(Coupang, 6)
            DotItem("생수"); DotItem("세제"); DotItem("우유")
            DotItem("계란"); DotItem("바나나"); DotItem("라면")
        }
        WidgetSize.MEDIUM -> SampleCard(208.dp, 108.dp) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    MartHeaderMini(EMart, 4)
                    DotItem("라면"); DotItem("홈런볼"); DotItem("바나나우유")
                }
                Box(Modifier.width(1.dp).height(76.dp).background(PvDivider))
                Spacer(Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    MartHeaderMini(Daiso, 3)
                    DotItem("모공크림"); DotItem("케이블"); DotItem("수세미")
                }
            }
        }
        // Largest preview — the 4x4 widget is a big square. Same width as
        // Medium (both 4 cells wide) and same height as Long (both 4 tall),
        // so every sample is proportional to the real widget grid.
        WidgetSize.LARGE -> SampleCard(208.dp, 208.dp) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    MartHeaderMini(EMart, 4); DotItem("라면"); DotItem("계란")
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    MartHeaderMini(Daiso, 3); DotItem("수건"); DotItem("건전지")
                }
            }
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(PvDivider))
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    MartHeaderMini(Coupang, 3); DotItem("생수"); DotItem("세제")
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    MartHeaderMini(Kurly, 2); DotItem("샐러드"); DotItem("요거트")
                }
            }
        }
    }
}

@Composable
private fun SampleCard(w: Dp, h: Dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .size(width = w, height = h)
            .background(PvCard, RoundedCornerShape(12.dp))
            .border(1.dp, PvDivider, RoundedCornerShape(12.dp))
            .padding(8.dp),
        content = content,
    )
}

@Composable
private fun CountRow(m: Mart, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
    ) {
        Box(
            modifier = Modifier.size(14.dp).background(m.soft, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) { Text(m.emoji, fontSize = 8.sp) }
        Spacer(Modifier.width(5.dp))
        Text(m.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PvText, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.background(m.soft, RoundedCornerShape(6.dp)).padding(horizontal = 5.dp, vertical = 1.dp),
        ) { Text("$count", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = m.accent) }
    }
}

@Composable
private fun MartHeaderMini(m: Mart, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(13.dp).background(m.soft, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) { Text(m.emoji, fontSize = 7.sp) }
        Spacer(Modifier.width(4.dp))
        Text(m.name, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = PvText, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.background(m.soft, RoundedCornerShape(5.dp)).padding(horizontal = 4.dp),
        ) { Text("$count", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = m.accent) }
    }
}

@Composable
private fun DotItem(name: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp),
    ) {
        Box(Modifier.size(4.dp).background(PvSub, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(name, fontSize = 8.sp, color = PvText, maxLines = 1)
    }
}

// ── Manual placement guide ─────────────────────────────────────────
@Composable
private fun ManualAddGuide(
    size: WidgetSize,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val sizeLabel = when (size) {
        WidgetSize.TWO_BY_ONE -> "Mini"
        WidgetSize.SMALL -> "Small (2×2)"
        WidgetSize.LONG -> "Long (2×4)"
        WidgetSize.MEDIUM -> "Medium (4×2)"
        WidgetSize.LARGE -> "Large (4×4)"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "뒤로",
            tint = colors.textSecondary,
            modifier = Modifier.size(22.dp).clickable(onClick = onBack),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "$sizeLabel 위젯 추가",
            style = typo.headingM.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
        )
    }
    Spacer(Modifier.height(14.dp))
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        WidgetSamplePreview(size)
    }
    Spacer(Modifier.height(16.dp))
    GuideStep("1", "‘홈 화면으로 이동’을 누르면 바탕화면이 열려요")
    GuideStep("2", "빈 곳을 길게 누른 뒤 ‘위젯’을 선택")
    GuideStep("3", "‘마트노트’의 $sizeLabel 을(를) 원하는 위치에 끌어다 놓으세요")
    Spacer(Modifier.height(18.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(colors.brandPrimary, RoundedCornerShape(12.dp))
            .clickable(onClick = onGoHome),
        contentAlignment = Alignment.Center,
    ) {
        Text("홈 화면으로 이동", style = typo.title, color = Color.White)
    }
}

@Composable
private fun GuideStep(num: String, text: String) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Row(modifier = Modifier.padding(vertical = 5.dp)) {
        Box(
            modifier = Modifier.size(20.dp).background(colors.brandPrimarySoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(num, style = typo.caption.copy(fontWeight = FontWeight.Bold), color = colors.brandPrimary)
        }
        Spacer(Modifier.width(10.dp))
        Text(text, style = typo.bodyS, color = colors.textSecondary, modifier = Modifier.weight(1f))
    }
}
