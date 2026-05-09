package pl.edu.pb.jardinito.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.theme.colors
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme

enum class DialogVariant { Info, Warning, Error, Success }

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = stringResource(R.string.confirm),
    dismissText: String = stringResource(R.string.cancel),
    variant: DialogVariant = DialogVariant.Info,
    singleButton: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            securePolicy = SecureFlagPolicy.Inherit
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.05f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            ConfirmDialogContent(
                title = title,
                message = message,
                confirmText = confirmText,
                dismissText = dismissText,
                variant = variant,
                singleButton = singleButton,
                onConfirm = onConfirm,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
fun ConfirmDialogContent(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    variant: DialogVariant,
    singleButton: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val containerBorderColor = when (variant) {
        DialogVariant.Info -> Color.Transparent
        DialogVariant.Warning -> colors.primary900
        DialogVariant.Error -> colors.error
        DialogVariant.Success -> colors.primary500
    }

    val confirmButtonVariant = when (variant) {
        DialogVariant.Info -> ButtonVariant.Secondary
        DialogVariant.Warning -> ButtonVariant.Tertiary
        DialogVariant.Error -> ButtonVariant.Tertiary
        DialogVariant.Success -> ButtonVariant.Tertiary
    }

    val confirmButtonColor = when (variant) {
        DialogVariant.Info -> null
        DialogVariant.Warning -> null
        DialogVariant.Error -> colors.error
        DialogVariant.Success -> colors.primary500
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.primary50.copy(alpha = 0.95f))
            .border(2.dp, containerBorderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = false) {}
            .padding(24.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!singleButton) {
                AppButton(
                    text = dismissText,
                    size = ButtonSize.Small,
                    variant = ButtonVariant.Primary,
                    onClick = onDismiss
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            AppButton(
                text = confirmText,
                size = ButtonSize.Small,
                variant = confirmButtonVariant,
                buttonColor = confirmButtonColor,
                onClick = onConfirm
            )
        }
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun ConfirmDialogPreview() {
    JardinitoTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info - dwa przyciski
            ConfirmDialogContent(
                title = "Usuń zdjęcie",
                message = "Czy na pewno chcesz usunąć swoje zdjęcie profilowe?",
                confirmText = "Usuń",
                dismissText = "Anuluj",
                variant = DialogVariant.Info,
                singleButton = false,
                onConfirm = {},
                onDismiss = {}
            )
            // Warning - dwa przyciski
            ConfirmDialogContent(
                title = "Uwaga!",
                message = "Ta operacja jest nieodwracalna. Czy chcesz kontynuować?",
                confirmText = "Kontynuuj",
                dismissText = "Anuluj",
                variant = DialogVariant.Warning,
                singleButton = false,
                onConfirm = {},
                onDismiss = {}
            )
            // Error - dwa przyciski
            ConfirmDialogContent(
                title = "Usuń konto",
                message = "Twoje konto zostanie trwale usunięte.",
                confirmText = "Usuń konto",
                dismissText = "Anuluj",
                variant = DialogVariant.Error,
                singleButton = false,
                onConfirm = {},
                onDismiss = {}
            )
            // Info - jeden przycisk
            ConfirmDialogContent(
                title = "Konto usunięte",
                message = "Twoje konto zostało pomyślnie usunięte.",
                confirmText = "OK",
                dismissText = "",
                variant = DialogVariant.Success,
                singleButton = true,
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}