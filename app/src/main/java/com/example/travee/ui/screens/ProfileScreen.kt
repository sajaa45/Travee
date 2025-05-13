package com.example.travee.ui.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.travee.R
import com.example.travee.ui.components.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    // Sample user data - in a real app, this would come from your user repository or API
    val userName = "Hazar"
    val userEmail = "hazar@example.com"
    val scrollState = rememberScrollState()

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController, selectedItem = 3) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile header with background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF1EBFC3))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Profile",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
            }

            // Profile picture - positioned to overlap the header and content
            Box(
                modifier = Modifier
                    .offset(y = (-50).dp)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(width = 4.dp, color = Color.White, shape = CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.yoda),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            // User name
            Text(
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .offset(y = (-40).dp)
            )

            // User email
            Text(
                text = userEmail,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .offset(y = (-40).dp)
            )

            // Profile content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .offset(y = (-30).dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Personal Information Section
                    Text(
                        text = "Personal Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.Person,
                        title = "Edit Profile",
                        subtitle = "Change your personal information"
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    ProfileMenuItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        subtitle = "Manage your notification preferences"
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    ProfileMenuItem(
                        icon = Icons.Outlined.CreditCard,
                        title = "Payment Methods",
                        subtitle = "Manage your payment options"
                    )

                    // Travel Preferences Section
                    Text(
                        text = "Travel Preferences",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.FlightTakeoff,
                        title = "Preferred Airlines",
                        subtitle = "Set your airline preferences"
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    ProfileMenuItem(
                        icon = Icons.Outlined.Hotel,
                        title = "Accommodation",
                        subtitle = "Set your accommodation preferences"
                    )

                    // App Settings Section
                    Text(
                        text = "App Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.Language,
                        title = "Language",
                        subtitle = "Change app language"
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    ProfileMenuItem(
                        icon = Icons.Outlined.DarkMode,
                        title = "Theme",
                        subtitle = "Change app theme"
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    ProfileMenuItem(
                        icon = Icons.Outlined.Help,
                        title = "Help & Support",
                        subtitle = "Get help with the app"
                    )
                }
            }

            // Logout button
            Button(
                onClick = {
                    // TODO: Implement logout logic
                    navController.navigate("welcome") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .offset(y = (-20).dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1EBFC3)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App version
            Text(
                text = "Version 1.0.0",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color(0xFF1EBFC3),
            modifier = Modifier
                .size(24.dp)
                .padding(end = 4.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
