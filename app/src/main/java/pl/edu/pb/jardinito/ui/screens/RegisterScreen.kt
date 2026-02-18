package pl.edu.pb.jardinito.ui.screens

import android.annotation.SuppressLint
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.components.FormTextField
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.ui.theme.colors
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
        authViewModel = authViewModel,
        state = state,
        onRegisterClick = { username, email, password ->
            authViewModel.register(username, email, password)
        },
        onLoginClick = onLoginClick,
        onGoogleSignInClick = onGoogleSignInClick
    )
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun RegisterScreenContent(
    authViewModel: AuthViewModel,
    state: AuthState,
    onRegisterClick: (String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    val form by authViewModel.registerFormState.collectAsState()

    // Form validation – do włączania submit button
    val formValid = listOf(
        form.usernameError,
        form.emailError,
        form.passwordError,
        form.repeatedPasswordError
    ).all { it == null }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FormTextField(
            label = stringResource(R.string.username),
            value = form.username,
            onValueChange = { authViewModel.onUsernameChanged(it) },
            required = true,
            errorRes = form.usernameError,
            isValid = form.usernameIsValid
        )

        FormTextField(
            label = stringResource(R.string.email),
            value = form.email,
            onValueChange = { authViewModel.onEmailChanged(it) },
            required = true,
            errorRes = form.emailError,
            isValid = form.emailIsValid
        )

        FormTextField(
            label = stringResource(R.string.password),
            value = form.password,
            onValueChange = { authViewModel.onPasswordChanged(it) },
            required = true,
            errorRes = form.passwordError,
            isPassword = true,
            isValid = form.passwordIsValid
        )

        FormTextField(
            label = stringResource(R.string.repeat_password),
            value = form.repeatedPassword,
            onValueChange = { authViewModel.onRepeatedPasswordChanged(it) },
            required = true,
            errorRes = form.repeatedPasswordError,
            isPassword = true,
            isValid = form.repeatedPasswordIsValid
        )

        AppButton(
            text = stringResource(R.string.register),
            size = ButtonSize.Max,
            variant = ButtonVariant.Tertiary,
            enabled = formValid,
            onClick = {
                    onRegisterClick(form.username, form.email, form.password)
            }
        )
//        AppButton(
//            text = "TEST 400",
//            onClick = { authViewModel.testRegister400() }
//        )

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
                text = stringResource(R.string.have_account),
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = onLoginClick,
                contentPadding = PaddingValues(2.dp)
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

//@Preview(showBackground = true, apiLevel = 34)
//@Composable
//fun RegisterScreenPreview() {
//    JardinitoTheme {
//        RegisterScreenContent(
//            state = AuthState.Idle,
//            onRegisterClick = { _, _, _ -> },
//            onLoginClick = {},
//            onGoogleSignInClick = {}
//        )
//    }
//}
