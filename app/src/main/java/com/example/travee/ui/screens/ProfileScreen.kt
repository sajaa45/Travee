package com.example.travee.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.travee.model.UserProfile
import com.example.travee.ui.components.BottomNavBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // State for user profile
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    // Load user profile when screen loads
    LaunchedEffect(key1 = auth.currentUser?.uid) {
        if (auth.currentUser != null) {
            loadUserProfile(auth, db) { profile ->
                userProfile = profile
                isLoading = false
            }
        } else {
            // Not logged in, navigate to login screen
            navController.navigate("login") {
                popUpTo("profile") { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController, selectedItem = 3) }
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
            } else {
                // Profile content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                            .clickable {
                                // Show avatar options dialog
                                showAvatarDialog = true
                            }
                    ) {
                        // Profile image
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(userProfile?.photoUrl.takeIf { !it.isNullOrEmpty() }
                                    ?: userProfile?.getAvatarUrl())
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        // Camera icon overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1EBFC3))
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Photo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // User name
                    Text(
                        text = userProfile?.getFullName() ?: "User",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .offset(y = (-40).dp)
                    )

                    // User email
                    Text(
                        text = userProfile?.email ?: "",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .offset(y = (-40).dp)
                    )

                    // Edit profile button
                    Button(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .offset(y = (-30).dp)
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1EBFC3)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile")
                    }

                    // Profile content
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .offset(y = (-20).dp),
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
                                title = "Name",
                                subtitle = userProfile?.getFullName() ?: "Not set"
                            )

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            ProfileMenuItem(
                                icon = Icons.Outlined.Email,
                                title = "Email",
                                subtitle = userProfile?.email ?: "Not set"
                            )

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            ProfileMenuItem(
                                icon = Icons.Outlined.Phone,
                                title = "Phone",
                                subtitle = userProfile?.phoneNumber?.takeIf { it.isNotEmpty() } ?: "Not set"
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
                                subtitle = userProfile?.preferredLanguage ?: "English"
                            )

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            ProfileMenuItem(
                                icon = Icons.Outlined.AttachMoney,
                                title = "Currency",
                                subtitle = userProfile?.preferredCurrency ?: "TND"
                            )

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            ProfileMenuItem(
                                icon = Icons.Outlined.DarkMode,
                                title = "Theme",
                                subtitle = if (userProfile?.darkModeEnabled == true) "Dark Mode" else "Light Mode"
                            )

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            ProfileMenuItem(
                                icon = Icons.Outlined.Notifications,
                                title = "Notifications",
                                subtitle = if (userProfile?.notificationsEnabled == true) "Enabled" else "Disabled"
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
                        onClick = { showLogoutConfirmation = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .offset(y = (-10).dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A6572)
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
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            userProfile = userProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updatedProfile ->
                coroutineScope.launch {
                    try {
                        // Update profile in Firestore
                        updateUserProfile(auth, db, updatedProfile)
                        // Refresh user profile
                        loadUserProfile(auth, db) { profile ->
                            userProfile = profile
                        }
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("ProfileScreen", "Error updating profile", e)
                        Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
                showEditProfileDialog = false
            }
        )
    }

    // Avatar Options Dialog
    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text("Profile Photo") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Option 1: Use generated avatar
                    ListItem(
                        headlineContent = { Text("Use generated avatar") },
                        supportingContent = { Text("Create an avatar based on your name") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Generated Avatar",
                                tint = Color(0xFF1EBFC3)
                            )
                        },
                        modifier = Modifier.clickable {
                            coroutineScope.launch {
                                try {
                                    // Generate avatar URL based on name
                                    val avatarUrl = userProfile?.getAvatarUrl() ?: ""

                                    // Update profile with avatar URL
                                    updateProfilePhotoUrl(auth, db, avatarUrl)

                                    // Refresh user profile
                                    loadUserProfile(auth, db) { profile ->
                                        userProfile = profile
                                    }

                                    Toast.makeText(context, "Avatar updated", Toast.LENGTH_SHORT).show()
                                    showAvatarDialog = false
                                } catch (e: Exception) {
                                    Log.e("ProfileScreen", "Error updating avatar", e)
                                    Toast.makeText(context, "Failed to update avatar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )

                    Divider()

                    // Option 2: Enter URL
                    ListItem(
                        headlineContent = { Text("Enter photo URL") },
                        supportingContent = { Text("Use an existing image URL") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Enter URL",
                                tint = Color(0xFF1EBFC3)
                            )
                        },
                        modifier = Modifier.clickable {
                            // Close this dialog and show URL input dialog
                            showAvatarDialog = false
                            // Show URL input dialog (implement separately)
                            showPhotoUrlDialog(context, auth, db) { success ->
                                if (success) {
                                    // Refresh user profile
                                    coroutineScope.launch {
                                        loadUserProfile(auth, db) { profile ->
                                            userProfile = profile
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        auth.signOut()
                        showLogoutConfirmation = false
                        navController.navigate("welcome") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("No")
                }
            }
        )
    }
}

// Function to show photo URL input dialog
private fun showPhotoUrlDialog(
    context: android.content.Context,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onComplete: (Boolean) -> Unit
) {
    val builder = android.app.AlertDialog.Builder(context)
    builder.setTitle("Enter Photo URL")

    // Set up the input
    val input = android.widget.EditText(context)
    input.hint = "https://example.com/photo.jpg"
    input.inputType = android.text.InputType.TYPE_CLASS_TEXT
    builder.setView(input)

    // Set up the buttons
    builder.setPositiveButton("OK") { dialog, _ ->
        val photoUrl = input.text.toString().trim()
        if (photoUrl.isNotEmpty()) {
            // Update profile with the entered URL
            kotlinx.coroutines.MainScope().launch {
                try {
                    updateProfilePhotoUrl(auth, db, photoUrl)
                    android.widget.Toast.makeText(context, "Profile photo updated", android.widget.Toast.LENGTH_SHORT).show()
                    onComplete(true)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Failed to update profile photo", android.widget.Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }
        } else {
            android.widget.Toast.makeText(context, "Please enter a valid URL", android.widget.Toast.LENGTH_SHORT).show()
            onComplete(false)
        }
        dialog.dismiss()
    }

    builder.setNegativeButton("Cancel") { dialog, _ ->
        dialog.cancel()
        onComplete(false)
    }

    builder.show()
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    userProfile: UserProfile?,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var firstName by remember { mutableStateOf(userProfile?.firstName ?: "") }
    var lastName by remember { mutableStateOf(userProfile?.lastName ?: "") }
    var phoneNumber by remember { mutableStateOf(userProfile?.phoneNumber ?: "") }
    var preferredLanguage by remember { mutableStateOf(userProfile?.preferredLanguage ?: "English") }
    var preferredCurrency by remember { mutableStateOf(userProfile?.preferredCurrency ?: "TND") }
    var notificationsEnabled by remember { mutableStateOf(userProfile?.notificationsEnabled ?: true) }
    var darkModeEnabled by remember { mutableStateOf(userProfile?.darkModeEnabled ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // First Name
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true
                )

                // Last Name
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true
                )

                // Phone Number
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true
                )

                // Language Dropdown
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = preferredLanguage,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Language") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Language"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                }

                // Currency Dropdown
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = preferredCurrency,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Currency") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Currency"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                }

                // Notifications Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Notifications")
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }

                // Dark Mode Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dark Mode")
                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedProfile = userProfile?.copy(
                        firstName = firstName,
                        lastName = lastName,
                        phoneNumber = phoneNumber,
                        preferredLanguage = preferredLanguage,
                        preferredCurrency = preferredCurrency,
                        notificationsEnabled = notificationsEnabled,
                        darkModeEnabled = darkModeEnabled
                    ) ?: UserProfile(
                        userId = "",
                        firstName = firstName,
                        lastName = lastName,
                        email = "",
                        phoneNumber = phoneNumber,
                        preferredLanguage = preferredLanguage,
                        preferredCurrency = preferredCurrency,
                        notificationsEnabled = notificationsEnabled,
                        darkModeEnabled = darkModeEnabled
                    )
                    onSave(updatedProfile)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Function to load user profile from Firestore
private suspend fun loadUserProfile(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onComplete: (UserProfile) -> Unit
) {
    try {
        val user = auth.currentUser ?: return

        // Try to get user profile from Firestore
        val document = db.collection("users")
            .document(user.uid)
            .get()
            .await()

        if (document.exists()) {
            // User profile exists in Firestore
            val userProfile = document.toObject(UserProfile::class.java)
                ?.copy(
                    userId = user.uid,
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString() ?: ""
                ) ?: createDefaultUserProfile(user)

            onComplete(userProfile)
        } else {
            // Create a new user profile
            val newProfile = createDefaultUserProfile(user)

            // Save to Firestore
            db.collection("users")
                .document(user.uid)
                .set(newProfile)
                .await()

            onComplete(newProfile)
        }
    } catch (e: Exception) {
        Log.e("ProfileScreen", "Error loading user profile", e)
        // Return a default profile if there's an error
        auth.currentUser?.let {
            onComplete(createDefaultUserProfile(it))
        }
    }
}

// Function to create a default user profile
private fun createDefaultUserProfile(user: com.google.firebase.auth.FirebaseUser): UserProfile {
    val displayName = user.displayName ?: ""
    val nameParts = displayName.split(" ")
    val firstName = if (nameParts.isNotEmpty()) nameParts[0] else ""
    val lastName = if (nameParts.size > 1) nameParts.subList(1, nameParts.size).joinToString(" ") else ""

    return UserProfile(
        userId = user.uid,
        firstName = firstName,
        lastName = lastName,
        email = user.email ?: "",
        photoUrl = user.photoUrl?.toString() ?: "",
        phoneNumber = user.phoneNumber ?: ""
    )
}

// Function to update user profile in Firestore
private suspend fun updateUserProfile(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    userProfile: UserProfile
) {
    try {
        val user = auth.currentUser ?: return

        // Update display name in Firebase Auth
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName("${userProfile.firstName} ${userProfile.lastName}")
            .build()

        user.updateProfile(profileUpdates).await()

        // Update profile in Firestore
        db.collection("users")
            .document(user.uid)
            .set(userProfile)
            .await()
    } catch (e: Exception) {
        Log.e("ProfileScreen", "Error updating user profile", e)
        throw e
    }
}

// Function to update profile photo URL
private suspend fun updateProfilePhotoUrl(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    photoUrl: String
) {
    try {
        val user = auth.currentUser ?: return

        // Update photo URL in Firebase Auth
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(photoUrl))
            .build()

        user.updateProfile(profileUpdates).await()

        // Update photo URL in Firestore
        db.collection("users")
            .document(user.uid)
            .update("photoUrl", photoUrl)
            .await()
    } catch (e: Exception) {
        Log.e("ProfileScreen", "Error updating profile photo URL", e)
        throw e
    }
}
