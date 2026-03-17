package pl.edu.pb.jardinito.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pl.edu.pb.jardinito.ui.screens.AuthEntryScreen
import pl.edu.pb.jardinito.ui.screens.HomeScreen
import pl.edu.pb.jardinito.ui.screens.FocusScreen
import pl.edu.pb.jardinito.ui.screens.ProfileScreen
import pl.edu.pb.jardinito.viewmodel.AuthViewModel

@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel,
    onGoogleSignInClick: () -> Unit
) {
    val navController = rememberNavController()
    val actions = remember(navController) { NavActions(navController) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Routes.HOME,
        Routes.FOCUS,
        Routes.PROFILE
    )

    Scaffold(
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

            composable(Routes.PROFILE) {
                val user by authViewModel.currentUser.collectAsState(initial = null)

                ProfileScreen(
                    user = user,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Routes.ENTRY) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    viewModel = authViewModel
                )
            }
        }
    }
}
