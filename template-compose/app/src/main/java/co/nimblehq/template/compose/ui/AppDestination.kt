package co.nimblehq.template.compose.ui

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink

sealed class AppDestination(val route: String = "") {

    open val arguments: List<NamedNavArgument> = emptyList()

    open val deepLinks: List<NavDeepLink> = emptyList()

    open var destination: String = route

    data class Up(val results: List<Result> = emptyList()) : AppDestination()

    object RootNavGraph : AppDestination("rootNavGraph")

    object MainNavGraph : AppDestination("mainNavGraph")

    object Home : AppDestination("home")
}

data class Result(
    val key: String,
    val value: Any,
)
