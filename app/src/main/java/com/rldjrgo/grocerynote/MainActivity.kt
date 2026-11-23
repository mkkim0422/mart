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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.rldjrgo.grocerynote.data.local.DarkModePref
import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import com.rldjrgo.grocerynote.ui.components.BottomNavBar
import com.rldjrgo.grocerynote.ui.navigation.AppNavHost
import com.rldjrgo.grocerynote.notify.NudgeReceiver
import kotlinx.coroutines.launch
import com.rldjrgo.grocerynote.ui.navigation.Routes
import com.rldjrgo.grocerynote.ui.navigation.currentRouteOrNull
import com.rldjrgo.grocerynote.ui.screens.onboarding.OnboardingScreen
import com.rldjrgo.grocerynote.ui.theme.AppTheme
import com.rldjrgo.grocerynote.ui.theme.LocalAppColors
import com.rldjrgo.grocerynote.util.Analytics
import com.rldjrgo.grocerynote.util.SharedTextParser
import com.rldjrgo.grocerynote.util.WidgetUpdater
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settings: SettingsDataStore
    @Inject lateinit var widgetUpdater: WidgetUpdater

    /**
     * User is leaving the app → the next thing they see is the home screen.
     * Push one more render now, while the process is still alive, so the
     * widget count matches whatever they just did in the app. Cheap: no-op if
     * no widget is placed, and if a widget session is already live it only
     * refreshes Glance state (the session's own Flow already has the data).
     */
    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) widgetUpdater.updateAll()
    }

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
        debugRunNudge(intent)
        setContent {
            val darkPref by settings.darkMode.collectAsStateWithLifecycle(initialValue = DarkModePref.Off)
            val system = isSystemInDarkTheme()
            val dark = when (darkPref) {
                DarkModePref.Auto -> system
                DarkModePref.On -> true
                DarkModePref.Off -> false
            }
            AppTheme(darkTheme = dark) {
                // Status/nav-bar icon color must follow the APP theme, not the
                // system one — enableEdgeToEdge's auto style reads only the
                // system setting, so in-app dark mode left the clock/icons dark
                // on a dark background (invisible).
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !dark
                        isAppearanceLightNavigationBars = !dark
                    }
                }
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
        debugRunNudge(intent)
    }

    /**
     * 디버그 빌드 전용 수동 시험: `adb shell am start -n <pkg>/.MainActivity --es debug_nudge_slot MORNING|EVENING`.
     * Android 14+는 adb가 비공개 리시버로 브로드캐스트를 못 보내서(not exported) 이 경로로 같은 코드를 돌린다.
     * 릴리스 빌드에서는 아무 일도 하지 않는다.
     */
    private fun debugRunNudge(intent: Intent?) {
        if (!BuildConfig.DEBUG) return
        val slotName = intent?.getStringExtra("debug_nudge_slot") ?: return
        intent.removeExtra("debug_nudge_slot")
        val slot = runCatching { com.rldjrgo.grocerynote.notify.NudgeSlot.valueOf(slotName) }.getOrNull() ?: return
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            runCatching { com.rldjrgo.grocerynote.notify.NudgeRunner.run(applicationContext, slot, rearm = false) }
                .onFailure { android.util.Log.w("Nudge", "debug run failed", it) }
        }
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
        /** 시즌 알림 탭 → 요리별 재료 탭에서 이 요리를 연다 (RecipesScreen이 소비). */
        val recipeName: String? = null,
        /** Items from a received list link (martnote://add) → straight to SharedItemsSheet. */
        val linkItems: List<String>? = null,
        /** Mart name the sender's list came from — preselected if the receiver has it. */
        val linkStoreName: String? = null,
        /** Sender's mart color "#RRGGBB" / emoji — used if the receiver creates that mart. */
        val linkStoreColor: String? = null,
        val linkStoreEmoji: String? = null,
    )
    val flow = MutableStateFlow<Payload?>(null)

    fun consume(intent: Intent?) {
        if (intent == null) return
        // 리텐션 알림을 눌러서 열림 → 집계 후 extra 제거(재생성 때 중복 집계 방지).
        intent.getStringExtra(NudgeReceiver.EXTRA_NUDGE_TYPE)?.let { t ->
            Analytics.nudgeOpened(t)
            intent.removeExtra(NudgeReceiver.EXTRA_NUDGE_TYPE)
        }
        intent.getStringExtra(NudgeReceiver.EXTRA_RECIPE_NAME)?.let { name ->
            intent.removeExtra(NudgeReceiver.EXTRA_RECIPE_NAME)
            flow.value = Payload(recipeName = name)
            return
        }
        // Share sheet (카톡 등 텍스트 공유) → 한 줄이면 추가 시트 프리필,
        // 여러 줄이면 HomeScreen이 SharedItemsSheet로 줄마다 항목을 만든다.
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()?.take(2000)
            if (!text.isNullOrEmpty()) {
                flow.value = Payload(openAddItem = true, sharedText = text)
                return
            }
        }
        // 받은 목록 링크 (1.5.0): 가족이 보낸 공유 메시지의 링크 → 목록 페이지 → 여기.
        val data = intent.data
        if (intent.action == Intent.ACTION_VIEW && data?.scheme == "martnote" && data.host == "add") {
            val names = SharedTextParser.parse(
                runCatching { data.getQueryParameters("i") }.getOrDefault(emptyList()).joinToString("\n"),
            )
            if (names.isNotEmpty()) {
                Analytics.listLinkOpened(names.size)
                flow.value = Payload(
                    openAddItem = true,
                    linkItems = names,
                    linkStoreName = data.getQueryParameter("s")?.trim()?.take(30),
                    linkStoreColor = data.getQueryParameter("c")
                        ?.takeIf { Regex("[0-9A-Fa-f]{6}").matches(it) }?.let { "#$it" },
                    linkStoreEmoji = data.getQueryParameter("e")?.trim()?.take(8)?.takeIf { it.isNotEmpty() },
                )
            }
            return
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
        else -> MainScaffold(settings)
    }
}

@Composable
private fun MainScaffold(settings: SettingsDataStore) {
    val navController = rememberNavController()
    val current = navController.currentRouteOrNull()

    // "요리별 재료" (new in 1.6.0): a small dot on its tab until it has been opened once —
    // people who update never see onboarding again, so this is the only quiet hint.
    val recipesOpened by settings.hasOpenedRecipes.collectAsState(initial = true)
    LaunchedEffect(current) { if (current == Routes.RECIPES) settings.setRecipesOpened() }

    // A widget deep-link can arrive while the app sits on Settings/Completed.
    // HomeScreen only reacts when it's composed, so route to Home FIRST here
    // (don't clear the bus — HomeScreen still consumes it to select the mart).
    LaunchedEffect(Unit) {
        DeepLinkBus.flow.collect { payload ->
            if (payload == null) return@collect
            val route = navController.currentDestination?.route
            // 시즌 알림은 요리별 재료 탭으로, 나머지는 홈으로.
            val target = if (payload.recipeName != null) Routes.RECIPES else Routes.HOME
            if (route != null && route != target) {
                navController.navigate(target) {
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
                    dotRoutes = if (recipesOpened) emptySet() else setOf(Routes.RECIPES),
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
