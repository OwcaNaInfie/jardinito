import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import pl.edu.pb.jardinito.ui.utils.rememberPlantName
import pl.edu.pb.jardinito.ui.utils.rememberSvgImageRequest
import pl.edu.pb.jardinito.viewmodel.MarketError

// =====================
// DIALOGS
// =====================

@Composable
internal fun MarketErrorDialog(error: MarketError, onDismiss: () -> Unit) {
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
internal fun MarketSuccessDialog(plant: Plant, onDismiss: () -> Unit) {
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