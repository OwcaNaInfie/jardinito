package pl.edu.pb.jardinito.ui.screens
import androidx.compose.foundation.background
import pl.edu.pb.jardinito.R

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import pl.edu.pb.jardinito.viewmodel.AuthState
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import pl.edu.pb.jardinito.ui.components.AppButton
import pl.edu.pb.jardinito.ui.components.ButtonSize
import pl.edu.pb.jardinito.ui.components.ButtonVariant
import pl.edu.pb.jardinito.ui.components.FormTextField
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.validateEmail
import pl.edu.pb.jardinito.ui.utils.validatePassword


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
    var showErrors by remember { mutableStateOf(false) }

    val emailError = if (showErrors) validateEmail(email) else null
    val passwordError = if (showErrors) validatePassword(password) else null

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val containerHeight = maxHeight * 3 / 5

        // TŁO
        Image(
            painter = painterResource(R.drawable.onboarding_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(containerHeight)
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
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
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
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation()
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
                    text = stringResource(R.string.no_account),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onRegisterClick) {
                    Text(
                        text = stringResource(R.string.go_to_register),
                        color = colors.secondaryBlue,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

//@Preview(
//    showBackground = true,
//    apiLevel = 34
//)
//@Composable
//fun LoginScreenPreview() {
//    JardinitoTheme {
//        LoginScreenContent(
//            state = AuthState.Idle,
//            onLoginClick = { _, _ -> },
//            onRegisterClick = {},
//            onGoogleSignInClick = {}
//        )
//    }
//}

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