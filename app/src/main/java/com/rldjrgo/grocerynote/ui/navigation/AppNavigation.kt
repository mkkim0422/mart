package com.rldjrgo.grocerynote.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rldjrgo.grocerynote.ui.screens.completed.CompletedScreen
import com.rldjrgo.grocerynote.ui.screens.home.HomeScreen
import com.rldjrgo.grocerynote.ui.screens.settings.SettingsScreen
import com.rldjrgo.grocerynote.ui.screens.store.StoreManageScreen

private const val ANIM_MS = 300

/** Left→right order of the bottom-nav tabs. Slide direction is decided by the
 *  relative position of the two tabs, NOT by Compose's forward/pop guess —
 *  otherwise (with popUpTo+restoreState) 설정→완료 reads as "forward" and slides
 *  the wrong way vs 완료→구매예정. Going to a tab on the right slides like
 *  "next" (Left); to a tab on the left slides like "back" (Right). */
private val TAB_ORDER = listOf(Routes.HOME, Routes.COMPLETED, Routes.SETTINGS)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabDir(
    forwardDefault: Boolean,
): SlideDirection {
    val f = TAB_ORDER.indexOf(initialState.destination.route)
    val t = TAB_ORDER.indexOf(targetState.destination.route)
    return if (f >= 0 && t >= 0) {
        if (t >= f) SlideDirection.Left else SlideDirection.Right
    } else {
        // Non-tab route (e.g. STORE_MANAGE push/pop): keep normal push=Left,
        // back=Right behaviour.
        if (forwardDefault) SlideDirection.Left else SlideDirection.Right
    }
}

/**
 * Tab NavHost. Onboarding is NOT here — it lives outside (MainActivity.AppRoot)
 * so the graph's start destination is always HOME. That keeps bottom-nav
 * popUpTo(startDestinationId) pointed at a real, present destination (HOME).
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier.padding(contentPadding),
        enterTransition = { slideIntoContainer(tabDir(forwardDefault = true), tween(ANIM_MS)) },
        exitTransition = { slideOutOfContainer(tabDir(forwardDefault = true), tween(ANIM_MS)) },
        popEnterTransition = { slideIntoContainer(tabDir(forwardDefault = false), tween(ANIM_MS)) },
        popExitTransition = { slideOutOfContainer(tabDir(forwardDefault = false), tween(ANIM_MS)) },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onManageStores = { navController.navigate(Routes.STORE_MANAGE) },
            )
        }
        composable(Routes.COMPLETED) {
            CompletedScreen()
        }
        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
        composable(Routes.STORE_MANAGE) {
            StoreManageScreen(onClose = { navController.popBackStack() })
        }
    }
}

@Composable
fun NavHostController.currentRouteOrNull(): String? {
    val entry by currentBackStackEntryAsState()
    return entry?.destination?.route
}
