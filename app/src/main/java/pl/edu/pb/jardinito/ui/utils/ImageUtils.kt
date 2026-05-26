package pl.edu.pb.jardinito.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import coil.size.Size as CoilSize
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.request.Options
import com.caverock.androidsvg.SVG

class PixelArtSvgDecoder(
    private val source: ImageSource,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult {
        val svg = source.source().use { SVG.getFromInputStream(it.inputStream()) }

        val width = svg.documentWidth.coerceAtLeast(1f).toInt()
        val height = svg.documentHeight.coerceAtLeast(1f).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawFilter = PaintFlagsDrawFilter(
            Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG,
            0
        )

        svg.renderToCanvas(canvas)

        return DecodeResult(
            drawable = BitmapDrawable(options.context.resources, bitmap),
            isSampled = false
        )
    }

    class Factory : Decoder.Factory {
        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            if (!result.mimeType.equals("image/svg+xml", ignoreCase = true)) return null
            return PixelArtSvgDecoder(result.source, options)
        }
    }
}

@Composable
fun rememberSvgImageRequest(url: String): ImageRequest {
    val context = LocalContext.current
    return remember(url) {
        ImageRequest.Builder(context)
            .data(url)
            .size(CoilSize.ORIGINAL)
            .decoderFactory(PixelArtSvgDecoder.Factory())
            .allowHardware(false)
            .build()
    }
}