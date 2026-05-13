package pl.edu.pb.jardinito.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import pl.edu.pb.jardinito.ui.theme.colors

@Composable
fun BottomBar(
    navController: NavController,
    navRoutes: List<NavRoute>
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = colors.neutralLight
    ) {
        navRoutes.forEach { navRoute ->
            val selected = currentRoute == navRoute.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != navRoute.route) {
                        navController.navigate(navRoute.route) {
                            popUpTo(Routes.HOME)
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = navRoute.iconRes),
                        contentDescription = navRoute.title
                    )
                },
                label = { Text(navRoute.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.secondaryBlue,
                    selectedTextColor = colors.secondaryBlue,
                    unselectedIconColor = colors.neutralGray,
                    unselectedTextColor = colors.neutralGray,
                    indicatorColor = colors.transparent
                )
            )
        }
    }
}
