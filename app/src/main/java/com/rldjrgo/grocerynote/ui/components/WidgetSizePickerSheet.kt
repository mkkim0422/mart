package com.rldjrgo.grocerynote.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
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
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.util.WidgetKind
import com.rldjrgo.grocerynote.widget.actions.WidgetStateKeys

// Warm sample palette — the preview always shows the light design sample.
private val PvCard = Color(0xFFFAFAF7)
private val PvDivider = Color(0xFFF0EEE9)
private val PvText = Color(0xFF191F28)
private val PvSub = Color(0xFFB0B0B0)

// One UI / Pixel show their own "홈 화면에 추가" popup for requestPinAppWidget — an app
// can't open the launcher's place-it mode directly. Long-pressing the widget in that
// popup does exactly that (verified on S24+ 2026-11-12), so tell people.
private const val PIN_HINT =
    "다음 창에서 위젯 그림을 꾹 누르면 홈 화면으로 넘어가요. 원하는 자리에 끌어다 놓으세요. ‘추가’를 누르면 빈자리에 바로 놓여요."

private data class Mart(val emoji: String, val name: String, val accent: Color, val soft: Color)

private val Daiso = Mart("🏪", "다이소", Color(0xFFF04452), Color(0xFFFFE3E8))
private val Coupang = Mart("🚀", "쿠팡", Color(0xFF3182F6), Color(0xFFE1F3FA))
// Short name on purpose — the Large 2-column grid is narrow; a 4-char name
// wrapped to 2 lines and broke the layout. Kept ≤3 chars like every other
// sample mart so all previews render consistently.
private val Kurly = Mart("🛍", "컬리", Color(0xFF8B5CF6), Color(0xFFEDDCF4))

/**
 * Pick 목록 위젯 / 개수 위젯 (2026-11-12: 5 sizes → 2 kinds) with a faithful
 * mini-render. Tapping a card only selects it (blue border + radio); the bottom
 * "홈 화면에 추가" button fires the system "add widget to home" dialog via [onPick]
 * (requestPinAppWidget). 목록 위젯 is placed as 전체 — no mart question here
 * (user 2026-11-12: a mart screen, then inline mart chips, were both unneeded);
 * long-press the widget → 설정 to narrow it to one mart. If the launcher can't
 * pin → manual guide.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSizePickerSheet(
    stores: List<Store>,
    onPick: (WidgetKind, Long?) -> Boolean,
    onDismiss: () -> Unit,
    counts: Map<Long, Int>? = null,
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
    var manualFallback by remember { mutableStateOf<WidgetKind?>(null) }
    var selected by remember { mutableStateOf(WidgetKind.LIST) }

    fun choose(kind: WidgetKind, martId: Long? = null) {
        // On Samsung One UI / most launchers, requestPinAppWidget shows its
        // "홈 화면에 추가" confirm popup *over this app* — the app MUST stay in
        // the foreground. Jumping to HOME here suppresses that popup (the app
        // just bounces out with nothing shown). So: stay in-app on success;
        // only fall back to the manual guide when the launcher can't pin.
        if (onPick(kind, martId)) onDismiss() else manualFallback = kind
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
                        text = "위젯 추가",
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
                    text = PIN_HINT + "\n놓은 뒤 위젯을 길게 누르면 크기를 바꿀 수 있어요.",
                    style = typo.bodyS,
                    color = Color(0xFF6B6B6B),
                )
                Spacer(Modifier.height(20.dp))

                WidgetSizeOption(
                    WidgetKind.LIST, "목록 위젯", "살 것 목록, 동그라미 눌러 바로 체크",
                    recommended = true,
                    selected = selected == WidgetKind.LIST,
                    onClick = { selected = WidgetKind.LIST },
                )
                Spacer(Modifier.height(12.dp))
                WidgetSizeOption(
                    WidgetKind.COUNT, "개수 위젯", "마트별 남은 개수만, 작게 한 줄\n늘리면 마트가 더 보여요",
                    recommended = false,
                    selected = selected == WidgetKind.COUNT,
                    onClick = { selected = WidgetKind.COUNT },
                )
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(colors.brandPrimary, RoundedCornerShape(12.dp))
                        .clickable {
                            if (selected == WidgetKind.LIST) choose(WidgetKind.LIST, WidgetStateKeys.ALL_MARTS)
                            else choose(WidgetKind.COUNT)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("홈 화면에 추가", style = typo.title, color = Color.White)
                }
            } else {
                ManualAddGuide(
                    kind = fallback!!,
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
    kind: WidgetKind,
    title: String,
    subtitle: String,
    recommended: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.brandPrimary else colors.divider,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        WidgetSamplePreview(kind)
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
        // Radio — same look as MartPicker's.
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(
                    width = if (selected) 7.dp else 2.dp,
                    color = if (selected) colors.brandPrimary else colors.textDisabled,
                    shape = CircleShape,
                ),
        )
    }
    }
}

// ── Faithful mini-render of the actual widget ──────────────────────
// internal: WidgetNudgeSheet(위젯 유도 시트)에서도 같은 미리보기를 쓴다.
@Composable
internal fun WidgetSamplePreview(kind: WidgetKind) {
    when (kind) {
        // Pins as the 2x1 strip → 2 rows, no "+".
        WidgetKind.COUNT -> SampleCard(96.dp, 52.dp) {
            CountRow(Coupang, 4)
            CountRow(Daiso, 3)
        }
        WidgetKind.LIST -> SampleCard(96.dp, 120.dp) {
            MartHeaderMini(Coupang, 4)
            CheckItem("생수"); CheckItem("세제"); CheckItem("우유"); CheckItem("계란")
            Spacer(Modifier.weight(1f))
            // The widget's one "+" (bottom-right), like widget_fab.
            Box(
                modifier = Modifier.align(Alignment.End).size(16.dp).background(Coupang.accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text("+", fontSize = 11.sp, lineHeight = 11.sp, color = Color.White) }
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
        verticalArrangement = if (h < 80.dp) Arrangement.Center else Arrangement.Top,
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
        ) { Text(m.emoji, fontSize = 8.sp, lineHeight = 10.sp) }
        Spacer(Modifier.width(5.dp))
        Text(m.name, fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.Bold, color = PvText, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.background(m.soft, RoundedCornerShape(6.dp)).padding(horizontal = 5.dp, vertical = 1.dp),
        ) { Text("$count", fontSize = 8.sp, lineHeight = 10.sp, fontWeight = FontWeight.Bold, color = m.accent) }
    }
}

@Composable
private fun MartHeaderMini(m: Mart, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(13.dp).background(m.soft, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) { Text(m.emoji, fontSize = 7.sp, lineHeight = 9.sp) }
        Spacer(Modifier.width(4.dp))
        Text(m.name, fontSize = 8.sp, lineHeight = 10.sp, fontWeight = FontWeight.Bold, color = PvText, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.background(m.soft, RoundedCornerShape(5.dp)).padding(horizontal = 4.dp),
        ) { Text("$count", fontSize = 7.sp, lineHeight = 9.sp, fontWeight = FontWeight.Bold, color = m.accent) }
    }
}

@Composable
private fun CheckItem(name: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 5.dp),
    ) {
        Box(Modifier.size(8.dp).border(1.dp, PvSub, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(name, fontSize = 8.sp, lineHeight = 10.sp, color = PvText, maxLines = 1)
    }
}

// ── Manual placement guide ─────────────────────────────────────────
@Composable
private fun ManualAddGuide(
    kind: WidgetKind,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val sizeLabel = when (kind) {
        WidgetKind.COUNT -> "개수 위젯"
        WidgetKind.LIST -> "목록 위젯"
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
            text = "$sizeLabel 추가",
            style = typo.headingM.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
        )
    }
    Spacer(Modifier.height(14.dp))
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        WidgetSamplePreview(kind)
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
