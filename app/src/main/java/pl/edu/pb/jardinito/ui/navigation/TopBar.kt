package pl.edu.pb.jardinito.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    currentRoute: String?,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val title = when (currentRoute) {
        Routes.HOME -> "Home"
        Routes.FOCUS -> "Plant"
        Routes.TAGS -> "Tags"
        Routes.STATISTICS -> "Statistics"
        Routes.PROFILE -> "Profile"
        else -> ""
    }

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            if (currentRoute == Routes.PROFILE) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }
    )
}