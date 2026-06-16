package pl.edu.pb.jardinito.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.components.ChipRow
import pl.edu.pb.jardinito.ui.components.ChipRowItem
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_xs
import pl.edu.pb.jardinito.ui.theme.colors
import kotlin.math.roundToInt

private enum class ChartType { Bar, Pie }

data class ChartEntry(
    val label: String,
    val legendLabel: String = label,
    val value: Float,
    val color: Color
)

// Opisuje pojedynczy wiersz na liście pod wykresem.
// color odpowiada kolorowi użytemu w ChartEntry — kółeczko jest tym samym kolorem co
// segment/słupek na wykresie.
data class ChartLegendItem(
    val label: String,
    val count: Int,
    val percentage: Float,
    val color: androidx.compose.ui.graphics.Color
)

// Główny komponent zastępujący ToggleChart.
//
// entries              — dane dla wykresów (label, value, color)
// legendItems          — opcjonalna lista pod wykresem; null = lista ukryta
// legendValueFormatter — opcjonalne formatowanie kolumny wartości w legendzie;
//                        null = domyślne "${count}x" (np. liczba sesji)
//                        podaj np. { formatIdleTime(it, devMode) } dla wykresu czasu skupienia
// modifier             — przekazywany do Column
@Composable
fun ChartSection(
    entries: List<ChartEntry>,
    legendItems: List<ChartLegendItem>? = null,
    legendValueFormatter: ((Int) -> String)? = null,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    if (entries.isEmpty()) return

    // rememberSaveable żeby wybór wykresu przeżył nawigację do SessionDetail i powrót
    var chartType by rememberSaveable { mutableStateOf(ChartType.Bar) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(itemsSpacing_s)) {
        ChipRow(
            items = listOf(
                ChipRowItem(
                    text = stringResource(R.string.chart_type_bar),
                    isActive = chartType == ChartType.Bar,
                    onClick = { chartType = ChartType.Bar }
                ),
                ChipRowItem(
                    text = stringResource(R.string.chart_type_pie),
                    isActive = chartType == ChartType.Pie,
                    onClick = { chartType = ChartType.Pie }
                )
            )
        )

        val nonEmptyEntries = entries.filter { it.value > 0f }

        when (chartType) {
            ChartType.Bar -> BarChart(entries = entries)
            ChartType.Pie -> PieChart(entries = nonEmptyEntries)
        }

        if (legendItems != null) {
            ChartLegend(items = legendItems, valueFormatter = legendValueFormatter)
        }
    }
}

@Composable
private fun ChartLegend(
    items: List<ChartLegendItem>,
    valueFormatter: ((Int) -> String)?
) {
    Column {
        items.forEach { item ->
            ChartLegendRow(item = item, valueFormatter = valueFormatter)
        }
    }
}

@Composable
private fun ChartLegendRow(
    item: ChartLegendItem,
    valueFormatter: ((Int) -> String)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = itemsSpacing_xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(itemsSpacing_s)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(item.color)
        )
        Text(
            text = item.label,
            style = typography.bodyMedium,
            color = colors.neutralGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valueFormatter?.invoke(item.count) ?: "${item.count}x",
            style = typography.bodyMedium,
            color = colors.neutralGray
        )
        Text(
            text = "${item.percentage.roundToInt()}%",
            style = typography.bodyMedium,
            color = colors.neutralGray
        )
    }
}