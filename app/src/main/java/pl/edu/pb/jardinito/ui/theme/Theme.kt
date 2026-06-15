package pl.edu.pb.jardinito.ui.theme

import androidx.compose.ui.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.SecondaryYellow,
    secondary = AppColors.SecondaryBlue,
    background = Color.Transparent,
    surface = Color.Transparent,
    error = AppColors.Error,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = AppColors.NeutralDark,
    onSurface = AppColors.NeutralDark,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.SecondaryYellow,
    secondary = AppColors.SecondaryBlue,
    background = Color.Transparent,
    surface = Color.Transparent,
    error = AppColors.Error,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = AppColors.NeutralDark,
    onSurface = AppColors.NeutralDark,
    onError = Color.White
)

@Composable
fun JardinitoTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val appColors = AppColorPalette(
        primary50  = AppColors.Primary50,
        primary100 = AppColors.Primary100,
        primary300 = AppColors.Primary300,
        primary500 = AppColors.Primary500,
        primary700 = AppColors.Primary700,
        primary900 = AppColors.Primary900,
        secondaryBlue   = AppColors.SecondaryBlue,
        secondaryBeige  = AppColors.SecondaryBeige,
        secondaryCream  = AppColors.SecondaryCream,
        secondaryYellow = AppColors.SecondaryYellow,
        neutralDark  = AppColors.NeutralDark,
        neutralBlack = AppColors.NeutralGray,
        neutralGray = AppColors.NeutralGray,
        neutralLightGray = AppColors.NeutralLightGray,
        neutralInvisibleGray = AppColors.NeutralInvisibleGray,
        neutralLight = AppColors.NeutralLight,
        neutralWhite = AppColors.NeutralWhite,
        error = AppColors.Error,
        transparent = AppColors.Transparent
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalAbsoluteTonalElevation provides 0.dp
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

val colors: AppColorPalette
    @Composable
    get() = LocalAppColors.current