package pl.edu.pb.jardinito.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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

    BottomBarContent(
        navRoutes = navRoutes,
        currentRoute = currentRoute,
        onRouteClick = { route ->
            if (currentRoute != route) {
                navController.navigate(route) {
                    popUpTo(Routes.HOME)
                    launchSingleTop = true
                }
            }
        }
    )
}

@Composable
fun BottomBarContent(
    navRoutes: List<NavRoute>,
    currentRoute: String?,
    onRouteClick: (String) -> Unit
) {
    NavigationBar(
        containerColor = colors.neutralLight,
        tonalElevation = 0.dp,
        modifier = Modifier.height(85.dp)
    ) {
        navRoutes.forEach { navRoute ->
            val selected = currentRoute == navRoute.route
            val iconSize = if (selected) 30.dp else 28.dp
            val contentColor = if (selected) colors.secondaryBlue else colors.neutralLightGray
            val textStyle = if (selected) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onRouteClick(navRoute.route) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = navRoute.iconRes),
                        contentDescription = navRoute.title,
                        tint = contentColor,
                        modifier = Modifier.size(iconSize)
                    )
                    Text(
                        text = navRoute.title,
                        style = textStyle,
                        color = contentColor
                    )
                }
            }
        }
    }
}