package com.nkds.hosikoouma.nouma.features.main

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val selectedChatIds by chatsViewModel.selectedChatIds.collectAsState()
    val isChatSelectionMode = selectedChatIds.isNotEmpty()
    var showNewChatDialog by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val shouldShowBottomBar = bottomBarScreens.any { it.route == currentRoute }

    if (isChatSelectionMode && currentRoute == BottomBarScreen.Chats.route) {
        BackHandler { chatsViewModel.clearSelections() }
    }

    Scaffold(
        topBar = {
            if (isChatSelectionMode && currentRoute == BottomBarScreen.Chats.route) {
                TopAppBar(
                    title = { Text(stringResource(R.string.chats_selected_count, selectedChatIds.size)) },
                    navigationIcon = { IconButton(onClick = { chatsViewModel.clearSelections() }) { Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.action_cancel)) } },
                    actions = { IconButton(onClick = { chatsViewModel.requestDeletion() }) { Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.action_delete)) } }
                )
            } else if (shouldShowBottomBar) {
                when (currentRoute) {
                    BottomBarScreen.Chats.route -> TopAppBar(title = { Text(stringResource(R.string.main_chats_title)) })
                    BottomBarScreen.Contacts.route -> TopAppBar(title = { Text(stringResource(R.string.main_contacts_title)) })
                    BottomBarScreen.Settings.route -> TopAppBar(
                        title = { Text(stringResource(R.string.settings_title)) },
                        actions = {
                            val settingsUiState by settingsViewModel.uiState.collectAsState()
                            if (settingsUiState.isEditing) {
                                IconButton(onClick = { settingsViewModel.onSave() }) {
                                    Icon(Icons.Default.Save, contentDescription = stringResource(R.string.settings_save))
                                }
                                IconButton(onClick = { settingsViewModel.onCancel() }) {
                                    Icon(Icons.Default.Cancel, contentDescription = stringResource(R.string.action_cancel))
                                }
                            } else {
                                IconButton(onClick = { settingsViewModel.onEdit() }) {
                                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.settings_edit))
                                }
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(tonalElevation = 0.dp) { 
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
        },
        floatingActionButton = {
            if (currentRoute == BottomBarScreen.Chats.route && !isChatSelectionMode) {
                LargeFloatingActionButton(onClick = { showNewChatDialog = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.new_chat_dialog_title),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomBarScreen.Chats.route,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
        ) {
            val animationSpec: FiniteAnimationSpec<IntOffset> = tween(300)
            composable(
                route = BottomBarScreen.Contacts.route,
                enterTransition = { slideInHorizontally(animationSpec = animationSpec) { fullWidth -> -fullWidth } },
                exitTransition = { slideOutHorizontally(animationSpec = animationSpec) { fullWidth -> -fullWidth } },
                popEnterTransition = { slideInHorizontally(animationSpec = animationSpec) { fullWidth -> -fullWidth } },
                popExitTransition = { slideOutHorizontally(animationSpec = animationSpec) { fullWidth -> -fullWidth } }
            ) { ContactsScreen() }
            composable(
                route = BottomBarScreen.Chats.route,
                enterTransition = { 
                    if (initialState.destination.route == BottomBarScreen.Contacts.route) {
                        slideInHorizontally(animationSpec = animationSpec) { fullWidth -> fullWidth }
                    } else {
                        slideInHorizontally(animationSpec = animationSpec) { fullWidth -> -fullWidth }
                    }
                },
                exitTransition = { 
                    if (targetState.destination.route == BottomBarScreen.Contacts.route) {
                        slideOutHorizontally(animationSpec = animationSpec) { fullWidth -> fullWidth }
                    } else {
                        slideOutHorizontally(animationSpec = animationSpec) { fullWidth -> -fullWidth }
                    }
                },
                popEnterTransition = { 
                    if (initialState.destination.route == BottomBarScreen.Contacts.route) {
                        slideInHorizontally(animationSpec = animationSpec) { fullWidth -> fullWidth }
                    } else {
                        slideInHorizontally(animationSpec = animationSpec) { fullWidth -> -fullWidth }
                    }
                },
                popExitTransition = { 
                     if (targetState.destination.route == BottomBarScreen.Contacts.route) {
                        slideOutHorizontally(animationSpec = animationSpec) { fullWidth -> fullWidth }
                    } else {
                        slideOutHorizontally(animationSpec = animationSpec) { fullWidth -> -fullWidth }
                    }
                }
            ) { 
                ChatsScreen(
                    chatsViewModel = chatsViewModel, 
                    showNewChatDialog = showNewChatDialog,
                    setShowNewChatDialog = { showNewChatDialog = it },
                    onChatClick = { chatId -> navController.navigate("${MainDestinations.CONVERSATION_ROUTE}/$chatId") } 
                )
            }
            composable(
                route = BottomBarScreen.Settings.route,
                enterTransition = { slideInHorizontally(animationSpec = animationSpec) { fullWidth -> fullWidth } },
                exitTransition = { slideOutHorizontally(animationSpec = animationSpec) { fullWidth -> fullWidth } },
                popEnterTransition = { slideInHorizontally(animationSpec = animationSpec) { fullWidth -> fullWidth } },
                popExitTransition = { slideOutHorizontally(animationSpec = animationSpec) { fullWidth -> fullWidth } }
            ) { SettingsScreen(settingsViewModel = settingsViewModel, onLogoutClick = onLogoutClick) }
            composable(
                route = "${MainDestinations.CONVERSATION_ROUTE}/{chatId}",
                arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
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
                enterTransition = { fadeIn(animationSpec = tween(0)) },
                exitTransition = { fadeOut(animationSpec = tween(0)) }
            ) { 
                MediaViewerScreen(
                    viewModel = viewModel(factory = MediaViewerViewModelFactory(conversationRepository)),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "${MainDestinations.PROFILE_ROUTE}/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType }),
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) { 
                ProfileScreen(
                    viewModel = viewModel(factory = ProfileViewModelFactory(userRepository)),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
