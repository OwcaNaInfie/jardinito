package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
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
import coil.compose.AsyncImage
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.DialogConfig
import pl.edu.pb.jardinito.ui.components.DialogVariant
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
    val unlockedPlantIds: Set<String> = emptySet()
)

data class MarketActions(
    val onBuyPlant: (Plant) -> Unit,
    val onErrorDismissed: () -> Unit,
    val onBuySuccessDismissed: () -> Unit
)

// =====================
// SCREEN
// =====================

@Composable
fun MarketScreen(
    marketViewModel: MarketViewModel,
    userId: String
) {
    val plants by marketViewModel.plants.collectAsState()
    val coins by marketViewModel.coins.collectAsState()
    val unlockedPlantIds by marketViewModel.unlockedPlantIds.collectAsState()
    val error by marketViewModel.error.collectAsState()
    val buySuccess by marketViewModel.buySuccess.collectAsState()
    val isLoading by marketViewModel.isLoading.collectAsState()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) marketViewModel.load(userId)
    }

    MarketScreenContent(
        data = MarketData(
            plants = plants,
            coins = coins,
            unlockedPlantIds = unlockedPlantIds
        ),
        uiState = MarketUiState(
            isLoading = isLoading,
            error = error,
            buySuccess = buySuccess
        ),
        actions = MarketActions(
            onBuyPlant = { marketViewModel.buyPlant(userId, it) },
            onErrorDismissed = { marketViewModel.clearError() },
            onBuySuccessDismissed = { marketViewModel.clearBuySuccess() }
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
                        onBuy = { actions.onBuyPlant(plant) }
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
            color = colors.primary900
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = null,
            tint = Color.Unspecified,
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
            .clip(RoundedCornerShape(16.dp))
            .background(colors.primary50)
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
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MarketPlantCard(
    plant: Plant,
    isUnlocked: Boolean,
    onBuy: () -> Unit
) {
    val imageUrl = rememberSvgImageRequest("${RetrofitInstance.BASE_URL}plants/${plant.images.large}")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.primary100)
            .padding(PaddingValues(start = 10.dp, end = 10.dp, bottom = 8.dp))
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = plant.name,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
        Text(
            text = rememberPlantName(plant),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.neutralBlack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
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

// =====================
// DIALOGS
// =====================

@Composable
private fun MarketErrorDialog(error: MarketError, onDismiss: () -> Unit) {
    val (title, message) = when (error) {
        is MarketError.InsufficientCoins -> Pair(
            stringResource(R.string.market_error_insufficient_title),
            stringResource(R.string.market_error_insufficient_message)
        )
        is MarketError.AlreadyUnlocked -> Pair(
            stringResource(R.string.market_error_unlocked_title),
            stringResource(R.string.market_error_unlocked_message)
        )
        is MarketError.NetworkError -> Pair(
            stringResource(R.string.market_error_network_title),
            stringResource(R.string.market_error_network_message)
        )
    }
    ConfirmDialog(
        config = DialogConfig(
            title = title,
            message = message,
            variant = DialogVariant.Error,
            singleButton = true,
            confirmText = stringResource(R.string.ok)
        ),
        onConfirm = onDismiss,
        onDismiss = onDismiss
    )
}

@Composable
private fun MarketSuccessDialog(plant: Plant, onDismiss: () -> Unit) {
    val imageUrl = rememberSvgImageRequest("${RetrofitInstance.BASE_URL}plants/${plant.images.medium}")
    ConfirmDialog(
        config = DialogConfig(
            title = stringResource(R.string.market_buy_success_title),
            message = stringResource(R.string.market_buy_success_message, rememberPlantName(plant)),
            variant = DialogVariant.Success,
            singleButton = true,
            confirmText = stringResource(R.string.ok)
        ),
        content = {
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.size(100.dp)
                )
            }
        },
        onConfirm = onDismiss,
        onDismiss = onDismiss
    )
}