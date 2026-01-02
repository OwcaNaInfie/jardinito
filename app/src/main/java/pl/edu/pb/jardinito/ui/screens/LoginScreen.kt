package pl.edu.pb.jardinito.ui.screens
import pl.edu.pb.jardinito.R

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.edu.pb.jardinito.ui.navigation.Routes
import pl.edu.pb.jardinito.viewmodel.AuthState
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state by authViewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = stringResource(R.string.email)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = stringResource(R.string.password)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                authViewModel.login(email, password)
            }
        ) {
            Text(text = stringResource(R.string.login))
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is AuthState.Loading -> Text(text = stringResource(R.string.loading))
            is AuthState.Error ->
                Text((state as AuthState.Error).message)
            else -> {}
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(R.string.no_account))

            Text(
                text = stringResource(R.string.go_to_register),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    onRegisterClick()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is AuthState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is AuthState.Error -> {
                Text(
                    text = (state as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {}
        }
    }
}
