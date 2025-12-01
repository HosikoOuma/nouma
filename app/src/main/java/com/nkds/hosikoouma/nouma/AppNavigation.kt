package com.nkds.hosikoouma.nouma

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nkds.hosikoouma.nouma.features.auth.AuthScreen
import com.nkds.hosikoouma.nouma.features.auth.LoginScreen
import com.nkds.hosikoouma.nouma.features.auth.RegisterScreen
import com.nkds.hosikoouma.nouma.features.welcome.WelcomeScreen

object AppDestinations {
    const val WELCOME_ROUTE = "welcome"
    const val AUTH_ROUTE = "auth"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppDestinations.WELCOME_ROUTE) {
        composable(
            route = AppDestinations.WELCOME_ROUTE,
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) }
        ) {
            WelcomeScreen(
                onNextClick = { navController.navigate(AppDestinations.AUTH_ROUTE) }
            )
        }
        composable(
            route = AppDestinations.AUTH_ROUTE,
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) }
        ) {
            AuthScreen(
                onLoginClick = { navController.navigate(AppDestinations.LOGIN_ROUTE) },
                onRegisterClick = { navController.navigate(AppDestinations.REGISTER_ROUTE) }
            )
        }
        composable(
            route = AppDestinations.LOGIN_ROUTE,
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) }
        ) {
            LoginScreen()
        }
        composable(
            route = AppDestinations.REGISTER_ROUTE,
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) }
        ) {
            RegisterScreen()
        }
    }
}
