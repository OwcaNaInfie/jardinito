package pl.edu.pb.jardinito.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.components.BasePickerSheet
import pl.edu.pb.jardinito.ui.components.PickerSheetContent
import pl.edu.pb.jardinito.ui.components.SearchInput
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_xs
import pl.edu.pb.jardinito.ui.theme.Dimensions.roundedCorner_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_s
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.PlantColor
import pl.edu.pb.jardinito.ui.utils.PlantOwnershipStatus
import pl.edu.pb.jardinito.ui.utils.PlantSize
import pl.edu.pb.jardinito.ui.utils.PriceSortOrder
import pl.edu.pb.jardinito.ui.utils.isActiveFilter
import pl.edu.pb.jardinito.ui.utils.toChipLabel
import pl.edu.pb.jardinito.ui.utils.toChipLabelRes
import pl.edu.pb.jardinito.viewmodel.MarketFilterState

// =====================
// FILTER BAR
// =====================

private enum class FilterDrawerType { COLOR, SIZE, STATUS }

@Composable
fun MarketFilterBar(
    filterState: MarketFilterState,
    onSearchQueryChange: (String) -> Unit,
    onColorFilterChange: (Set<PlantColor>) -> Unit,
    onSizeFilterChange: (Set<PlantSize>) -> Unit,
    onStatusFilterChange: (PlantOwnershipStatus?) -> Unit,
    onPriceSortOrderToggle: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeDrawer by remember { mutableStateOf<FilterDrawerType?>(null) }

    Column(modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(itemsSpacing_s)
    ) {
        SearchInput(
            modifier = Modifier.padding( horizontal = screenPadding_s),
            value = filterState.searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = stringResource(R.string.filter_search_hint)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = itemsSpacing_s)
                .padding(horizontal = screenPadding_s),
            horizontalArrangement = Arrangement.spacedBy(itemsSpacing_xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val colorLabel  = filterState.filterColors.toChipLabelRes()
                ?.let { stringResource(it) } ?: stringResource(R.string.filter_color)

            val sizeLabel   = filterState.filterSizes.toChipLabelRes()
                ?.let { stringResource(it) } ?: stringResource(R.string.filter_size)

            val statusActive = filterState.filterStatus.isActiveFilter()

            val statusLabel  = filterState.filterStatus.toChipLabelRes()
                ?.let { stringResource(it) } ?: stringResource(R.string.filter_status)

            val priceLabel = filterState.priceSortOrder.toChipLabel(stringResource(R.string.filter_price))

            MarketFilterChip(
                label = colorLabel,
                isActive = filterState.filterColors.isNotEmpty(),
                onClick = { activeDrawer = FilterDrawerType.COLOR }
            )

            MarketFilterChip(
                label = sizeLabel,
                isActive = filterState.filterSizes.isNotEmpty(),
                onClick = { activeDrawer = FilterDrawerType.SIZE }
            )

            MarketFilterChip(
                label = statusLabel,
                isActive = statusActive,
                onClick = { activeDrawer = FilterDrawerType.STATUS }
            )

            MarketFilterChip(
                label = priceLabel,
                isActive = filterState.priceSortOrder != null,
                onClick = onPriceSortOrderToggle
            )

            if (filterState.hasActiveFilters) {
                ClearFiltersChip(onClick = onClearFilters)
            }
        }
    }

    // Drawery
    when (activeDrawer) {
        FilterDrawerType.COLOR -> ColorFilterDrawer(
            selectedColors = filterState.filterColors,
            onSelectionChange = onColorFilterChange,
            onDismiss = { activeDrawer = null }
        )
        FilterDrawerType.SIZE -> SizeFilterDrawer(
            selectedSizes = filterState.filterSizes,
            onSelectionChange = onSizeFilterChange,
            onDismiss = { activeDrawer = null }
        )
        FilterDrawerType.STATUS -> StatusFilterDrawer(
            selectedStatus = filterState.filterStatus,
            onStatusSelected = onStatusFilterChange,
            onDismiss = { activeDrawer = null }
        )
        null -> Unit
    }
}

// =====================
// CHIPS
// =====================

@Composable
private fun MarketFilterChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {

    val color = if (isActive) colors.primary500 else colors.neutralLightGray
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(roundedCorner_s))
            .clickable(onClick = onClick)
            .border(1.dp, color, RoundedCornerShape(roundedCorner_s))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

@Composable
private fun ClearFiltersChip(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(roundedCorner_s))
            .background(colors.error.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = colors.error,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = stringResource(R.string.filter_clear),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.error
        )
    }
}

// =====================
// DRAWERYS
// =====================

@Composable
private fun ColorFilterDrawer(
    selectedColors: Set<PlantColor>,
    onSelectionChange: (Set<PlantColor>) -> Unit,
    onDismiss: () -> Unit
) {
    BasePickerSheet(
        onDismiss = onDismiss,
        title = stringResource(R.string.filter_color),
        content = PickerSheetContent.List { _ ->
            PlantColor.values().forEach { color ->
                val isSelected = color in selectedColors
                MultiSelectRow(
                    label = stringResource(color.labelRes),
                    isSelected = isSelected,
                    indicator = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color.color)
                        )
                    },
                    onToggle = {
                        onSelectionChange(
                            if (isSelected) selectedColors - color else selectedColors + color
                        )
                    }
                )
            }
        }
    )
}

@Composable
private fun SizeFilterDrawer(
    selectedSizes: Set<PlantSize>,
    onSelectionChange: (Set<PlantSize>) -> Unit,
    onDismiss: () -> Unit
) {
    BasePickerSheet(
        onDismiss = onDismiss,
        title = stringResource(R.string.filter_size),
        content = PickerSheetContent.List { _ ->
            PlantSize.values().forEach { size ->
                val isSelected = size in selectedSizes
                MultiSelectRow(
                    label = stringResource(size.labelRes),
                    isSelected = isSelected,
                    indicator = null,
                    onToggle = {
                        onSelectionChange(
                            if (isSelected) selectedSizes - size else selectedSizes + size
                        )
                    }
                )
            }
        }
    )
}

@Composable
private fun StatusFilterDrawer(
    selectedStatus: PlantOwnershipStatus?,
    onStatusSelected: (PlantOwnershipStatus?) -> Unit,
    onDismiss: () -> Unit
) {
    BasePickerSheet(
        onDismiss = onDismiss,
        title = stringResource(R.string.filter_status),
        content = PickerSheetContent.List { hideAndDismiss ->
            SingleSelectRow(
                label = stringResource(R.string.filter_status_all),
                isSelected = selectedStatus == null || selectedStatus == PlantOwnershipStatus.ALL,
                onSelect = {
                    onStatusSelected(null)
                    hideAndDismiss()
                }
            )
            PlantOwnershipStatus.values()
                .filter { it != PlantOwnershipStatus.ALL }
                .forEach { status ->
                    SingleSelectRow(
                        label = stringResource(status.labelRes),
                        isSelected = selectedStatus == status,
                        onSelect = {
                            onStatusSelected(status)
                            hideAndDismiss()
                        }
                    )
                }
        }
    )
}

// =====================
// ROW COMPONENTS
// =====================

@Composable
private fun MultiSelectRow(
    label: String,
    isSelected: Boolean,
    indicator: (@Composable () -> Unit)?,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.primary500.copy(alpha = 0.12f) else Color.Transparent)
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            indicator?.invoke()
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.neutralBlack
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colors.primary500,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SingleSelectRow(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.primary500.copy(alpha = 0.12f) else Color.Transparent)
            .clickable { onSelect() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.neutralBlack
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colors.primary500,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}