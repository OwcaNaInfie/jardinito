package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.auth.AuthSheetState
import pl.edu.pb.jardinito.ui.components.AuthBottomSheet
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.DialogVariant
import pl.edu.pb.jardinito.ui.components.LoadingOverlay
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import pl.edu.pb.jardinito.viewmodel.PasswordResetViewModel
import pl.edu.pb.jardinito.viewmodel.VerificationViewModel
import pl.edu.pb.jardinito.viewmodel.state.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthEntryScreen(
    authViewModel: AuthViewModel,
    verificationViewModel: VerificationViewModel,
    passwordResetViewModel: PasswordResetViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var sheetContent by remember { mutableStateOf<AuthSheetState?>(null) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiState by authViewModel.uiState.collectAsState()
    val pendingEmail by verificationViewModel.pendingEmail.collectAsState()
    var showUnverifiedDialog by remember { mutableStateOf(false) }
    var showCodeSentDialog by remember { mutableStateOf(false) }
    var showResetCodeSentDialog by remember { mutableStateOf(false) }

    fun switchSheet(target: AuthSheetState) {
        scope.launch {
            if (sheetContent != null) sheetState.hide()
            delay(300)
            sheetContent = target
            sheetState.show()
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthState.VerificationRequired -> switchSheet(AuthSheetState.AccountVerification)
            is AuthState.UnverifiedAccount -> showUnverifiedDialog = true
            is AuthState.PasswordResetRequired -> showResetCodeSentDialog = true
            is AuthState.PasswordResetSuccess -> switchSheet(AuthSheetState.Login)
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        OnboardingScreen(
            onLoginClick = { switchSheet(AuthSheetState.Login) },
            onRegisterClick = { switchSheet(AuthSheetState.Register) }
        )

        sheetContent?.let { state ->
            AuthBottomSheet(
                sheetState = sheetState,
                onDismiss = {
                    sheetContent = null
                    authViewModel.resetUiState()
                    passwordResetViewModel.resetPasswordFlow()
                    passwordResetViewModel.resetPasswordFormClear()
                }
            ) {
                AuthSheetContent(
                    state = state,
                    authViewModel = authViewModel,
                    verificationViewModel = verificationViewModel,
                    passwordResetViewModel = passwordResetViewModel,
                    pendingEmail = pendingEmail,
                    onLoginSuccess = { sheetContent = null; onLoginSuccess() },
                    onRegisterSuccess = { sheetContent = null; onRegisterSuccess() },
                    onGoogleSignInClick = onGoogleSignInClick,
                    onSwitchSheet = { switchSheet(it) }
                )
            }
        }

        AuthDialogs(
            showUnverifiedDialog = showUnverifiedDialog,
            showCodeSentDialog = showCodeSentDialog,
            showResetCodeSentDialog = showResetCodeSentDialog,
            pendingEmail = pendingEmail,
            onUnverifiedConfirm = {
                showUnverifiedDialog = false
                verificationViewModel.resendVerification()
                showCodeSentDialog = true
            },
            onUnverifiedDismiss = { showUnverifiedDialog = false },
            onCodeSentConfirm = {
                showCodeSentDialog = false
                scope.launch { switchSheet(AuthSheetState.AccountVerification) }
            },
            onCodeSentDismiss = {
                showCodeSentDialog = false
                scope.launch { switchSheet(AuthSheetState.AccountVerification) }
            },
            onResetCodeSentDismiss = { showResetCodeSentDialog = false }
        )

        if (uiState is AuthState.Loading) {
            LoadingOverlay()
        }
    }
}

@Composable
private fun AuthSheetContent(
    state: AuthSheetState,
    authViewModel: AuthViewModel,
    verificationViewModel: VerificationViewModel,
    passwordResetViewModel: PasswordResetViewModel,
    pendingEmail: String?,
    onLoginSuccess: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onSwitchSheet: (AuthSheetState) -> Unit
) {
    when (state) {
        AuthSheetState.Login -> LoginScreen(
            authViewModel = authViewModel,
            onLoginSuccess = onLoginSuccess,
            onRegisterClick = { onSwitchSheet(AuthSheetState.Register) },
            onGoogleSignInClick = onGoogleSignInClick,
            onForgotPasswordClick = { onSwitchSheet(AuthSheetState.ForgotPassword) }
        )
        AuthSheetState.Register -> RegisterScreen(
            authViewModel = authViewModel,
            onRegisterSuccess = onRegisterSuccess,
            onLoginClick = { onSwitchSheet(AuthSheetState.Login) },
            onGoogleSignInClick = onGoogleSignInClick
        )
        AuthSheetState.AccountVerification -> VerificationScreen(
            verificationViewModel = verificationViewModel,
            email = pendingEmail,
            onVerificationSuccess = onRegisterSuccess
        )
        AuthSheetState.ForgotPassword -> ForgotPasswordScreen(
            passwordResetViewModel = passwordResetViewModel,
            onPasswordResetSuccess = { onSwitchSheet(AuthSheetState.Login) }
        )
    }
}

@Composable
private fun AuthDialogs(
    showUnverifiedDialog: Boolean,
    showCodeSentDialog: Boolean,
    showResetCodeSentDialog: Boolean,
    pendingEmail: String?,
    onUnverifiedConfirm: () -> Unit,
    onUnverifiedDismiss: () -> Unit,
    onCodeSentConfirm: () -> Unit,
    onCodeSentDismiss: () -> Unit,
    onResetCodeSentDismiss: () -> Unit
) {
    if (showUnverifiedDialog) {
        ConfirmDialog(
            title = stringResource(R.string.verification_title),
            message = stringResource(R.string.verification_resend_prompt),
            confirmText = stringResource(R.string.resend_code),
            dismissText = stringResource(R.string.cancel),
            onConfirm = onUnverifiedConfirm,
            onDismiss = onUnverifiedDismiss
        )
    }

    if (showCodeSentDialog) {
        ConfirmDialog(
            title = stringResource(R.string.verification_title),
            message = stringResource(R.string.verification_code_sent, pendingEmail ?: ""),
            confirmText = "OK",
            singleButton = true,
            variant = DialogVariant.Success,
            onConfirm = onCodeSentConfirm,
            onDismiss = onCodeSentDismiss
        )
    }

    if (showResetCodeSentDialog) {
        ConfirmDialog(
            title = stringResource(R.string.reset_password),
            message = stringResource(R.string.reset_code_sent),
            confirmText = "OK",
            singleButton = true,
            variant = DialogVariant.Success,
            onConfirm = onResetCodeSentDismiss,
            onDismiss = onResetCodeSentDismiss
        )
    }
}