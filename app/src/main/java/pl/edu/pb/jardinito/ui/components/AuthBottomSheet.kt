package pl.edu.pb.jardinito.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.material3.SheetState
import pl.edu.pb.jardinito.ui.theme.colors
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.primary100,
        windowInsets = WindowInsets(0),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        content()
    }
}