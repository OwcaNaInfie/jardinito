package pl.edu.pb.jardinito.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(

    // B1 Bottom (48)
    displayLarge = TextStyle(
        fontFamily = ComicNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        lineHeight = 48.sp
    ),

    // T1 Timer (36)
    displaySmall = TextStyle(
        fontFamily = ComicNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 36.sp
    ),

    // H1 Heading (32)
    headlineLarge = TextStyle(
        fontFamily = ComicNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 32.sp
    ),

    // H2 Heading (20)
    headlineMedium = TextStyle(
        fontFamily = ComicNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 28.sp
    ),

    // 24
    titleLarge = TextStyle(
        fontFamily = ComicNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 24.sp
    ),

    // Body1 (16)
    bodyLarge = TextStyle(
        fontFamily = ComicNeue,
        fontWeight = FontWeight.Light,
        fontSize = 16.sp,
        lineHeight = 16.sp
    ),

    // Body2 (14)
    bodyMedium = TextStyle(
        fontFamily = ComicNeue,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 14.sp
    ),

    // Body3 (12)
    bodySmall = TextStyle(
        fontFamily = ComicNeue,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        lineHeight = 12.sp
    ),

    // B1 Button
    labelLarge = TextStyle(
        fontFamily = ComicNeue,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 22.sp
    ),

    // B2 Button
    labelMedium = TextStyle(
        fontFamily = ComicNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 20.sp
    ),

    // B3 Button
    labelSmall = TextStyle(
        fontFamily = ComicNeue,
        fontWeight = FontWeight.Light,
        fontSize = 18.sp,
        lineHeight = 18.sp
    )
)