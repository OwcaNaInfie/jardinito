package pl.edu.pb.jardinito.ui.navigation

import androidx.navigation.NavController

class NavActions(private val navController: NavController) {

    val toLogin: () -> Unit = {
        navController.navigate(Routes.LOGIN)
    }

    val toRegister: () -> Unit = {
        navController.navigate(Routes.REGISTER)
    }

    val toHomeFromLogin: () -> Unit = {
        navController.navigate(Routes.HOME) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    val toHomeFromRegister: () -> Unit = {
        navController.navigate(Routes.HOME) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }
}
