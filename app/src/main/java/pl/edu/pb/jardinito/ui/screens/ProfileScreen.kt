package pl.edu.pb.jardinito.ui.screens

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.data.model.User
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import pl.edu.pb.jardinito.viewmodel.AvatarUploadState
import java.io.File

@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    viewModel: AuthViewModel,
) {
    val context = LocalContext.current
    val avatarUploadState by viewModel.avatarUploadState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteAvatarDialog by remember { mutableStateOf(false) }


// 1. Receives the cropped image result
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val croppedUri = UCrop.getOutput(result.data!!)
            if (croppedUri != null) {
                viewModel.uploadAvatar(croppedUri, context)
            }
        }
    }

// 2. Picks image from gallery, then launches UCrop
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

    val avatarUrl = when (user?.avatar?.activeType()) {
        "default", "custom" -> "${RetrofitInstance.BASE_URL}avatars/${user.avatar.activeValue()}"
        "google" -> user.avatar.activeValue()
        else -> null
    }

    LaunchedEffect(avatarUploadState) {
        when (avatarUploadState) {
            is AvatarUploadState.Success -> {
                Log.d("ProfileScreen", "Avatar uploaded successfully")
                viewModel.resetAvatarUploadState()
            }
            is AvatarUploadState.Error -> {
                Log.d("ProfileScreen", (avatarUploadState as AvatarUploadState.Error).message)
                viewModel.resetAvatarUploadState()
            }
            else -> {}
        }
    }

    if (user != null) {
        ProfileScreenContent(
            user = user,
            avatarUrl = avatarUrl,
            galleryLauncher = galleryLauncher,
            isEditing = isEditing,
            onLogout = onLogout,
            onSettingsClick = { isEditing = !isEditing },
            onDeleteAvatar = { viewModel.deleteAvatar() },
            onDeleteAccount = {}
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Brak danych użytkownika")
        }
    }
}

@Composable
fun ProfileHeader() {}

@Composable
fun AvatarSection(
    user: User,
    isEditing: Boolean,
    avatarUrl: String?,
    galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
    onDeleteAvatar: () -> Unit
) {
    var showDeleteAvatarDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.size(120.dp)) {
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
    isEditing: Boolean
) {}

@Composable
fun ProfileActions(
    user: User,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Column {
        Button(onClick = onLogout) {
            Text(stringResource(R.string.log_out))
        }
        Button(onClick = {showDeleteAccountDialog = true}) {
            Text(stringResource(R.string.delete_account))
        }
    }
    if (showDeleteAccountDialog) {
        ConfirmDialog(
            title = "Usuń konto",
            message = "Konto zostanie usunięte na zawsze",
            confirmText = "Usuń konto",
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
    avatarUrl: String?,
    galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
    isEditing: Boolean,
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit,
    onDeleteAvatar: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary50)
            .padding(
                top = 72.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
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
        }
//        Spacer(modifier = Modifier.height(32.dp))
        Spacer(modifier = Modifier.weight(1f))
        if (isEditing) {
            ProfileActions(
                onLogout = onLogout,
                onDeleteAccount = onDeleteAccount,
                user = user
            )
        }
    }
}

//@Preview(
//    showBackground = true,
//    apiLevel = 34
//)
//@Composable
//fun ProfileScreenPreview() {
//    JardinitoTheme {
//        ProfileScreenContent(
//            user = User(
//                username = "test_user",
//                email = "test@gmail.com",
//                userId = "2",
//                avatar = Avatar(
//                    type = "default",
//                    value = "default_3.png"
//                )
//            ),
//            avatarUrl = "http://10.0.2.2:5000/avatars/default_3.png",
//            onLogout = {},
//            onSettingsClick = {},
//            galleryLauncher: {}
//        )
//    }
//}