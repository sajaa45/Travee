package com.example.travee.ui.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.travee.R
import com.example.travee.model.FavoriteDestination
import com.example.travee.ui.components.BottomNavBar
import com.example.travee.ui.components.UserProfileHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(navController: NavController) {
    // Sample data for favorites
    val favoriteDestinations = remember {
        listOf(
            FavoriteDestination(
                id = 1,
                name = "Mount Bromo",
                distance = "5 km away",
                rating = 4.7f,
                imageResId = R.drawable.featured_2
            ),
            FavoriteDestination(
                id = 2,
                name = "Bali",
                distance = "15 km away",
                rating = 4.7f,
                imageResId = R.drawable.mask_group
            ),
            FavoriteDestination(
                id = 3,
                name = "Borobudur Temple",
                distance = "12 km away",
                rating = 4.5f,
                imageResId = R.drawable.featured_2
            ),
            FavoriteDestination(
                id = 4,
                name = "Raja Ampat",
                distance = "320 km away",
                rating = 4.9f,
                imageResId = R.drawable.mask_group
            )
        )
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController, selectedItem = 2) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // User profile header
            UserProfileHeader(name = "Hazar")

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text(
                    text = "Temukan Liburan Anda",
                    color = Color.LightGray
                ) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    containerColor = Color(0xFF2D3B41),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Favorites title
            Text(
                text = "Favorites",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Favorites list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favoriteDestinations) { destination ->
                    FavoriteDestinationCard(
                        destination = destination,
                        onClick = {
                            // TODO: Navigate to destination details
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteDestinationCard(
    destination: FavoriteDestination,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Destination image
            Image(
                painter = painterResource(id = destination.imageResId),
                contentDescription = destination.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay for better text visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0f,
                            endY = 300f
                        )
                    )
            )

            // Destination info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = destination.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = destination.distance,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            // Rating
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = destination.rating.toString(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
