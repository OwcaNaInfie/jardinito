package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.model.User

@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        if (user != null) {

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {

                // ⚙ Ikona ustawień
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }

                // 👤 Avatar + tekst
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.username.first().uppercase(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Gardener",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }

        } else {
            Text(text = "Brak danych użytkownika")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onLogout) {
            Text("Wyloguj się")
        }
    }
}


