package pl.edu.pb.jardinito.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.delay
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.model.profile.User
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.DialogConfig
import pl.edu.pb.jardinito.ui.components.DialogVariant
import pl.edu.pb.jardinito.ui.components.FormTextField
import pl.edu.pb.jardinito.ui.components.UserAvatar
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.theme.Dimensions
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_m
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.roundedCorner_m
import pl.edu.pb.jardinito.ui.theme.Dimensions.roundedCorner_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_l
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_m
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_s
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.rememberSvgImageRequest
import pl.edu.pb.jardinito.ui.utils.validateVerificationCode
import pl.edu.pb.jardinito.viewmodel.AuthViewModel
import pl.edu.pb.jardinito.viewmodel.ProfileViewModel
import pl.edu.pb.jardinito.viewmodel.UserViewModel
import java.io.File

// =====================
// SCREEN
// =====================

@Composable
fun ProfileScreen(
    user: User,
    onLogout: () -> Unit,
    isEditing: Boolean,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    profileViewModel: ProfileViewModel,
    onPlantClick: (Plant) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showAccountDeletedDialog by remember { mutableStateOf(false) }
    var showUsernameDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showEmailVerificationDialog by remember { mutableStateOf(false) }

    val coins by profileViewModel.coins.collectAsState()
    val favouritePlants by profileViewModel.favouritePlants.collectAsState()

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            user.userId.let { profileViewModel.load(it) }
        }
    }

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
        coins = coins,
        favouritePlants = favouritePlants,
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
        onEmailClick = { showEmailDialog = true },
        onPlantClick = onPlantClick
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
            config = DialogConfig(
                title = stringResource(R.string.account_deleted_title),
                message = stringResource(R.string.account_deleted_message),
                confirmText = "OK",
                singleButton = true,
                variant = DialogVariant.Success
            ),
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

// =====================
// CONTENT
// =====================

@Composable
fun ProfileScreenContent(
    user: User,
    isEditing: Boolean,
    coins: Int,
    favouritePlants: List<Plant>,
    galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
    onDeleteAvatar: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onUsernameClick: () -> Unit,
    onEmailClick: () -> Unit,
    onPlantClick: (Plant) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary50)
            .verticalScroll(rememberScrollState())
            .padding(top = Dimensions.topBarHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            ProfileHeader(
                user = user,
                isEditing = isEditing,
                galleryLauncher = galleryLauncher,
                onDeleteAvatar = onDeleteAvatar,
                onUsernameClick = onUsernameClick,
                onEmailClick = onEmailClick
            )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .clip(RoundedCornerShape(topStart = roundedCorner_m, topEnd = roundedCorner_m))
                .background(colors.neutralLight)
                .padding(
                    start = screenPadding_s,
                    end = screenPadding_s,
                    top = screenPadding_m
                ),
            verticalArrangement = Arrangement.spacedBy(itemsSpacing_m)
        ) {
            WalletTile(coins = coins)
            FavouritesTile(favouritePlants = favouritePlants, onPlantClick = onPlantClick)

            Spacer(modifier = Modifier.weight(1f))

            if (isEditing) {
                ProfileActions(
                    onLogout = onLogout,
                    onDeleteAccount = onDeleteAccount
                )
            }
        }
    }
}

// =====================
// HEADER
// =====================

@Composable
fun ProfileHeader(
    user: User,
    isEditing: Boolean,
    galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
    onDeleteAvatar: () -> Unit,
    onUsernameClick: () -> Unit,
    onEmailClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AvatarSection(
            user = user,
            isEditing = isEditing,
            galleryLauncher = galleryLauncher,
            onDeleteAvatar = onDeleteAvatar
        )

        // Nazwa użytkownika
        Row(
            modifier = Modifier.padding(start = 16.dp),

            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.headlineLarge,
                color = colors.neutralBlack,
                textAlign = TextAlign.Center
            )
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = colors.primary300,
                modifier = Modifier
                    .size(16.dp)
                    .alpha(if (isEditing) 1f else 0f)
                    .clickable(enabled = isEditing) { onUsernameClick() }
            )
        }

// Email
        Row(
            modifier = Modifier.padding(start = 16.dp),

            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.neutralLightGray,
                textAlign = TextAlign.Center
            )
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = colors.primary300,
                modifier = Modifier
                    .size(14.dp)
                    .alpha(if (isEditing) 1f else 0f)
                    .clickable(enabled = isEditing) { onEmailClick() }
            )
        }
    }
}

// =====================
// TILES
// =====================

@Composable
private fun ProfileTile(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.neutralBlack
        )
        Row(
            modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp),
        verticalAlignment = Alignment.CenterVertically
        ){
            content()
        }

    }
}

@Composable
private fun WalletTile(coins: Int) {
    ProfileTile(title = stringResource(R.string.profile_wallet)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),

            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(itemsSpacing_s)
        ) {
            Text(
                text = coins.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = colors.neutralBlack
            )
            Icon(
                imageVector = Icons.Default.Toll,
                contentDescription = null,
                tint = colors.neutralBlack,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun FavouritesTile(favouritePlants: List<Plant>, onPlantClick: (Plant) -> Unit) {
    ProfileTile(title = stringResource(R.string.profile_favourites)) {
        if (favouritePlants.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.profile_favourites_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.neutralGray
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(favouritePlants) { plant ->
                    val imageUrl = rememberSvgImageRequest(
                        "${RetrofitInstance.BASE_URL}plants/${plant.images.small}"
                    )
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = plant.name,
                        contentScale = ContentScale.Fit,
                        filterQuality = FilterQuality.None,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.primary50)
                            .clickable { onPlantClick(plant) }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

// =====================
// AVATAR
// =====================

@Composable
fun AvatarSection(
    modifier: Modifier = Modifier,
    user: User,
    isEditing: Boolean,
    galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
    onDeleteAvatar: () -> Unit
) {
    var showDeleteAvatarDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.size(150.dp)) {
        UserAvatar(
            user = user,
            size = 150.dp,
            borderWidth = 4.dp,
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
                    .offset(x = -8.dp, y = -8.dp)
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
                        .offset(x = 8.dp, y = (-8).dp)
                )
            }
        }
    }

    if (showDeleteAvatarDialog) {
        ConfirmDialog(
            config = DialogConfig(
                title = stringResource(R.string.delete_avatar_title),
                message = stringResource(R.string.delete_avatar_message),
                confirmText = stringResource(R.string.delete_avatar_confirm),
                variant = DialogVariant.Warning
            ),
            onConfirm = {
                showDeleteAvatarDialog = false
                onDeleteAvatar()
            },
            onDismiss = { showDeleteAvatarDialog = false }
        )
    }
}

// =====================
// ACTIONS
// =====================

@Composable
fun ProfileActions(
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogOutDialog by remember { mutableStateOf(false) }

    val borderColor = colors.primary50

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
            config = DialogConfig(
                title = stringResource(R.string.log_out),
                message = stringResource(R.string.logout_message),
                confirmText = stringResource(R.string.log_out),
                variant = DialogVariant.Warning
            ),
            onConfirm = {
                showLogOutDialog = false
                onLogout()
            },
            onDismiss = { showLogOutDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        ConfirmDialog(
            config = DialogConfig(
                title = stringResource(R.string.delete_account),
                message = stringResource(R.string.delete_account_message),
                confirmText = stringResource(R.string.delete_account)
            ),
            onConfirm = {
                showDeleteAccountDialog = false
                onDeleteAccount()
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }
}

// =====================
// DIALOGS
// =====================

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