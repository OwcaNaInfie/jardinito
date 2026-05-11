package pl.edu.pb.jardinito.ui.components.appButton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.ui.theme.colors

@Composable
fun AppButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    iconRes: Int? = null,
    iconVector: ImageVector? = null,
    size: ButtonSize = ButtonSize.Medium,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,

    // ===== CIRCLE =====
    circle: Boolean = false,
    buttonColor: Color? = null,
    contentColor: Color? = null,

    onClick: () -> Unit
) {
    val sizeTokens = buttonSizeTokens(size)

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
        enabled = enabled,
        colors = buttonColors(
            variant = variant,
            buttonColor = buttonColor,
            contentColor = contentColor
        ),
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
                    tint = contentColor ?: LocalContentColor.current
                )

                iconVector != null -> Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    modifier = Modifier.size(sizeTokens.iconSize),
                    tint = contentColor ?: LocalContentColor.current
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
                tint = contentColor ?: LocalContentColor.current
            )

            iconVector != null -> Icon(
                imageVector = iconVector,
                contentDescription = null,
                modifier = Modifier.size(sizeTokens.iconSize),
                tint = contentColor ?: LocalContentColor.current
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
            AppButton(
                text = "Continue",
                size = ButtonSize.Medium,
                variant = ButtonVariant.Primary,
                enabled = false,
                onClick = {}
            )

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
                    buttonColor = colors.primary100,
                    contentColor = Color.Unspecified,
                    onClick = {}
                )

                AppButton(
                    iconVector = Icons.AutoMirrored.Filled.ArrowForward,
                    size = ButtonSize.Large,
                    circle = true,
                    buttonColor = colors.primary500,
                    contentColor = Color.White,
                    onClick = {}
                )
                AppButton(
                    iconRes = R.drawable.google,
                    size = ButtonSize.Max,
                    circle = true,
                    buttonColor = colors.primary500,
                    contentColor = Color.Unspecified,
                    onClick = {}
                )
            }
        }
    }
}
