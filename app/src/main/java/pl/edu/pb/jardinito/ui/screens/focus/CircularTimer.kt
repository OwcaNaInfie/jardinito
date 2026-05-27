package pl.edu.pb.jardinito.ui.screens.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.rememberSvgImageRequest
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CircularTimer(
    progress: Float,
    isIdle: Boolean,
    selectedDuration: Int,
    selectedPlant: Plant?,
    callbacks: TimerCallbacks,
    config: TimerConfig = TimerConfig()
) {
    val strokeWidth = 20.dp
    val size = 260.dp

    val canvasColors = TimerCanvasColors(
        selectionColor = colors.primary700,
        timerColor = colors.primary300,
        trackColor = colors.primary500
    )

    val maxValue = config.maxValue
    val minValue = config.minValue
    val selectionProgress = selectedDuration.toFloat() / maxValue.toFloat()

    val currentDuration = rememberUpdatedState(selectedDuration)
    val currentMinValue = rememberUpdatedState(minValue)
    val currentMaxValue = rememberUpdatedState(maxValue)
    val lastAngle = remember { mutableStateOf(0f) }
    val strokePx = with(LocalDensity.current) { strokeWidth.toPx() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .pointerInput(isIdle) {
                if (!isIdle) return@pointerInput
                detectDragGestures(
                    onDragStart = { position ->
                        val center = Offset(this.size.width / 2f, this.size.height / 2f)
                        val dx = position.x - center.x
                        val dy = position.y - center.y
                        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                        if (kotlin.math.abs(distance - this.size.width / 2f) > 40f) return@detectDragGestures
                        lastAngle.value = atan2(dy, dx) * (180f / PI.toFloat()) + 90f
                    }
                ) { change, _ ->
                    change.consume()
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    val angle = atan2(
                        change.position.y - center.y,
                        change.position.x - center.x
                    ) * (180f / PI.toFloat()) + 90f

                    lastAngle.value = handleDragGesture(
                        angle = angle,
                        lastAngle = lastAngle.value,
                        currentDuration = currentDuration.value,
                        maxValue = currentMaxValue.value,
                        devMode = config.devMode,
                        minValue = currentMinValue.value,
                        onDurationChange = callbacks.onDurationChange
                    )
                }
            }
    ) {
        TimerCanvas(
            modifier = Modifier.fillMaxSize(),
            state = TimerCanvasState(
                isIdle = isIdle,
                progress = progress,
                selectionProgress = selectionProgress,
                selectedDuration = selectedDuration,
                minValue = minValue
            ),
            colors = canvasColors,
            strokeWidth = strokePx
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .then(if (isIdle) Modifier.clickable { callbacks.onPlantClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            selectedPlant?.let { plant ->
                val imageUrl = "${RetrofitInstance.BASE_URL}plants/${plant.images.medium}"
                AsyncImage(
                    model = rememberSvgImageRequest(imageUrl),
                    contentDescription = plant.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                    filterQuality = FilterQuality.None,
                )
            }
        }
    }
}

@Composable
private fun TimerCanvas(
    modifier: Modifier = Modifier,
    state: TimerCanvasState,
    colors: TimerCanvasColors,
    strokeWidth: Float
) {
    Canvas(modifier = modifier) {
        val diameter = this.size.width - strokeWidth
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        val arcSize = Size(diameter, diameter)

        drawArc(
            color = colors.trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )

        if (state.isIdle) {
            drawArc(
                color = colors.selectionColor,
                startAngle = -90f,
                sweepAngle = 360f * state.selectionProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            val angleRad = (-90f + 360f * state.selectionProgress) * (PI / 180f).toFloat()
            val radius = diameter / 2
            drawCircle(
                color = colors.selectionColor,
                radius = strokeWidth,
                center = Offset(
                    x = center.x + radius * cos(angleRad),
                    y = center.y + radius * sin(angleRad)
                )
            )
        } else {
            drawArc(
                color = colors.timerColor,
                startAngle = -90f,
                sweepAngle = 360f * state.progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            val angleRad = (-90f + 360f * state.progress) * (PI / 180f).toFloat()
            val radius = diameter / 2
            drawCircle(
                color = colors.timerColor,
                radius = strokeWidth,
                center = Offset(
                    x = center.x + radius * cos(angleRad),
                    y = center.y + radius * sin(angleRad)
                )
            )
        }
    }
}

// =====================
// HELPERS
// =====================

private fun calculateSnappedDuration(
    normalizedAngle: Float,
    devMode: Boolean,
    minValue: Int,
    maxValue: Int
): Int {
    val fraction = normalizedAngle / 360f
    val step = if (devMode) 5 else 15
    val raw = fraction * maxValue
    return ((raw / step).roundToInt() * step).coerceIn(minValue, maxValue)
}

private fun handleDragGesture(
    angle: Float,
    lastAngle: Float,
    currentDuration: Int,
    maxValue: Int,
    devMode: Boolean,
    minValue: Int,
    onDurationChange: (Int) -> Unit
): Float {
    val normalizedAngle = if (angle < 0) angle + 360f else angle
    val normalizedLast = if (lastAngle < 0) lastAngle + 360f else lastAngle
    val diff = normalizedAngle - normalizedLast
    val movingForward = diff > 0 || diff < -180f
    val movingBackward = diff > 180f

    if (currentDuration >= maxValue && movingForward) return angle
    if (currentDuration <= minValue && !movingForward) return angle

    if (!movingBackward && diff > -180f) {
        onDurationChange(calculateSnappedDuration(normalizedAngle, devMode, minValue, maxValue))
    }

    return angle
}