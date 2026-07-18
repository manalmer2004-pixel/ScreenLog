package com.screenlog.app.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.screenlog.app.presentation.auth.AuthViewModel
import com.screenlog.app.presentation.auth.LoginScreen
import com.screenlog.app.presentation.auth.RegisterScreen
import com.screenlog.app.presentation.components.ScreenLogBottomBar
import com.screenlog.app.presentation.detail.TitleDetailScreen
import com.screenlog.app.presentation.discover.RegionalDiscoveryScreen
import com.screenlog.app.presentation.home.HomeScreen
import com.screenlog.app.presentation.log.LogTitleScreen
import com.screenlog.app.presentation.profile.ProfileScreen
import com.screenlog.app.presentation.search.SearchScreen

@Composable
fun ScreenLogNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState = authViewModel.uiState.collectAsState()

    if (authState.value.isCheckingSession) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Discover.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ScreenLogBottomBar(
                    navController = navController,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (authState.value.isAuthenticated) Screen.Home.route else Screen.Login.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } }
                )
            }
            
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Register.route) { inclusive = true } } }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToDetail = { type, id -> navController.navigate(Screen.Detail.createRoute(type, id)) },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onNavigateToDetail = { type, id -> navController.navigate(Screen.Detail.createRoute(type, id)) }
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("titleType") { type = NavType.StringType },
                    navArgument("tmdbId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val titleType = backStackEntry.arguments?.getString("titleType") ?: "movie"
                val tmdbId = backStackEntry.arguments?.getString("tmdbId") ?: "0"
                TitleDetailScreen(
                    titleType = titleType,
                    tmdbId = tmdbId,
                    onNavigateToLog = { type, id -> navController.navigate(Screen.Log.createRoute(type, id)) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Log.route,
                arguments = listOf(
                    navArgument("titleType") { type = NavType.StringType },
                    navArgument("tmdbId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val titleType = backStackEntry.arguments?.getString("titleType") ?: "movie"
                val tmdbId = backStackEntry.arguments?.getString("tmdbId") ?: "0"
                LogTitleScreen(
                    titleType = titleType,
                    tmdbId = tmdbId,
                    onLogSuccess = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Discover.route) {
                RegionalDiscoveryScreen(
                    onNavigateToDetail = { type, id -> navController.navigate(Screen.Detail.createRoute(type, id)) }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToDetail = { type, id -> navController.navigate(Screen.Detail.createRoute(type, id)) },
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
