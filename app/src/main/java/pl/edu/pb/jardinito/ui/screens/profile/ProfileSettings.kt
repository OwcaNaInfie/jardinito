package pl.edu.pb.jardinito.ui.screens.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import pl.edu.pb.jardinito.viewmodel.UserViewModel

@Composable
fun ProfileSettings(
    onLogout: () -> Unit,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    userId: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    var showLogOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showDeleteAccountSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            hasOverlayPermission = Settings.canDrawOverlays(context)
            hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            else true
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasNotificationPermission = granted }

    val borderColor = colors.primary100

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        Text(
            text = stringResource(R.string.profile_settings),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.neutralBlack,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        PermissionToggleRow(
            label = stringResource(R.string.permission_notifications),
            isGranted = hasNotificationPermission,
            onToggle = {
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                }
            }
        )
        PermissionToggleRow(
            label = stringResource(R.string.permission_overlay),
            isGranted = hasOverlayPermission,
            onToggle = {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = borderColor
        )

        TextButton(onClick = { showLogOutDialog = true }) {
            Text(
                text = stringResource(R.string.log_out),
                color = colors.neutralBlack,
                style = MaterialTheme.typography.titleSmall
            )
        }
        TextButton(onClick = { showDeleteAccountDialog = true }) {
            Text(
                text = stringResource(R.string.delete_account),
                color = colors.error,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }

    if (showLogOutDialog) {
        LogOutDialog(
            onConfirm = {
                showLogOutDialog = false
                onLogout()
            },
            onDismiss = { showLogOutDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onConfirm = {
                showDeleteAccountDialog = false
                userViewModel.deleteAccount(userId) {
                    showDeleteAccountSuccessDialog = true
                }
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }

    if (showDeleteAccountSuccessDialog) {
        DeleteAccountSuccessDialog(
            onDismiss = {
                showDeleteAccountSuccessDialog = false
                authViewModel.clearUserSession()
                onLogout()
            }
        )
    }
}

@Composable
private fun PermissionToggleRow(
    label: String,
    isGranted: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = colors.neutralBlack
        )
        Switch(
            checked = isGranted,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.neutralWhite,
                checkedTrackColor = colors.primary500,
                uncheckedThumbColor = colors.neutralWhite,
                uncheckedTrackColor = colors.secondaryBlue,
                uncheckedBorderColor = colors.secondaryBlue
            )
        )
    }
}