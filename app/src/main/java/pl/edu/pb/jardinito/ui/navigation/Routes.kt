package pl.edu.pb.jardinito.ui.navigation

object Routes {
    const val ENTRY = "entry"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val GARDEN = "garden"
    const val FOCUS = "focus"
    const val PROFILE = "profile"
    const val TAGS = "tags"
    const val STATISTICS = "statistics"
    const val MARKET = "market"
    const val PLANT_DETAIL = "plant_detail/{plantId}"
    const val SESSION_DETAIL = "session_detail/{sessionId}"

    fun plantDetail(plantId: String) = "plant_detail/$plantId"
    fun sessionDetail(sessionId: String) = "session_detail/$sessionId"
}