package com.screenlog.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "Home", Icons.Default.Home)
    object Search : BottomNavItem(Screen.Search.route, "Search", Icons.Default.Search)
    object Log : BottomNavItem("log/movie/0", "Log", Icons.Default.AddCircle) // Placeholders since Log takes arguments, but we default to dummy in menu
    object Discover : BottomNavItem(Screen.Discover.route, "Discover", Icons.Default.PlayArrow)
    object Profile : BottomNavItem(Screen.Profile.route, "Profile", Icons.Default.Person)
}
