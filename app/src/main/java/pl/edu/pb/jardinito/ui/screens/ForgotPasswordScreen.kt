package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.DialogConfig
import pl.edu.pb.jardinito.ui.components.DialogVariant
import pl.edu.pb.jardinito.ui.components.FormTextField
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.validateVerificationCode
import pl.edu.pb.jardinito.viewmodel.PasswordResetViewModel
import pl.edu.pb.jardinito.viewmodel.state.AuthState

@Composable
fun ForgotPasswordScreen(
    passwordResetViewModel: PasswordResetViewModel,
    onPasswordResetSuccess: () -> Unit
) {
    val state by passwordResetViewModel.uiState.collectAsState()
    var lastIdentifier by remember { mutableStateOf("") }
    var sentCode by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        when (state) {
            is AuthState.PasswordResetRequired -> sentCode = true
            is AuthState.PasswordResetSuccess  -> showSuccessDialog = true
            else -> Unit
        }
    }

    if (showSuccessDialog) {
        ConfirmDialog(
            config = DialogConfig(
                title       = stringResource(R.string.reset_password),
                message     = stringResource(R.string.password_reset_success),
                confirmText = "OK",
                singleButton = true,
                variant     = DialogVariant.Success
            ),
            onConfirm = { showSuccessDialog = false; onPasswordResetSuccess() },
            onDismiss = { showSuccessDialog = false; onPasswordResetSuccess() }
        )
    }

    if (!sentCode) {
        ForgotPasswordRequestContent(
            state = state,
            onSendCode = { identifier ->
                lastIdentifier = identifier
                passwordResetViewModel.forgotPassword(identifier)
            }
        )
    } else {
        ForgotPasswordResetContent(
            state = state,
            identifier = lastIdentifier,
            passwordResetViewModel = passwordResetViewModel,
            onResetPassword = { code, newPassword ->
                passwordResetViewModel.resetPassword(code, newPassword)
            },
            onResendCode = { passwordResetViewModel.forgotPassword(lastIdentifier) }
        )
    }
}

@Composable
fun ForgotPasswordRequestContent(
    state: AuthState,
    onSendCode: (String) -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    val serverError = if (state is AuthState.Error) state.messageRes else null

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.forgot_password),
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            FormTextField(
                label = stringResource(R.string.enter_email_or_username),
                value = identifier,
                onValueChange = { identifier = it },
                required = true,
                isError = serverError != null,
                errorRes = serverError
            )

            AppButton(
                text = stringResource(R.string.send_reset_code),
                size = ButtonSize.Max,
                variant = ButtonVariant.Tertiary,
                onClick = { onSendCode(identifier) }
            )
        }
    }
}

@Composable
fun ForgotPasswordResetContent(
    state: AuthState,
    identifier: String,
    passwordResetViewModel: PasswordResetViewModel,
    onResetPassword: (String, String) -> Unit,
    onResendCode: () -> Unit
) {
    val form by passwordResetViewModel.resetPasswordFormState.collectAsState()
    var timeLeft by remember { mutableIntStateOf(120) }
    val serverError = if (state is AuthState.Error) state.messageRes else null

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timerText = "%d:%02d".format(minutes, seconds)
    val timerColor = if (timeLeft <= 30) colors.error else colors.neutralGray

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.reset_password),
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = timerText,
                style = MaterialTheme.typography.headlineSmall,
                color = timerColor
            )

            FormTextField(
                label = stringResource(R.string.verification_code_hint),
                value = form.code,
                onValueChange = {
                    if (validateVerificationCode(it)) {
                        passwordResetViewModel.onResetCodeChanged(it)
                    }
                },
                required = true,
                isError = serverError != null,
                errorRes = serverError,
                keyboardType = KeyboardType.Number
            )

            FormTextField(
                label = stringResource(R.string.new_password),
                value = form.newPassword,
                onValueChange = { passwordResetViewModel.onResetPasswordChanged(it) },
                required = true,
                isPassword = true,
                errorRes = form.newPasswordError,
                isValid = form.newPasswordIsValid
            )

            FormTextField(
                label = stringResource(R.string.repeat_password),
                value = form.repeatedPassword,
                onValueChange = { passwordResetViewModel.onResetRepeatedPasswordChanged(it) },
                required = true,
                isPassword = true,
                errorRes = form.repeatedPasswordError,
                isValid = form.repeatedPasswordIsValid
            )

            AppButton(
                text = stringResource(R.string.reset_password),
                size = ButtonSize.Max,
                variant = ButtonVariant.Tertiary,
                enabled = timeLeft > 0
                        && form.code.length == 6
                        && form.newPasswordIsValid
                        && form.repeatedPasswordIsValid,
                onClick = { onResetPassword(form.code, form.newPassword) }
            )

            TextButton(
                onClick = {
                    onResendCode()
                    timeLeft = 120
                }
            ) {
                Text(
                    text = stringResource(R.string.resend_code),
                    color = colors.secondaryBlue,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE0F4F5, apiLevel = 34)
@Composable
fun ForgotPasswordRequestPreview() {
    JardinitoTheme {
        ForgotPasswordRequestContent(
            state = AuthState.Idle,
            onSendCode = {}
        )
    }
}