package pl.edu.pb.jardinito.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.edu.pb.jardinito.ui.screens.LoginScreen
import pl.edu.pb.jardinito.ui.screens.RegisterScreen
import pl.edu.pb.jardinito.ui.screens.HomeScreen
import pl.edu.pb.jardinito.viewmodel.AuthViewModel

@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen()
        }
    }
}
