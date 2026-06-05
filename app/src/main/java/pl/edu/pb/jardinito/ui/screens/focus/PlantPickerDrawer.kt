package pl.edu.pb.jardinito.ui.screens.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.components.BasePickerSheet
import pl.edu.pb.jardinito.ui.components.PickerSheetContent
import pl.edu.pb.jardinito.ui.theme.Dimensions.roundedCorner_s
import pl.edu.pb.jardinito.ui.theme.colors

@Composable
fun PlantPickerDrawer(
    plants: List<Plant>,
    selectedPlant: Plant?,
    unlockedPlantIds: Set<String>,
    onPlantSelected: (Plant) -> Unit,
    onDismiss: () -> Unit
) {
    BasePickerSheet(
        onDismiss = onDismiss,
        title = stringResource(R.string.focus_pick_plant),
        containerColor = colors.primary50,
        content = PickerSheetContent.Grid(columns = 3) { hideAndDismiss ->
            items(plants) { plant ->
                PlantPickerItem(
                    plant = plant,
                    isSelected = plant.plantId == selectedPlant?.plantId,
                    isLocked = !unlockedPlantIds.contains(plant.plantId),
                    onSelected = {
                        onPlantSelected(plant)
                        hideAndDismiss()
                    }
                )
            }
        }
    )
}

@Composable
fun PlantPickerItem(
    plant: Plant,
    isSelected: Boolean,
    isLocked: Boolean,
    onSelected: () -> Unit
) {
    val context = LocalContext.current
    val imageUrl = "${RetrofitInstance.BASE_URL}plants/${plant.images.large}"

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.primary100)
            .then(
                if (isSelected) Modifier.border(2.dp, colors.primary500, RoundedCornerShape(roundedCorner_s))
                else Modifier
            )
            .clickable(enabled = !isLocked) { onSelected() },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .decoderFactory(SvgDecoder.Factory())
                .build(),
            contentDescription = plant.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        )
        if (isLocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.5f))
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(colors.primary500),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}