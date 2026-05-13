package pl.edu.pb.jardinito.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pl.edu.pb.jardinito.data.model.User
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.theme.colors

@Composable
fun UserAvatar(
    user: User,
    size: Dp = 48.dp,
    borderWidth: Dp = 5.dp,
    modifier: Modifier = Modifier
) {
    val avatarUrl = when (user.avatar.activeType()) {
        "default", "custom" -> "${RetrofitInstance.BASE_URL}avatars/${user.avatar.activeValue()}"
        "google" -> user.avatar.activeValue()
        else -> null
    }

    AsyncImage(
        model = avatarUrl,
        contentDescription = "Avatar",
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth, colors.primary300, CircleShape),
        contentScale = ContentScale.Crop
    )
}