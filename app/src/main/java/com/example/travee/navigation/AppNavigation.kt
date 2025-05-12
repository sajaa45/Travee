package com.example.travee.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.travee.ui.screens.WelcomeScreen
import com.example.travee.ui.screens.HomeScreen
import com.example.travee.ui.screens.SearchScreen
import com.example.travee.ui.screens.FlightDetailsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("search") {
            SearchScreen(navController = navController)
        }
        composable("flight_details") {
            FlightDetailsScreen(navController = navController)
        }
    }
}
