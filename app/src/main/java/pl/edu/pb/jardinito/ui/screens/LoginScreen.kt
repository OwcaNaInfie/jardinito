package pl.edu.pb.jardinito.ui.screens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.viewmodel.state.AuthState
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
        onRegisterClick = onRegisterClick,
        onGoogleSignInClick = onGoogleSignInClick,
        authViewModel = authViewModel
    )
}

@Composable
fun LoginScreenContent(
    authViewModel: AuthViewModel,
    state: AuthState,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {

    val form by authViewModel.loginFormState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            FormTextField(
                label = stringResource(R.string.email_or_username),
                value = form.loginIdentifier,
                required = true,
                onValueChange = { authViewModel.onLoginIdentifierChanged(it) },
                errorRes = form.loginIdentifierError,
                isError = form.serverError != null
            )

            FormTextField(
                label = stringResource(R.string.password),
                value = form.loginPassword,
                required = true,
                onValueChange = { authViewModel.onLoginPasswordChanged(it) },
                isPassword = true,
                errorRes = form.loginPasswordError,
                isError = form.serverError != null
            )
        }
        AppButton(
            text = stringResource(R.string.login),
            size = ButtonSize.Max,
            variant = ButtonVariant.Tertiary,
            onClick = { authViewModel.submitLogin() }
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

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            when (state) {
                is AuthState.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                is AuthState.Error -> Text(
                    text = stringResource(state.messageRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                else -> {}
            }
        }

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
