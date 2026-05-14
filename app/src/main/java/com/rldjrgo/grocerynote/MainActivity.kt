package com.rldjrgo.grocerynote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.ui.theme.LocalAppColors
import com.rldjrgo.grocerynote.ui.theme.LocalAppTypography
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            )
        )
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LocalAppColors.current.bgPrimary,
                ) {
                    Phase1Placeholder()
                }
            }
        }
    }
}

@Composable
private fun Phase1Placeholder() {
    val colors = LocalAppColors.current
    val typo = LocalAppTypography.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 96.dp)
                .size(56.dp)
                .background(colors.brandPrimary, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
        )
        Text(
            text = "장보기 메모",
            style = typo.headingL,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            text = "Phase 1: 디자인 시스템 검증 화면",
            style = typo.body,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "곧 마트별 리스트가 여기에 표시됩니다.",
            style = typo.bodyS,
            color = colors.textTertiary,
            modifier = Modifier.padding(top = 4.dp),
        )

        Box(
            modifier = Modifier
                .padding(top = 48.dp)
                .size(width = 280.dp, height = 1.dp)
                .background(colors.divider),
        )

        // 토스 컬러 팔레트 미리보기 — 8개 마트 컬러
        Column(
            modifier = Modifier.padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "마트 컬러 팔레트",
                style = typo.title,
                color = colors.textPrimary,
            )
            val martColors = listOf(
                Color(0xFF3182F6),
                Color(0xFFFFB800),
                Color(0xFFF04452),
                Color(0xFF2BA471),
                Color(0xFF8B5CF6),
                Color(0xFFF564A9),
                Color(0xFFFF8A3D),
                Color(0xFF6B7684),
            )
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            ) {
                martColors.forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(c, shape = androidx.compose.foundation.shape.CircleShape),
                    )
                }
            }
        }

        Text(
            text = "v1.0.0 · com.rldjrgo.grocerynote",
            style = typo.caption,
            color = colors.textTertiary,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(top = 64.dp),
        )
    }
}
