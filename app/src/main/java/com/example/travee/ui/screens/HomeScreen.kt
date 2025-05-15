package com.example.travee.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.travee.R
import com.example.travee.ui.components.BottomNavBar
import com.example.travee.ui.components.UserProfileHeader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // State to hold user's first name
    var userName by remember { mutableStateOf("User") }

    // State for input fields
    var budgetInput by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<Calendar?>(null) }
    var endDate by remember { mutableStateOf<Calendar?>(null) }
    var departureCity by remember { mutableStateOf("") }

    // State for dialogs
    var showDatePicker by remember { mutableStateOf(false) }
    var dateSelectionMode by remember { mutableStateOf<DateSelectionMode>(DateSelectionMode.Start) }
    var showCountryPicker by remember { mutableStateOf(false) }
    val DarkGray = Color(0xFFA9A9A9)
    // Countries list and filtered list
    val countries = remember { getAllCountries() }
    var countrySearchQuery by remember { mutableStateOf("") }
    val filteredCountries = remember(countrySearchQuery, countries) {
        if (countrySearchQuery.isEmpty()) {
            countries
        } else {
            countries.filter { it.lowercase().contains(countrySearchQuery.lowercase()) }
        }
    }

    // Focus manager
    val focusManager = LocalFocusManager.current

    // Fetch user's first name from Firestore
    LaunchedEffect(key1 = auth.currentUser?.uid) {
        auth.currentUser?.uid?.let { userId ->
            try {
                db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val firstName = document.getString("firstName")
                            if (!firstName.isNullOrEmpty()) {
                                userName = firstName
                                Log.d("HomeScreen", "User name loaded: $userName")
                            }
                        } else {
                            Log.d("HomeScreen", "No user document found")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("HomeScreen", "Error getting user data", e)
                    }
            } catch (e: Exception) {
                Log.e("HomeScreen", "Error retrieving user data", e)
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController, selectedItem = 0) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // User profile header with dynamically loaded name
            UserProfileHeader(name = userName)

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

            // Budget input (float only)
            OutlinedTextField(
                value = budgetInput,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() ||
                        newValue.matches(Regex("^\\d*\\.?\\d*$")) &&
                        !newValue.startsWith(".") &&
                        newValue.count { it == '.' } <= 1
                    ) {
                        budgetInput = newValue
                    }
                },
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF1EBFC3)
                )
            )

            // Date range input styled like departure city input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable {
                        focusManager.clearFocus()
                        dateSelectionMode = if (startDate == null) DateSelectionMode.Start else DateSelectionMode.End
                        showDatePicker = true
                    }
            ) {
                OutlinedTextField(
                    value = formatDateRange(startDate, endDate),
                    onValueChange = {},
                    enabled = false,
                    placeholder = { Text("Select Date Range", color = Color.DarkGray) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.vector__1_),
                            contentDescription = "Date Range",
                            tint = Color.DarkGray
                        )
                    },
                    trailingIcon = {
                        if (startDate != null || endDate != null) {
                            IconButton(onClick = {
                                startDate = null
                                endDate = null
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear dates", tint = Color.DarkGray)
                            }
                        } else {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select dates", tint = Color.DarkGray)
                        }
                    },// match the structure with city input
                    readOnly = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.LightGray,
                        disabledPlaceholderColor = Color.Gray,
                        disabledLeadingIconColor = Color.DarkGray,
                        disabledTrailingIconColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Departure city input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable {
                        focusManager.clearFocus()
                        showCountryPicker = true

                    }
            ) {
                OutlinedTextField(
                    value = departureCity,
                    onValueChange = {},
                    enabled = false, // keep disabled
                    placeholder = { Text("Departure City", color = Color.DarkGray) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.location_on),
                            contentDescription = "Location",
                            tint = Color.DarkGray // custom color for disabled icon
                        )
                    },
                    trailingIcon = {
                        if (departureCity.isNotEmpty()) {
                            IconButton(onClick = { departureCity = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear city", tint = Color.DarkGray)
                            }
                        } else {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select city", tint = Color.DarkGray)
                        }
                    },
                    readOnly = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.LightGray,
                        disabledPlaceholderColor = Color.Gray,
                        disabledLeadingIconColor = Color.DarkGray,
                        disabledTrailingIconColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Search button
            Button(
                onClick = {
                    val budget = budgetInput.toDoubleOrNull() ?: 0.0

                    // Calculate trip duration in days
                    val tripDays = if (startDate != null && endDate != null) {
                        val diffInMillis = endDate!!.timeInMillis - startDate!!.timeInMillis
                        (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1 // +1 to include both start and end days
                    } else {
                        7 // Default to 7 days if no date range selected
                    }

                    // Format departure date for API
                    val departDateFormatted = if (startDate != null) {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val localDate = startDate!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        localDate.format(formatter)
                    } else {
                        // Default to today's date if no start date selected
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }

                    // Navigate to flight details with all required parameters
                    navController.navigate(
                        "flight_details?budget=$budget" +
                                "&departureCountry=${departureCity.lowercase()}" +
                                "&tripDays=$tripDays" +
                                "&departDate=$departDateFormatted"
                    )
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(150.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1EBFC3)
                ),
                shape = RoundedCornerShape(24.dp),
                enabled = budgetInput.isNotEmpty() && departureCity.isNotEmpty() && startDate != null
            ) {
                Text(
                    text = "Search",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val initialDate = when (dateSelectionMode) {
            DateSelectionMode.Start -> startDate?.timeInMillis ?: System.currentTimeMillis()
            DateSelectionMode.End -> endDate?.timeInMillis ?: (startDate?.timeInMillis ?: System.currentTimeMillis())
        }

        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate)

        Dialog(onDismissRequest = { showDatePicker = false }) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select ${if (dateSelectionMode == DateSelectionMode.Start) "Start" else "End"} Date",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = {
                            val selection = datePickerState.selectedDateMillis
                            if (selection != null) {
                                val calendar = Calendar.getInstance()
                                calendar.timeInMillis = selection

                                when (dateSelectionMode) {
                                    DateSelectionMode.Start -> {
                                        startDate = calendar
                                        // If end date is before new start date, clear it
                                        if (endDate != null && endDate!!.before(calendar)) {
                                            endDate = null
                                        }
                                        // Prompt for end date if not set
                                        if (endDate == null) {
                                            dateSelectionMode = DateSelectionMode.End
                                            return@Button
                                        }
                                    }
                                    DateSelectionMode.End -> {
                                        endDate = calendar
                                        // If start date is after new end date, clear it
                                        if (startDate != null && startDate!!.after(calendar)) {
                                            startDate = null
                                        }
                                    }
                                }
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }

    // Country picker dialog
    if (showCountryPicker) {
        CountryPickerDialog(
            countries = filteredCountries,
            searchQuery = countrySearchQuery,
            onSearchQueryChange = { countrySearchQuery = it },
            onDismiss = {
                showCountryPicker = false
                countrySearchQuery = ""
            },
            onCountrySelected = { country ->
                departureCity = country
                showCountryPicker = false
                countrySearchQuery = ""
                focusManager.clearFocus()
            }
        )
    }
}

enum class DateSelectionMode {
    Start, End
}

@Composable
fun CountryPickerDialog(
    countries: List<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCountrySelected: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Country",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    placeholder = { Text("Search countries") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(countries) { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCountrySelected(country)
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = country,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (country != countries.last()) {
                            Divider()
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

private fun formatDateRange(startDate: Calendar?, endDate: Calendar?): String {
    return when {
        startDate != null && endDate != null -> {
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            "${dateFormat.format(startDate.time)} - ${dateFormat.format(endDate.time)}"
        }
        startDate != null -> {
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            "${dateFormat.format(startDate.time)} - Select End Date"
        }
        else -> "Select Date Range"
    }
}

private fun getAllCountries(): List<String> {
    return Locale.getISOCountries().map { countryCode ->
        Locale("", countryCode).displayCountry
    }.sorted()
}
