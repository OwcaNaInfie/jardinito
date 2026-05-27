package pl.edu.pb.jardinito.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(

    // =====================
    // DISPLAY (hero / timer)
    // =====================

    displayLarge = TextStyle(
        fontFamily = MountainsOfChristmas,
        fontWeight = FontWeight.Bold,
        fontSize = 80.sp,
        lineHeight = 80.sp
    ),

    displayMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 36.sp
    ),

    displaySmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 28.sp
    ),

    // =====================
    // HEADINGS (UI sections)
    // =====================

    headlineLarge = TextStyle(
        fontFamily = MsMadi,
        fontWeight = FontWeight.Normal,
        fontSize = 46.sp,
        lineHeight = 46.sp
    ),

    headlineMedium = TextStyle(
        fontFamily = MsMadi,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 32.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = MsMadi,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 20.sp
    ),

    // =====================
    // TITLE (cards / rows)
    // =====================

    titleLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 24.sp
    ),

    titleMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 16.sp
    ),

    titleSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 14.sp
    ),

    // =====================
    // BODY (content)
    // =====================

    bodyLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 16.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 14.sp
    ),

    bodySmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        lineHeight = 12.sp
    ),

    // =====================
    // LABEL (buttons / chips / UI microtext)
    // =====================

    labelLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 20.sp
    ),

    labelMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 18.sp
    ),

    labelSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 14.sp
    )
)