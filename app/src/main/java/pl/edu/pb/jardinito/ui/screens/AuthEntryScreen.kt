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
import pl.edu.pb.jardinito.data.model.AuthSheetState
import pl.edu.pb.jardinito.ui.components.AuthBottomSheet
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.DialogVariant
import pl.edu.pb.jardinito.ui.components.LoadingOverlay
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import pl.edu.pb.jardinito.viewmodel.state.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthEntryScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var sheetContent by remember { mutableStateOf<AuthSheetState?>(null) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiState by authViewModel.uiState.collectAsState()
    val pendingEmail by authViewModel.pendingEmail.collectAsState()
    var showUnverifiedDialog by remember { mutableStateOf(false) }
    var showCodeSentDialog by remember { mutableStateOf(false) }
    var showResetCodeSentDialog by remember { mutableStateOf(false) }


    fun switchSheet(target: AuthSheetState) {
        scope.launch {
            if (sheetContent != null) {
                sheetState.hide()
                delay(300)
                sheetContent = target
                sheetState.show()
            } else {
                sheetContent = target
                delay(300)
                sheetState.show()
            }
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthState.VerificationRequired -> {
                switchSheet(AuthSheetState.AccountVerification)
            }
            is AuthState.UnverifiedAccount -> {
                showUnverifiedDialog = true
            }
            is AuthState.PasswordResetRequired -> showResetCodeSentDialog = true
            is AuthState.PasswordResetSuccess -> switchSheet(AuthSheetState.Login)
            else -> {}
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
                    authViewModel.resetPasswordFlow()
                }
            ) {
                when (state) {
                    AuthSheetState.Login -> LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = {
                            sheetContent = null
                            onLoginSuccess()
                        },
                        onRegisterClick = { switchSheet(AuthSheetState.Register) },
                        onGoogleSignInClick = onGoogleSignInClick,
                        onForgotPasswordClick = { switchSheet(AuthSheetState.ForgotPassword) }
                    )
                    AuthSheetState.Register -> RegisterScreen(
                        authViewModel = authViewModel,
                        onRegisterSuccess = {
                            sheetContent = null
                            onRegisterSuccess()
                        },
                        onLoginClick = { switchSheet(AuthSheetState.Login) },
                        onGoogleSignInClick = onGoogleSignInClick
                    )
                    AuthSheetState.AccountVerification -> VerificationScreen(
                        authViewModel = authViewModel,
                        email = pendingEmail,
                        onVerificationSuccess = {
                            sheetContent = null
                            onRegisterSuccess()
                        }
                    )
                    AuthSheetState.ForgotPassword -> ForgotPasswordScreen(
                        authViewModel = authViewModel,
                        onPasswordResetSuccess = {
                            switchSheet(AuthSheetState.Login)
                        }
                    )
                }
            }
        }

        if (showUnverifiedDialog) {
            ConfirmDialog(
                title = stringResource(R.string.verification_title),
                message = stringResource(R.string.verification_resend_prompt),
                confirmText = stringResource(R.string.resend_code),
                dismissText = stringResource(R.string.cancel),
                onConfirm = {
                    showUnverifiedDialog = false
                    authViewModel.resendVerification()
                    showCodeSentDialog = true
                },
                onDismiss = { showUnverifiedDialog = false }
            )
        }
        if (showCodeSentDialog) {
            ConfirmDialog(
                title = stringResource(R.string.verification_title),
                message = stringResource(R.string.verification_code_sent, pendingEmail ?: ""),
                confirmText = "OK",
                singleButton = true,
                variant = DialogVariant.Success,
                onConfirm = {
                    showCodeSentDialog = false
                    scope.launch {
                        switchSheet(AuthSheetState.AccountVerification)
                    }
                },
                onDismiss = {
                    showCodeSentDialog = false
                    scope.launch {
                        switchSheet(AuthSheetState.AccountVerification)
                    }
                }
            )
        }

        if (showResetCodeSentDialog) {
            ConfirmDialog(
                title = stringResource(R.string.reset_password),
                message = stringResource(R.string.reset_code_sent),
                confirmText = "OK",
                singleButton = true,
                variant = DialogVariant.Success,
                onConfirm = { showResetCodeSentDialog = false },
                onDismiss = { showResetCodeSentDialog = false }
            )
        }

        if (uiState is AuthState.Loading) {
            LoadingOverlay()
        }
    }
}