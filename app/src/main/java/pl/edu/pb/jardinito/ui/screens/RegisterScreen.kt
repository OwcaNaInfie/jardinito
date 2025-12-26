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
fun RegisterScreen(
    authViewModel: AuthViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state by authViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                println("REGISTER CLICKED: $username $email $password")

                authViewModel.register(
                    username = username,
                    email = email,
                    password = password
                )
            }
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is AuthState.Loading -> Text("Loading...")
            is AuthState.Success -> Text((state as AuthState.Success).message)
            is AuthState.Error -> Text((state as AuthState.Error).message)
            else -> {}
        }
    }
}
