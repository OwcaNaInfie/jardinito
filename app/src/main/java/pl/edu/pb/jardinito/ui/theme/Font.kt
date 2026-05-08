package pl.edu.pb.jardinito.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import pl.edu.pb.jardinito.R

val ComicNeue = FontFamily(
    Font(
        resId = R.font.comic_neue_light,
        weight = FontWeight.Light
    ),
    Font(
        resId = R.font.comic_neue_light_italic,
        weight = FontWeight.Light,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.comic_neue_regular,
        weight = FontWeight.Normal
    ),
    Font(
        resId = R.font.comic_neue_italic,
        weight = FontWeight.Normal,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.comic_neue_bold,
        weight = FontWeight.Bold
    ),
    Font(
        resId = R.font.comic_neue_bold_italic,
        weight = FontWeight.Bold,
        style = FontStyle.Italic
    )
)

val MsMadi = FontFamily(
    Font(
        resId = R.font.ms_madi_regular,
        weight = FontWeight.Normal
    )
)

val BadScript = FontFamily(
    Font(
        resId = R.font.bad_script_regular,
        weight = FontWeight.Normal
    )
)

val MountainsOfChristmas = FontFamily(
    Font(
        resId = R.font.mountains_of_christmas_regular,
        weight = FontWeight.Normal
    ),
    Font(
        resId = R.font.mountains_of_christmas_bold,
        weight = FontWeight.Bold
    )
)
