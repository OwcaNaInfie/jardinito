package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.viewmodel.AuthState
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun LoginScreen(authViewModel: AuthViewModel = viewModel()) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state by authViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )

        Button(
            onClick = {
                authViewModel.login(email, password)
            }
        ) {
            Text("Login")
        }

        when (state) {
            is AuthState.Loading -> Text("Loading...")
            is AuthState.Success -> Text((state as AuthState.Success).message)
            is AuthState.Error -> Text((state as AuthState.Error).message)
            else -> {}
        }
    }
}

