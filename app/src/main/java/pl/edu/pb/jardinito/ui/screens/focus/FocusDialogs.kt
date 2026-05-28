package pl.edu.pb.jardinito.ui.screens.focus

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.DialogConfig
import pl.edu.pb.jardinito.ui.components.DialogVariant
import pl.edu.pb.jardinito.ui.utils.rememberPlantName
import pl.edu.pb.jardinito.ui.utils.rememberSvgImageRequest
import pl.edu.pb.jardinito.viewmodel.SessionResult

// =====================
// SESSION DIALOGS
// =====================

@Composable
fun StopConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        config = DialogConfig(
            title = stringResource(R.string.session_stop_title),
            message = stringResource(R.string.session_stop_message),
            confirmText = stringResource(R.string.session_stop_confirm),
            variant = DialogVariant.Warning
        ),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun SessionCompletedDialog(result: SessionResult.Completed, onDismiss: () -> Unit) {
    val imageUrl = rememberSvgImageRequest("${RetrofitInstance.BASE_URL}plants/${result.plant.images.medium}")
    ConfirmDialog(
        config = DialogConfig(
            title = stringResource(R.string.session_completed_title),
            message = stringResource(R.string.session_completed_message, rememberPlantName(result.plant), result.coinsEarned),
            variant = DialogVariant.Success,
            singleButton = true,
            confirmText = stringResource(R.string.ok)
        ),
        content = {
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.size(100.dp)
                )
            }
        },
        onConfirm = onDismiss,
        onDismiss = onDismiss
    )
}

@Composable
fun SessionFailedDialog(result: SessionResult.Failed, onDismiss: () -> Unit) {
    val imageUrl = rememberSvgImageRequest("${RetrofitInstance.BASE_URL}plants/${result.plant.witheredImages.medium}")
    ConfirmDialog(
        config = DialogConfig(
            title = stringResource(R.string.session_failed_title),
            message = stringResource(R.string.session_failed_message, rememberPlantName(result.plant)),
            variant = DialogVariant.Error,
            singleButton = true,
            confirmText = stringResource(R.string.ok)
        ),
        content = {
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.size(100.dp)
                )
            }
        },
        onConfirm = onDismiss,
        onDismiss = onDismiss
    )
}

// =====================
// PERMISSION DIALOGS
// =====================

@Composable
fun OverlayPermissionDialog(context: Context, onDismiss: () -> Unit) {
    ConfirmDialog(
        config = DialogConfig(
            title = stringResource(R.string.focus_overlay_permission_title),
            message = stringResource(R.string.focus_overlay_permission_message),
            confirmText = stringResource(R.string.focus_overlay_permission_confirm),
            variant = DialogVariant.Warning
        ),
        onConfirm = {
            onDismiss()
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        },
        onDismiss = onDismiss
    )
}

@Composable
fun NotificationPermissionDialog(
    launcher: ManagedActivityResultLauncher<String, Boolean>,
    onDismiss: () -> Unit
) {
    ConfirmDialog(
        config = DialogConfig(
            title = stringResource(R.string.focus_notif_permission_title),
            message = stringResource(R.string.focus_notif_permission_message),
            confirmText = stringResource(R.string.focus_notif_permission_confirm),
            variant = DialogVariant.Warning
        ),
        onConfirm = {
            onDismiss()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onDismiss = onDismiss
    )
}