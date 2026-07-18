package com.screenlog.app.core.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Search : Screen("search")
    object Detail : Screen("detail/{titleType}/{tmdbId}") {
        fun createRoute(titleType: String, tmdbId: String) = "detail/$titleType/$tmdbId"
    }
    object Log : Screen("log/{titleType}/{tmdbId}") {
        fun createRoute(titleType: String, tmdbId: String) = "log/$titleType/$tmdbId"
    }
    object Discover : Screen("discover")
    object Recommendations : Screen("recommendations")
    object Analytics : Screen("analytics")
    object Profile : Screen("profile")
    object Moderation : Screen("moderation")
}
