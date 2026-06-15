package pl.edu.pb.jardinito.ui.screens.market

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import pl.edu.pb.jardinito.ui.components.AppChip
import pl.edu.pb.jardinito.ui.components.AppChipVariant
import pl.edu.pb.jardinito.ui.components.BasePickerSheet
import pl.edu.pb.jardinito.ui.components.ChipRow
import pl.edu.pb.jardinito.ui.components.ChipRowItem
import pl.edu.pb.jardinito.ui.components.PickerSheetContent
import pl.edu.pb.jardinito.ui.components.SearchInput
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_xs
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_s
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.PlantColor
import pl.edu.pb.jardinito.ui.utils.PlantOwnershipStatus
import pl.edu.pb.jardinito.ui.utils.PlantSize
import pl.edu.pb.jardinito.ui.utils.isActiveFilter
import pl.edu.pb.jardinito.ui.utils.toChipLabel
import pl.edu.pb.jardinito.ui.utils.toChipLabelRes
import pl.edu.pb.jardinito.viewmodel.MarketFilterState

// =====================
// DATA
// =====================

private enum class FilterDrawerType(
    @StringRes val labelRes: Int,
    val isActive: (MarketFilterState) -> Boolean,
    val selectedLabelRes: (MarketFilterState) -> Int?
) {
    COLOR(
        R.string.filter_color,
        isActive = { it.filterColors.isNotEmpty() },
        selectedLabelRes = { it.filterColors.toChipLabelRes() }
    ),
    SIZE(
        R.string.filter_size,
        isActive = { it.filterSizes.isNotEmpty() },
        selectedLabelRes = { it.filterSizes.toChipLabelRes() }
    ),
    STATUS(
        R.string.filter_status,
        isActive = { it.filterStatus.isActiveFilter() },
        selectedLabelRes = { it.filterStatus.toChipLabelRes() }
    )
}

// =====================
// FILTER BAR
// =====================

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

    val priceLabel = filterState.priceSortOrder.toChipLabel(stringResource(R.string.filter_price))
    val chips = buildList {
        FilterDrawerType.entries.forEach { drawerType ->
            val label = drawerType.selectedLabelRes(filterState)
                ?.let { stringResource(it) }
                ?: stringResource(drawerType.labelRes)
            add(ChipRowItem(
                text = label,
                isActive = drawerType.isActive(filterState),
                onClick = { activeDrawer = drawerType }
            ))
        }
        add(ChipRowItem(
            text = priceLabel,
            isActive = filterState.priceSortOrder != null,
            onClick = onPriceSortOrderToggle
        ))
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(itemsSpacing_s)) {
        SearchInput(
            modifier = Modifier.padding(horizontal = screenPadding_s),
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
            horizontalArrangement = Arrangement.spacedBy(itemsSpacing_xs)
        ) {
            ChipRow(
                items = chips
            )
            if (filterState.hasActiveFilters) {
                AppChip(
                    text = stringResource(R.string.filter_clear),
                    variant = AppChipVariant.TintedColored(colors.error),
                    iconSize = 14.dp,
                    leadingIcon = Icons.Default.Close,
                    onClick = onClearFilters
                )
            }
        }
    }

    val drawer = activeDrawer
    when (drawer) {
        FilterDrawerType.COLOR -> MultiSelectFilterDrawer(
            title = stringResource(drawer.labelRes),
            values = PlantColor.values(),
            isSelected = { it in filterState.filterColors },
            onToggle = { color ->
                onColorFilterChange(
                    if (color in filterState.filterColors) filterState.filterColors - color
                    else filterState.filterColors + color
                )
            },
            onDismiss = { activeDrawer = null },
            label = { stringResource(it.labelRes) },
            indicator = { color ->
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color.color))
            }
        )
        FilterDrawerType.SIZE -> MultiSelectFilterDrawer(
            title = stringResource(drawer.labelRes),
            values = PlantSize.values(),
            isSelected = { it in filterState.filterSizes },
            onToggle = { size ->
                onSizeFilterChange(
                    if (size in filterState.filterSizes) filterState.filterSizes - size
                    else filterState.filterSizes + size
                )
            },
            onDismiss = { activeDrawer = null },
            label = { stringResource(it.labelRes) }
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
// DRAWERS
// =====================

@Composable
private fun <T : Enum<T>> MultiSelectFilterDrawer(
    title: String,
    values: Array<T>,
    isSelected: (T) -> Boolean,
    onToggle: (T) -> Unit,
    onDismiss: () -> Unit,
    label: @Composable (T) -> String,
    indicator: (@Composable (T) -> Unit)? = null
) {
    BasePickerSheet(
        onDismiss = onDismiss,
        title = title,
        content = PickerSheetContent.List { _ ->
            values.forEach { value ->
                FilterPickerRow(
                    label = label(value),
                    isSelected = isSelected(value),
                    indicator = indicator?.let { { it(value) } },
                    onClick = { onToggle(value) }
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
            FilterPickerRow(
                label = stringResource(R.string.filter_status_all),
                isSelected = selectedStatus == null || selectedStatus == PlantOwnershipStatus.ALL,
                onClick = {
                    onStatusSelected(null)
                    hideAndDismiss()
                }
            )
            PlantOwnershipStatus.values()
                .filter { it != PlantOwnershipStatus.ALL }
                .forEach { status ->
                    FilterPickerRow(
                        label = stringResource(status.labelRes),
                        isSelected = selectedStatus == status,
                        onClick = {
                            onStatusSelected(status)
                            hideAndDismiss()
                        }
                    )
                }
        }
    )
}

// =====================
// COMPONENTS
// =====================

@Composable
private fun FilterPickerRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    indicator: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.primary500.copy(alpha = 0.12f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            indicator?.invoke()
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = colors.neutralBlack)
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