package pl.edu.pb.jardinito.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.ui.theme.colors

// ===== ENUMY =====
enum class IconButtonSize { Small, Medium, Large }

// ===== TOKENY =====
data class IconButtonTokens(
    val size: Dp,
    val shape: RoundedCornerShape
)

@Composable
fun iconButtonTokens(size: IconButtonSize): IconButtonTokens {
    return when (size) {
        IconButtonSize.Small -> IconButtonTokens(
            size = 40.dp,
            shape = RoundedCornerShape(50.dp) // kółko
        )
        IconButtonSize.Medium -> IconButtonTokens(
            size = 56.dp,
            shape = RoundedCornerShape(16.dp) // lekko zaokrąglone
        )
        IconButtonSize.Large -> IconButtonTokens(
            size = 72.dp,
            shape = RoundedCornerShape(6.dp) // bardziej kwadratowe
        )
    }
}

// ===== KOMPONENT =====
@Composable
fun AppIconButtonDrawable(
    modifier: Modifier = Modifier,
    iconRes: Int,
    size: IconButtonSize = IconButtonSize.Medium,
    backgroundColor: Color = colors.primary100,
    contentColor: Color = colors.primary300,
    onClick: () -> Unit
) {
    val tokens = iconButtonTokens(size)

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = tokens.shape,
        contentPadding = ButtonDefaults.ContentPadding,
        modifier = modifier.size(tokens.size)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.fillMaxSize(0.9f) // ikona wypełnia ~60% przycisku
        )
    }
}

// ===== PREVIEW =====
@Preview(showBackground = true, apiLevel = 34)
@Composable
fun AppIconButtonDrawablePreview() {
    JardinitoTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppIconButtonDrawable(
                iconRes = R.drawable.arrow_left,
                size = IconButtonSize.Small,
                backgroundColor = colors.primary100,
                contentColor = colors.primary300,
                onClick = {}
            )

            AppIconButtonDrawable(
                iconRes = R.drawable.arrow_left,
                size = IconButtonSize.Medium,
                backgroundColor = colors.secondaryBlue,
                contentColor = colors.neutralWhite,
                onClick = {}
            )

            AppIconButtonDrawable(
                iconRes = R.drawable.arrow_left,
                size = IconButtonSize.Large,
                backgroundColor = colors.primary900,
//                contentColor = colors.neutralWhite,
                onClick = {}
            )
        }
    }
}
