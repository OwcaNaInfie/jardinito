package pl.edu.pb.jardinito.ui.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import pl.edu.pb.jardinito.data.model.Plant

@Composable
fun rememberPlantName(plant: Plant): String {
    val context = LocalContext.current
    return remember(plant.nameKey) {
        resolveString(context, plant.nameKey)
    }
}

@Composable
fun rememberPlantDescription(plant: Plant): String {
    val context = LocalContext.current
    return remember(plant.descriptionKey) {
        resolveString(context, plant.descriptionKey)
    }
}

fun resolveString(context: Context, key: String): String {
    val resId = context.resources.getIdentifier(key, "string", context.packageName)
    return if (resId != 0) context.getString(resId) else key
}