package pl.edu.pb.jardinito.ui.screens.garden

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Session
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_l
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_xs
import pl.edu.pb.jardinito.ui.utils.rememberSvgImageRequest

// =====================
// DATA
// =====================

private val GrassTop  = Color(0xFF7F9864)
private val SideLeft  = Color(0xFF415133)
private val SideRight = Color(0xFF29351F)
private val GridLine  = Color(0xFF5A7A45)

private data class RhombusCorners(
    val top: Offset,
    val left: Offset,
    val bottom: Offset,
    val right: Offset
)

// =====================
// COMPONENTS
// =====================

@Composable
fun GardenGrid(
    sessions: List<Session>,
    gridSize: Int,
    useSmall: Boolean,
    positions: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val positionToSession = sessions.associateBy { positions[it.sessionId] }
    val density = LocalDensity.current
    val context = LocalContext.current
    val grassDrawable = remember {
        ContextCompat.getDrawable(context, R.drawable.bg_grass)
    }
    val sideDepth = 28.dp

    BoxWithConstraints(modifier = modifier) {
        val totalWidth = maxWidth
        val sideDepthPx = with(density) { sideDepth.toPx() }
        val rhombusWidthPx = with(density) { totalWidth.toPx() }
        val rhombusHeightPx = rhombusWidthPx * 0.5f
        val totalHeightDp = with(density) {
            (rhombusHeightPx + sideDepthPx).toDp()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeightDp + screenPadding_l)
                .padding(top = screenPadding_l)

        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val corners = RhombusCorners(
                    top    = Offset(rhombusWidthPx / 2f, 0f),
                    left   = Offset(0f, rhombusHeightPx / 2f),
                    bottom = Offset(rhombusWidthPx / 2f, rhombusHeightPx),
                    right  = Offset(rhombusWidthPx, rhombusHeightPx / 2f)
                )

                drawIsometricTop(corners, grassDrawable)
                drawGridLines(corners, gridSize)
                drawIsometricSide(corners.left,  corners.bottom, sideDepthPx, SideLeft)
                drawIsometricSide(corners.right, corners.bottom, sideDepthPx, SideRight)
            }

            IsometricFlowers(
                positionToSession = positionToSession,
                gridSize          = gridSize,
                useSmall          = useSmall,
                rhombusWidthPx    = with(density) { totalWidth.toPx() },
                rhombusHeightPx   = rhombusHeightPx,
                totalWidth        = totalWidth,
                totalHeight       = totalHeightDp
            )
        }
    }
}

@Composable
private fun IsometricFlowers(
    positionToSession: Map<Int?, Session>,
    gridSize: Int,
    useSmall: Boolean,
    rhombusWidthPx: Float,
    rhombusHeightPx: Float,
    totalWidth: Dp,
    totalHeight: Dp
) {
    val density = LocalDensity.current

    val flowerSize: Dp = when {
        gridSize <= 3 -> 64.dp
        gridSize <= 4 -> 56.dp
        gridSize <= 5 -> 48.dp
        else          -> 32.dp
    }
    val flowerSizePx = with(density) { flowerSize.toPx() }

    Box(
        modifier = Modifier
            .width(totalWidth)
            .height(totalHeight)
    ) {
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val index = row * gridSize + col
                val session = positionToSession[index] ?: continue

                val u = (col + 0.5f) / gridSize
                val v = (row + 0.5f) / gridSize

                val (jitterU, jitterV) = sessionJitter(session.sessionId)
                val uJittered = u + jitterU / gridSize
                val vJittered = v + jitterV / gridSize

                val cellWidth = rhombusWidthPx / gridSize
                val centerX = (uJittered - vJittered + 1f) / 2f * rhombusWidthPx
                val centerY = (uJittered + vJittered) / 2f * rhombusHeightPx +
                        jitterV * cellWidth * 0.1f

                val imageFile = if (session.status == "failed") {
                    if (useSmall) session.plant.witheredImages.small else session.plant.witheredImages.medium
                } else {
                    if (useSmall) session.plant.images.small else session.plant.images.medium
                }
                val imageUrl  = "${RetrofitInstance.BASE_URL}plants/$imageFile"
                val request   = rememberSvgImageRequest(imageUrl)

                val offsetXDp = with(density) { (centerX - flowerSizePx / 2f).toDp() }
                val offsetYDp = with(density) { (centerY - flowerSizePx).toDp() }

                AsyncImage(
                    model = request,
                    contentDescription = null,
                    modifier = Modifier
                        .size(flowerSize)
                        .offset(x = offsetXDp, y = offsetYDp),
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.None
                )
            }
        }
    }
}


// =====================
// HELPERS
// =====================


private fun sessionJitter(
    sessionId: String,
    maxJitterFraction: Float = 0.3f
): Pair<Float, Float> {
    val hash = sessionId.hashCode()
    val jitterX = ((hash and 0xFF) / 255f - 0.5f) * 2f * maxJitterFraction
    val jitterY = ((hash shr 8 and 0xFF) / 255f - 0.5f) * 2f * maxJitterFraction
    return Pair(jitterX, jitterY)
}

private fun DrawScope.drawIsometricTop(
    corners: RhombusCorners,
    drawable: Drawable?
) {
    val path = Path().apply {
        moveTo(corners.top.x, corners.top.y)
        lineTo(corners.right.x, corners.right.y)
        lineTo(corners.bottom.x, corners.bottom.y)
        lineTo(corners.left.x, corners.left.y)
        close()
    }

    if (drawable == null) {
        drawPath(path, GrassTop)
        return
    }

    drawIntoCanvas { canvas ->
        canvas.save()
        canvas.nativeCanvas.clipPath(path.asAndroidPath())
        val padding = size.width * 0.2f
        drawable.setBounds(
            (corners.left.x - padding).toInt(),
            (corners.top.y - padding).toInt(),
            (corners.right.x + padding).toInt(),
            (corners.bottom.y + padding).toInt()
        )
        drawable.draw(canvas.nativeCanvas)
        canvas.restore()
    }
}

private fun DrawScope.drawIsometricSide(
    sideStart: Offset,
    bottom: Offset,
    depth: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(sideStart.x, sideStart.y)
        lineTo(bottom.x, bottom.y)
        lineTo(bottom.x, bottom.y + depth)
        lineTo(sideStart.x, sideStart.y + depth)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawGridLines(
    corners: RhombusCorners,
    gridSize: Int
) {
    val axes = listOf(
        corners.top to corners.right to (corners.left to corners.bottom),
        corners.top to corners.left to (corners.right to corners.bottom)
    )
    axes.forEach { (startAxis, endAxis) ->
        val (axisStart, axisEnd) = startAxis
        val (edgeStart, edgeEnd) = endAxis
        for (i in 1 until gridSize) {
            val t = i.toFloat() / gridSize
            drawLine(
                color       = GridLine.copy(alpha = 0.4f),
                start       = Offset(axisStart.x + (axisEnd.x - axisStart.x) * t, axisStart.y + (axisEnd.y - axisStart.y) * t),
                end         = Offset(edgeStart.x + (edgeEnd.x - edgeStart.x) * t, edgeStart.y + (edgeEnd.y - edgeStart.y) * t),
                strokeWidth = 1.5f
            )
        }
    }
}