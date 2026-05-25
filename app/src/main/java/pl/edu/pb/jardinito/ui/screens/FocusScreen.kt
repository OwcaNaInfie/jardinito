package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.theme.Dimensions
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.rememberSvgImageRequest
import pl.edu.pb.jardinito.viewmodel.FocusViewModel
import pl.edu.pb.jardinito.viewmodel.state.TimerState
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// =====================
// DATA CLASSES
// =====================

data class TimerUiState(
    val timerState: TimerState,
    val selectedDuration: Int,
    val remainingSeconds: Int,
    val progress: Float,
)

data class PlantUiState(
    val plants: List<Plant>,
    val selectedPlant: Plant?
)

data class TimerActions(
    val onDurationChange: (Int) -> Unit,
    val onStart: () -> Unit,
    val onPause: () -> Unit,
    val onResume: () -> Unit,
    val onStop: () -> Unit
)

data class TimerCallbacks(
    val onDurationChange: (Int) -> Unit,
    val onPlantClick: () -> Unit
)

data class TimerConfig(
    val devMode: Boolean = false,
    val minValue: Int = if (devMode) 5 else 15,
    val maxValue: Int = if (devMode) 60 else 120
) {
    companion object {
        fun forPlant(plant: Plant?, devMode: Boolean): TimerConfig {
            val base = TimerConfig(devMode = devMode)
            return base.copy(
                minValue = plant?.let {
                    if (devMode) it.minDurationDev else it.minDuration
                } ?: base.minValue
            )
        }
    }
}

data class TimerCanvasState(
    val isIdle: Boolean,
    val progress: Float,
    val selectionProgress: Float,
    val selectedDuration: Int,
    val minValue: Int
)

data class TimerCanvasColors(
    val selectionColor: Color,
    val timerColor: Color,
    val trackColor: Color
)

// =====================
// SCREEN
// =====================

@Composable
fun FocusScreen(focusViewModel: FocusViewModel, userId: String) {
    val timerState by focusViewModel.timerState.collectAsState()
    val selectedDuration by focusViewModel.selectedDuration.collectAsState()
    val remainingSeconds by focusViewModel.remainingSeconds.collectAsState()
    val progress by focusViewModel.progress.collectAsState()
    val plants by focusViewModel.plants.collectAsState()
    val selectedPlant by focusViewModel.selectedPlant.collectAsState()

    val config = TimerConfig.forPlant(selectedPlant, focusViewModel.devMode)

    FocusScreenContent(
        timerUiState = TimerUiState(
            timerState = timerState,
            selectedDuration = selectedDuration,
            remainingSeconds = remainingSeconds,
            progress = progress
        ),
        plantUiState = PlantUiState(plants = plants, selectedPlant = selectedPlant),
        timerActions = TimerActions(
            onDurationChange = {
                if (config.devMode) focusViewModel.setDurationDev(it)
                else focusViewModel.setDuration(it)
            },
            onStart = { focusViewModel.start(userId) },
            onPause = { focusViewModel.pause() },
            onResume = { focusViewModel.resume(userId) },
            onStop = { focusViewModel.stop(userId) }
        ),
        config = config,
        onPlantSelected = { focusViewModel.selectPlant(it) }
    )
}

@Composable
fun FocusScreenContent(
    timerUiState: TimerUiState,
    plantUiState: PlantUiState,
    timerActions: TimerActions,
    config: TimerConfig,
    onPlantSelected: (Plant) -> Unit
) {
    val isIdle = timerUiState.timerState is TimerState.Idle
    val isRunning = timerUiState.timerState is TimerState.Running
    val isPaused = timerUiState.timerState is TimerState.Paused
    var showPlantPicker by remember { mutableStateOf(false) }

    val minutes = timerUiState.remainingSeconds / 60
    val seconds = timerUiState.remainingSeconds % 60
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
            progress = timerUiState.progress,
            isIdle = isIdle,
            selectedDuration = timerUiState.selectedDuration,
            selectedPlant = plantUiState.selectedPlant,
            callbacks = TimerCallbacks(
                onDurationChange = timerActions.onDurationChange,
                onPlantClick = { if (isIdle) showPlantPicker = true }
            ),
            config = config
        )

        val unit = if (config.devMode) "s" else "min"

        Text(
            text = if (isIdle) "${timerUiState.selectedDuration} $unit" else timeText,
            style = MaterialTheme.typography.headlineLarge,
            color = colors.neutralGray,
            modifier = Modifier.padding(top = 24.dp)
        )

        TimerControls(
            isIdle = isIdle,
            isRunning = isRunning,
            isPaused = isPaused,
            actions = timerActions
        )
    }

    if (showPlantPicker) {
        PlantPickerDrawer(
            plants = plantUiState.plants,
            selectedPlant = plantUiState.selectedPlant,
            onPlantSelected = onPlantSelected,
            onDismiss = { showPlantPicker = false }
        )
    }
}

// =====================
// COMPONENTS
// =====================

@Composable
private fun TimerControls(
    isIdle: Boolean,
    isRunning: Boolean,
    isPaused: Boolean,
    actions: TimerActions
) {
    Row(
        modifier = Modifier.padding(top = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            isIdle -> AppButton(
                text = "Start",
                size = ButtonSize.Large,
                variant = ButtonVariant.Tertiary,
                onClick = actions.onStart
            )
            isRunning -> {
                AppButton(
                    iconVector = Icons.Default.Pause,
                    size = ButtonSize.Large,
                    circle = true,
                    variant = ButtonVariant.Primary,
                    onClick = actions.onPause
                )
                AppButton(
                    iconVector = Icons.Default.Stop,
                    size = ButtonSize.Large,
                    circle = true,
                    variant = ButtonVariant.Primary,
                    onClick = actions.onStop
                )
            }
            isPaused -> {
                AppButton(
                    iconVector = Icons.Default.PlayArrow,
                    size = ButtonSize.Large,
                    circle = true,
                    variant = ButtonVariant.Primary,
                    onClick = actions.onResume
                )
                AppButton(
                    iconVector = Icons.Default.Stop,
                    size = ButtonSize.Large,
                    circle = true,
                    variant = ButtonVariant.Primary,
                    onClick = actions.onStop
                )
            }
        }
    }
}

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
                        // Accept touch only within ring around circle edge (±40px)
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

        // Plant image
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

        // Track — full circle background
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

            // Handle dot
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
            // Timer arc — shrinks as time runs out
            drawArc(
                color = colors.timerColor,
                startAngle = -90f,
                sweepAngle = 360f * state.progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Timer dot — follows arc end
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

    // Block movement at exceeding values
    if (currentDuration >= maxValue && movingForward) return angle
    if (currentDuration <= minValue && !movingForward) return angle

    if (!movingBackward && diff > -180f) {
        onDurationChange(calculateSnappedDuration(normalizedAngle, devMode, minValue, maxValue))
    }

    return angle
}