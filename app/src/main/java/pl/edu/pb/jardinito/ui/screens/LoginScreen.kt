package pl.edu.pb.jardinito.ui.screens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.DialogVariant
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.components.FormTextField
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.viewmodel.state.AuthState
import pl.edu.pb.jardinito.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
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
        authViewModel = authViewModel,
        onForgotPasswordClick = onForgotPasswordClick
    )
}

@Composable
fun LoginScreenContent(
    authViewModel: AuthViewModel,
    state: AuthState,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {

    val form by authViewModel.loginFormState.collectAsState()
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(state) {
        if (state is AuthState.Error) {
            errorMessage = state.messageRes
            showErrorDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
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

                Spacer(modifier = Modifier.height(4.dp))

                FormTextField(
                    label = stringResource(R.string.password),
                    value = form.loginPassword,
                    required = true,
                    onValueChange = { authViewModel.onLoginPasswordChanged(it) },
                    isPassword = true,
                    errorRes = form.loginPasswordError,
                    isError = form.serverError != null
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-10).dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onForgotPasswordClick,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(16.dp)

                        ) {
                        Text(
                            text = stringResource(R.string.forgot_password),
                            color = colors.secondaryBlue,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    contentColor = Color.Unspecified,
                    onClick = onGoogleSignInClick
                )
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
            if (showErrorDialog && errorMessage != null) {
                ConfirmDialog(
                    title = stringResource(R.string.error_title),
                    message = stringResource(errorMessage!!),
                    confirmText = "OK",
                    singleButton = true,
                    variant = DialogVariant.Error,
                    onConfirm = {
                        showErrorDialog = false
                        authViewModel.resetUiState()
                    },
                    onDismiss = {
                        showErrorDialog = false
                        authViewModel.resetUiState()
                    }
                )
            }
        }

    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE004F5, apiLevel = 34)
@Composable
fun LoginScreenLoadingPreview() {
    JardinitoTheme {
        LoginScreenContent(
            authViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            state = AuthState.Loading,
            onRegisterClick = {},
            onGoogleSignInClick = {},
            onForgotPasswordClick = {}
        )
    }
}