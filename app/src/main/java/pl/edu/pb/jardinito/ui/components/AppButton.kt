package pl.edu.pb.jardinito.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.R

// ===== ENUMY =====
enum class ButtonSize { Small, Medium, Large, Max }
enum class ButtonVariant { Primary, Secondary, Tertiary }

// ===== TOKENY ROZMIARU =====
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
            textStyle = MaterialTheme.typography.labelSmall,
            shape = RoundedCornerShape(50.dp),
            iconSize = 16.dp,
            height = 30.dp
        )

        ButtonSize.Medium -> ButtonSizeTokens(
            padding = PaddingValues(horizontal = 56.dp),
            textStyle = MaterialTheme.typography.labelMedium,
            shape = RoundedCornerShape(50.dp),
            iconSize = 24.dp,
            height = 48.dp
        )

        ButtonSize.Large -> ButtonSizeTokens(
            padding = PaddingValues(horizontal = 80.dp),
            textStyle = MaterialTheme.typography.labelLarge,
            shape = RoundedCornerShape(16.dp),
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

// ===== KOMPONENT =====
@Composable
fun AppButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    iconRes: Int? = null,
    iconVector: ImageVector? = null,
    size: ButtonSize = ButtonSize.Medium,
    variant: ButtonVariant = ButtonVariant.Primary,

    // ===== CIRCLE =====
    circle: Boolean = false,
    buttonColor: Color? = null,
    iconColor: Color? = null,

    onClick: () -> Unit
) {
    val sizeTokens = buttonSizeTokens(size)

    // ===== KOLORY =====
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = buttonColor ?: when (variant) {
            ButtonVariant.Primary -> colors.primary100
            ButtonVariant.Secondary -> colors.primary300
            ButtonVariant.Tertiary -> colors.primary900
        },
        contentColor = iconColor ?: when (variant) {
            ButtonVariant.Primary -> colors.primary300
            ButtonVariant.Secondary -> colors.primary50
            ButtonVariant.Tertiary -> colors.neutralDark
        }
    )

    // ===== MODIFIER =====
    val buttonModifier = modifier
        .height(sizeTokens.height)
        .then(
            when {
                sizeTokens.fullWidth && !circle -> Modifier.fillMaxWidth()
                circle -> Modifier.width(sizeTokens.height)
                else -> Modifier
            }
        )

    Button(
        onClick = onClick,
        colors = buttonColors,
        shape = sizeTokens.shape,
        contentPadding = if (circle) PaddingValues(0.dp) else sizeTokens.padding,
        modifier = buttonModifier
    ) {

        // ===== TRYB CIRCLE =====
        if (circle) {
            when {
                iconRes != null -> Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(sizeTokens.iconSize),
                    tint = iconColor ?: LocalContentColor.current
                )

                iconVector != null -> Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    modifier = Modifier.size(sizeTokens.iconSize),
                    tint = iconColor ?: LocalContentColor.current
                )
            }
            return@Button
        }

        // ===== TRYB NORMALNY =====
        when {
            iconRes != null -> Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(sizeTokens.iconSize),
                tint = iconColor ?: LocalContentColor.current
            )

            iconVector != null -> Icon(
                imageVector = iconVector,
                contentDescription = null,
                modifier = Modifier.size(sizeTokens.iconSize),
                tint = iconColor ?: LocalContentColor.current
            )

            text != null -> Text(
                text = text,
                style = sizeTokens.textStyle
            )
        }
    }
}

// ===== PREVIEW =====
@Preview(showBackground = true, apiLevel = 34)
@Composable
fun AppButtonPreviews() {
    JardinitoTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppButton(
                    text = "Continue",
                    size = ButtonSize.Small,
                    variant = ButtonVariant.Secondary,
                    onClick = {}
                )
                AppButton(
                    text = "Exit",
                    size = ButtonSize.Small,
                    variant = ButtonVariant.Primary,
                    onClick = {}
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AppButton(
                    text = "Focus",
                    size = ButtonSize.Large,
                    variant = ButtonVariant.Tertiary,
                    onClick = {}
                )
                AppButton(
                    text = "Focus",
                    size = ButtonSize.Large,
                    variant = ButtonVariant.Primary,
                    onClick = {}
                )
            }

            AppButton(
                text = "Log in",
                size = ButtonSize.Medium,
                variant = ButtonVariant.Primary,
                onClick = {}
            )

            AppButton(
                text = "Log in",
                size = ButtonSize.Max,
                variant = ButtonVariant.Tertiary,
                onClick = {}
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppButton(
                    iconRes = R.drawable.arrow_left,
                    size = ButtonSize.Small,
                    variant = ButtonVariant.Primary,
                    onClick = {}
                )

                AppButton(
                    iconVector = Icons.AutoMirrored.Filled.ArrowForward,
                    size = ButtonSize.Medium,
                    variant = ButtonVariant.Secondary,
                    onClick = {}
                )

                // ===== CIRCLE =====
                AppButton(
                    iconRes = R.drawable.arrow_left,
                    size = ButtonSize.Small,
                    circle = true,
                    onClick = {}
                )

                AppButton(
                    iconRes = R.drawable.arrow_left,
                    size = ButtonSize.Medium,
                    circle = true,
                    buttonColor = Color.Magenta,
                    iconColor = Color.White,
                    onClick = {}
                )

                AppButton(
                    iconVector = Icons.AutoMirrored.Filled.ArrowForward,
                    size = ButtonSize.Medium,
                    circle = true,
                    buttonColor = Color.Magenta,
                    iconColor = Color.White,
                    onClick = {}
                )
            }
        }
    }
}
