package pl.edu.pb.jardinito.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.data.model.AuthSheetState
import pl.edu.pb.jardinito.ui.components.AuthBottomSheet
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import androidx.compose.material3.rememberModalBottomSheetState

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

//    fun switchSheet(target: AuthSheetState) {
//        scope.launch {
//            sheetContent =
//                null
//            delay(300)
//            sheetContent = target
//        }
//    }
fun switchSheet(target: AuthSheetState) {
    scope.launch {
        if (sheetContent != null) {
            sheetState.hide()   // animacja zamknięcia
            delay(300)          // poczekaj, aż animacja się skończy
        }
        sheetContent = target
        sheetState.show()      // animacja otwarcia
    }
}

    Box(modifier = Modifier.fillMaxSize()) {
        // =====================
        // ONBOARDING (tło)
        // =====================
        OnboardingScreen(
            onLoginClick = { switchSheet(AuthSheetState.Login) },
            onRegisterClick = { switchSheet(AuthSheetState.Register) }
        )
        // =====================
        // BOTTOM SHEET
        // =====================
        sheetContent?.let { state ->
            AuthBottomSheet(
                state = state,
                onDismiss = { sheetContent = null }
            ) {
                when (state) {
                    AuthSheetState.Login -> LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = {
                            sheetContent = null
                            onLoginSuccess ()
                                         },
                        onRegisterClick = { sheetContent = AuthSheetState.Register },
                        onGoogleSignInClick = onGoogleSignInClick
                    )
                    AuthSheetState.Register -> RegisterScreen(
                        authViewModel = authViewModel,
                        onRegisterSuccess = {
                            sheetContent = null
                            onRegisterSuccess() },
                        onLoginClick = { sheetContent = AuthSheetState.Login },
                        onGoogleSignInClick = onGoogleSignInClick
                    )
                }
            }
        }
    }
}
