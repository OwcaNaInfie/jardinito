package pl.edu.pb.jardinito.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(

    // B1 Bottom (48)
    displayLarge = TextStyle(
        fontFamily = ComingSoon,
        fontWeight = FontWeight.Medium,
        fontSize = 48.sp,
        lineHeight = 48.sp
    ),

    // T1 Timer (36)
    displaySmall = TextStyle(
        fontFamily = ComingSoon,
        fontWeight = FontWeight.Medium,
        fontSize = 36.sp,
        lineHeight = 36.sp
    ),

    // H1 Heading (32)
    headlineLarge = TextStyle(
        fontFamily = ComingSoon,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 32.sp
    ),

    // H2 Heading (20)
    headlineMedium = TextStyle(
        fontFamily = ComingSoon,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 20.sp
    ),

    // L1 Label (24)
    titleLarge = TextStyle(
        fontFamily = ComingSoon,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 24.sp
    ),

    // Body1 (15)
    bodyLarge = TextStyle(
        fontFamily = ComingSoon,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 15.sp
    ),

    // Body2 (12)
    bodyMedium = TextStyle(
        fontFamily = ComingSoon,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),

    // C1 / C2 Calendar
    bodySmall = TextStyle(
        fontFamily = ComingSoon,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 18.sp
    ),

    // B1 Button
    labelLarge = TextStyle(
        fontFamily = ComingSoon,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 15.sp
    ),

    // B2 Button
    labelMedium = TextStyle(
        fontFamily = ComingSoon,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 15.sp
    ),

    // B3 Button
    labelSmall = TextStyle(
        fontFamily = ComingSoon,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 13.sp
    )
)