package pl.edu.pb.jardinito.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import pl.edu.pb.jardinito.data.model.BottomNavItem

@Composable
fun BottomBar(navController: NavController) {

    val items = listOf(
        BottomNavItem(
            route = Routes.HOME,
            icon = Icons.Default.Home,
            label = "Home"
        ),
        BottomNavItem(
            route = Routes.FOCUS,
            icon = Icons.Default.LocalFlorist,
            label = "Plant"
        ),
        BottomNavItem(
            route = Routes.PROFILE,
            icon = Icons.Default.AccountCircle,
            label = "Profile"
        )
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Routes.HOME)
                            launchSingleTop = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
