package pl.edu.pb.jardinito.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.ui.theme.Dimensions.roundedCorner_s
import pl.edu.pb.jardinito.ui.theme.colors

sealed class AppChipVariant {
    data object Price : AppChipVariant()
    data object Outlined : AppChipVariant()
    data class Tinted(val color: Color) : AppChipVariant()
    data class TintedColored(val color: Color) : AppChipVariant()
}

@Composable
fun AppChip(
    text: String,
    variant: AppChipVariant,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isActive: Boolean = false,
    iconSize: Dp = 12.dp,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    val baseBackgroundColor = when (variant) {
        is AppChipVariant.Price         -> colors.neutralLight
        is AppChipVariant.Outlined      -> Color.Transparent
        is AppChipVariant.Tinted        -> variant.color.copy(alpha = 0.2f)
        is AppChipVariant.TintedColored -> variant.color.copy(alpha = 0.2f)
    }

    val baseTextColor = when (variant) {
        is AppChipVariant.Price         -> colors.primary500
        is AppChipVariant.Outlined      -> colors.neutralLightGray
        is AppChipVariant.Tinted        -> colors.neutralLightGray
        is AppChipVariant.TintedColored -> variant.color
    }

    val baseBorderColor = when (variant) {
        is AppChipVariant.Outlined -> colors.neutralLightGray
        else                       -> colors.transparent
    }

    val backgroundColor = if (isActive) colors.transparent else baseBackgroundColor
    val textColor       = if (isActive) colors.primary500 else baseTextColor
    val borderColor     = if (isActive) colors.primary500 else baseBorderColor

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(roundedCorner_s))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(roundedCorner_s))
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .then(
                if (variant is AppChipVariant.Price) Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                else Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
    ) {
        leadingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(iconSize)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
        trailingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}