package pl.edu.pb.jardinito.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R

@Composable
fun NatureBackground(modifier: Modifier = Modifier) {
    val offsetAnim = remember { Animatable(100f) }
    LaunchedEffect(Unit) {
        offsetAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 600, easing = EaseOutCubic)
        )
    }
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.bg_sky),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.2f)
                .offset(y = (offsetAnim.value * 0.5f).dp)
        )
        Image(
            painter = painterResource(R.drawable.bg_grass),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxSize()
                .offset(y = offsetAnim.value.dp)
                .align(Alignment.BottomCenter)
        )
    }
}