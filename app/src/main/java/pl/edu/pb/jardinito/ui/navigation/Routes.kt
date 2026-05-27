package pl.edu.pb.jardinito.ui.navigation

object Routes {
    const val ENTRY = "entry"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val FOCUS = "focus"
    const val PROFILE = "profile"
    const val TAGS = "tags"
    const val STATISTICS = "statistics"
    const val MARKET = "market"
    const val PLANT_DETAIL = "plant_detail/{plantId}"

    fun plantDetail(plantId: String) = "plant_detail/$plantId"
}
