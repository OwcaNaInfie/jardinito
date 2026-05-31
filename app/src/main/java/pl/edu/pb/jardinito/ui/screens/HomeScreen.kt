package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.ui.theme.colors

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary100)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        TypographyItem("displayLarge", MaterialTheme.typography.displayLarge)
        TypographyItem("displaySmall", MaterialTheme.typography.displaySmall)

        TypographyItem("headlineLarge", MaterialTheme.typography.headlineLarge)
        TypographyItem("headlineMedium", MaterialTheme.typography.headlineMedium)
        TypographyItem("headlineSmall", MaterialTheme.typography.headlineSmall)

        TypographyItem("titleLarge", MaterialTheme.typography.titleLarge)
        TypographyItem("titleMedium", MaterialTheme.typography.titleMedium)
        TypographyItem("titleSmall", MaterialTheme.typography.titleSmall)

        TypographyItem("bodyLarge", MaterialTheme.typography.bodyLarge)
        TypographyItem("bodyMedium", MaterialTheme.typography.bodyMedium)
        TypographyItem("bodySmall", MaterialTheme.typography.bodySmall)

        TypographyItem("labelLarge", MaterialTheme.typography.labelLarge)
        TypographyItem("labelMedium", MaterialTheme.typography.labelMedium)
        TypographyItem("labelSmall", MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TypographyItem(name: String, style: androidx.compose.ui.text.TextStyle) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = colors.neutralBlack
        )

        Text(
            text = "The quick brown fox jumps over the lazy dog",
            style = style,
            color = colors.neutralBlack
        )
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun HomeScreenPreview() {
    JardinitoTheme {
        HomeScreen()
    }
}