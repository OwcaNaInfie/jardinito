package pl.edu.pb.jardinito.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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

    NavigationBar(
        containerColor = colors.neutralLight,
        tonalElevation = 0.dp,
        modifier = Modifier.height(85.dp)
    ) {
        navRoutes.forEach { navRoute ->
            val selected = currentRoute == navRoute.route

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        if (currentRoute != navRoute.route) {
                            navController.navigate(navRoute.route) {
                                popUpTo(Routes.HOME)
                                launchSingleTop = true
                            }
                        }
                    },
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
                        tint = if (selected) colors.secondaryBlue else colors.neutralLightGray,
                        modifier = if (selected) Modifier.size(30.dp) else Modifier.size(28.dp)
                    )
                    Text(
                        text = navRoute.title,
                        style = if (selected) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                        color = if (selected) colors.secondaryBlue else colors.neutralLightGray
                    )
                }
            }
        }
    }
}