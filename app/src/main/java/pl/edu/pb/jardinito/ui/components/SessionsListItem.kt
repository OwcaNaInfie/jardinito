package pl.edu.pb.jardinito.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
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
import pl.edu.pb.jardinito.data.model.Session
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.roundedCorner_xs
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_xs
import pl.edu.pb.jardinito.ui.theme.TagColors
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.formatSessionDate
import pl.edu.pb.jardinito.ui.utils.rememberSvgImageRequest

@Composable
fun SessionsListItem(
    session: Session,
    onClick: () -> Unit,
) {
    val imageUrl = "${RetrofitInstance.BASE_URL}plants/${
        if (session.status == "failed") session.plant.witheredImages.mediumOutlined
        else session.plant.images.mediumOutlined
    }"
    val request = rememberSvgImageRequest(imageUrl)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(roundedCorner_xs))
            .background(colors.neutralLight)
            .clickable(onClick = onClick)
            .padding(vertical = screenPadding_xs)
            .padding(end = screenPadding_s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(itemsSpacing_s)
    ) {
        AsyncImage(
            model = request,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None
        )
        Text(
            text = formatSessionDate(session.startedAt),
            style = typography.bodySmall,
            color = colors.neutralGray,
            modifier = Modifier.weight(1f)
        )
        AppChip(
            text = session.tag?.name ?: stringResource(R.string.session_detail_no_tag),
            variant = session.tag?.let { AppChipVariant.Tinted(TagColors.colorCompose(it.color)) }
                ?: AppChipVariant.Outlined
        )
    }
}