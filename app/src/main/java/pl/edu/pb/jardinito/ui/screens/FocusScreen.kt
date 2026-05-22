package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.theme.Dimensions
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.viewmodel.FocusViewModel
import pl.edu.pb.jardinito.viewmodel.state.TimerState
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun FocusScreen(focusViewModel: FocusViewModel) {
    val timerState by focusViewModel.timerState.collectAsState()
    val selectedDuration by focusViewModel.selectedDuration.collectAsState()
    val remainingSeconds by focusViewModel.remainingSeconds.collectAsState()
    val progress by focusViewModel.progress.collectAsState()

    FocusScreenContent(
        timerState = timerState,
        selectedDuration = selectedDuration,
        remainingSeconds = remainingSeconds,
        progress = progress,
        onDurationChange = {
            // DEV:
            focusViewModel.setDurationDev(it)
            // PROD:
            // focusViewModel.setDuration(it)
        },
        onStart = { focusViewModel.start() },
        onPause = { focusViewModel.pause() },
        onResume = { focusViewModel.resume() },
        onStop = { focusViewModel.stop() }
    )
}

@Composable
fun FocusScreenContent(
    timerState: TimerState,
    selectedDuration: Int,
    remainingSeconds: Int,
    progress: Float,
    onDurationChange: (Int) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    val isIdle = timerState is TimerState.Idle
    val isRunning = timerState is TimerState.Running
    val isPaused = timerState is TimerState.Paused

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeText = "%d:%02d".format(minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary50)
            .padding(top = Dimensions.topBarHeight, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularTimer(
            progress = progress,
            isIdle = isIdle,
            timeText = timeText,
            selectedDuration = selectedDuration,
            onDurationChange = onDurationChange
        )

        Text(
            text = if (isIdle) "$selectedDuration min" else timeText,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.neutralGray,
            modifier = Modifier.padding(top = 24.dp)
        )

        Row(
            modifier = Modifier.padding(top = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                isIdle -> {
                    AppButton(
                        text = "Start",
                        size = ButtonSize.Large,
                        variant = ButtonVariant.Tertiary,
                        onClick = onStart
                    )
                }
                isRunning -> {
                    AppButton(
                        iconVector = Icons.Default.Pause,
                        size = ButtonSize.Large,
                        circle = true,
                        variant = ButtonVariant.Primary,
                        onClick = onPause
                    )
                    AppButton(
                        iconVector = Icons.Default.Stop,
                        size = ButtonSize.Large,
                        circle = true,
                        variant = ButtonVariant.Primary,
                        onClick = onStop
                    )
                }
                isPaused -> {
                    AppButton(
                        iconVector = Icons.Default.PlayArrow,
                        size = ButtonSize.Large,
                        circle = true,
                        variant = ButtonVariant.Primary,
                        onClick = onResume
                    )
                    AppButton(
                        iconVector = Icons.Default.Stop,
                        size = ButtonSize.Large,
                        circle = true,
                        variant = ButtonVariant.Primary,
                        onClick = onStop
                    )
                }
            }
        }
    }
}

@Composable
fun CircularTimer(
    progress: Float,
    isIdle: Boolean,
    timeText: String,
    selectedDuration: Int,
    onDurationChange: (Int) -> Unit,
    devMode: Boolean = true
) {
    val strokeWidth = 20.dp
    val size = 260.dp

    val selectionColor = colors.primary700
    val timerColor = colors.primary300
    val trackColor = colors.primary500

    val maxValue = if (devMode) 60 else 120
    val minValue = if (devMode) 5 else 15

    val selectionProgress = (selectedDuration - minValue).toFloat() / (maxValue - minValue).toFloat()

    val currentDuration = rememberUpdatedState(selectedDuration)
    val lastAngle = remember { mutableStateOf(0f) }

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
                        val distanceFromCenter = kotlin.math.sqrt(dx * dx + dy * dy)
                        val radius = this.size.width / 2f

                        // Akceptuj dotyk tylko w pierścieniu wokół koła (±40px od krawędzi)
                        if (kotlin.math.abs(distanceFromCenter - radius) > 40f) return@detectDragGestures

                        lastAngle.value = atan2(dy, dx) * (180f / PI.toFloat()) + 90f
                    }
                ) { change, _ ->
                    change.consume()
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    val angle = atan2(
                        change.position.y - center.y,
                        change.position.x - center.x
                    ) * (180f / PI.toFloat()) + 90f

                    val normalizedAngle = if (angle < 0) angle + 360f else angle
                    val normalizedLast = if (lastAngle.value < 0) lastAngle.value + 360f else lastAngle.value
                    val diff = normalizedAngle - normalizedLast

                    val movingForward = diff > 0 || diff < -180f
                    val movingBackward = diff > 180f

                    if (currentDuration.value >= maxValue && movingForward) {
                        lastAngle.value = angle
                        return@detectDragGestures
                    }

                    if (!movingBackward && diff > -180f) {
                        val fraction = normalizedAngle / 360f
                        val step = if (devMode) 5 else 15
                        val range = if (devMode) 60 else 120
                        val raw = fraction * (maxValue - minValue) + minValue
                        val snapped = ((raw / step).roundToInt() * step).coerceIn(minValue, maxValue)
                        onDurationChange(snapped)
                    }

                    lastAngle.value = angle
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val diameter = this.size.width - strokePx
            val topLeft = Offset(strokePx / 2, strokePx / 2)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokePx, cap = StrokeCap.Round)
            )

            if (isIdle) {
                if (selectedDuration > minValue) {
                    drawArc(
                        color = selectionColor,
                        startAngle = -90f,
                        sweepAngle = 360f * selectionProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(strokePx, cap = StrokeCap.Round)
                    )
                }

                val angleRad = (-90f + 360f * selectionProgress) * (PI / 180f).toFloat()
                val radius = diameter / 2
                drawCircle(
                    color = selectionColor,
                    radius = strokePx,
                    center = Offset(
                        x = center.x + radius * cos(angleRad),
                        y = center.y + radius * sin(angleRad)
                    )
                )
            } else {
                drawArc(
                    color = timerColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokePx, cap = StrokeCap.Round)
                )

                val angleRad = (-90f + 360f * progress) * (PI / 180f).toFloat()
                val radius = diameter / 2
                drawCircle(
                    color = timerColor,
                    radius = strokePx,
                    center = Offset(
                        x = center.x + radius * cos(angleRad),
                        y = center.y + radius * sin(angleRad)
                    )
                )
            }
        }

        Text(
            text = timeText,
            style = MaterialTheme.typography.displaySmall,
            color = colors.neutralBlack
        )
    }
}