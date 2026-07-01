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
import com.rldjrgo.grocerynote.ui.screens.onboarding.OnboardingScreen
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.ui.theme.LocalAppColors
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settings: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Keep the system splash up until we know onboarding-vs-Home. The
        // DataStore read is async; without this NavHost starts at Home for a
        // frame and Home flashes before onboarding on first launch.
        var keepSplash = true
        splash.setKeepOnScreenCondition { keepSplash }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            )
        )
        DeepLinkBus.consume(intent)
        setContent {
            val darkPref by settings.darkMode.collectAsStateWithLifecycle(initialValue = DarkModePref.Off)
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
                    AppRoot(settings = settings, onResolved = { keepSplash = false })
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
    data class Payload(
        val storeId: Long? = null,
        val itemId: Long? = null,
        val openAddItem: Boolean = false,
        val sharedText: String? = null,
    )
    val flow = MutableStateFlow<Payload?>(null)

    fun consume(intent: Intent?) {
        if (intent == null) return
        // Share sheet (카톡 등 텍스트 공유) → 항목 추가 시트 프리필
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()?.take(100)
            if (!text.isNullOrEmpty()) {
                flow.value = Payload(openAddItem = true, sharedText = text)
                return
            }
        }
        // Widget "+" button / App Shortcut → 해당 마트로 추가 시트 자동
        val action = intent.getStringExtra("action")
        if (action == "ADD_ITEM") {
            val sid = intent.getLongExtra("store_id", -1L).takeIf { it > 0 }
            flow.value = Payload(storeId = sid, openAddItem = true)
            return
        }
        // Empty-widget tap → just force-route to Home (no mart/item to select).
        if (action == "OPEN_HOME") {
            flow.value = Payload()
            return
        }
        val storeId = intent.getLongExtra(Routes.HOME_DEEPLINK_STORE_ARG, -1L).takeIf { it > 0 }
        val itemId = intent.getLongExtra(Routes.HOME_DEEPLINK_ITEM_ARG, -1L).takeIf { it > 0 }
        if (storeId != null || itemId != null) flow.value = Payload(storeId, itemId)
    }
}

@Composable
private fun AppRoot(settings: SettingsDataStore, onResolved: () -> Unit) {
    // null = DataStore not read yet → keep the splash up (no flash).
    val hasSeen by settings.hasSeenOnboarding.collectAsState(initial = null)
    LaunchedEffect(hasSeen) { if (hasSeen != null) onResolved() }
    when (hasSeen) {
        null -> Unit // splash still held; draw nothing
        // Onboarding lives OUTSIDE the tab NavHost. OnboardingScreen calls
        // settings.setOnboardingSeen() on finish → hasSeen flips true → this
        // recomposes to the main scaffold. That's a plain composable swap:
        // instant, NO nav slide/fade. It also keeps the tab NavHost's
        // startDestination always HOME, so bottom-nav popUpTo() is never
        // pointed at a popped ONBOARDING (the cause of the weird tab swipe).
        false -> OnboardingScreen(onDone = {})
        else -> MainScaffold()
    }
}

@Composable
private fun MainScaffold() {
    val navController = rememberNavController()
    val current = navController.currentRouteOrNull()

    // A widget deep-link can arrive while the app sits on Settings/Completed.
    // HomeScreen only reacts when it's composed, so route to Home FIRST here
    // (don't clear the bus — HomeScreen still consumes it to select the mart).
    LaunchedEffect(Unit) {
        DeepLinkBus.flow.collect { payload ->
            if (payload == null) return@collect
            val route = navController.currentDestination?.route
            if (route != null && route != Routes.HOME) {
                navController.navigate(Routes.HOME) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppTheme.colors.bgPrimary,
        bottomBar = {
            if (current != Routes.STORE_MANAGE) {
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
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding()),
        )
    }
}
