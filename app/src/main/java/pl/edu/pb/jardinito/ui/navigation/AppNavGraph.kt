package pl.edu.pb.jardinito.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.ui.screens.AuthEntryScreen
import pl.edu.pb.jardinito.ui.screens.FocusScreen
import pl.edu.pb.jardinito.ui.screens.HomeScreen
import pl.edu.pb.jardinito.ui.screens.ProfileScreen
import pl.edu.pb.jardinito.ui.screens.StatisticsScreen
import pl.edu.pb.jardinito.ui.screens.TagsScreen
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import pl.edu.pb.jardinito.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel,
    onGoogleSignInClick: () -> Unit
) {
    val navController = rememberNavController()
    val actions = remember(navController) { NavActions(navController) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val userViewModel: UserViewModel = viewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isEditingProfile by rememberSaveable { mutableStateOf(false) }

    val bottomBarRoutes = listOf(
        Routes.HOME,
        Routes.FOCUS,
        Routes.PROFILE,
        Routes.TAGS,
        Routes.STATISTICS
    )

    val drawerRoutes = listOf(
        Triple(Routes.HOME, Icons.Default.Home, "Home"),
        Triple(Routes.FOCUS, Icons.Default.LocalFlorist, "Plant"),
        Triple(Routes.TAGS, Icons.AutoMirrored.Filled.Label, "Tags"),
        Triple(Routes.STATISTICS, Icons.Default.BarChart, "Statistics"),
        Triple(Routes.PROFILE, Icons.Default.AccountCircle, "Profile")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Jardinito",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = androidx.compose.ui.Modifier.padding(24.dp)
                )
                HorizontalDivider()
                drawerRoutes.forEach { (route, icon, label) ->
                    NavigationDrawerItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentRoute == route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo(Routes.HOME)
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = androidx.compose.ui.Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            topBar = {
                if (currentRoute in bottomBarRoutes) {
                    TopBar(
                        currentRoute = currentRoute,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onSettingsClick = { isEditingProfile = !isEditingProfile }
                    )
                }
            },
            bottomBar = {
                if (currentRoute in bottomBarRoutes) {
                    BottomBar(navController)
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Routes.ENTRY,
                modifier = androidx.compose.ui.Modifier.padding(paddingValues)
            ) {
                composable(Routes.ENTRY) {
                    AuthEntryScreen(
                        authViewModel = authViewModel,
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
                    TagsScreen()
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
                                    popUpTo(Routes.HOME) { inclusive = true }
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
    }
}