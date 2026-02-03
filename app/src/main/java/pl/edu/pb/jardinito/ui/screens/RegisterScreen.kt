package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.components.AppButton
import pl.edu.pb.jardinito.ui.components.ButtonSize
import pl.edu.pb.jardinito.ui.components.ButtonVariant
import pl.edu.pb.jardinito.ui.components.FormTextField
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.validateEmail
import pl.edu.pb.jardinito.ui.utils.validatePassword
import pl.edu.pb.jardinito.viewmodel.AuthState
import pl.edu.pb.jardinito.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    val state by authViewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onRegisterSuccess()
        }
    }

    RegisterScreenContent(
        state = state,
        onRegisterClick = { username, email, password ->
            authViewModel.register(username, email, password)
        },
        onLoginClick = onLoginClick,
        onGoogleSignInClick = onGoogleSignInClick
    )
}

@Composable
fun RegisterScreenContent(
    state: AuthState,
    onRegisterClick: (String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }

    val emailError = if (showErrors) validateEmail(email) else null
    val passwordError = if (showErrors) validatePassword(password) else null

    Box(modifier = Modifier.fillMaxSize()) {

        // =====================
        // TŁO
        // =====================
        Image(
            painter = painterResource(R.drawable.onboarding_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    color = colors.primary100,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
        ) {

            // =====================
            // SCROLLOWANA TREŚĆ
            // =====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                FormTextField(
                    label = stringResource(R.string.username),
                    value = username,
                    onValueChange = { username = it },
                    required = true
                )

                FormTextField(
                    label = stringResource(R.string.email),
                    value = email,
                    onValueChange = { email = it },
                    required = true,
                    validator = ::validateEmail
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
                    text = stringResource(R.string.register),
                    size = ButtonSize.Max,
                    variant = ButtonVariant.Tertiary,
                    onClick = {
                        showErrors = true
                        if (emailError == null && passwordError == null) {
                            onRegisterClick(username, email, password)
                        }
                    }
                )

                Text(
                    text = stringResource(R.string.or),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.neutralBlack
                )

                // 🔵 GOOGLE – ta sama akcja co login
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
            }

            // =====================
            // STOPKA (BEZ SCROLLA)
            // =====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.have_account),
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = onLoginClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.go_to_login),
                        color = colors.secondaryBlue,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun RegisterScreenPreview() {
    JardinitoTheme {
        RegisterScreenContent(
            state = AuthState.Idle,
            onRegisterClick = { _, _, _ -> },
            onLoginClick = {},
            onGoogleSignInClick = {}
        )
    }
}
