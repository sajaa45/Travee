package com.example.travee.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.travee.R
import com.example.travee.navigation.navigateWithSaveState
import com.example.travee.ui.components.BottomNavBar
import com.example.travee.ui.components.UserProfileHeader
import com.example.travee.viewmodel.HomeViewModel
import com.example.travee.viewmodel.SharedViewModel
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
    db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    viewModel: HomeViewModel = viewModel(),
    sharedViewModel: SharedViewModel = viewModel()
) {
    // Collect state from ViewModel
    val budgetInput by viewModel.budgetInput.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val departureCity by viewModel.departureCity.collectAsState()
    val userName by viewModel.userName.collectAsState()

    // Get saved scroll position from SharedViewModel
    val savedScrollPosition by sharedViewModel.homeScrollPosition.collectAsState()
    val scrollState = rememberScrollState()

    // And add this LaunchedEffect to set the scroll position:
    LaunchedEffect(savedScrollPosition) {
        if (savedScrollPosition > 0) {
            scrollState.scrollTo(savedScrollPosition)
        }
    }

    // Save scroll position when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            sharedViewModel.updateHomeScrollPosition(scrollState.value)
        }
    }

    // State for dialogs
    var showDateRangePicker by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }

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
        viewModel.fetchUserData(auth, db)
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController, selectedItem = 0) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // User profile header with dynamically loaded name
            UserProfileHeader()

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
                        viewModel.updateBudget(newValue)
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
                        showDateRangePicker = true
                    }
            ) {OutlinedTextField(
                value = formatDateRange(startDate, endDate),
                onValueChange = {},
                enabled = false,
                placeholder = {
                    Text(
                        "Select Date Range",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.vector__1_),
                        contentDescription = "Date Range",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (startDate != null || endDate != null) {
                        IconButton(onClick = {
                            viewModel.clearDates()
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear dates",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select dates",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                readOnly = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                    enabled = false,
                    placeholder = { Text("Departure City", color = Color.DarkGray) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.location_on),
                            contentDescription = "Location",
                            tint = Color.DarkGray
                        )
                    },
                    trailingIcon = {
                        if (departureCity.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateDepartureCity("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear city", tint = Color.DarkGray)
                            }
                        } else {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select city", tint = Color.DarkGray)
                        }
                    },
                    readOnly = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
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

                    // Format departure date for API - FIXED to ensure we're using the start date
                    val departDateFormatted = if (startDate != null) {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val localDate = startDate!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        localDate.format(formatter)
                    } else {
                        // Default to today's date if no start date selected
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }

                    // Log the dates for debugging
                    Log.d("HomeScreen", "Search with: Start date = $departDateFormatted, Trip days = $tripDays")

                    // Navigate to flight details with all required parameters
                    navController.navigateWithSaveState(
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

    // Improved date range picker dialog
    if (showDateRangePicker) {
        ImprovedDateRangePickerDialog(
            initialStartDate = startDate,
            initialEndDate = endDate,
            onDismiss = { showDateRangePicker = false },
            onDateRangeSelected = { start, end ->
                viewModel.updateStartDate(start)
                viewModel.updateEndDate(end)
                showDateRangePicker = false
            }
        )
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
                viewModel.updateDepartureCity(country)
                showCountryPicker = false
                countrySearchQuery = ""
                focusManager.clearFocus()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImprovedDateRangePickerDialog(
    initialStartDate: Calendar?,
    initialEndDate: Calendar?,
    onDismiss: () -> Unit,
    onDateRangeSelected: (Calendar?, Calendar?) -> Unit
) {
    val today = Calendar.getInstance()

    // State for selected dates - Initialize start date to today if null
    var startDate by remember { mutableStateOf(initialStartDate ?: today) }
    var endDate by remember { mutableStateOf(initialEndDate) }

    // State for current view
    var currentView by remember { mutableStateOf(DatePickerView.Start) }

    // Initial date for the date picker
    val initialDate = when (currentView) {
        DatePickerView.Start -> startDate.timeInMillis
        DatePickerView.End -> endDate?.timeInMillis ?:
        (startDate.timeInMillis + 7 * 24 * 60 * 60 * 1000)
    }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Title
                Text(
                    text = "Select Travel Dates",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Date range display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Start date card
                    DateCard(
                        label = "Departure",
                        date = startDate,
                        isSelected = currentView == DatePickerView.Start,
                        onClick = { currentView = DatePickerView.Start }
                    )

                    // Arrow between dates
                    Icon(
                        painter = painterResource(id = R.drawable.vector),
                        contentDescription = "To",
                        tint = Color(0xFF1EBFC3),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .align(Alignment.CenterVertically)
                    )

                    // End date card
                    DateCard(
                        label = "Return",
                        date = endDate,
                        isSelected = currentView == DatePickerView.End,
                        onClick = { currentView = DatePickerView.End }
                    )
                }

                // Date picker
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    title = null,
                    headline = null,
                    showModeToggle = false
                )

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFF1EBFC3)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF1EBFC3))
                    }

                    // Next/OK button
                    Button(
                        onClick = {
                            val selection = datePickerState.selectedDateMillis
                            if (selection != null) {
                                val calendar = Calendar.getInstance()
                                calendar.timeInMillis = selection

                                when (currentView) {
                                    DatePickerView.Start -> {
                                        startDate = calendar
                                        // If end date is before new start date, clear it
                                        if (endDate != null && endDate!!.before(calendar)) {
                                            endDate = null
                                        }

                                        // Switch to end date selection if not set
                                        if (endDate == null) {
                                            currentView = DatePickerView.End
                                            return@Button
                                        }
                                    }
                                    DatePickerView.End -> {
                                        // Ensure end date is not before start date
                                        if (calendar.before(startDate)) {
                                            // Show error or set to start date + 1 day
                                            val newEnd = Calendar.getInstance()
                                            newEnd.timeInMillis = startDate.timeInMillis
                                            newEnd.add(Calendar.DAY_OF_MONTH, 1)
                                            endDate = newEnd
                                        } else {
                                            endDate = calendar
                                        }
                                    }
                                }
                            }

                            // If both dates are selected or we're on the end date view, confirm selection
                            if (endDate != null || currentView == DatePickerView.End) {
                                onDateRangeSelected(startDate, endDate)
                            } else {
                                // Otherwise switch to end date selection
                                currentView = DatePickerView.End
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1EBFC3)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(if (endDate != null || currentView == DatePickerView.End) "OK" else "Next")
                    }
                }
            }
        }
    }
}

@Composable
fun DateCard(
    label: String,
    date: Calendar?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1EBFC3).copy(alpha = 0.1f) else Color.White
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF1EBFC3) else Color.LightGray
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (date != null) {
                    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    dateFormat.format(date.time)
                } else {
                    "Select"
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color(0xFF1EBFC3) else Color.Black
            )
        }
    }
}

enum class DatePickerView {
    Start, End
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
