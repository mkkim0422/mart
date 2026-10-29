package com.rldjrgo.grocerynote.ui.screens.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.rldjrgo.grocerynote.R
import com.rldjrgo.grocerynote.ui.components.WidgetSizePickerSheet
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settings: com.rldjrgo.grocerynote.data.local.SettingsDataStore,
    private val widgetPin: com.rldjrgo.grocerynote.util.WidgetPinHelper,
) : androidx.lifecycle.ViewModel() {
    fun markSeen() {
        viewModelScope.launch {
            settings.setOnboardingSeen()
        }
    }

    fun pinWidget(size: com.rldjrgo.grocerynote.util.WidgetSize): Boolean =
        widgetPin.pinWidget(size)
}

private data class Page(
    val title: String,
    val subtitle: String,
    /** Real in-app screenshot, or null when [drawn] draws the picture instead. */
    @param:DrawableRes val illustration: Int?,
    val drawn: Drawn? = null,
)

private enum class Drawn { SHOP_SHORTCUT, HOME_WIDGET }

private val pages = listOf(
    Page(
        "마트별로 따로 정리하세요",
        "쿠팡, 다이소 따로따로, 어디 가서도 헷갈리지 않게",
        R.drawable.onboarding_page1,
    ),
    Page(
        "이름만 적으면 끝나요",
        "가격도 수량도 필요 없어요. 빠르고 심플하게!",
        R.drawable.onboarding_page3,
    ),
    // v1.3 headline feature: 쇼핑몰 앱 바로가기. No screenshot asset yet → drawn in Compose.
    Page(
        "적어둔 그대로 쇼핑 앱에서 검색",
        "항목 옆 🔍를 누르면 쿠팡·컬리 앱에서 바로 찾아줘요. 앱이 없으면 설치 화면으로 가요",
        null,
        drawn = Drawn.SHOP_SHORTCUT,
    ),
    // LAST page on purpose: the widget-or-start choice is rendered on
    // pages.lastIndex, so the widget intro and its two equal buttons stay together.
    // Picture = the widget sitting on a phone home screen (not the size picker).
    Page(
        "생각날 때 바로 구매물품 추가해요",
        "홈 화면 위젯을 누르면 앱을 찾지 않아도 바로 그 마트가 열려요",
        null,
        drawn = Drawn.HOME_WIDGET,
    ),
)

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    var showWidgetHint by remember { mutableStateOf(false) }
    var showSizePicker by remember { mutableStateOf(false) }

    val finish: () -> Unit = {
        viewModel.markSeen()
        onDone()
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.bgPrimary).statusBarsPadding()) {
        // Top right "건너뛰기"
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            if (pagerState.currentPage < pages.lastIndex) {
                Text(
                    text = "건너뛰기",
                    style = typo.bodyS,
                    color = colors.textTertiary,
                    modifier = Modifier.clickable { finish() },
                )
            } else {
                Spacer(Modifier.height(16.dp))
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            val p = pages[page]
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(12.dp))
                // Real in-app screenshot (with sample data) shown in a
                // phone-style framed card. Flexible height (weight) so it
                // scales down on short screens without clipping the text.
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    val frame = Modifier
                        .fillMaxHeight()
                        .aspectRatio(0.495f)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, colors.divider, RoundedCornerShape(24.dp))
                    if (p.illustration != null) {
                        Image(
                            painter = painterResource(p.illustration),
                            contentDescription = p.title,
                            contentScale = ContentScale.Fit,
                            modifier = frame,
                        )
                    } else when (p.drawn) {
                        Drawn.SHOP_SHORTCUT -> ShopShortcutIllustration(frame)
                        Drawn.HOME_WIDGET -> HomeWidgetIllustration(frame)
                        null -> Unit
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    p.title,
                    style = typo.headingL,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    p.subtitle,
                    style = typo.body,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        // Indicator
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(pages.size) { i ->
                val on = i == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .size(if (on) 10.dp else 8.dp)
                        .padding(2.dp)
                        .background(
                            if (on) colors.brandPrimary else colors.textDisabled,
                            CircleShape,
                        ),
                )
                Spacer(Modifier.width(4.dp))
            }
        }

        // Bottom button
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            if (pagerState.currentPage < pages.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .background(colors.brandPrimary, RoundedCornerShape(12.dp))
                        .clickable {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("다음", style = typo.title, color = colors.bgPrimary)
                }
            } else {
                // Last page: two equal choices side by side — add the widget, or just start.
                // (Was a small soft "위젯 추가 (선택)" link above a big 시작하기 — hard to read.)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .background(colors.brandPrimary, RoundedCornerShape(12.dp))
                        // Don't mark onboarding done here: AppRoot swaps to the main screen the
                        // moment it's saved, which killed the size picker before it showed.
                        .clickable { showSizePicker = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("위젯 추가하기", style = typo.title, color = colors.bgPrimary)
                }
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .background(colors.brandPrimary, RoundedCornerShape(12.dp))
                        .clickable(onClick = finish),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("바로 시작하기", style = typo.title, color = colors.bgPrimary)
                }
            }
        }
    }

    if (showSizePicker) {
        WidgetSizePickerSheet(
            onPick = { size ->
                // Pin request sent → the system "홈 화면에 추가" dialog is on top, so it's
                // now safe to finish onboarding (the main screen appears underneath).
                viewModel.pinWidget(size).also { ok -> if (ok) viewModel.markSeen() }
            },
            // Closed without pinning (or after the manual guide) → onboarding is over too.
            onDismiss = { showSizePicker = false; finish() },
        )
    }

    if (showWidgetHint) {
        com.rldjrgo.grocerynote.ui.screens.home.ConfirmDialog(
            title = "위젯 추가 안내",
            message = "홈 화면을 길게 누른 뒤 ‘위젯’ → ‘마트노트’ 를 선택해주세요.",
            confirmLabel = "확인",
            destructive = false,
            onConfirm = { showWidgetHint = false },
            onDismiss = { showWidgetHint = false },
        )
    }
}

/**
 * Drawn stand-in for a screenshot of the 쇼핑몰 앱 바로가기 feature: a 쿠팡 tab with the
 * "쿠팡 앱 바로가기" switch on and 🔍 next to each item, plus a callout on the first 🔍.
 * Mirrors the real Home layout (PageTitle, pill tabs, ItemRow) at a small scale.
 */
@Composable
private fun ShopShortcutIllustration(modifier: Modifier) {
    val colors = AppTheme.colors
    val coupang = Color(0xFF3182F6)
    val small = AppTheme.typography.bodyS.copy(fontSize = 12.sp)
    Column(
        modifier = modifier
            .background(colors.bgPrimary)
            .padding(horizontal = 12.dp, vertical = 16.dp),
    ) {
        Text("구매예정", style = AppTheme.typography.title.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold), color = colors.textPrimary)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.background(coupang, RoundedCornerShape(12.dp)).padding(horizontal = 9.dp, vertical = 4.dp)) {
                Text("🚀 쿠팡 6", style = small.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
            }
            Box(Modifier.border(1.dp, colors.divider, RoundedCornerShape(12.dp)).padding(horizontal = 9.dp, vertical = 4.dp)) {
                Text("🏪 다이소 2", style = small, color = colors.textSecondary)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("쿠팡 앱 바로가기", style = small, color = colors.textSecondary, modifier = Modifier.weight(1f))
            Box(
                Modifier.width(30.dp).height(18.dp).background(colors.brandPrimary, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(Modifier.padding(end = 2.dp).size(14.dp).background(Color.White, CircleShape))
            }
        }
        Spacer(Modifier.height(6.dp))
        listOf("생수 2L", "달걀", "참치캔", "두부", "라면", "키친타월").forEachIndexed { i, name ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
            ) {
                Text(name, style = small.copy(fontSize = 14.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
                Box(
                    Modifier
                        .size(22.dp)
                        .background(coupang.copy(alpha = if (i == 0) 0.28f else 0.12f), CircleShape)
                        .then(if (i == 0) Modifier.border(1.5.dp, coupang, CircleShape) else Modifier),
                    contentAlignment = Alignment.Center,
                ) { Text("🔍", style = small.copy(fontSize = 10.sp)) }
                Spacer(Modifier.width(6.dp))
                Box(Modifier.border(1.dp, colors.divider, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 3.dp)) {
                    Text("완료", style = small.copy(fontSize = 10.sp), color = colors.textSecondary)
                }
            }
            if (i == 0) {
                // Callout: what the 🔍 does.
                Box(
                    Modifier
                        .align(Alignment.End)
                        .background(colors.textPrimary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                ) {
                    Text("쿠팡 앱에서 '생수 2L' 검색", style = small.copy(fontSize = 10.sp), color = colors.bgPrimary)
                }
            }
        }
    }
}

/**
 * Drawn phone home screen with the Medium (4x2) widget placed: wallpaper, app icons,
 * the widget (two marts, colored headers, count pills), dock.
 * Widget styling copies widget/components (WidgetStoreHeader, WidgetColors light).
 */
@Composable
private fun HomeWidgetIllustration(modifier: Modifier) {
    val wall = androidx.compose.ui.graphics.Brush.verticalGradient(
        listOf(Color(0xFF5B7FD6), Color(0xFF8DB2F2), Color(0xFFC9D9F7)),
    )
    val label = AppTheme.typography.bodyS.copy(fontSize = 9.sp)
    Column(
        modifier = modifier
            .background(wall)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("9:41", style = label.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold), color = Color.White)
            Text("▲ ▮ 100%", style = label, color = Color.White)
        }
        Spacer(Modifier.height(14.dp))
        AppIconRow(listOf("전화", "카카오톡", "네이버", "쿠팡"))
        Spacer(Modifier.height(14.dp))
        // The widget
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(10.dp),
        ) {
            WidgetMart(
                modifier = Modifier.weight(1f),
                emoji = "🚀", name = "쿠팡", color = Color(0xFF3182F6),
                items = listOf("생수 2L", "달걀", "라면"), highlight = true,
            )
            Box(Modifier.padding(horizontal = 5.dp).width(1.dp).height(96.dp).background(Color(0xFFF0EEE9)))
            WidgetMart(
                modifier = Modifier.weight(1f),
                emoji = "🏪", name = "다이소", color = Color(0xFFF04452),
                items = listOf("건전지", "지퍼백"), highlight = false,
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .background(Color(0xFF191F28), RoundedCornerShape(8.dp))
                .padding(horizontal = 9.dp, vertical = 5.dp),
        ) {
            Text("마트를 누르면 바로 그 목록이 열려요", style = label.copy(fontSize = 10.sp), color = Color.White)
        }
        Spacer(Modifier.height(14.dp))
        AppIconRow(listOf("🛒 마트노트", "유튜브", "갤러리", "설정"))
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            repeat(4) { Box(Modifier.size(30.dp).background(Color.White.copy(alpha = 0.32f), RoundedCornerShape(9.dp))) }
        }
    }
}

@Composable
private fun AppIconRow(names: List<String>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        names.forEach { n ->
            val isOurs = n.startsWith("🛒")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(30.dp).background(
                        if (isOurs) Color.White else Color.White.copy(alpha = 0.32f),
                        RoundedCornerShape(9.dp),
                    ),
                    contentAlignment = Alignment.Center,
                ) { if (isOurs) Text("🛒", style = AppTheme.typography.bodyS.copy(fontSize = 15.sp)) }
                Spacer(Modifier.height(3.dp))
                Text(n.removePrefix("🛒 "), style = AppTheme.typography.bodyS.copy(fontSize = 8.sp), color = Color.White)
            }
        }
    }
}

@Composable
private fun WidgetMart(
    modifier: Modifier,
    emoji: String,
    name: String,
    color: Color,
    items: List<String>,
    highlight: Boolean,
) {
    val t = AppTheme.typography.bodyS
    // highlight = the mart the callout points at (soft tint + outline, "tap here").
    Column(
        modifier
            .then(if (highlight) Modifier.background(color.copy(alpha = 0.06f), RoundedCornerShape(10.dp)).border(1.5.dp, color.copy(alpha = 0.6f), RoundedCornerShape(10.dp)) else Modifier)
            .padding(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(18.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.Center,
            ) { Text(emoji, style = t.copy(fontSize = 10.sp)) }
            Spacer(Modifier.width(5.dp))
            Text(name, style = t.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium), color = Color(0xFF191F28), maxLines = 1, softWrap = false, modifier = Modifier.weight(1f))
            // Fixed 20×14 pill (wider than tall) like the real widget's count pill —
            // padding-only sizing made a single digit render as a tall rectangle.
            Box(
                Modifier
                    .width(20.dp)
                    .height(14.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    items.size.toString(),
                    style = t.copy(fontSize = 9.sp, lineHeight = 9.sp, fontWeight = FontWeight.Medium),
                    color = color,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0EEE9)))
        Spacer(Modifier.height(5.dp))
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                Box(Modifier.size(4.dp).background(Color(0xFFD5D2CB), CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(item, style = t.copy(fontSize = 10.sp), color = Color(0xFF191F28))
            }
        }
    }
}