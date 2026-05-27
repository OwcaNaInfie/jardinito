package pl.edu.pb.jardinito.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

fun slideUpEnter(durationMillis: Int = 700): EnterTransition =
    slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(durationMillis)
    ) + fadeIn(animationSpec = tween(durationMillis))

fun slideUpExit(durationMillis: Int = 700): ExitTransition =
    slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(durationMillis)
    ) + fadeOut(animationSpec = tween(durationMillis))