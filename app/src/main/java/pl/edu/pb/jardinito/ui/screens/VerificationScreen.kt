package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.delay
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.DialogVariant
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.components.DialogConfig
import pl.edu.pb.jardinito.ui.components.FormTextField
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.validateVerificationCode
import pl.edu.pb.jardinito.viewmodel.VerificationViewModel
import pl.edu.pb.jardinito.viewmodel.state.AuthState

@Composable
fun VerificationScreen(
    verificationViewModel: VerificationViewModel,
    email: String?,
    onVerificationSuccess: () -> Unit
) {
    val state by verificationViewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onVerificationSuccess()
        }
    }

    VerificationScreenContent(
        state = state,
        email = email,
        onVerifyClick = { code -> verificationViewModel.verifyEmail(code) },
        onResendClick = { verificationViewModel.resendVerification() },
        onCodeChange = { verificationViewModel.resetUiState() }
    )
}

@Composable
fun VerificationScreenContent(
    state: AuthState,
    email: String?,
    onVerifyClick: (String) -> Unit,
    onResendClick: () -> Unit,
    onCodeChange: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var timeLeft by remember { mutableIntStateOf(120) }
    var showCodeSentDialog by remember { mutableStateOf(false) }
    val serverError = if (state is AuthState.Error) state.messageRes else null

    // Timer odliczający 2 minuty
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (email != null) {
            Text(
                text = stringResource(R.string.verification_message, email),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.neutralGray
            )
        }

        Text(
            text = timerText,
            style = MaterialTheme.typography.headlineSmall,
            color = timerColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        FormTextField(
            label = stringResource(R.string.verification_code_hint),
            value = code,
            onValueChange = {
                if (validateVerificationCode(it)) {
                    code = it
                    onCodeChange()
                }},
            required = true,
            isError = serverError != null,
            errorRes = serverError,
            keyboardType = KeyboardType.Number
        )

        AppButton(
            text = stringResource(R.string.verify),
            size = ButtonSize.Max,
            variant = ButtonVariant.Tertiary,
            enabled = timeLeft > 0,
            onClick = { onVerifyClick(code) }
        )

        TextButton(onClick = {
            onResendClick()
            timeLeft = 120
            showCodeSentDialog = true
        }) {
            Text(
                text = stringResource(R.string.resend_code),
                color = colors.secondaryBlue,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (showCodeSentDialog) {
        ConfirmDialog(
            config = DialogConfig(
            title = stringResource(R.string.verification_title),
            message = stringResource(R.string.verification_code_sent, email ?: ""),
            confirmText = "OK",
            singleButton = true,
            variant = DialogVariant.Success,
            ),
            onConfirm = { showCodeSentDialog = false },
            onDismiss = { showCodeSentDialog = false }
        )
    }
}