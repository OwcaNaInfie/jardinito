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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.edu.pb.jardinito.ui.navigation.Routes
import pl.edu.pb.jardinito.viewmodel.AuthState
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.edu.pb.jardinito.ui.components.AppButton
import pl.edu.pb.jardinito.ui.components.ButtonSize
import pl.edu.pb.jardinito.ui.components.ButtonVariant
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    val state by authViewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onLoginSuccess()
        }
    }

    LoginScreenContent(
        state = state,
        onLoginClick = { email, password ->
            authViewModel.login(email, password)
        },
        onRegisterClick = onRegisterClick,
        onGoogleSignInClick = onGoogleSignInClick
    )
}

@Composable
fun LoginScreenContent(
    state: AuthState,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppButton(
            text = stringResource(R.string.login),
            size = ButtonSize.Max,
            variant = ButtonVariant.Tertiary,
            onClick = { onLoginClick(email, password) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onGoogleSignInClick) {
            Text("Sign in with Google")
        }

        when (state) {
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.Error -> Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error
            )
            else -> {}
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(R.string.no_account))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.go_to_register),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onRegisterClick() }
            )
        }
    }
}

//@Composable
//fun LoginScreen(
//    authViewModel: AuthViewModel,
//    onLoginSuccess: () -> Unit,
//    onRegisterClick: () -> Unit,
//    onGoogleSignInClick: () -> Unit
//) {
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//
//    val state by authViewModel.uiState.collectAsState()
//
//    LaunchedEffect(state) {
//        if (state is AuthState.Success) {
//            onLoginSuccess()
//        }
//    }
//
//    Column(
//        modifier = Modifier.padding(16.dp)
//    ) {
//
//        TextField(
//            value = email,
//            onValueChange = { email = it },
//            label = { Text(text = stringResource(R.string.email)) }
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        TextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text(text = stringResource(R.string.password)) }
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        AppButton(
//            text = stringResource(R.string.login),
//            size = ButtonSize.Max,
//            variant = ButtonVariant.Tertiary,
//            onClick = {authViewModel.login(email, password)}
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = { onGoogleSignInClick() }
//        ) {
//            Text("Sign in with Google")
//        }
//
//        when (state) {
//            is AuthState.Loading -> Text(text = stringResource(R.string.loading))
//            is AuthState.Error ->
//                Text((state as AuthState.Error).message)
//            else -> {}
//        }
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center
//        ) {
//            Text(text = stringResource(R.string.no_account))
//
//            Text(
//                text = stringResource(R.string.go_to_register),
//                color = MaterialTheme.colorScheme.primary,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.clickable {
//                    onRegisterClick()
//                }
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        when (state) {
//            is AuthState.Loading -> {
//                CircularProgressIndicator(
//                    modifier = Modifier.align(Alignment.CenterHorizontally)
//                )
//            }
//            is AuthState.Error -> {
//                Text(
//                    text = (state as AuthState.Error).message,
//                    color = MaterialTheme.colorScheme.error
//                )
//            }
//            else -> {}
//        }
//    }
//}

@Preview(
    showBackground = true,
    apiLevel = 34
)
@Composable
fun LoginScreenPreview() {
    JardinitoTheme {
        LoginScreenContent(
            state = AuthState.Idle,
            onLoginClick = { _, _ -> },
            onRegisterClick = {},
            onGoogleSignInClick = {}
        )
    }
}

@Preview(
    name = "Loading",
    showBackground = true,
    apiLevel = 34)
@Composable
fun LoginLoadingPreview() {
    JardinitoTheme {
        LoginScreenContent(
            state = AuthState.Loading,
            onLoginClick = { _, _ -> },
            onRegisterClick = {},
            onGoogleSignInClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    apiLevel = 34,
    name = "Error")
@Composable
fun LoginErrorPreview() {
    JardinitoTheme {
        LoginScreenContent(
            state = AuthState.Error("Invalid credentials"),
            onLoginClick = { _, _ -> },
            onRegisterClick = {},
            onGoogleSignInClick = {}
        )
    }
}