package pl.edu.pb.jardinito.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.DialogConfig
import pl.edu.pb.jardinito.ui.components.DialogVariant
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.components.FormTextField
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.theme.Dimensions.roundedCorner_s
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.validateVerificationCode

// =====================
// DIALOGS
// =====================

@Composable
fun DeleteAvatarDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        config = DialogConfig(
            title = stringResource(R.string.dialog_delete_avatar_title),
            message = stringResource(R.string.dialog_delete_avatar_message),
            confirmText = stringResource(R.string.dialog_delete_avatar_confirm),
            variant = DialogVariant.Warning
        ),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun EditFieldDialog(
    title: String,
    currentValue: String,
    label: String,
    confirmText: String = stringResource(R.string.confirm),
    isValid: Boolean = true,
    errorRes: Int? = null,
    onValueChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(currentValue) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(roundedCorner_s))
                .background(colors.primary50)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            FormTextField(
                label = label,
                value = value,
                onValueChange = {
                    value = it
                    onValueChange(it)
                },
                required = true,
                errorRes = errorRes,
                isValid = isValid && value.isNotBlank()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                AppButton(
                    text = stringResource(R.string.cancel),
                    size = ButtonSize.Small,
                    variant = ButtonVariant.Primary,
                    onClick = onDismiss
                )
                Spacer(modifier = Modifier.width(8.dp))
                AppButton(
                    text = confirmText,
                    size = ButtonSize.Small,
                    variant = ButtonVariant.Secondary,
                    enabled = isValid && value.isNotBlank(),
                    onClick = { onConfirm(value) }
                )
            }
        }
    }
}

@Composable
fun EmailVerificationDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var timeLeft by remember { mutableIntStateOf(120) }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timerText = "%d:%02d".format(minutes, seconds)
    val timerColor = if (timeLeft <= 30) colors.error else colors.neutralGray

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(roundedCorner_s))
                .background(colors.primary50)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.verification_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = timerText,
                style = MaterialTheme.typography.headlineSmall,
                color = timerColor
            )
            FormTextField(
                label = stringResource(R.string.verification_code_hint),
                value = code,
                onValueChange = {
                    if (validateVerificationCode(it)) code = it
                },
                required = true,
                keyboardType = KeyboardType.Number
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                AppButton(
                    text = stringResource(R.string.cancel),
                    size = ButtonSize.Small,
                    variant = ButtonVariant.Primary,
                    onClick = onDismiss
                )
                Spacer(modifier = Modifier.width(8.dp))
                AppButton(
                    text = stringResource(R.string.verify),
                    size = ButtonSize.Small,
                    variant = ButtonVariant.Secondary,
                    enabled = code.length == 6 && timeLeft > 0,
                    onClick = { onConfirm(code) }
                )
            }
        }
    }
}

@Composable
fun LogOutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        config = DialogConfig(
            title = stringResource(R.string.log_out),
            message = stringResource(R.string.logout_message),
            confirmText = stringResource(R.string.log_out),
            variant = DialogVariant.Warning
        ),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun DeleteAccountDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        config = DialogConfig(
            title = stringResource(R.string.dialog_delete_account_title),
            message = stringResource(R.string.dialog_delete_account_message),
            confirmText = stringResource(R.string.delete),
            variant = DialogVariant.Warning
        ),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun DeleteAccountSuccessDialog(onDismiss: () -> Unit) {
    ConfirmDialog(
        config = DialogConfig(
            title = stringResource(R.string.dialog_account_deleted_title),
            message = stringResource(R.string.dialog_account_deleted_message),
            confirmText = stringResource(R.string.ok),
            singleButton = true,
            variant = DialogVariant.Success
        ),
        onConfirm = onDismiss,
        onDismiss = onDismiss
    )
}