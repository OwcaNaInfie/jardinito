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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import pl.edu.pb.jardinito.ui.theme.Dimensions.roundedCorner_s
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.theme.Dimensions
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.rememberPlantName
import pl.edu.pb.jardinito.ui.utils.rememberSvgImageRequest
import pl.edu.pb.jardinito.viewmodel.MarketError
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
    val favouritePlantIds: Set<String> = emptySet()
)

data class MarketActions(
    val onBuyPlant: (Plant) -> Unit,
    val onErrorDismissed: () -> Unit,
    val onBuySuccessDismissed: () -> Unit,
    val onPlantClick: (Plant) -> Unit,
    val onToggleFavourite: (Plant) -> Unit
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
    val plants by marketViewModel.plants.collectAsState()
    val coins by marketViewModel.coins.collectAsState()
    val unlockedPlantIds by marketViewModel.unlockedPlantIds.collectAsState()
    val favouritePlantIds by marketViewModel.favouritePlantIds.collectAsState()
    val error by marketViewModel.error.collectAsState()
    val buySuccess by marketViewModel.buySuccess.collectAsState()
    val isLoading by marketViewModel.isLoading.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    // Rośliny ładujemy raz
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) marketViewModel.loadPlants()
    }

    // Portfel ładujemy przy każdym wejściu na ekran
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (userId.isNotBlank()) marketViewModel.loadWallet(userId)
        }
    }

    MarketScreenContent(
        data = MarketData(
            plants = plants,
            coins = coins,
            unlockedPlantIds = unlockedPlantIds,
            favouritePlantIds = favouritePlantIds
        ),
        uiState = MarketUiState(
            isLoading = isLoading,
            error = error,
            buySuccess = buySuccess
        ),
        actions = MarketActions(
            onBuyPlant = { marketViewModel.buyPlant(it) },
            onErrorDismissed = { marketViewModel.clearError() },
            onBuySuccessDismissed = { marketViewModel.clearBuySuccess() },
            onPlantClick = onPlantClick,
            onToggleFavourite = { marketViewModel.toggleFavourite(it.plantId) }
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
            CoinBalanceRow(coins = data.coins)

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(data.plants) { plant ->
                    MarketPlantCard(
                        plant = plant,
                        isUnlocked = data.unlockedPlantIds.contains(plant.plantId),
                        isFavourite = data.favouritePlantIds.contains(plant.plantId),
                        onBuy = { actions.onBuyPlant(plant) },
                        onPlantClick = actions.onPlantClick,
                        onToggleFavourite = { actions.onToggleFavourite(plant) }
                    )
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
private fun CoinBalanceRow(coins: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = coins.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = colors.primary500
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.Toll,
            contentDescription = null,
            tint = colors.primary500,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PlantPriceChip(price: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(roundedCorner_s))
            .background(colors.neutralLight)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (price == 0) stringResource(R.string.market_plant_free) else price.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.primary500
        )
        if (price != 0) {
            Icon(
                imageVector = Icons.Default.Toll,
                contentDescription = null,
                tint = colors.primary500,
                modifier = Modifier.size(12.dp)
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
                    imageVector = if (isFavourite) Icons.Default.Favorite
                    else Icons.Default.FavoriteBorder,
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
            modifier = Modifier
                .fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlantPriceChip(price = plant.price)
            PlantActionButton(isUnlocked = isUnlocked, onBuy = onBuy)
        }
    }
}