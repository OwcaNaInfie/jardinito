package pl.edu.pb.jardinito.ui.components.charts

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.edu.pb.jardinito.ui.theme.colors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PieChart(
    entries: List<ChartEntry>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(260.dp)
) {
    if (entries.isEmpty()) return

    val total = entries.sumOf { it.value.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f
    val density = LocalDensity.current
    val textSizePx = with(density) { 10.sp.toPx() }
    val neutralLightGray = colors.neutralLightGray
    val neutralLightGrayArgb = neutralLightGray.toArgb()
    val isSingleEntry = entries.size == 1

    val labelPaint = remember(textSizePx, neutralLightGrayArgb) {
        Paint().apply {
            color = neutralLightGrayArgb
            textSize = textSizePx
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier) {
        val labelSpace = 52.dp.toPx()
        val pieSize = minOf(size.width - labelSpace * 2f, size.height - 16.dp.toPx())
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
            val label = entry.legendLabel
            drawContext.canvas.nativeCanvas.drawText(
                label,
                textAnchor.x + if (isRight) 3.dp.toPx() else -3.dp.toPx(),
                textAnchor.y + textSizePx / 3f,
                labelPaint
            )

            startAngle += sweepAngle
        }

        // Białe separatory na granicach wycinków — pomijamy dla pojedynczego elementu
        if (!isSingleEntry) {
            boundaries.forEach { angle ->
                val rad = angle * PI.toFloat() / 180f
                drawLine(
                    color = Color.White,
                    start = center,
                    end = Offset(center.x + radius * cos(rad), center.y + radius * sin(rad)),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Donut hole
        drawCircle(color = Color.White, radius = radius * 0.4f, center = center)
    }
}