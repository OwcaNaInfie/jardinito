package pl.edu.pb.jardinito.ui.components.charts

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.edu.pb.jardinito.ui.theme.colors
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

private const val ROTATED_LABELS_THRESHOLD = 7

@Composable
fun BarChart(
    entries: List<ChartEntry>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
) {
    if (entries.isEmpty()) return

    val total = entries.sumOf { it.value.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f
    val maxValue = entries.maxOf { it.value }.takeIf { it > 0f } ?: 1f
    val maxValueInt = maxValue.toInt().coerceAtLeast(1)
    val density = LocalDensity.current
    val textSizePx = with(density) { 10.sp.toPx() }
    val xLabelSizePx = with(density) {
        if (entries.size > ROTATED_LABELS_THRESHOLD) 8.sp.toPx() else 10.sp.toPx()
    }
    val maxBarWidthPx = with(density) { 20.dp.toPx() }
    val axisColor = colors.neutralInvisibleGray
    val neutralLightGrayArgb = colors.neutralInvisibleGray.toArgb()
    val rotateLabels = entries.size > ROTATED_LABELS_THRESHOLD

    val yLabelPaint = remember(textSizePx, neutralLightGrayArgb) {
        Paint().apply {
            color = neutralLightGrayArgb
            textSize = textSizePx
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
    }
    val xLabelPaint = remember(xLabelSizePx, neutralLightGrayArgb, rotateLabels) {
        Paint().apply {
            color = neutralLightGrayArgb
            textSize = xLabelSizePx
            textAlign = if (rotateLabels) Paint.Align.RIGHT else Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    val pctLabelPaint = remember(textSizePx, neutralLightGrayArgb) {
        Paint().apply {
            color = neutralLightGrayArgb
            textSize = textSizePx
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier) {
        val yAxisWidth = 10.dp.toPx()
        val xAxisHeight = 18.dp.toPx()
        val topPadding = 20.dp.toPx()
        val chartWidth = size.width - yAxisWidth
        val chartHeight = size.height - xAxisHeight - topPadding
        val chartTop = topPadding

        // Krok osi Y — ceil(maxValue / 4) gwarantuje unikalne liczby całkowite
        val rawStep = maxValueInt.toFloat() / 4f
        val step = ceil(rawStep).toInt().coerceAtLeast(1)
        val adjustedSteps = (maxValueInt / step).coerceAtLeast(1)

        // Gridlines + etykiety osi Y
        for (i in 0..adjustedSteps) {
            val value = step * i
            val y = chartTop + chartHeight * (1f - value.toFloat() / maxValueInt)
            drawLine(
                color = axisColor.copy(alpha = 0.7f),
                start = Offset(yAxisWidth, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText(
                value.toString(),
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

        val slotWidth = chartWidth / entries.size
        val barWidth = min(slotWidth * 0.9f, maxBarWidthPx)

        entries.forEachIndexed { i, entry ->
            val barH = (entry.value / maxValue) * chartHeight
            val x = 20 + yAxisWidth + slotWidth * i
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
                    pctLabelPaint
                )
            }

            if (entry.label.isNotEmpty()) {
                val labelX = x + barWidth / 2f
                val labelBaselineY = chartTop + chartHeight + xAxisHeight - 4.dp.toPx()

                if (rotateLabels) {
                    drawContext.canvas.nativeCanvas.save()
                    drawContext.canvas.nativeCanvas.rotate(-45f, labelX, labelBaselineY)
                    drawContext.canvas.nativeCanvas.drawText(
                        entry.label,
                        labelX,
                        labelBaselineY,
                        xLabelPaint
                    )
                    drawContext.canvas.nativeCanvas.restore()
                } else {
                    drawContext.canvas.nativeCanvas.drawText(
                        entry.label,
                        labelX,
                        size.height - 4.dp.toPx(),
                        xLabelPaint
                    )
                }
            }
        }
    }
}