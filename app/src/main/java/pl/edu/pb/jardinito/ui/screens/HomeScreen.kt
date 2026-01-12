package pl.edu.pb.jardinito.ui.screens
import pl.edu.pb.jardinito.R

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

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
            text = "Display Large – Lorem ipsum dolor sit amet",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Headline Large – Lorem ipsum dolor sit amet",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        Text(
            text = "Headline Medium – Lorem ipsum dolor sit amet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Title Large – Lorem ipsum dolor sit amet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Body Large – Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Body Medium – Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Label Large – Lorem ipsum",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        Text(
            text = "Label Medium – Lorem ipsum",
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

