package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.model.User

@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {

    val avatarUrl = when (user?.avatar?.type) {
        "default" -> "${RetrofitInstance.BASE_URL}avatars/${user.avatar.value}"
        "google" -> user.avatar.value
        "custom" -> user.avatar.value
        else -> null
    }

    if (user != null) {
        ProfileScreenContent(
            user = user,
            avatarUrl = avatarUrl,
            onLogout = onLogout,
            onSettingsClick = onSettingsClick
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Brak danych użytkownika")
        }
    }
}

@Composable
fun ProfileScreenContent(
    user: User,
    avatarUrl: String?,
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 72.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            )
    ) {

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Box(
                    modifier = Modifier.size(80.dp)
                ) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "User avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Gardeners name",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onLogout) {
            Text("Wyloguj się")
        }
    }
}



