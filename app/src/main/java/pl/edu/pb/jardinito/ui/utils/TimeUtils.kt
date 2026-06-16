package pl.edu.pb.jardinito.ui.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

fun formatIdleTime(duration: Int, devMode: Boolean): String {
    val minutes = if (devMode) duration / 60 else duration
    val seconds = if (devMode) duration % 60 else 0
    return "%d:%02d".format(minutes, seconds)
}

fun formatSessionDate(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(isoDate.take(19)) ?: return isoDate
        SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        isoDate
    }
}

fun formatSessionHour(time: String): String = "godz. $time"