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
import androidx.compose.ui.unit.dp
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
    @param:DrawableRes val illustration: Int,
)

private val pages = listOf(
    Page(
        "마트별로 따로 정리하세요",
        "쿠팡, 다이소 따로따로, 어디 가서도 헷갈리지 않게",
        R.drawable.onboarding_page1,
    ),
    Page(
        "위젯으로 한눈에 확인",
        "마트 가기 전에 홈화면에서 바로 확인, 탭하면 그 마트 리스트로 이어져요",
        R.drawable.onboarding_page2,
    ),
    Page(
        "이름만 적으면 끝",
        "가격도 수량도 필요 없어요. 빠르고 심플하게",
        R.drawable.onboarding_page3,
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
                    Image(
                        painter = painterResource(p.illustration),
                        contentDescription = p.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(0.495f)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, colors.divider, RoundedCornerShape(24.dp)),
                    )
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
                if (page == pages.lastIndex) {
                    Spacer(Modifier.height(20.dp))
                    // Secondary (soft) style — the solid-blue "시작하기" below is
                    // the primary action; this optional widget shortcut must NOT
                    // compete with it visually (two identical blue buttons read
                    // as a conflict — which do I press?).
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(52.dp)
                            .background(colors.brandPrimarySoft, RoundedCornerShape(12.dp))
                            .clickable {
                                showSizePicker = true
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("홈 화면에 위젯 추가 (선택)", style = typo.title, color = colors.brandPrimary)
                    }
                }
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
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .background(colors.brandPrimary, RoundedCornerShape(12.dp))
                        .clickable(onClick = finish),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("시작하기", style = typo.title, color = colors.bgPrimary)
                }
            }
        }
    }

    if (showSizePicker) {
        WidgetSizePickerSheet(
            onPick = viewModel::pinWidget,
            onDismiss = { showSizePicker = false },
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
