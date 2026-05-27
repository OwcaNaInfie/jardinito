package pl.edu.pb.jardinito.ui.components.appButton

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.ui.theme.Dimensions.roundedCorner_l
import pl.edu.pb.jardinito.ui.theme.Dimensions.roundedCorner_s

data class ButtonSizeTokens(
    val padding: PaddingValues,
    val textStyle: TextStyle,
    val shape: RoundedCornerShape,
    val iconSize: Dp,
    val height: Dp,
    val fullWidth: Boolean = false
)

@Composable
fun buttonSizeTokens(size: ButtonSize): ButtonSizeTokens {
    return when (size) {
        ButtonSize.Small -> ButtonSizeTokens(
            padding = PaddingValues(horizontal = 16.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(roundedCorner_l),
            iconSize = 16.dp,
            height = 30.dp
        )

        ButtonSize.Medium -> ButtonSizeTokens(
            padding = PaddingValues(horizontal = 56.dp),
            textStyle = MaterialTheme.typography.labelMedium,
            shape = RoundedCornerShape(roundedCorner_l),
            iconSize = 24.dp,
            height = 48.dp
        )

        ButtonSize.Large -> ButtonSizeTokens(
            padding = PaddingValues(horizontal = 80.dp),
            textStyle = MaterialTheme.typography.labelLarge,
            shape = RoundedCornerShape(roundedCorner_s),
            iconSize = 24.dp,
            height = 56.dp
        )

        ButtonSize.Max -> ButtonSizeTokens(
            padding = PaddingValues(horizontal = 16.dp),
            textStyle = MaterialTheme.typography.labelLarge,
            shape = RoundedCornerShape(6.dp),
            iconSize = 24.dp,
            height = 48.dp,
            fullWidth = true
        )
    }
}