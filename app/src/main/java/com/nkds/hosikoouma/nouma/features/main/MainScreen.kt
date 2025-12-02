package com.nkds.hosikoouma.nouma.features.main

import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nkds.hosikoouma.nouma.R
import com.nkds.hosikoouma.nouma.data.local.AppDatabase
import com.nkds.hosikoouma.nouma.data.repository.ChatRepository
import com.nkds.hosikoouma.nouma.data.repository.UserRepository
import com.nkds.hosikoouma.nouma.features.chats.ChatsScreen
import com.nkds.hosikoouma.nouma.features.chats.ChatsViewModel
import com.nkds.hosikoouma.nouma.features.chats.ChatsViewModelFactory
import com.nkds.hosikoouma.nouma.features.contacts.ContactsScreen
import com.nkds.hosikoouma.nouma.features.conversation.ConversationRepository
import com.nkds.hosikoouma.nouma.features.conversation.ConversationScreen
import com.nkds.hosikoouma.nouma.features.conversation.ConversationViewModelFactory
import com.nkds.hosikoouma.nouma.features.media.MediaViewerScreen
import com.nkds.hosikoouma.nouma.features.media.MediaViewerViewModelFactory
import com.nkds.hosikoouma.nouma.features.profile.ProfileScreen
import com.nkds.hosikoouma.nouma.features.profile.ProfileViewModelFactory
import com.nkds.hosikoouma.nouma.features.settings.SettingsScreen
import com.nkds.hosikoouma.nouma.features.settings.SettingsViewModel
import com.nkds.hosikoouma.nouma.utils.SessionManager
import com.nkds.hosikoouma.nouma.utils.performVibration
import androidx.compose.foundation.layout.WindowInsets // <-- Не забудьте импортировать
import androidx.compose.foundation.layout.consumeWindowInsets // <-- И это


sealed class BottomBarScreen(val route: String, @StringRes val titleResId: Int, val icon: ImageVector) {
    object Contacts : BottomBarScreen("contacts", R.string.bottom_nav_contacts, Icons.Default.Contacts)
    object Chats : BottomBarScreen("chats", R.string.bottom_nav_chats, Icons.Default.Chat)
    object Settings : BottomBarScreen("settings", R.string.bottom_nav_settings, Icons.Default.Settings)
}

object MainDestinations {
    const val CONVERSATION_ROUTE = "conversation"
    const val MEDIA_VIEWER_ROUTE = "media_viewer"
    const val PROFILE_ROUTE = "profile"
}

val bottomBarScreens = listOf(
    BottomBarScreen.Contacts,
    BottomBarScreen.Chats,
    BottomBarScreen.Settings,
)

@Composable
fun MainScreen(
    onLogoutClick: () -> Unit,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val db = AppDatabase.getDatabase(context)
    val sessionManager = SessionManager(context)
    val chatRepository = ChatRepository(db.userDao(), db.chatDao(), db.messageDao())
    val conversationRepository = ConversationRepository(db.messageDao(), db.chatDao())
    val userRepository = UserRepository(db.userDao())

    val chatsViewModel: ChatsViewModel = viewModel(
        factory = ChatsViewModelFactory(chatRepository, sessionManager)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val shouldShowBottomBar = bottomBarScreens.any { it.route == currentDestination?.route }

    Scaffold(
        //contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    tonalElevation = 0.dp
                            //containerColor = MaterialTheme.colorScheme.background
                ) { 
                    bottomBarScreens.forEach { screen ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                performVibration(context)
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = stringResource(screen.titleResId)) },
                            label = { Text(stringResource(screen.titleResId)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomBarScreen.Chats.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = BottomBarScreen.Contacts.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) { ContactsScreen() }
            composable(
                route = BottomBarScreen.Chats.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) { ChatsScreen(chatsViewModel = chatsViewModel, onChatClick = { chatId -> navController.navigate("${MainDestinations.CONVERSATION_ROUTE}/$chatId") }) }
            composable(
                route = BottomBarScreen.Settings.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) { SettingsScreen(settingsViewModel = settingsViewModel, onLogoutClick = onLogoutClick) }
            composable(
                route = "${MainDestinations.CONVERSATION_ROUTE}/{chatId}",
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: "-1"
                ConversationScreen(
                    viewModel = viewModel(factory = ConversationViewModelFactory(conversationRepository, sessionManager)),
                    chatId = chatId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMedia = { cId, mId ->
                        navController.navigate("${MainDestinations.MEDIA_VIEWER_ROUTE}/$cId/$mId")
                    },
                    onNavigateToProfile = { userId ->
                        navController.navigate("${MainDestinations.PROFILE_ROUTE}/$userId")
                    }
                )
            }
            composable(
                route = "${MainDestinations.MEDIA_VIEWER_ROUTE}/{chatId}/{messageId}",
                arguments = listOf(
                    navArgument("chatId") { type = NavType.StringType },
                    navArgument("messageId") { type = NavType.StringType }
                ),
                popExitTransition = { fadeOut(animationSpec = tween(0)) }
            ) { 
                MediaViewerScreen(
                    viewModel = viewModel(factory = MediaViewerViewModelFactory(conversationRepository)),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "${MainDestinations.PROFILE_ROUTE}/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { 
                ProfileScreen(
                    viewModel = viewModel(factory = ProfileViewModelFactory(userRepository)),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
