package pl.edu.pb.jardinito.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import pl.edu.pb.jardinito.R

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.ui.theme.colors

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ===== TYPOGRAPHY =====

        Text(
            text = "Jardinito",
            style = MaterialTheme.typography.displayLarge,
            color = colors.primary900
        )

        IconButton(
            onClick = {},
            modifier = Modifier
                .size(100.dp) // cały przycisk
                .padding(0.dp), // usuwa domyślne paddingi IconButton
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize() // ikona wypełnia cały przycisk
            )
        }

        ColorPreview(
            name = "Error",
            color = MaterialTheme.colorScheme.error
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row (
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                Column (
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
        }
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