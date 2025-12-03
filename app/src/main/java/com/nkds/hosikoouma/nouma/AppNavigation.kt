package com.nkds.hosikoouma.nouma

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.imageLoader
import com.nkds.hosikoouma.nouma.data.local.AppDatabase
import com.nkds.hosikoouma.nouma.data.repository.AuthRepository
import com.nkds.hosikoouma.nouma.data.repository.ChatRepository
import com.nkds.hosikoouma.nouma.features.auth.*
import com.nkds.hosikoouma.nouma.features.main.MainScreen
import com.nkds.hosikoouma.nouma.features.settings.SettingsViewModel
import com.nkds.hosikoouma.nouma.features.welcome.WelcomeScreen
import com.nkds.hosikoouma.nouma.utils.SessionManager
import kotlinx.coroutines.launch

object AppDestinations {
    const val WELCOME_ROUTE = "welcome"
    const val AUTH_ROUTE = "auth"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val MAIN_ROUTE = "main"
}

@Composable
fun AppNavigation(settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val db = AppDatabase.getDatabase(context)
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            AuthRepository(db.userDao()),
            ChatRepository(db.userDao(), db.chatDao(), db.messageDao()),
            sessionManager
        )
    )

    val startDestination = if (sessionManager.isLoggedIn()) AppDestinations.MAIN_ROUTE else AppDestinations.WELCOME_ROUTE

    NavHost(
        navController = navController, 
        startDestination = startDestination,
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        val animationSpec: FiniteAnimationSpec<IntOffset> = tween(300)

        val enterTransition = slideInHorizontally(animationSpec = animationSpec) { fullWidth -> fullWidth }
        val exitTransition = slideOutHorizontally(animationSpec = animationSpec) { fullWidth -> -fullWidth }
        val popEnterTransition = slideInHorizontally(animationSpec = animationSpec) { fullWidth -> -fullWidth }
        val popExitTransition = slideOutHorizontally(animationSpec = animationSpec) { fullWidth -> fullWidth }

        composable(
            route = AppDestinations.WELCOME_ROUTE,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) { 
            WelcomeScreen(onNextClick = { navController.navigate(AppDestinations.AUTH_ROUTE) }) 
        }
        composable(
            route = AppDestinations.AUTH_ROUTE,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) { 
            AuthScreen(onLoginClick = { navController.navigate(AppDestinations.LOGIN_ROUTE) }, onRegisterClick = { navController.navigate(AppDestinations.REGISTER_ROUTE) }) 
        }
        composable(
            route = AppDestinations.LOGIN_ROUTE,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) { 
            LoginScreen(authViewModel = authViewModel, onLoginSuccess = { navController.navigate(AppDestinations.MAIN_ROUTE) { popUpTo(AppDestinations.WELCOME_ROUTE) { inclusive = true } } }) 
        }
        composable(
            route = AppDestinations.REGISTER_ROUTE,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) { 
            RegisterScreen(authViewModel = authViewModel, onRegisterSuccess = { navController.navigate(AppDestinations.LOGIN_ROUTE) { popUpTo(AppDestinations.REGISTER_ROUTE) { inclusive = true } } }) 
        }
        composable(
            route = AppDestinations.MAIN_ROUTE,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) {
            MainScreen(
                onLogoutClick = { 
                    authViewModel.logout()
                    settingsViewModel.onLogout()
                    val imageLoader = context.imageLoader
                    imageLoader.memoryCache?.clear()
                    scope.launch { imageLoader.diskCache?.clear() }

                    navController.navigate(AppDestinations.WELCOME_ROUTE) { 
                        popUpTo(AppDestinations.MAIN_ROUTE) { inclusive = true } 
                    }
                },
                settingsViewModel = settingsViewModel
            )
        }
    }
}
