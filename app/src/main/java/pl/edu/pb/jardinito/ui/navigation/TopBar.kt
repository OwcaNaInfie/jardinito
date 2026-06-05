package pl.edu.pb.jardinito.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Toll
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.ui.theme.colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    onMenuClick: () -> Unit,
    actions: List<Pair<ImageVector, () -> Unit>> = emptyList(),
    coins: Int? = null
) {
    TopAppBar(
        windowInsets = WindowInsets(0),
        modifier = Modifier
            .padding(vertical = 4.dp)
            .padding(top = 18.dp),
        title = { Text(title, style = MaterialTheme.typography.headlineMedium) },

        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = colors.neutralGray
                )
            }
        },
        actions = {
            coins?.let { CoinBalanceTopBar(it) }
            actions.forEach { (icon, onClick) ->
                IconButton(onClick = onClick) {
                    Icon(icon, contentDescription = null, tint = colors.neutralGray)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.transparent,
            titleContentColor = colors.neutralGray
        )
    )
}

@Composable
private fun CoinBalanceTopBar(coins: Int) {
    Row(
        modifier = Modifier.padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val coinBalanceColor = if (coins == 0) colors.error  else colors.primary500
        Text(
            text = coins.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = coinBalanceColor
        )
        Icon(
            imageVector = Icons.Default.Toll,
            contentDescription = null,
            tint = coinBalanceColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun TopBarPreview() {
    JardinitoTheme {
        TopBar(
            title = "Home",
            onMenuClick = {}
        )
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun TopBarProfilePreview() {
    JardinitoTheme {
        TopBar(
            title = "Profile",
            onMenuClick = {}
        )
    }
}
