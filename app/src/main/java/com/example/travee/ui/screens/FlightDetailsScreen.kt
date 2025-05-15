package com.example.travee.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.travee.R
import com.example.travee.service.SkyScannerApiService
import com.example.travee.ui.components.BottomNavBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightDetailsScreen(
    navController: NavController,
    budget: Double = 0.0,
    departureCountry: String = "",
    tripDays: Int = 7,
    departDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
) {
    val skyScannerApiService = remember { SkyScannerApiService() }
    var flightResults by remember { mutableStateOf<List<SkyScannerApiService.FlightResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Format dates for display
    val departDateFormatted = remember(departDate) {
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val outputFormatter = DateTimeFormatter.ofPattern("EEE, MMM dd")
            val date = LocalDate.parse(departDate, inputFormatter)
            date.format(outputFormatter)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    val returnDateFormatted = remember(departDate, tripDays) {
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val outputFormatter = DateTimeFormatter.ofPattern("EEE, MMM dd")
            val date = LocalDate.parse(departDate, inputFormatter).plusDays(tripDays.toLong())
            date.format(outputFormatter)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    // Fetch flight results when screen loads
    LaunchedEffect(departureCountry, budget, tripDays, departDate) {
        isLoading = true
        errorMessage = null

        try {
            Log.d("FlightDetails", "Searching flights with: $departureCountry, $budget, $tripDays, $departDate")
            val results = skyScannerApiService.searchFlights(
                originCountry = departureCountry,
                budget = budget,
                days = tripDays,
                departDate = departDate
            )
            flightResults = results
            Log.d("FlightDetails", "Found ${results.size} flights")
        } catch (e: Exception) {
            Log.e("FlightDetails", "Error fetching flights", e)
            errorMessage = "Failed to load flights: ${e.message}"
        } finally {
            isLoading = false
        }
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
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Flight Results",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Open search */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = { /* TODO: Open settings */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.filter),
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavBar(navController = navController, selectedItem = 0) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF1EBFC3)
                )
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = errorMessage ?: "Unknown error occurred",
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val results = skyScannerApiService.searchFlights(
                                        originCountry = departureCountry,
                                        budget = budget,
                                        days = tripDays,
                                        departDate = departDate
                                    )
                                    flightResults = results
                                } catch (e: Exception) {
                                    errorMessage = "Failed to load flights: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1EBFC3)
                        )
                    ) {
                        Text("Retry")
                    }
                }
            } else if (flightResults.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No flights found matching your criteria",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1EBFC3)
                        )
                    ) {
                        Text("Modify Search")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    item {
                        // Date information
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Depart",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = departDateFormatted,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1EBFC3),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Return",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = returnDateFormatted,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1EBFC3),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Flight results
                    items(flightResults) { flight ->
                        FlightResultCard(
                            flight = flight,
                            onDetailsClick = {
                                // Navigate to single flight details screen
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
                                            "originCountry=$departureCountry"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlightResultCard(
    flight: SkyScannerApiService.FlightResult,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Destination and price (swapped with airline)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = flight.destination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = "${flight.price} TND",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1EBFC3)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Airline as the main information (swapped with destination)
            Text(
                text = flight.airline,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1EBFC3)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Departure and return dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Departure",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = formatApiDate(flight.departureAt),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Return",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = formatApiDate(flight.returnAt),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details button
            Button(
                onClick = onDetailsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1EBFC3)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
// Helper function to format API dates
private fun formatApiDate(dateString: String): String {
    return try {
        if (dateString.contains("T")) {
            // Parse ISO format
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
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
        Log.e("FlightDetails", "Error formatting date: $dateString", e)
        dateString
    }
}
