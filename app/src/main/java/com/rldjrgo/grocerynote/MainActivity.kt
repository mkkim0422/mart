package com.rldjrgo.grocerynote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.rldjrgo.grocerynote.data.local.DarkModePref
import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import com.rldjrgo.grocerynote.ui.components.BottomNavBar
import com.rldjrgo.grocerynote.ui.navigation.AppNavHost
import com.rldjrgo.grocerynote.ui.navigation.Routes
import com.rldjrgo.grocerynote.ui.navigation.currentRouteOrNull
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.ui.theme.LocalAppColors
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settings: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            )
        )
        DeepLinkBus.consume(intent)
        setContent {
            val darkPref by settings.darkMode.collectAsStateWithLifecycle(initialValue = DarkModePref.Auto)
            val system = isSystemInDarkTheme()
            val dark = when (darkPref) {
                DarkModePref.Auto -> system
                DarkModePref.On -> true
                DarkModePref.Off -> false
            }
            AppTheme(darkTheme = dark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LocalAppColors.current.bgPrimary,
                ) {
                    AppRoot(settings = settings)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        DeepLinkBus.consume(intent)
    }
}

/**
 * Tiny global bus for widget→app deep links. HomeScreen reads this on launch.
 * Pre-Hilt lifecycle so widget intents can land before ViewModels exist.
 */
object DeepLinkBus {
    data class Payload(val storeId: Long?, val itemId: Long?)
    val flow = MutableStateFlow<Payload?>(null)

    fun consume(intent: Intent?) {
        if (intent == null) return
        val storeId = intent.getLongExtra(Routes.HOME_DEEPLINK_STORE_ARG, -1L).takeIf { it > 0 }
        val itemId = intent.getLongExtra(Routes.HOME_DEEPLINK_ITEM_ARG, -1L).takeIf { it > 0 }
        if (storeId != null || itemId != null) flow.value = Payload(storeId, itemId)
    }
}

@Composable
private fun AppRoot(settings: SettingsDataStore) {
    val navController = rememberNavController()
    val current = navController.currentRouteOrNull()
    val hasSeen by settings.hasSeenOnboarding.collectAsState(initial = true)
    val start = if (hasSeen) Routes.HOME else Routes.ONBOARDING

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppTheme.colors.bgPrimary,
        bottomBar = {
            if (current != Routes.ONBOARDING) {
                BottomNavBar(
                    currentRoute = current,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        AppNavHost(
            navController = navController,
            startDestination = start,
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding()),
            onOnboardingComplete = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            },
        )
    }
}
