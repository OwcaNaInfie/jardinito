package pl.edu.pb.jardinito.ui.theme

import android.graphics.Color as TagColor

import androidx.compose.ui.graphics.Color

object TagColors {
    val palette = mapOf(
        "coffeeBean" to "#230C0F",
        "darkCoffee" to "#432818",
        "darkWalnut" to "#6A381F",
        "chocolateBrown" to "#99582A",
        "bronzeSpice" to "#CC5803",

        "graphite" to "#353535",
        "darkAmaranth" to "#6E0D25",
        "brickRed" to "#BB0A21",
        "harvestOrange" to "#F17F29",
        "goldenPollen" to "#FFC857",

        "blackForest" to "#134611",
        "yellowGreen" to "#8FC93A",
        "strongCyan" to "#00CECB",
        "lightCoral" to "#EF7A85",
        "pastelPink" to "#FBBFCA",

        "blueSlate" to "#19647E",
        "twitterBlue" to "#0072BB",
        "skyAqua" to "#5AD2F4",
        "cottonCandy" to "#FF90B3",
        "petalPink" to "#DC6BAD",

        "indigoInk" to "#340068",
        "oceanTwilight" to "#3943B7",
        "indigoVelvet" to "#4F359B",
        "amethystSmoke" to "#B084CC",
        "grapeSoda" to "#8F3985"
    )

    val default = "oceanTwilight"

    fun colorFor(name: String): String = palette[name] ?: palette[default]!!
    fun colorCompose(name: String): Color = Color(TagColor.parseColor(colorFor(name)))
}