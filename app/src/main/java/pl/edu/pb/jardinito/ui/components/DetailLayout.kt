package pl.edu.pb.jardinito.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.roundedCorner_m
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_l
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_s
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.rememberSvgImageRequest

@Composable
fun DetailLayout(
    imageUrl: String,
    onClose: () -> Unit,
    imageContentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val imageRequest = rememberSvgImageRequest(imageUrl)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary100)
            .verticalScroll(rememberScrollState())
    ) {
        NatureBackground(modifier = Modifier
            .fillMaxSize()
            .offset(y = 30.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = imageContentDescription,
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
                modifier = Modifier
                    .size(250.dp)
                    .offset(y = 40.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = roundedCorner_m, topEnd = roundedCorner_m))
                    .background(colors.neutralLight)
                    .padding(
                        start = screenPadding_s,
                        end = screenPadding_s,
                        top = screenPadding_s,
                        bottom = screenPadding_l
                    ),
                verticalArrangement = Arrangement.spacedBy(itemsSpacing_s),
                content = content
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.close),
                tint = colors.neutralBlack
            )
        }
    }
}