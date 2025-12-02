package com.nkds.hosikoouma.nouma

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nkds.hosikoouma.nouma.data.local.AppDatabase
import com.nkds.hosikoouma.nouma.data.repository.AuthRepository
import com.nkds.hosikoouma.nouma.data.repository.ChatRepository
import com.nkds.hosikoouma.nouma.features.auth.*
import com.nkds.hosikoouma.nouma.features.main.MainScreen
import com.nkds.hosikoouma.nouma.features.settings.SettingsViewModel
import com.nkds.hosikoouma.nouma.features.welcome.WelcomeScreen
import com.nkds.hosikoouma.nouma.utils.SessionManager

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
        startDestination = startDestination
    ) {
        composable(AppDestinations.WELCOME_ROUTE) { 
            WelcomeScreen(onNextClick = { navController.navigate(AppDestinations.AUTH_ROUTE) }) 
        }
        composable(AppDestinations.AUTH_ROUTE) { 
            AuthScreen(onLoginClick = { navController.navigate(AppDestinations.LOGIN_ROUTE) }, onRegisterClick = { navController.navigate(AppDestinations.REGISTER_ROUTE) }) 
        }
        composable(AppDestinations.LOGIN_ROUTE) { 
            LoginScreen(authViewModel = authViewModel, onLoginSuccess = { navController.navigate(AppDestinations.MAIN_ROUTE) { popUpTo(AppDestinations.WELCOME_ROUTE) { inclusive = true } } }) 
        }
        composable(AppDestinations.REGISTER_ROUTE) { 
            RegisterScreen(authViewModel = authViewModel, onRegisterSuccess = { navController.navigate(AppDestinations.LOGIN_ROUTE) { popUpTo(AppDestinations.REGISTER_ROUTE) { inclusive = true } } }) 
        }
        composable(AppDestinations.MAIN_ROUTE) {
            MainScreen(
                onLogoutClick = { 
                    authViewModel.logout()
                    navController.navigate(AppDestinations.WELCOME_ROUTE) { 
                        popUpTo(AppDestinations.MAIN_ROUTE) { inclusive = true } 
                    }
                },
                settingsViewModel = settingsViewModel
            )
        }
    }
}
