package pl.edu.pb.jardinito.ui.screens.market

import MarketErrorDialog
import MarketSuccessDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.components.AppChip
import pl.edu.pb.jardinito.ui.components.AppChipVariant
import pl.edu.pb.jardinito.ui.theme.Dimensions
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.roundedCorner_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_s
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.PlantColor
import pl.edu.pb.jardinito.ui.utils.PlantOwnershipStatus
import pl.edu.pb.jardinito.ui.utils.PlantSize
import pl.edu.pb.jardinito.ui.utils.rememberPlantName
import pl.edu.pb.jardinito.ui.utils.rememberSvgImageRequest
import pl.edu.pb.jardinito.viewmodel.MarketError
import pl.edu.pb.jardinito.viewmodel.MarketFilterState
import pl.edu.pb.jardinito.viewmodel.MarketViewModel

// =====================
// DATA CLASSES
// =====================

data class MarketUiState(
    val isLoading: Boolean = false,
    val error: MarketError? = null,
    val buySuccess: Plant? = null
)

data class MarketData(
    val plants: List<Plant> = emptyList(),
    val coins: Int = 0,
    val unlockedPlantIds: Set<String> = emptySet(),
    val favouritePlantIds: Set<String> = emptySet(),
    val filterState: MarketFilterState = MarketFilterState()
)

data class MarketActions(
    val onBuyPlant: (Plant) -> Unit,
    val onErrorDismissed: () -> Unit,
    val onBuySuccessDismissed: () -> Unit,
    val onPlantClick: (Plant) -> Unit,
    val onToggleFavourite: (Plant) -> Unit,
    // Filtry
    val onSearchQueryChange: (String) -> Unit = {},
    val onColorFilterChange: (Set<PlantColor>) -> Unit = {},
    val onSizeFilterChange: (Set<PlantSize>) -> Unit = {},
    val onStatusFilterChange: (PlantOwnershipStatus?) -> Unit = {},
    val onPriceSortOrderToggle: () -> Unit = {},
    val onClearFilters: () -> Unit = {}
)

// =====================
// SCREEN
// =====================

@Composable
fun MarketScreen(
    marketViewModel: MarketViewModel,
    userId: String,
    onPlantClick: (Plant) -> Unit
) {
    val filteredPlants by marketViewModel.filteredPlants.collectAsState()
    val filterState by marketViewModel.filterState.collectAsState()
    val coins by marketViewModel.coins.collectAsState()
    val unlockedPlantIds  by marketViewModel.unlockedPlantIds.collectAsState()
    val favouritePlantIds by marketViewModel.favouritePlantIds.collectAsState()
    val error by marketViewModel.error.collectAsState()
    val buySuccess by marketViewModel.buySuccess.collectAsState()
    val isLoading by marketViewModel.isLoading.collectAsState()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            marketViewModel.setUserId(userId)
            marketViewModel.loadPlants()
        }
    }

    MarketScreenContent(
        data = MarketData(
            plants           = filteredPlants,
            coins            = coins,
            unlockedPlantIds = unlockedPlantIds,
            favouritePlantIds = favouritePlantIds,
            filterState      = filterState
        ),
        uiState = MarketUiState(
            isLoading  = isLoading,
            error      = error,
            buySuccess = buySuccess
        ),
        actions = MarketActions(
            onBuyPlant            = { marketViewModel.buyPlant(it) },
            onErrorDismissed      = { marketViewModel.clearError() },
            onBuySuccessDismissed = { marketViewModel.clearBuySuccess() },
            onPlantClick          = onPlantClick,
            onToggleFavourite     = { marketViewModel.toggleFavourite(it.plantId) },
            onSearchQueryChange   = { marketViewModel.updateSearchQuery(it) },
            onColorFilterChange   = { marketViewModel.updateFilterColors(it) },
            onSizeFilterChange    = { marketViewModel.updateFilterSizes(it) },
            onStatusFilterChange  = { marketViewModel.updateFilterStatus(it) },
            onPriceSortOrderToggle = { marketViewModel.togglePriceSortOrder() },
            onClearFilters        = { marketViewModel.clearFilters() }
        )
    )
}

// =====================
// CONTENT
// =====================

@Composable
fun MarketScreenContent(
    data: MarketData,
    uiState: MarketUiState,
    actions: MarketActions
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary50)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = Dimensions.topBarHeight)
        ) {
            MarketFilterBar(
                filterState           = data.filterState,
                onSearchQueryChange   = actions.onSearchQueryChange,
                onColorFilterChange   = actions.onColorFilterChange,
                onSizeFilterChange    = actions.onSizeFilterChange,
                onStatusFilterChange  = actions.onStatusFilterChange,
                onPriceSortOrderToggle = actions.onPriceSortOrderToggle,
                onClearFilters        = actions.onClearFilters
            )

            if (data.plants.isEmpty() && !uiState.isLoading && data.filterState.hasActiveFilters) {
                MarketEmptyFiltered(
                    onClearFilters = actions.onClearFilters,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(itemsSpacing_s),
                    verticalArrangement = Arrangement.spacedBy(itemsSpacing_s),
                    contentPadding = PaddingValues(
                        bottom = screenPadding_s
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = screenPadding_s)

                ) {
                    items(data.plants) { plant ->
                        MarketPlantCard(
                            plant           = plant,
                            isUnlocked      = data.unlockedPlantIds.contains(plant.plantId),
                            isFavourite     = data.favouritePlantIds.contains(plant.plantId),
                            onBuy           = { actions.onBuyPlant(plant) },
                            onPlantClick    = actions.onPlantClick,
                            onToggleFavourite = { actions.onToggleFavourite(plant) }
                        )
                    }
                }
            }
        }

        uiState.error?.let { err ->
            MarketErrorDialog(error = err, onDismiss = actions.onErrorDismissed)
        }
        uiState.buySuccess?.let { plant ->
            MarketSuccessDialog(plant = plant, onDismiss = actions.onBuySuccessDismissed)
        }
    }
}

// =====================
// COMPONENTS
// =====================


@Composable
private fun MarketEmptyFiltered(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.market_empty_filtered),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.neutralGray,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onClearFilters) {
            Text(
                text = stringResource(R.string.filter_clear),
                color = colors.primary500
            )
        }
    }
}

@Composable
private fun PlantActionButton(isUnlocked: Boolean, onBuy: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (isUnlocked) colors.primary500 else colors.primary700)
            .then(if (!isUnlocked) Modifier.clickable { onBuy() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isUnlocked) Icons.Default.Check else Icons.Default.Add,
            contentDescription = null,
            tint = colors.neutralLight,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MarketPlantCard(
    plant: Plant,
    isUnlocked: Boolean,
    isFavourite: Boolean,
    onBuy: () -> Unit,
    onPlantClick: (Plant) -> Unit,
    onToggleFavourite: () -> Unit
) {
    val imageUrl = rememberSvgImageRequest("${RetrofitInstance.BASE_URL}plants/${plant.images.large}")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(roundedCorner_s))
            .background(colors.primary100)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = plant.name,
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { onPlantClick(plant) }
            )
            IconButton(
                onClick = onToggleFavourite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(30.dp)
            ) {
                Icon(
                    imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavourite) colors.error else colors.neutralLight,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
        Text(
            text = rememberPlantName(plant),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.neutralBlack,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppChip(
                text = if (plant.price == 0) stringResource(R.string.market_plant_free) else plant.price.toString(),
                variant = AppChipVariant.Price,
                trailingIcon = if (plant.price == 0) null else Icons.Default.Toll
            )
            PlantActionButton(isUnlocked = isUnlocked, onBuy = onBuy)
        }
    }
}