package com.example.travee.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.travee.data.models.FavoriteFlight
import com.example.travee.ui.components.BottomNavBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    var favoriteFlights by remember { mutableStateOf<List<FavoriteFlight>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showLoginPrompt by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Check if user is logged in and load favorites
    LaunchedEffect(key1 = auth.currentUser?.uid) {
        if (auth.currentUser != null) {
            coroutineScope.launch {
                loadFavorites(
                    db = db,
                    userId = auth.currentUser!!.uid,
                    onSuccess = { flights ->
                        favoriteFlights = flights
                        isLoading = false
                    },
                    onError = {
                        isLoading = false
                    }
                )
            }
        } else {
            isLoading = false
            showLoginPrompt = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Favorite Flights",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = { BottomNavBar(navController = navController, selectedItem = 2) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1EBFC3))
                }
            } else if (auth.currentUser == null) {
                // Not logged in state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sign in to view your favorites",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Save your favorite flights to access them anytime",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1EBFC3)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (favoriteFlights.isEmpty()) {
                // No favorites state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Favorites Yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Start exploring flights and save your favorites",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { navController.navigate("home") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1EBFC3)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Explore Flights",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Show favorites list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favoriteFlights) { flight ->
                        FavoriteFlightCard(
                            flight = flight,
                            onCardClick = {
                                // Navigate to flight details
                                navController.navigate(
                                    "single_flight_details?" +
                                            "destination=${flight.destination}&" +
                                            "destinationAirport=${flight.destinationAirport}&" +
                                            "destinationCountry=${flight.destinationCountry}&" +
                                            "price=${flight.price}&" +
                                            "airline=${flight.airline}&" +
                                            "departureAt=${flight.departureAt}&" +
                                            "returnAt=${flight.returnAt}&" +
                                            "link=${flight.link}&" +
                                            "originCountry=${flight.originCountry}"
                                )
                            },
                            onRemoveClick = {
                                coroutineScope.launch {
                                    removeFavorite(
                                        db = db,
                                        userId = auth.currentUser!!.uid,
                                        flightId = flight.id,
                                        onSuccess = {
                                            // Reload favorites
                                            coroutineScope.launch {
                                                loadFavorites(
                                                    db = db,
                                                    userId = auth.currentUser!!.uid,
                                                    onSuccess = { updatedFlights ->
                                                        favoriteFlights = updatedFlights
                                                    },
                                                    onError = {}
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        )
                    }

                    // Add some space at the bottom
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    // Login prompt dialog
    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = { showLoginPrompt = false },
            title = { Text("Sign In Required") },
            text = { Text("You need to be signed in to view your favorites. Would you like to sign in now?") },
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

@Composable
fun FavoriteFlightCard(
    flight: FavoriteFlight,
    onCardClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with destination and favorite icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${flight.destination}, ${flight.destinationCountry}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1EBFC3)
                        )

                        Text(
                            text = flight.airline,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = onRemoveClick,
                        modifier = Modifier
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Remove from favorites",
                            tint = Color.Red
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Flight details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Origin
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "From",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = getOriginCode(flight.originCountry),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = getOriginCity(flight.originCountry),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    // Flight icon
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = Color(0xFF1EBFC3),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Destination
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "To",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = flight.destinationAirport,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = flight.destination,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date and price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dates
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatDateShort(flight.departureAt),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatDateShort(flight.returnAt),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Price
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1EBFC3),
                                        Color(0xFF0A9396)
                                    )
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${flight.price} TND",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// Function to load favorites from Firestore
private suspend fun loadFavorites(
    db: FirebaseFirestore,
    userId: String,
    onSuccess: (List<FavoriteFlight>) -> Unit,
    onError: () -> Unit
) {
    try {
        val snapshot = db.collection("favorites")
            .document(userId)
            .collection("flights")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        val flights = snapshot.documents.mapNotNull { doc ->
            doc.toObject(FavoriteFlight::class.java)
        }

        onSuccess(flights)
    } catch (e: Exception) {
        Log.e("FavoritesScreen", "Error loading favorites", e)
        onError()
    }
}

// Function to remove a favorite
private suspend fun removeFavorite(
    db: FirebaseFirestore,
    userId: String,
    flightId: String,
    onSuccess: () -> Unit
) {
    try {
        db.collection("favorites")
            .document(userId)
            .collection("flights")
            .document(flightId)
            .delete()
            .await()

        onSuccess()
    } catch (e: Exception) {
        Log.e("FavoritesScreen", "Error removing favorite", e)
    }
}

// Helper function to format date for display
private fun formatDateShort(dateString: String): String {
    return try {
        if (dateString.contains("T")) {
            // Parse ISO format
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } else {
            // Parse simple date format
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        }
    } catch (e: Exception) {
        Log.e("FavoritesScreen", "Error formatting date: $dateString", e)
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