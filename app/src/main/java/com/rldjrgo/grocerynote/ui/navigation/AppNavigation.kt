package com.rldjrgo.grocerynote.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rldjrgo.grocerynote.ui.screens.completed.CompletedScreen
import com.rldjrgo.grocerynote.ui.screens.home.HomeScreen
import com.rldjrgo.grocerynote.ui.screens.onboarding.OnboardingScreen
import com.rldjrgo.grocerynote.ui.screens.settings.SettingsScreen
import com.rldjrgo.grocerynote.ui.screens.store.StoreManageScreen

private const val ANIM_MS = 300

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    contentPadding: PaddingValues,
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.padding(contentPadding),
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_MS))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_MS))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_MS))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_MS))
        },
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onDone = onOnboardingComplete)
        }
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
