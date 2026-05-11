package pl.edu.pb.jardinito.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.ui.screens.HomeScreen
import pl.edu.pb.jardinito.ui.theme.Dimensions
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.ui.theme.colors

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
        windowInsets = WindowInsets(0),
        modifier = Modifier
            .padding(vertical = 4.dp)
            .padding(top = 14.dp),

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
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.transparent
        ),
    )
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun TopBarPreview() {
    JardinitoTheme {
        TopBar(
            currentRoute = Routes.HOME,
            onMenuClick = {},
            onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun TopBarProfilePreview() {
    JardinitoTheme {
        TopBar(
            currentRoute = Routes.PROFILE,
            onMenuClick = {},
            onSettingsClick = {}
        )
    }
}