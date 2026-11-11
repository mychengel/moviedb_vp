package com.vp.moviedatabase.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vp.moviedatabase.ui.screen.DashboardScreen
import com.vp.moviedatabase.ui.screen.DetailScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = DashboardRoute) {
        composable<DashboardRoute> {
            DashboardScreen(
                onMovieClick = { movieId -> navController.navigate(DetailRoute(movieId)) },
            )
        }
        composable<DetailRoute> {
            DetailScreen(
                onBackClick = { navController.navigateUp() },
            )
        }
    }
}