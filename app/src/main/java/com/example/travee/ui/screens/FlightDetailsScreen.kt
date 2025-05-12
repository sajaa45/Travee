package com.example.travee.ui.screens

import com.example.travee.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.travee.ui.components.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightDetailsScreen(navController: NavController) {
    var selectedTripType by remember { mutableStateOf("One Way") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id =R.drawable.vector),
                            contentDescription = "Flight",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Planned Flight",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Open search */ }) {
                        Icon(
                            painter = painterResource(id =R.drawable.search),
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = { /* TODO: Open settings */ }) {
                        Icon(
                            painter = painterResource(id =R.drawable.filter),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Flight route information
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "From CGK",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Bengaluru",
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
                        text = "23:21 (BLR)",
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
                        text = "To BKK",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Bangkok",
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
                        text = "04:45 (BKK)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Trip type selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { selectedTripType = "One Way" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTripType == "One Way") Color(0xFF1EBFC3) else Color.Gray.copy(alpha = 0.2f),
                        contentColor = if (selectedTripType == "One Way") Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("One Way")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { selectedTripType = "Round Trip" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTripType == "Round Trip") Color(0xFF1EBFC3) else Color.Gray.copy(alpha = 0.2f),
                        contentColor = if (selectedTripType == "Round Trip") Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Round Trip")
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
                        text = "Sun, Jul 23",
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
                        text = "Mon, Jul 24",
                        fontSize = 16.sp,
                        color = Color(0xFF1EBFC3),
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

            Spacer(modifier = Modifier.height(32.dp))

            // Book flight button
            Button(
                onClick = { /* TODO: Book flight */ },
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

            Spacer(modifier = Modifier.height(24.dp))

            // Offers section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Offers",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1EBFC3)
                )

                TextButton(onClick = { /* TODO: See all offers */ }) {
                    Text(
                        text = "See All",
                        color = Color.Gray
                    )
                }
            }

            // Here you would add your offers carousel/grid
            // This would connect to your activity suggestion API
        }
    }
}
