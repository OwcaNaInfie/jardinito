package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.model.User

@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (user != null) {
            Text(text = "Witaj, ${user.username}")
            Text(text = "Email: ${user.email}")
        } else {
            Text(text = "Brak danych użytkownika")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onLogout) {
            Text("Wyloguj się")
        }
    }
}

