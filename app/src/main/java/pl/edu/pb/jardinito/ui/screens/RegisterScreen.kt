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
import pl.edu.pb.jardinito.viewmodel.AuthState
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel = viewModel(),
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state by authViewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(text = stringResource(R.string.username)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = stringResource(R.string.email) ) }
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
                println("REGISTER CLICKED: $username $email $password")

                authViewModel.register(
                    username = username,
                    email = email,
                    password = password
                )
            }
        ) {
            Text(text = stringResource(R.string.register))
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is AuthState.Loading -> Text(text = stringResource(R.string.loading) )
            is AuthState.Success -> Text((state as AuthState.Success).message)
            is AuthState.Error -> Text((state as AuthState.Error).message)
            else -> {}
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(R.string.have_account))

            Text(
                text = stringResource(R.string.go_to_login),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    onLoginClick()
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
