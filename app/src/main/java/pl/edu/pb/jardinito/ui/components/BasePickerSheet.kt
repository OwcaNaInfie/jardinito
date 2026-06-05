package pl.edu.pb.jardinito.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.ui.theme.Dimensions
import pl.edu.pb.jardinito.ui.theme.colors

sealed class PickerSheetContent {
    class List(
        val content: @Composable (hideAndDismiss: () -> Unit) -> Unit
    ) : PickerSheetContent()

    class Grid(
        val columns: Int = 3,
        val content: LazyGridScope.(hideAndDismiss: () -> Unit) -> Unit
    ) : PickerSheetContent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasePickerSheet(
    onDismiss: () -> Unit,
    title: String? = null,
    containerColor: Color = colors.neutralLight,
    content: PickerSheetContent
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    val hideAndDismiss: () -> Unit = {
        scope.launch {
            delay(300)
            sheetState.hide()
        }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerColor,
        windowInsets = WindowInsets(0)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .padding(horizontal = Dimensions.screenPadding_s)
                        .padding(bottom = 8.dp)
                )
            }
            when (val c = content) {
                is PickerSheetContent.List -> Column(
                    modifier = Modifier
                        .padding(horizontal = Dimensions.screenPadding_s)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.itemsSpacing_xs)
                ) {
                    c.content(hideAndDismiss)
                }
                is PickerSheetContent.Grid -> {
                    val gridContent = c.content
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(c.columns),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        gridContent(hideAndDismiss)
                    }
                }
            }
        }
    }
}