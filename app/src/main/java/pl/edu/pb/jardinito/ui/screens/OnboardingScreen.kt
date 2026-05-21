package pl.edu.pb.jardinito.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    var animationFinished by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetYFraction = remember { Animatable(0.5f) }
    val backgroundAlpha = remember { Animatable(0f) }
    val buttonsAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        titleAlpha.animateTo(1f, tween(1500, delayMillis = 500))
        delay(2500)
        titleOffsetYFraction.animateTo(0.27f, tween(2000))
        delay(2500)
        backgroundAlpha.animateTo(1f, tween(1500))
        delay(1500)
        buttonsAlpha.animateTo(1f, tween(1000))
        animationFinished = true
    }

    OnboardingScreenContent(
        animationState = OnboardingAnimationState(
            titleAlpha = titleAlpha.value,
            titleOffsetYFraction = titleOffsetYFraction.value,
            backgroundAlpha = backgroundAlpha.value,
            buttonsAlpha = buttonsAlpha.value,
            animationFinished = animationFinished
        ),
        onLoginClick = onLoginClick,
        onRegisterClick = onRegisterClick,
        onSkipAnimation = {
            if (!animationFinished) {
                scope.launch {
                    titleAlpha.snapTo(1f)
                    titleOffsetYFraction.snapTo(0.27f)
                    backgroundAlpha.snapTo(1f)
                    buttonsAlpha.snapTo(1f)
                    animationFinished = true
                }
            }
        }
    )
}

@Composable
private fun OnboardingScreenContent(
    animationState: OnboardingAnimationState,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onSkipAnimation: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary100)
            .windowInsetsPadding(WindowInsets(0, 0, 0, 0))
    ) {
        val screenHeight = maxHeight
        val titleOffsetY = screenHeight * animationState.titleOffsetYFraction

        Image(
            painter = painterResource(R.drawable.onboarding_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = animationState.backgroundAlpha }
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Jardinito",
                style = MaterialTheme.typography.displayLarge,
                color = colors.neutralLight.copy(alpha = animationState.titleAlpha),
                modifier = Modifier.offset(y = titleOffsetY),
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = screenHeight * 0.45f)
                .graphicsLayer { alpha = animationState.buttonsAlpha },
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppButton(
                text = stringResource(R.string.login),
                size = ButtonSize.Medium,
                variant = ButtonVariant.Primary,
                enabled = animationState.animationFinished,
                onClick = onLoginClick
            )
            AppButton(
                text = stringResource(R.string.register),
                size = ButtonSize.Medium,
                variant = ButtonVariant.Primary,
                enabled = animationState.animationFinished,
                onClick = onRegisterClick
            )
        }
    }

    if (!animationState.animationFinished) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onSkipAnimation() }
        )
    }
}

data class OnboardingAnimationState(
    val titleAlpha: Float,
    val titleOffsetYFraction: Float,
    val backgroundAlpha: Float,
    val buttonsAlpha: Float,
    val animationFinished: Boolean
)

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun OnboardingPreviewFinal() {
    JardinitoTheme {
        OnboardingScreenContent(
            animationState = OnboardingAnimationState(
                titleAlpha = 1f,
                titleOffsetYFraction = 0.27f,
                backgroundAlpha = 1f,
                buttonsAlpha = 1f,
                animationFinished = true
            ),
            onLoginClick = {},
            onRegisterClick = {},
            onSkipAnimation = {}
        )
    }
}
