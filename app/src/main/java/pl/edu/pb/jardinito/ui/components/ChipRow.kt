package pl.edu.pb.jardinito.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_xs

data class ChipRowItem(
    val text: String,
    val isActive: Boolean = false,
    val variant: AppChipVariant = AppChipVariant.Outlined,
    val onClick: () -> Unit
)

@Composable
fun ChipRow(
    items: List<ChipRowItem>,
    spacing: Dp = itemsSpacing_xs,
    horizontalAlignment: Alignment.Horizontal = Alignment.End

) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing, horizontalAlignment),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            AppChip(
                text = item.text,
                isActive = item.isActive,
                variant = item.variant,
                onClick = item.onClick
            )
        }
    }
}