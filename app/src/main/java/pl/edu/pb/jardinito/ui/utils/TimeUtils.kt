package pl.edu.pb.jardinito.ui.utils

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