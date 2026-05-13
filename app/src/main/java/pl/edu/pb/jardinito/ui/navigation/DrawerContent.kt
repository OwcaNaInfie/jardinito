package pl.edu.pb.jardinito.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.User
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.ui.theme.colors
import androidx.compose.foundation.layout.Arrangement
import pl.edu.pb.jardinito.ui.components.UserAvatar

@Composable
fun DrawerContent(
    navRoutes: List<NavRoute>,
    currentRoute: String?,
    user: User?,
    onRouteClick: (String) -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = colors.neutralLight,
        drawerShape = RectangleShape,
        modifier = Modifier.width(240.dp)
    ) {
        user?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserAvatar(
                    user = user,
                    size = 60.dp,
                    borderWidth = 2.dp,
                )
                Text(
                    text = it.username,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
        navRoutes.forEach { navRoute ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        painter = painterResource(id = navRoute.iconRes),
                        contentDescription = navRoute.title
                    )
                },
                label = { Text(navRoute.title) },
                selected = currentRoute == navRoute.route,
                onClick = { onRouteClick(navRoute.route) },
                shape = RectangleShape,
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = colors.neutralLight,
                    selectedContainerColor = colors.primary100
                )
            )
        }
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun DrawerContentPreview() {
    JardinitoTheme {
        DrawerContent(
            navRoutes = listOf(
                NavRoute(Routes.HOME, R.drawable.windmill, "Home"),
                NavRoute(Routes.FOCUS, R.drawable.pottedplant, "Plant"),
                NavRoute(Routes.PROFILE, R.drawable.rabbit, "Profile")
            ),
            currentRoute = Routes.HOME,
            onRouteClick = {},
            user = null
        )
    }
}