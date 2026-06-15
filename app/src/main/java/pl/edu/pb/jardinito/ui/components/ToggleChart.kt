package pl.edu.pb.jardinito.ui.components

import android.graphics.Paint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_xs
import pl.edu.pb.jardinito.ui.theme.colors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class ChartEntry(
    val label: String,
    val value: Float,
    val color: Color
)

private enum class ChartType { Bar, Pie }

@Composable
fun ToggleChart(
    entries: List<ChartEntry>,
    modifier: Modifier = Modifier
) {
    var chartType by remember { mutableStateOf(ChartType.Bar) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(itemsSpacing_s)) {
        Row(horizontalArrangement = Arrangement.spacedBy(itemsSpacing_xs)) {
            AppChip(
                text = stringResource(R.string.chart_type_bar),
                variant = AppChipVariant.Outlined,
                isActive = chartType == ChartType.Bar,
                onClick = { chartType = ChartType.Bar }
            )
            AppChip(
                text = stringResource(R.string.chart_type_pie),
                variant = AppChipVariant.Outlined,
                isActive = chartType == ChartType.Pie,
                onClick = { chartType = ChartType.Pie }
            )
        }

        AnimatedContent(
            targetState = chartType,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { type ->
            when (type) {
                ChartType.Bar -> BarChart(
                    entries = entries,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
                ChartType.Pie -> PieChart(
                    entries = entries,
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                )
            }
        }
    }
}

@Composable
private fun BarChart(entries: List<ChartEntry>, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) return

    val total = entries.sumOf { it.value.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f
    val maxValue = entries.maxOf { it.value }.takeIf { it > 0f } ?: 1f
    val density = LocalDensity.current
    val textSizePx = with(density) { 10.sp.toPx() }
    val axisColor = colors.neutralInvisibleGray

    val neutralLightGray = colors.neutralInvisibleGray.toArgb()

    val yLabelPaint = remember(textSizePx, neutralLightGray) {
        Paint().apply {
            color = neutralLightGray
            textSize = textSizePx
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
    }
    val xLabelPaint = remember(textSizePx, neutralLightGray) {
        Paint().apply {
            color = neutralLightGray
            textSize = textSizePx
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier) {
        val yAxisWidth = 10.dp.toPx()
        val xAxisHeight = 18.dp.toPx()
        val topPadding = 20.dp.toPx()  // miejsce na % nad słupkami
        val chartWidth = size.width - yAxisWidth
        val chartHeight = size.height - xAxisHeight - topPadding
        val chartTop = topPadding
        val ySteps = 4

        // Gridlines + etykiety osi Y
        for (step in 0..ySteps) {
            val value = maxValue * step / ySteps
            val y = chartTop + chartHeight * (1f - step.toFloat() / ySteps)

            drawLine(
                color = axisColor.copy(alpha = 0.7f),
                start = Offset(yAxisWidth, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText(
                value.roundToInt().toString(),
                yAxisWidth - 6.dp.toPx(),
                y + textSizePx / 3f,
                yLabelPaint
            )
        }

        // Oś Y
        drawLine(
            color = axisColor.copy(alpha = 0.7f),
            start = Offset(yAxisWidth, chartTop),
            end = Offset(yAxisWidth, chartTop + chartHeight),
            strokeWidth = 1.5f
        )

        // Oś X
        drawLine(
            color = axisColor.copy(alpha = 0.7f),
            start = Offset(yAxisWidth, chartTop + chartHeight),
            end = Offset(size.width, chartTop + chartHeight),
            strokeWidth = 1.5f
        )

        // Słupki + etykiety
        val slotWidth = chartWidth / entries.size
        val barWidth = slotWidth * 0.55f
        val barPadding = (slotWidth - barWidth) / 2f

        entries.forEachIndexed { i, entry ->
            val barH = (entry.value / maxValue) * chartHeight
            val x = yAxisWidth + slotWidth * i + barPadding
            val barTop = chartTop + chartHeight - barH

            if (barH > 0f) {
                drawRoundRect(
                    color = entry.color,
                    topLeft = Offset(x, barTop),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                val pct = (entry.value / total * 100).roundToInt()
                drawContext.canvas.nativeCanvas.drawText(
                    "$pct%",
                    x + barWidth / 2f,
                    barTop - 4.dp.toPx(),
                    xLabelPaint
                )
            }

            if (entry.label.isNotEmpty()) {
                drawContext.canvas.nativeCanvas.drawText(
                    entry.label,
                    x + barWidth / 2f,
                    size.height - 4.dp.toPx(),
                    xLabelPaint
                )
            }
        }
    }
}

@Composable
private fun PieChart(entries: List<ChartEntry>, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) return

    val total = entries.sumOf { it.value.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f
    val density = LocalDensity.current
    val textSizePx = with(density) { 10.sp.toPx() }
    val neutralLightGray = colors.neutralLightGray
    val neutralLightGrayArgb = neutralLightGray.toArgb()

    val labelPaint = remember(textSizePx, neutralLightGrayArgb) {
        Paint().apply {
            color = neutralLightGrayArgb
            textSize = textSizePx
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier) {
        val labelSpace = 52.dp.toPx()
        val pieSize = minOf(size.width - labelSpace * 2f, size.height - 16.dp.toPx()) * 1f
        val radius = pieSize / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        var startAngle = -90f
        val boundaries = mutableListOf<Float>()

        entries.forEach { entry ->
            val sweepAngle = (entry.value / total) * 360f
            boundaries.add(startAngle)

            drawArc(
                color = entry.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f)
            )

            val midAngle = startAngle + sweepAngle / 2f
            val midAngleRad = midAngle * PI.toFloat() / 180f

            val lineStart = Offset(
                center.x + radius * cos(midAngleRad),
                center.y + radius * sin(midAngleRad)
            )
            val lineEnd = Offset(
                center.x + (radius + 14.dp.toPx()) * cos(midAngleRad),
                center.y + (radius + 14.dp.toPx()) * sin(midAngleRad)
            )

            val isRight = lineEnd.x >= center.x
            val horizLen = 10.dp.toPx()
            val textAnchor = Offset(
                lineEnd.x + if (isRight) horizLen else -horizLen,
                lineEnd.y
            )

            drawLine(color = neutralLightGray, start = lineStart, end = lineEnd, strokeWidth = 1.5f)
            drawLine(color = neutralLightGray, start = lineEnd, end = textAnchor, strokeWidth = 1.5f)

            labelPaint.textAlign = if (isRight) Paint.Align.LEFT else Paint.Align.RIGHT
            val label = if (entry.label.length > 8) "${entry.label.take(7)}…" else entry.label
            drawContext.canvas.nativeCanvas.drawText(
                label,
                textAnchor.x + if (isRight) 3.dp.toPx() else -3.dp.toPx(),
                textAnchor.y + textSizePx / 3f,
                labelPaint
            )

            startAngle += sweepAngle
        }
        boundaries.forEach { angle ->
            val rad = angle * PI.toFloat() / 180f
            drawLine(
                color = Color.White,
                start = center,
                end = Offset(center.x + radius * cos(rad), center.y + radius * sin(rad)),
                strokeWidth = 1.dp.toPx()
            )
        }

        drawCircle(color = Color.White, radius = radius * 0.4f, center = center)
    }
}