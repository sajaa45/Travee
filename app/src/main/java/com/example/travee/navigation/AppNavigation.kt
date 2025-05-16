package com.example.travee.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.travee.ui.screens.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AppNavigation(
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
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
            SignupScreen(navController = navController, auth = auth, db = db)
        }

        composable("home") {
            HomeScreen(navController = navController, auth = auth, db = db)
        }

        composable("search") {
            SearchScreen(navController = navController)
        }

        composable("favorites") {
            FavoritesScreen(navController = navController, auth = auth, db = db)
        }

        composable("profile") {
            ProfileScreen(navController = navController, auth = auth, db = db)
        }

        composable(
            route = "flight_details?budget={budget}&departureCountry={departureCountry}&tripDays={tripDays}&departDate={departDate}",
            arguments = listOf(
                navArgument("budget") {
                    type = NavType.FloatType
                    defaultValue = 0f
                },
                navArgument("departureCountry") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("tripDays") {
                    type = NavType.IntType
                    defaultValue = 7
                },
                navArgument("departDate") {
                    type = NavType.StringType
                    defaultValue = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                }
            )
        ) { backStackEntry ->
            val budget = backStackEntry.arguments?.getFloat("budget")?.toDouble() ?: 0.0
            val departureCountry = backStackEntry.arguments?.getString("departureCountry") ?: ""
            val tripDays = backStackEntry.arguments?.getInt("tripDays") ?: 7
            val departDate = backStackEntry.arguments?.getString("departDate") ?:
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            FlightDetailsScreen(
                navController = navController,
                budget = budget,
                departureCountry = departureCountry,
                tripDays = tripDays,
                departDate = departDate
            )
        }

        composable(
            route = "single_flight_details?" +
                    "destination={destination}&" +
                    "destinationAirport={destinationAirport}&" +
                    "destinationCountry={destinationCountry}&" +
                    "price={price}&" +
                    "airline={airline}&" +
                    "departureAt={departureAt}&" +
                    "returnAt={returnAt}&" +
                    "link={link}&" +
                    "originCountry={originCountry}",
            arguments = listOf(
                navArgument("destination") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("destinationAirport") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("destinationCountry") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("price") {
                    type = NavType.FloatType
                    defaultValue = 0f
                },
                navArgument("airline") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("departureAt") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("returnAt") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("link") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("originCountry") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val destination = backStackEntry.arguments?.getString("destination") ?: ""
            val destinationAirport = backStackEntry.arguments?.getString("destinationAirport") ?: ""
            val destinationCountry = backStackEntry.arguments?.getString("destinationCountry") ?: ""
            val price = backStackEntry.arguments?.getFloat("price")?.toDouble() ?: 0.0
            val airline = backStackEntry.arguments?.getString("airline") ?: ""
            val departureAt = backStackEntry.arguments?.getString("departureAt") ?: ""
            val returnAt = backStackEntry.arguments?.getString("returnAt") ?: ""
            val link = backStackEntry.arguments?.getString("link") ?: ""
            val originCountry = backStackEntry.arguments?.getString("originCountry") ?: ""

            SingleFlightDetailsScreen(
                navController = navController,
                destination = destination,
                destinationAirport = destinationAirport,
                destinationCountry = destinationCountry,
                price = price,
                airline = airline,
                departureAt = departureAt,
                returnAt = returnAt,
                link = link,
                originCountry = originCountry,
                auth = auth,
                db = db
            )
        }
    }
}
