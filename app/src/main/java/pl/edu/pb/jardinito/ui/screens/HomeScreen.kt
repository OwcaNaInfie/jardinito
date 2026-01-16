package pl.edu.pb.jardinito.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import pl.edu.pb.jardinito.R

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ===== TYPOGRAPHY =====

        Text(
            text = "Display",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Headline Large",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        Text(
            text = "Headline Medium",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Title Large",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Body Large",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Body Medium",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Label Large",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        Text(
            text = "Label Medium",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ===== COLOR BLOCKS =====

        ColorPreview(
            name = "Primary",
            color = MaterialTheme.colorScheme.primary
        )

        ColorPreview(
            name = "Secondary",
            color = MaterialTheme.colorScheme.secondary
        )

        ColorPreview(
            name = "Surface",
            color = MaterialTheme.colorScheme.surface
        )

        ColorPreview(
            name = "Error",
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun ColorPreview(
    name: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(color)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
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