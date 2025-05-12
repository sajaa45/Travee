package com.example.travee.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travee.R
import androidx.navigation.NavController
import com.example.travee.ui.components.BottomNavBar
import com.example.travee.ui.components.UserProfileHeader
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavBar(navController = navController, selectedItem = 0) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // User profile header
            UserProfileHeader(name = "Hazar")

            Spacer(modifier = Modifier.height(24.dp))

            // Welcome text with colored parts
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(
                        color = Color(0xFF1EBFC3),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )) {
                        append("Enter your requirements and\nwe will give you ")
                    }
                    withStyle(style = SpanStyle(
                        color = Color(0xFF4A6572),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )) {
                        append("Everything")
                    }
                },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Budget input
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Budget") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = "Budget"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { /* TODO: Open filter */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.filter),
                            contentDescription = "Filter"
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF1EBFC3)
                )
            )

            // Date range input
            OutlinedTextField(
                value = "July 08 - July 15",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.vector__1_),
                        contentDescription = "Date Range"
                    )
                },
                readOnly = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF1EBFC3)
                )
            )

            // Departure city input
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                placeholder = { Text("Departure City") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.location_on),
                        contentDescription = "Location"
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF1EBFC3)
                )
            )

            // Search button
            Button(
                onClick = { navController.navigate("search") },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(150.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1EBFC3)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Search",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
