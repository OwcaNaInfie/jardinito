package pl.edu.pb.jardinito.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.ui.theme.colors

@Composable
fun ScreenContainer(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .background(colors.transparent)
            .fillMaxSize()
            .padding(top = 5.dp)
    ) {
        content()
    }
}