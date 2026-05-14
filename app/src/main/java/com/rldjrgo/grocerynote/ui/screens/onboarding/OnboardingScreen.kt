package com.rldjrgo.grocerynote.ui.screens.onboarding

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Widgets
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.rldjrgo.grocerynote.widget.GroceryWidgetReceiver
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settings: com.rldjrgo.grocerynote.data.local.SettingsDataStore,
) : androidx.lifecycle.ViewModel() {
    fun markSeen() {
        viewModelScope.launch {
            settings.setOnboardingSeen()
        }
    }
}

private data class Page(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)

private val pages = listOf(
    Page(
        "마트별로 따로 적어두세요",
        "이마트, 다이소, 쿠팡… 한 번에 정리",
        Icons.Outlined.Storefront,
    ),
    Page(
        "홈화면 위젯에서 바로 체크",
        "앱 안 열고도 한 번에",
        Icons.Outlined.Widgets,
    ),
    Page(
        "심플하게, 이름만 적으면 끝",
        "복잡한 정보 없이, 빠르게",
        Icons.AutoMirrored.Outlined.Assignment,
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

    val finish: () -> Unit = {
        viewModel.markSeen()
        onDone()
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.bgPrimary)) {
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
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = p.icon,
                    contentDescription = null,
                    tint = colors.brandPrimary,
                    modifier = Modifier.size(96.dp),
                )
                Spacer(Modifier.height(24.dp))
                Text(p.title, style = typo.headingL, color = colors.textPrimary)
                Spacer(Modifier.height(8.dp))
                Text(p.subtitle, style = typo.body, color = colors.textSecondary)
                if (page == pages.lastIndex) {
                    Spacer(Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(48.dp)
                            .background(colors.bgTertiary, RoundedCornerShape(12.dp))
                            .clickable {
                                val ok = tryPinWidget(context)
                                if (!ok) showWidgetHint = true
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("위젯 추가하기", style = typo.title, color = colors.textSecondary)
                    }
                }
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

    if (showWidgetHint) {
        com.rldjrgo.grocerynote.ui.screens.home.ConfirmDialog(
            title = "위젯 추가 안내",
            message = "홈 화면을 길게 누른 뒤 ‘위젯’ → ‘장보기 메모’ 를 선택해주세요.",
            confirmLabel = "확인",
            destructive = false,
            onConfirm = { showWidgetHint = false },
            onDismiss = { showWidgetHint = false },
        )
    }
}

private fun tryPinWidget(context: android.content.Context): Boolean {
    val mgr = AppWidgetManager.getInstance(context)
    if (!mgr.isRequestPinAppWidgetSupported) return false
    val provider = ComponentName(context, GroceryWidgetReceiver::class.java)
    val callback = PendingIntent.getBroadcast(
        context, 0, Intent(),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    return runCatching { mgr.requestPinAppWidget(provider, null, callback) }.getOrDefault(false)
}
