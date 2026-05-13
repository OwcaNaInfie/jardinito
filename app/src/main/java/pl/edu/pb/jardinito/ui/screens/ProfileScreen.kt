package pl.edu.pb.jardinito.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.yalantis.ucrop.UCrop
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.User
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.DialogVariant
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import pl.edu.pb.jardinito.viewmodel.UserViewModel
import java.io.File
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.components.FormTextField
import pl.edu.pb.jardinito.ui.components.UserAvatar
import pl.edu.pb.jardinito.ui.theme.Dimensions
import pl.edu.pb.jardinito.ui.utils.validateVerificationCode

@Composable
fun ProfileScreen(
    user: User,
    onLogout: () -> Unit,
    isEditing: Boolean,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
) {
    val context = LocalContext.current
    var showAccountDeletedDialog by remember { mutableStateOf(false) }
    var showUsernameDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showEmailVerificationDialog by remember { mutableStateOf(false) }

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

    ProfileScreenContent(
        user = user,
        isEditing = isEditing,
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
        },
        onUsernameClick = { showUsernameDialog = true },
        onEmailClick = { showEmailDialog = true }
    )

    if (showUsernameDialog) {
        val form by userViewModel.profileFormState.collectAsState()

        EditFieldDialog(
            title = stringResource(R.string.edit_username),
            currentValue = user.username,
            label = stringResource(R.string.username),
            errorRes = form.usernameError,
            isValid = form.usernameIsValid,
            onValueChange = { userViewModel.onProfileUsernameChanged(it, user.username) },
            onConfirm = { newUsername ->
                if (form.usernameError != null) return@EditFieldDialog
                val userId = user.userId ?: return@EditFieldDialog
                userViewModel.updateUsername(userId, newUsername) { updatedUsername ->
                    authViewModel.updateUsername(updatedUsername)
                    showUsernameDialog = false
                    userViewModel.clearProfileForm()
                }
            },
            onDismiss = {
                showUsernameDialog = false
                userViewModel.clearProfileForm()
            }
        )
    }

    if (showEmailDialog) {
        val form by userViewModel.profileFormState.collectAsState()

        EditFieldDialog(
            title = stringResource(R.string.edit_email),
            currentValue = user.email,
            label = stringResource(R.string.email),
            confirmText = stringResource(R.string.send_reset_code),
            errorRes = form.emailError,
            isValid = form.emailIsValid,
            onValueChange = { userViewModel.onProfileEmailChanged(it, user.email) },
            onConfirm = { newEmail ->
                if (form.emailError != null) return@EditFieldDialog
                val userId = user.userId ?: return@EditFieldDialog
                userViewModel.requestEmailChange(userId, newEmail) {
                    showEmailDialog = false
                    userViewModel.clearProfileForm()
                    showEmailVerificationDialog = true
                }
            },
            onDismiss = {
                showEmailDialog = false
                userViewModel.clearProfileForm()
            }
        )
    }

    if (showEmailVerificationDialog) {
        EmailVerificationDialog(
            onConfirm = { code ->
                userViewModel.confirmEmailChange(code) { newEmail ->
                    authViewModel.updateEmail(newEmail)
                    showEmailVerificationDialog = false
                }
            },
            onDismiss = {
                showEmailVerificationDialog = false
                userViewModel.clearPendingEmailChange()
            }
        )
    }

    if (showAccountDeletedDialog) {
        ConfirmDialog(
            title = "Konto usunięte",
            message = "Twoje konto zostało pomyślnie usunięte.",
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
    galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
    onDeleteAvatar: () -> Unit,
    onUsernameClick: () -> Unit,
    onEmailClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AvatarSection(
                modifier = Modifier.weight(1f),
                user = user,
                isEditing = isEditing,
                galleryLauncher = galleryLauncher,
                onDeleteAvatar = onDeleteAvatar
            )
            Spacer(modifier = Modifier.width(16.dp))
            UserInfoSection(
                modifier = Modifier.weight(1f),
                user = user,
                isEditing = isEditing,
                onUsernameClick = onUsernameClick,
                onEmailClick = onEmailClick
            )
        }
    }
}

@Composable
fun AvatarSection(
    modifier: Modifier = Modifier,
    user: User,
    isEditing: Boolean,
    galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
    onDeleteAvatar: () -> Unit
) {
    var showDeleteAvatarDialog by remember { mutableStateOf(false) }

    Box (modifier = modifier.aspectRatio(1f))  {
        UserAvatar(
            user = user,
            size = 130.dp,
            borderWidth = 5.dp,
            modifier = Modifier.fillMaxSize()
        )
        if (isEditing) {
            AppButton(
                iconVector = Icons.Filled.Edit,
                size = ButtonSize.Small,
                circle = true,
                onClick = { galleryLauncher.launch("image/*") },
                contentColor = Color.White,
                buttonColor = colors.primary300,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-12).dp, y = (-12).dp)
            )
            if (user.avatar.custom != null) {
                AppButton(
                    iconVector = Icons.Filled.Close,
                    size = ButtonSize.Small,
                    circle = true,
                    onClick = { showDeleteAvatarDialog = true },
                    contentColor = Color.White,
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
fun UserInfoSection(
    modifier: Modifier = Modifier,
    user: User,
    isEditing: Boolean,
    onUsernameClick: () -> Unit,
    onEmailClick: () -> Unit
) {
    val fieldModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(50.dp))
        .background(colors.neutralLight)
        .padding(horizontal = 12.dp, vertical = 6.dp)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.gardeners_name),
                style = MaterialTheme.typography.titleMedium,
                color = colors.primary300
            )
            Box(
                modifier = fieldModifier
                    .then(if (isEditing) Modifier.clickable { onUsernameClick() } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colors.neutralBlack
                )
                if (isEditing) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit username",
                        tint = colors.primary300,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(16.dp)
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.email),
                style = MaterialTheme.typography.titleMedium,
                color = colors.primary300
            )
            Box(
                modifier = fieldModifier
                    .then(if (isEditing) Modifier.clickable { onEmailClick() } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colors.primary300
                )
                if (isEditing) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit email",
                        tint = colors.primary300,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(16.dp)
                    )
                }
            }
        }
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
        TextButton(onClick = { showLogOutDialog = true }) {
            Text(
                text = stringResource(R.string.log_out),
                color = colors.neutralBlack,
                style = MaterialTheme.typography.titleMedium
            )
        }
        TextButton(onClick = { showDeleteAccountDialog = true }) {
            Text(
                text = stringResource(R.string.delete_account),
                color = colors.error,
                style = MaterialTheme.typography.titleMedium
            )
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
    galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
    onDeleteAvatar: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onUsernameClick: () -> Unit,
    onEmailClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary100)
            .padding(top = Dimensions.topBarHeight, bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        ProfileHeader(
            user = user,
            isEditing = isEditing,
            galleryLauncher = galleryLauncher,
            onDeleteAvatar = onDeleteAvatar,
            onUsernameClick = onUsernameClick,
            onEmailClick = onEmailClick
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
                .clip(RoundedCornerShape(16.dp))
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
                .clip(RoundedCornerShape(16.dp))
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