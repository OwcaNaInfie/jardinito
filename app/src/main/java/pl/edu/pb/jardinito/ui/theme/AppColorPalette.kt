package pl.edu.pb.jardinito.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.staticCompositionLocalOf

data class AppColorPalette(
    /* ======================
       Primary colours
       ====================== */
    val primary50  : Color,
    val primary100 : Color,
    val primary300 : Color,
    val primary500 : Color,
    val primary700 : Color,
    val primary900 : Color,
    /* ======================
       Secondary colours
       ====================== */
    val secondaryBlue   : Color,
    val secondaryBeige  : Color,
    val secondaryCream  : Color,
    val secondaryYellow : Color,
    /* ======================
       Neutral colours
       ====================== */
    val neutralDark  : Color,
    val neutralBlack : Color,
    val neutralGray : Color,
    val neutralLightGray : Color,
    val neutralLight : Color,
    val neutralWhite : Color,
    /* ======================
       Semantic colours
       ====================== */
    val error : Color,
)

val LocalAppColors = staticCompositionLocalOf {
    AppColorPalette(
        primary50 = Color.Unspecified,
        primary100 = Color.Unspecified,
        primary300 = Color.Unspecified,
        primary500 = Color.Unspecified,
        primary700 = Color.Unspecified,
        primary900 = Color.Unspecified,
        secondaryBlue = Color.Unspecified,
        secondaryBeige = Color.Unspecified,
        secondaryCream = Color.Unspecified,
        secondaryYellow = Color.Unspecified,
        neutralDark = Color.Unspecified,
        neutralBlack = Color.Unspecified,
        neutralGray = Color.Unspecified,
        neutralLightGray = Color.Unspecified,
        neutralLight = Color.Unspecified,
        neutralWhite = Color.Unspecified,
        error = Color.Unspecified,
    )
}