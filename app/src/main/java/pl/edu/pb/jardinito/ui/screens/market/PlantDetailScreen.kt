package pl.edu.pb.jardinito.ui.screens.market

import MarketErrorDialog
import MarketSuccessDialog
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.components.AppChip
import pl.edu.pb.jardinito.ui.components.AppChipVariant
import pl.edu.pb.jardinito.ui.components.DetailLayout
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_xs
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.PlantColor
import pl.edu.pb.jardinito.ui.utils.PlantSize
import pl.edu.pb.jardinito.ui.utils.rememberPlantDescription
import pl.edu.pb.jardinito.ui.utils.rememberPlantName
import pl.edu.pb.jardinito.viewmodel.MarketError
import pl.edu.pb.jardinito.viewmodel.MarketViewModel

// =====================
// DATA CLASSES
// =====================

data class PlantDetailData(
    val plant: Plant,
    val isUnlocked: Boolean
)

data class PlantDetailActions(
    val onBack: () -> Unit,
    val onBuy: (Plant) -> Unit,
    val onErrorDismissed: () -> Unit,
    val onBuySuccessDismissed: () -> Unit
)

// =====================
// SCREEN
// =====================

@Composable
fun PlantDetailScreen(
    marketViewModel: MarketViewModel,
    plantId: String,
    onBack: () -> Unit
) {
    val plants by marketViewModel.plants.collectAsState()
    val unlockedPlantIds by marketViewModel.unlockedPlantIds.collectAsState()
    val error by marketViewModel.error.collectAsState()
    val buySuccess by marketViewModel.buySuccess.collectAsState()

    LaunchedEffect(Unit) {
        if (plants.isEmpty()) marketViewModel.loadPlants()
    }

    val plant = plants.firstOrNull { it.plantId == plantId } ?: return

    BackHandler { onBack() }

    PlantDetailContent(
        data = PlantDetailData(
            plant = plant,
            isUnlocked = unlockedPlantIds.contains(plantId)
        ),
        actions = PlantDetailActions(
            onBack = onBack,
            onBuy = { marketViewModel.buyPlant(it) },
            onErrorDismissed = { marketViewModel.clearError() },
            onBuySuccessDismissed = {
                marketViewModel.clearBuySuccess()
            }
        ),
        error = error,
        buySuccess = buySuccess
    )
}

// =====================
// CONTENT
// =====================

@Composable
fun PlantDetailContent(
    data: PlantDetailData,
    actions: PlantDetailActions,
    error: MarketError?,
    buySuccess: Plant?
) {
    DetailLayout(
        imageUrl = "${RetrofitInstance.BASE_URL}plants/${data.plant.images.large}",
        imageContentDescription = data.plant.name,
        onClose = actions.onBack
    ) {
        Text(
            text = rememberPlantName(data.plant),
            style = MaterialTheme.typography.headlineLarge,
            color = colors.neutralBlack
        )
        PlantPriceChip(price = data.plant.price)
        PlantTagsRow(plant = data.plant)
        Column(verticalArrangement = Arrangement.spacedBy(itemsSpacing_xs)) {
            Text(
                text = stringResource(R.string.plant_detail_about),
                style = MaterialTheme.typography.titleMedium,
                color = colors.neutralBlack
            )
            Text(
                text = rememberPlantDescription(data.plant),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.neutralLightGray
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (data.isUnlocked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colors.primary500,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.plant_detail_owned),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.primary500
                    )
                }
            } else {
                AppButton(
                    text = stringResource(R.string.plant_detail_buy),
                    size = ButtonSize.Max,
                    variant = ButtonVariant.Tertiary,
                    onClick = { actions.onBuy(data.plant) }
                )
            }
        }
    }

    error?.let { MarketErrorDialog(error = it, onDismiss = actions.onErrorDismissed) }
    buySuccess?.let { MarketSuccessDialog(plant = it, onDismiss = actions.onBuySuccessDismissed) }
}

// =====================
// COMPONENTS
// =====================

@Composable
private fun PlantPriceChip(price: Int) {
    val plantPriceText = if (price == 0) stringResource(R.string.market_plant_free) else price.toString()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = plantPriceText,
            style = MaterialTheme.typography.labelMedium,
            color = colors.neutralDark
        )
        if (price != 0) {
            Icon(
                imageVector = Icons.Default.Toll,
                contentDescription = null,
                tint = colors.neutralBlack,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlantTagsRow(plant: Plant) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(itemsSpacing_xs),
        verticalArrangement = Arrangement.spacedBy(itemsSpacing_xs)
    ) {
        val size = PlantSize.fromKey(plant.size)
        size?.let {
            AppChip(
                text = stringResource(it.labelRes).replaceFirstChar { it.lowercase() },
                variant = AppChipVariant.Outlined,
            )
        }
        AppChip(
            text = "${plant.minDuration} min",
            variant = AppChipVariant.Outlined,
        )

        // Color tags
        plant.colors.forEach { colorKey ->
            val plantColor = PlantColor.fromKey(colorKey)
            plantColor?.let {
                AppChip(
                    text = stringResource(it.labelRes).replaceFirstChar { it.lowercase() },
                    variant = AppChipVariant.Tinted(it.color),
                )
            }
        }
    }
}

@Composable
private fun PlantDetailBackground(modifier: Modifier = Modifier) {
    val offsetAnim = remember { Animatable(100f) }

    LaunchedEffect(Unit) {
        offsetAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 600, easing = EaseOutCubic)
        )
    }

    Box(modifier = modifier) {
        // Warstwa 1 — niebo (przesuwa się wolniej)
        Image(
            painter = painterResource(R.drawable.bg_sky),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.2f)
                .offset(y = (offsetAnim.value * 0.5f).dp)
        )
        // Warstwa 2 — trawa (przesuwa się szybciej)
        Image(
            painter = painterResource(R.drawable.bg_grass),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxSize()
                .offset(y = offsetAnim.value.dp)
                .align(Alignment.BottomCenter)
        )
    }
}