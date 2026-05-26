package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
            .background(colors.primary500)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ===== TYPOGRAPHY =====

        Text(
            text = "displayLarge",
            style = MaterialTheme.typography.displayLarge,
            color = colors.primary900
        )

        Row (
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "displaySmall",
                style = MaterialTheme.typography.displaySmall
            )

            Text(
                text = "headlineLarge",
                style = MaterialTheme.typography.headlineLarge
            )
        }
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "headlineMedium",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.primary900
            )
            Text(
                text = "bodyLarge",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "bodyMedium",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "bodySmall",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "labelLarge",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "labelMedium",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "labelSmall",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Preview(
    showBackground = true,
    apiLevel = 34
)
@Composable
fun HomeScreenPreview() {
    JardinitoTheme {
        HomeScreen()
    }
}