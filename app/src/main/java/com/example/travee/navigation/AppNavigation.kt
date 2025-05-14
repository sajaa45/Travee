package com.example.travee.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.travee.ui.screens.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppNavigation(auth: FirebaseAuth,
                  db: FirebaseFirestore
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }
        composable("login") {
            LoginScreen(navController = navController, auth = auth)
        }
        composable("signup") {
            SignupScreen(navController = navController, auth = auth, db=db)
        }
        composable("profile") {
            ProfileScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController = navController, auth = auth, db = db)
        }
        composable("search") {
            SearchScreen(navController = navController)
        }
        composable("favorites") {
            FavoritesScreen(navController = navController)
        }
        composable("flight_details") {
            FlightDetailsScreen(navController = navController)
        }
    }
}
