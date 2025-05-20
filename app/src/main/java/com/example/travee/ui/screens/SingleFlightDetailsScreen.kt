package com.example.travee.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.travee.R
import com.example.travee.data.models.FavoriteFlight
import com.example.travee.navigation.navigateWithSaveState
import com.example.travee.service.GroqApiService
import com.example.travee.ui.components.BottomNavBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleFlightDetailsScreen(
    navController: NavController,
    destination: String,
    destinationAirport: String,
    destinationCountry: String,
    price: Double,
    airline: String,
    departureAt: String,
    returnAt: String,
    link: String,
    originCountry: String,
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    var selectedTripType by remember { mutableStateOf("Round Trip") }
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // For activities dialog
    var showActivitiesDialog by remember { mutableStateOf(false) }
    var activitiesText by remember { mutableStateOf("") }
    var isLoadingActivities by remember { mutableStateOf(false) }
    val groqApiService = remember { GroqApiService() }

    // For favorites functionality
    var isFavorite by remember { mutableStateOf(false) }
    var isCheckingFavorite by remember { mutableStateOf(true) }
    var showLoginPrompt by remember { mutableStateOf(false) }

    // Format dates for display
    val departureTime = formatApiDateWithTime(departureAt)
    val returnTime = formatApiDateWithTime(returnAt)

    // Format dates for display in the date section
    val departureDate = formatApiDateForDisplay(departureAt)
    val returnDate = formatApiDateForDisplay(returnAt)

    // Calculate trip duration in days
    val tripDays = calculateTripDuration(departureAt, returnAt)

    // Create a flight object for favorites
    val flightId = remember {
        "$destinationAirport-$departureAt-$returnAt".hashCode().toString()
    }

    val favoriteFlight = remember {
        FavoriteFlight(
            id = flightId,
            userId = auth.currentUser?.uid ?: "",
            destination = destination,
            destinationAirport = destinationAirport,
            destinationCountry = destinationCountry,
            price = price,
            airline = airline,
            departureAt = departureAt,
            returnAt = returnAt,
            link = link,
            originCountry = originCountry
        )
    }

    // Check if flight is already in favorites
    LaunchedEffect(key1 = auth.currentUser?.uid) {
        isCheckingFavorite = true
        if (auth.currentUser != null) {
            try {
                val document = db.collection("favorites")
                    .document(auth.currentUser!!.uid)
                    .collection("flights")
                    .document(flightId)
                    .get()
                    .await()

                isFavorite = document.exists()
            } catch (e: Exception) {
                Log.e("SingleFlightDetails", "Error checking favorite status", e)
            }
        }
        isCheckingFavorite = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.vector),
                            contentDescription = "Flight",
                            tint = MaterialTheme.colorScheme.onPrimary  // Use onPrimary for icon tint on primary container
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Flight Details",
                            color = MaterialTheme.colorScheme.onPrimary,  // Text color on primary container
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // Get the previous screen from the navigation arguments or a saved state
                            val previousRoute = navController.previousBackStackEntry?.destination?.route

                            when {
                                previousRoute?.startsWith("favorites") == true ->
                                    navController.navigate("favorites") {
                                        popUpTo("favorites") { inclusive = false }
                                    }
                                else ->
                                    // Navigate to flight_details with the specified parameters
                                    navController.navigateWithSaveState(
                                        "flight_details?budget=1000.0" +
                                                "&departureCountry=tunisia" +
                                                "&tripDays=7" +
                                                "&departDate=${LocalDate.now().plusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
                                    )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },actions = {
                    if (isCheckingFavorite) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)  // lighter color for indicator
                        )
                    } else {
                        IconButton(
                            onClick = {
                                if (auth.currentUser == null) {
                                    showLoginPrompt = true
                                } else {
                                    coroutineScope.launch {
                                        toggleFavorite(
                                            db = db,
                                            auth = auth,
                                            flight = favoriteFlight,
                                            isFavorite = isFavorite,
                                            onSuccess = { newFavoriteStatus ->
                                                isFavorite = newFavoriteStatus
                                            }
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary  // Use primary color container for dark and light mode
                )
            )
        },
        bottomBar = { BottomNavBar(navController = navController, selectedItem = 0) }
    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // Flight route information - Origin
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "From ${getOriginCode(originCountry)}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = getOriginCity(originCountry),
                        fontSize = 24.sp,
                        color = Color(0xFF1EBFC3),
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text(
                        text = departureTime,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Route line with arrows
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(Color.Gray)
                        .padding(start = 24.dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.swap__1_),
                        contentDescription = "Up",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.swap),
                        contentDescription = "Down",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Destination information
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "To $destinationAirport",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "$destination, $destinationCountry",
                        fontSize = 24.sp,
                        color = Color(0xFF1EBFC3),
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text(
                        text = returnTime,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))



            // Date information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Depart",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = departureDate,
                        fontSize = 16.sp,
                        color = Color(0xFF1EBFC3),
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Arrive",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = returnDate,
                        fontSize = 16.sp,
                        color = Color(0xFF1EBFC3),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Airline information
            Column {
                Text(
                    text = "Airline",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = airline,
                    fontSize = 16.sp,
                    color = Color(0xFF1EBFC3),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price information
            Column {
                Text(
                    text = "Price",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "$price TND",
                    fontSize = 20.sp,
                    color = Color(0xFF1EBFC3),
                    fontWeight = FontWeight.Bold
                )
            }



            Spacer(modifier = Modifier.height(16.dp))
            // Book flight button
            Button(
                onClick = {
                    try {
                        uriHandler.openUri(link)
                    } catch (e: Exception) {
                        Log.e("FlightDetails", "Error opening booking link", e)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1EBFC3)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Book Flight",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Favorite button
            Button(
                onClick = {
                    if (auth.currentUser == null) {
                        showLoginPrompt = true
                    } else {
                        coroutineScope.launch {
                            toggleFavorite(
                                db = db,
                                auth = auth,
                                flight = favoriteFlight,
                                isFavorite = isFavorite,
                                onSuccess = { newFavoriteStatus ->
                                    isFavorite = newFavoriteStatus
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFavorite) Color.Red.copy(alpha = 0.8f) else Color(0xFF4A6572)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activities button
            Button(
                onClick = {
                    showActivitiesDialog = true
                    isLoadingActivities = true

                    coroutineScope.launch {
                        try {
                            // Clean the destination string to avoid special characters
                            val cleanDestination = destination.replace(",", "")
                            val cleanCountry = destinationCountry.replace(",", "")

                            // Get activities for the destination country
                            activitiesText = groqApiService.getActivityRecommendations(
                                destination = "$cleanDestination, $cleanCountry",
                                budget = price * 0.3, // Allocate 30% of flight price for activities
                                days = tripDays
                            )
                        } catch (e: Exception) {
                            Log.e("FlightDetails", "Error loading activities", e)
                            activitiesText = "Failed to load activities: ${e.message}\n\n" +
                                    "• Visit local landmarks and historical sites\n" +
                                    "• Try local cuisine at affordable restaurants\n" +
                                    "• Explore parks and natural attractions"
                        } finally {
                            isLoadingActivities = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1EBFC3)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Activities",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Things to Do in $destination",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Traveler information
            Column {
                Text(
                    text = "Traveller & Class",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "1, Economy/Premium Economy",
                    fontSize = 16.sp,
                    color = Color(0xFF1EBFC3),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Activities Dialog
    if (showActivitiesDialog) {
        Dialog(onDismissRequest = { showActivitiesDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp), // Set maximum height
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header with gradient background (fixed)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1EBFC3),
                                        Color(0xFF0A9396)
                                    )
                                )
                            )
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Things to Do in $destination",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Budget-friendly activities for your $tripDays-day trip:",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // Scrollable content area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (isLoadingActivities) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF1EBFC3)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Finding the best activities...",
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else {
                            // Parse the bullet points for better display
                            val activities = activitiesText.split("\n").filter { it.trim().startsWith("•") || it.trim().startsWith("-") }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                            ) {
                                activities.forEachIndexed { index, activity ->
                                    ActivityItem(activity = activity.trim())

                                    if (index < activities.size - 1) {
                                        Divider(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            color = Color.LightGray.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                // If no activities were parsed, show the raw text
                                if (activities.isEmpty()) {
                                    Text(
                                        text = activitiesText,
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Close button (fixed at bottom)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Button(
                            onClick = { showActivitiesDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1EBFC3)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(
                                text = "Close",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // Login prompt dialog
    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = { showLoginPrompt = false },
            title = { Text("Sign In Required") },
            text = { Text("You need to be signed in to save favorites. Would you like to sign in now?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLoginPrompt = false
                        navController.navigate("login")
                    }
                ) {
                    Text("Sign In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginPrompt = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Function to toggle favorite status
private suspend fun toggleFavorite(
    db: FirebaseFirestore,
    auth: FirebaseAuth,
    flight: FavoriteFlight,
    isFavorite: Boolean,
    onSuccess: (Boolean) -> Unit
) {
    val userId = auth.currentUser?.uid ?: return

    try {
        val flightRef = db.collection("favorites")
            .document(userId)
            .collection("flights")
            .document(flight.id)

        if (isFavorite) {
            // Remove from favorites
            flightRef.delete().await()
            onSuccess(false)
        } else {
            // Add to favorites
            flightRef.set(flight).await()
            onSuccess(true)
        }
    } catch (e: Exception) {
        Log.e("SingleFlightDetails", "Error toggling favorite", e)
    }
}

// Add this composable function at the end of the file, outside the SingleFlightDetailsScreen function
@Composable
private fun ActivityItem(activity: String) {
    val activityText = activity.replace("•", "").replace("-", "").trim()
    val costPattern = Regex("\$$(\\d+[-–]\\d+|\\d+)\\s*TND\$$")
    val costMatch = costPattern.find(activityText)

    val mainText = if (costMatch != null) {
        activityText.substring(0, costMatch.range.first).trim()
    } else {
        activityText
    }

    val costText = costMatch?.groupValues?.get(0) ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon based on activity type
        val icon = when {
            activityText.lowercase().contains("garden") ||
                    activityText.lowercase().contains("park") -> painterResource(id = R.drawable.location_on)
            activityText.lowercase().contains("museum") ||
                    activityText.lowercase().contains("visit") -> painterResource(id = R.drawable.search)
            activityText.lowercase().contains("walk") ||
                    activityText.lowercase().contains("tour") -> painterResource(id = R.drawable.vector)
            activityText.lowercase().contains("market") ||
                    activityText.lowercase().contains("shop") -> painterResource(id = R.drawable.filter)
            else -> painterResource(id = R.drawable.location_on)
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF1EBFC3).copy(alpha = 0.15f), CircleShape)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = Color(0xFF1EBFC3),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mainText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp
            )

            if (costText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = costText,
                    fontSize = 14.sp,
                    color = Color(0xFF1EBFC3),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Helper function to calculate trip duration in days
private fun calculateTripDuration(departureAt: String, returnAt: String): Int {
    return try {
        if (departureAt.contains("T") && returnAt.contains("T")) {
            val departFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val returnFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

            val departDate = LocalDate.parse(departureAt.split("T")[0])
            val returnDate = LocalDate.parse(returnAt.split("T")[0])

            val days = ChronoUnit.DAYS.between(departDate, returnDate).toInt() + 1
            days.coerceAtLeast(1)
        } else {
            // Simple date format
            val departFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val returnFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            val departDate = LocalDate.parse(departureAt, departFormat)
            val returnDate = LocalDate.parse(returnAt, returnFormat)

            val days = ChronoUnit.DAYS.between(departDate, returnDate).toInt() + 1
            days.coerceAtLeast(1)
        }
    } catch (e: Exception) {
        Log.e("FlightDetails", "Error calculating trip duration: $e")
        3 // Default to 3 days if calculation fails
    }
}

// Helper function to format API dates with time
private fun formatApiDateWithTime(dateString: String): String {
    return try {
        if (dateString.contains("T")) {
            // Parse ISO format
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } else {
            // If no time information, return empty string
            ""
        }
    } catch (e: Exception) {
        Log.e("FlightDetails", "Error formatting date with time: $dateString", e)
        ""
    }
}

// Helper function to format API dates for display
private fun formatApiDateForDisplay(dateString: String): String {
    return try {
        if (dateString.contains("T")) {
            // Parse ISO format
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } else {
            // Parse simple date format
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        }
    } catch (e: Exception) {
        Log.e("FlightDetails", "Error formatting date for display: $dateString", e)
        dateString
    }
}

// Helper function to get origin city name from country
private fun getOriginCity(originCountry: String): String {
    return when (originCountry.lowercase()) {
        "tunisia" -> "Tunis"
        "france" -> "Paris"
        "italy" -> "Rome"
        "germany" -> "Frankfurt"
        "spain" -> "Madrid"
        else -> originCountry.capitalize()
    }
}

// Helper function to get origin airport code from country
private fun getOriginCode(originCountry: String): String {
    return when (originCountry.lowercase()) {
        "tunisia" -> "TUN"
        "france" -> "CDG"
        "italy" -> "FCO"
        "germany" -> "FRA"
        "spain" -> "MAD"
        else -> "???"
    }
}

// Extension function to capitalize first letter
private fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}
