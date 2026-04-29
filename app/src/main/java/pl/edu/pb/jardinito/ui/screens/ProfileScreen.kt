package pl.edu.pb.jardinito.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yalantis.ucrop.UCrop
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.User
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.DialogVariant
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import pl.edu.pb.jardinito.viewmodel.UserViewModel
import java.io.File

@Composable
fun ProfileScreen(
    user: User,
    onLogout: () -> Unit,
    isEditing: Boolean,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val userState by userViewModel.userState.collectAsState()
    var showAccountDeletedDialog by remember { mutableStateOf(false) }

    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val croppedUri = UCrop.getOutput(result.data!!)
            if (croppedUri != null) {
                val userId = user.userId ?: return@rememberLauncherForActivityResult
                userViewModel.uploadAvatar(userId, croppedUri, context) { newAvatar ->
                    authViewModel.updateAvatar(newAvatar)
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val destFile = File(context.cacheDir, "avatar_crop_temp.jpg")
            val destUri = Uri.fromFile(destFile)

            val cropIntent = UCrop.of(uri, destUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(400, 400)
                .getIntent(context)

            cropLauncher.launch(cropIntent)
        }
    }

    val avatarUrl = when (user.avatar?.activeType()) {
        "default", "custom" -> "${RetrofitInstance.BASE_URL}avatars/${user.avatar.activeValue()}"
        "google" -> user.avatar.activeValue()
        else -> null
    }

    ProfileScreenContent(
        user = user,
        isEditing = isEditing,
        avatarUrl = avatarUrl,
        galleryLauncher = galleryLauncher,
        onDeleteAvatar = {
            val userId = user.userId ?: return@ProfileScreenContent
            userViewModel.deleteAvatar(userId) { newAvatar ->
                authViewModel.updateAvatar(newAvatar)
            }
        },
        onLogout = onLogout,
        onDeleteAccount = {
            val userId = user.userId ?: return@ProfileScreenContent
            userViewModel.deleteAccount(userId) {
                showAccountDeletedDialog = true
            }
        }
    )

    if (showAccountDeletedDialog) {
        ConfirmDialog(
            title = "Konto usunięte",
            message = "Twoje konto zostało pomyślnie usunięte. Zostaniesz przekierowany do ekranu startowego.",
            confirmText = "OK",
            singleButton = true,
            variant = DialogVariant.Success,
            onConfirm = {
                showAccountDeletedDialog = false
                authViewModel.clearUserSession()
                onLogout()
            },
            onDismiss = {
                showAccountDeletedDialog = false
                authViewModel.clearUserSession()
                onLogout()
            }
        )
    }
}

@Composable
fun ProfileHeader(
    user: User,
    isEditing: Boolean,
    avatarUrl: String?,
    galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
    onDeleteAvatar: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AvatarSection(
                user = user,
                isEditing = isEditing,
                avatarUrl = avatarUrl,
                galleryLauncher = galleryLauncher,
                onDeleteAvatar = onDeleteAvatar
            )
            Spacer(modifier = Modifier.width(16.dp))
            UsernameSection(
                user = user
            )
        }
    }
}

@Composable
fun AvatarSection(
    user: User,
    isEditing: Boolean,
    avatarUrl: String?,
    galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
    onDeleteAvatar: () -> Unit
) {
    var showDeleteAvatarDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.size(130.dp)) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "User avatar",
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .border(4.dp, colors.primary300, CircleShape),
            contentScale = ContentScale.Crop
        )
        if (isEditing) {
            AppButton(
                iconVector = Icons.Filled.Edit,
                size = ButtonSize.Small,
                circle = true,
                onClick = { galleryLauncher.launch("image/*") },
                iconColor = Color.White,
                buttonColor = colors.primary300,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-5).dp, y = (-5).dp)
            )
            if (user.avatar.custom != null) {
                AppButton(
                    iconVector = Icons.Filled.Close,
                    size = ButtonSize.Small,
                    circle = true,
                    onClick = { showDeleteAvatarDialog = true },
                    iconColor = Color.White,
                    buttonColor = colors.error,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (5).dp, y = (-5).dp)
                )
            }
        }
    }

    if (showDeleteAvatarDialog) {
        ConfirmDialog(
            title = "Usuń zdjęcie profilowe",
            message = "Czy na pewno chcesz usunąć swoje zdjęcie profilowe?",
            confirmText = "Usuń",
            variant = DialogVariant.Warning,
            onConfirm = {
                showDeleteAvatarDialog = false
                onDeleteAvatar()
            },
            onDismiss = { showDeleteAvatarDialog = false }
        )
    }
}

@Composable
fun UsernameSection(
    user: User
) {
    Column {
        Text(
            text = "Gardeners name",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = user.username,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
fun ProfileActions(
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogOutDialog by remember { mutableStateOf(false) }

    Column {
        Button(
            modifier = Modifier.background(colors.transparent),
            onClick = { showLogOutDialog = true }
        ) {
            Text(stringResource(R.string.log_out), color = colors.neutralBlack)
        }
        Button(onClick = { showDeleteAccountDialog = true }) {
            Text(stringResource(R.string.delete_account))
        }
    }

    if (showLogOutDialog) {
        ConfirmDialog(
            title = "Wyloguj",
            message = "Czy na pewno chcesz się wylogować?",
            confirmText = "Wyloguj",
            variant = DialogVariant.Warning,
            onConfirm = {
                showLogOutDialog = false
                onLogout()
            },
            onDismiss = { showLogOutDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        ConfirmDialog(
            title = stringResource(R.string.delete_account),
            message = stringResource(R.string.delete_account_message),
            confirmText = stringResource(R.string.delete_account),
            onConfirm = {
                showDeleteAccountDialog = false
                onDeleteAccount()
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }
}

@Composable
fun ProfileScreenContent(
    user: User,
    isEditing: Boolean,
    avatarUrl: String?,
    galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
    onDeleteAvatar: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary50)
            .padding(top = 28.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        ProfileHeader(
            user = user,
            isEditing = isEditing,
            avatarUrl = avatarUrl,
            galleryLauncher = galleryLauncher,
            onDeleteAvatar = onDeleteAvatar,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (isEditing) {
            ProfileActions(
                onLogout = onLogout,
                onDeleteAccount = onDeleteAccount,
            )
        }
    }
}
