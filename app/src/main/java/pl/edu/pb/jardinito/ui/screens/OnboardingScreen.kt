package pl.edu.pb.jardinito.ui.screens

import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.ui.theme.colors

@Composable
fun OnboardingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
    ) {
    var showTitle by remember { mutableStateOf(false) }
    var moveTitle by remember { mutableStateOf(false) }
    var showBackground by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showTitle = true
        delay(2500)
        moveTitle = true
        delay(2500)
        showBackground = true
        delay(1500)
        showButtons = true
    }

    OnboardingScreenContent(
        showTitle = showTitle,
        moveTitle = moveTitle,
        showBackground = showBackground,
        showButtons = showButtons,
        onLoginClick = onLoginClick,
        onRegisterClick = onRegisterClick
    )
}

@Composable
private fun OnboardingScreenContent(
    showTitle: Boolean,
    moveTitle: Boolean,
    showBackground: Boolean,
    showButtons: Boolean,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit

) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary100)
    ) {
        val screenHeight = maxHeight

        val (titleAlpha, titleOffsetY, backgroundAlpha, buttonsAlpha) =
            rememberOnboardingAnimations(
                screenHeight = screenHeight,
                showTitle = showTitle,
                moveTitle = moveTitle,
                showBackground = showBackground,
                showButtons = showButtons
            )

        Image(
            painter = painterResource(R.drawable.onboarding_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = backgroundAlpha }
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Jardinito",
                style = MaterialTheme.typography.displayLarge,
                color = colors.neutralLight.copy(alpha = titleAlpha),
                modifier = Modifier.offset(y = titleOffsetY),
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = screenHeight * 0.45f)
                .graphicsLayer { alpha = buttonsAlpha },
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppButton(
                text = stringResource(R.string.login),
                size = ButtonSize.Medium,
                variant = ButtonVariant.Primary,
                onClick = onLoginClick
            )
            AppButton(
                text = stringResource(R.string.register),
                size = ButtonSize.Medium,
                variant = ButtonVariant.Primary,
                onClick = onRegisterClick
            )
        }
    }
}

// UI MODELS
data class OnboardingAnimations(
    val titleAlpha: Float,
    val titleOffsetY: Dp,
    val backgroundAlpha: Float,
    val buttonsAlpha: Float
)

// ANIMATION LOGIC
@Composable
private fun rememberOnboardingAnimations(
    screenHeight: Dp,
    showTitle: Boolean,
    moveTitle: Boolean,
    showBackground: Boolean,
    showButtons: Boolean
): OnboardingAnimations {

    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, delayMillis = 500),
        label = "titleAlpha"
    )

    val titleOffsetY by animateFloatAsState(
        targetValue = if (moveTitle) screenHeight.value * 0.27f else screenHeight.value * 0.5f,
        animationSpec = tween(durationMillis = 2000),
        label = "titleOffset"
    )

    val backgroundAlpha by animateFloatAsState(
        targetValue = if (showBackground) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "backgroundAlpha"
    )

    val buttonsAlpha by animateFloatAsState(
        targetValue = if (showButtons) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "buttonsAlpha"
    )

    return OnboardingAnimations(
        titleAlpha = titleAlpha,
        titleOffsetY = titleOffsetY.dp,
        backgroundAlpha = backgroundAlpha,
        buttonsAlpha = buttonsAlpha
    )
}

//@Preview(showBackground = true, apiLevel = 34)
//@Composable
//fun OnboardingPreviewTitleCentered() {
//    JardinitoTheme {
//        OnboardingScreenContent(
//            showTitle = true,
//            moveTitle = false,
//            showBackground = false,
//            showButtons = false,
//            onLoginClick = {},
//            onRegisterClick = {}
//        )
//    }
//}
//
//@Preview(showBackground = true, apiLevel = 34)
//@Composable
//fun OnboardingPreviewTitleMovedUp() {
//    JardinitoTheme {
//        OnboardingScreenContent(
//            showTitle = true,
//            moveTitle = true,
//            showBackground = false,
//            showButtons = false,
//            onLoginClick = {},
//            onRegisterClick = {}
//        )
//    }
//}
//
//@Preview(showBackground = true, apiLevel = 34)
//@Composable
//fun OnboardingPreviewWithBackground() {
//    JardinitoTheme {
//        OnboardingScreenContent(
//            showTitle = true,
//            moveTitle = true,
//            showBackground = true,
//            showButtons = false,
//            onLoginClick = {},
//            onRegisterClick = {}
//        )
//    }
//}
//
@Preview(showBackground = true, apiLevel = 34)
@Composable
fun OnboardingPreviewFinal() {
    JardinitoTheme {
        OnboardingScreenContent(
            showTitle = true,
            moveTitle = true,
            showBackground = true,
            showButtons = true,
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}
