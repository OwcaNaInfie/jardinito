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
            popUpTo(Routes.LOGIN) { inclusive = true }
        }
    }

    val toHomeFromRegister: () -> Unit = {
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.REGISTER) { inclusive = true }
        }
    }
}
