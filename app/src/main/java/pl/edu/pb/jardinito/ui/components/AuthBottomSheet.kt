package pl.edu.pb.jardinito.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import pl.edu.pb.jardinito.ui.theme.colors
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.data.model.AuthSheetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    state: AuthSheetState,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        sheetState.show()
    }
//    LaunchedEffect (state) {
//        if (state != null)
//            sheetState.show()
//        else sheetState.hide()
//    }

    ModalBottomSheet (
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.primary100,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) {
        content()
    }
}