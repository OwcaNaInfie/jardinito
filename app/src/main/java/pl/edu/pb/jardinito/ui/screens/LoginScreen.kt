package pl.edu.pb.jardinito.ui.screens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.components.AppButton
import pl.edu.pb.jardinito.ui.components.ButtonSize
import pl.edu.pb.jardinito.ui.components.ButtonVariant
import pl.edu.pb.jardinito.ui.components.FormTextField
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.validateEmail
import pl.edu.pb.jardinito.ui.utils.validatePassword
import pl.edu.pb.jardinito.viewmodel.AuthState
import pl.edu.pb.jardinito.viewmodel.AuthViewModel

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
        onLoginClick = { email, password -> authViewModel.login(email, password) },
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
    var showErrors by remember { mutableStateOf(false) }

    val emailError = if (showErrors) validateEmail(email) else null
    val passwordError = if (showErrors) validatePassword(password) else null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        FormTextField(
            label = stringResource(R.string.email),
            value = email,
            onValueChange = { email = it },
            required = true,
            validator = ::validateEmail,
            keyboardType = KeyboardType.Email
        )

        FormTextField(
            label = stringResource(R.string.password),
            value = password,
            onValueChange = { password = it },
            required = true,
            validator = ::validatePassword,
            isPassword = true
        )

        AppButton(
            text = stringResource(R.string.login),
            size = ButtonSize.Max,
            variant = ButtonVariant.Tertiary,
            onClick = {
                showErrors = true
                if (emailError == null && passwordError == null) {
                    onLoginClick(email, password)
                }
            }
        )

        Text(
            text = stringResource(R.string.or),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.neutralBlack
        )

        AppButton(
            iconRes = R.drawable.google,
            size = ButtonSize.Large,
            circle = true,
            buttonColor = colors.primary50,
            iconColor = Color.Unspecified,
            onClick = onGoogleSignInClick
        )

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
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.no_account),
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = onRegisterClick,
                contentPadding = PaddingValues(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.go_to_register),
                    color = colors.secondaryBlue,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

//@Preview(
//    name = "Loading",
//    showBackground = true,
//    apiLevel = 34)
//@Composable
//fun LoginLoadingPreview() {
//    JardinitoTheme {
//        LoginScreenContent(
//            state = AuthState.Loading,
//            onLoginClick = { _, _ -> },
//            onRegisterClick = {},
//            onGoogleSignInClick = {}
//        )
//    }
//}
//
//@Preview(
//    showBackground = true,
//    apiLevel = 34,
//    name = "Error")
//@Composable
//fun LoginErrorPreview() {
//    JardinitoTheme {
//        LoginScreenContent(
//            state = AuthState.Error("Invalid credentials"),
//            onLoginClick = { _, _ -> },
//            onRegisterClick = {},
//            onGoogleSignInClick = {}
//        )
//    }
//}