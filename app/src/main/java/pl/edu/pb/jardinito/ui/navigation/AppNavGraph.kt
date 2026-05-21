package pl.edu.pb.jardinito.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.screens.AuthEntryScreen
import pl.edu.pb.jardinito.ui.screens.FocusScreen
import pl.edu.pb.jardinito.ui.screens.HomeScreen
import pl.edu.pb.jardinito.ui.screens.ProfileScreen
import pl.edu.pb.jardinito.ui.screens.StatisticsScreen
import pl.edu.pb.jardinito.ui.screens.TagsScreen
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import pl.edu.pb.jardinito.viewmodel.PasswordResetViewModel
import pl.edu.pb.jardinito.viewmodel.TagViewModel
import pl.edu.pb.jardinito.viewmodel.UserViewModel
import pl.edu.pb.jardinito.viewmodel.VerificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    onGoogleSignInClick: () -> Unit
) {
    val navController = rememberNavController()
    val actions = remember(navController) { NavActions(navController) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isEditingProfile by rememberSaveable { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }


    val authViewModel: AuthViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val verificationViewModel: VerificationViewModel = hiltViewModel()
    val passwordResetViewModel: PasswordResetViewModel = hiltViewModel()
    val tagViewModel: TagViewModel = hiltViewModel()

    val bottomBarRoutes = listOf(
        Routes.HOME,
        Routes.FOCUS,
        Routes.PROFILE,
        Routes.MARKET,
        Routes.TAGS,
        Routes.STATISTICS
    )

    val navRoutes = listOf(
        NavRoute(Routes.HOME, R.drawable.windmill, "Home"),
        NavRoute(Routes.FOCUS, R.drawable.pottedplant, "Plant"),
        NavRoute(Routes.MARKET, R.drawable.market, "Market"),
        NavRoute(Routes.TAGS, R.drawable.signpost, "Tags"),
        NavRoute(Routes.STATISTICS, R.drawable.chartpieslice, "Statistics"),
        NavRoute(Routes.PROFILE, R.drawable.rabbit, "Profile")
    )

    val bottomNavRoutes = listOf(
        NavRoute(Routes.HOME, R.drawable.windmill, "Home"),
        NavRoute(Routes.FOCUS, R.drawable.pottedplant, "Plant"),
        NavRoute(Routes.PROFILE, R.drawable.rabbit, "Profile")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                navRoutes = navRoutes,
                currentRoute = currentRoute,
                user = authViewModel.currentUser.collectAsState().value,
                onRouteClick = { route ->
                    scope.launch { drawerState.close() }
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(Routes.HOME)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                contentWindowInsets = WindowInsets(0),
                containerColor = colors.transparent,

                bottomBar = {
                    if (currentRoute in bottomBarRoutes) {
                        BottomBar(
                            navController = navController,
                            navRoutes = bottomNavRoutes
                        )
                    }
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = Routes.ENTRY,
                    modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
                ) {
                    composable(Routes.ENTRY) {
                        AuthEntryScreen(
                            authViewModel = authViewModel,
                            verificationViewModel = verificationViewModel,
                            passwordResetViewModel = passwordResetViewModel,
                            onRegisterSuccess = actions.toHomeFromRegister,
                            onLoginSuccess = actions.toHomeFromLogin,
                            onGoogleSignInClick = onGoogleSignInClick
                        )
                    }
                    composable(Routes.HOME) {
                        HomeScreen()
                    }
                    composable(Routes.FOCUS) {
                        FocusScreen()
                    }
                    composable(Routes.TAGS) {
                        TagsScreen(
                            tagViewModel = tagViewModel,
                            userId = authViewModel.currentUser.collectAsState().value?.userId ?: "",
                            showAddTagDialog = showAddTagDialog,
                            onAddTagDialogDismiss = { showAddTagDialog = false }
                        )
                    }
                    composable(Routes.STATISTICS) {
                        StatisticsScreen()
                    }
                    composable(Routes.PROFILE) {
                        val user by authViewModel.currentUser.collectAsState(initial = null)
                        user?.let {
                                ProfileScreen(
                                    user = it,
                                    onLogout = {
                                        authViewModel.logout()
                                        navController.navigate(Routes.ENTRY) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    authViewModel = authViewModel,
                                    userViewModel = userViewModel,
                                    isEditing = isEditingProfile
                                )
                            }
                    }
                }
            }
            if (currentRoute in bottomBarRoutes) {
                TopBar(
                    title = navRoutes.find { it.route == currentRoute }?.title ?: "",
                    onMenuClick = { scope.launch { drawerState.open() } },
                    actions = when (currentRoute) {
                        Routes.PROFILE -> listOf(Icons.Default.Settings to { isEditingProfile = !isEditingProfile })
                        Routes.TAGS -> listOf(Icons.Default.Add to { showAddTagDialog = true })
                        else -> emptyList()
                    }
                )
            }
        }
    }
}