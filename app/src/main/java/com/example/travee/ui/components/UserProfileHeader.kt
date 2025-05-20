package com.example.travee.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.travee.data.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

@Composable
fun UserProfileHeader(
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val defaultImageUrl = "https://i.ebayimg.com/images/g/AxUAAOSw0t9f26n8/s-l1200.jpg"
    var profileImageUrl by remember { mutableStateOf(defaultImageUrl) }

    // Load user profile
    LaunchedEffect(key1 = auth.currentUser?.uid) {
        coroutineScope.launch {
            if (auth.currentUser != null) {
                try {
                    // Try to get user profile from Firestore
                    val document = db.collection("users")
                        .document(auth.currentUser!!.uid)
                        .get()
                        .await()

                    if (document.exists()) {
                        // Debug: Log the document data to see what's actually stored
                        Log.d("UserProfileHeader", "Document data: ${document.data}")

                        // Check for both photoUrl and avatarUrl fields
                        val photoUrl = document.getString("photoUrl")
                        val avatarUrl = document.getString("avatarUrl")

                        // Use whichever is not null or empty, with preference for photoUrl
                        profileImageUrl = when {
                            !photoUrl.isNullOrEmpty() -> photoUrl
                            !avatarUrl.isNullOrEmpty() -> avatarUrl
                            else -> defaultImageUrl
                        }

                        Log.d("UserProfileHeader", "Using profile image URL: $profileImageUrl")

                        // User profile exists in Firestore
                        val profile = document.toObject(UserProfile::class.java)
                        userProfile = profile?.copy(
                            userId = auth.currentUser!!.uid,
                            email = auth.currentUser!!.email ?: "",
                            photoUrl = profileImageUrl
                        )
                    } else {
                        // Create default profile
                        val displayName = auth.currentUser!!.displayName ?: ""
                        val nameParts = displayName.split(" ")
                        val firstName = if (nameParts.isNotEmpty()) nameParts[0] else ""
                        val lastName = if (nameParts.size > 1) nameParts.subList(1, nameParts.size).joinToString(" ") else ""

                        userProfile = UserProfile(
                            userId = auth.currentUser!!.uid,
                            firstName = firstName,
                            lastName = lastName,
                            email = auth.currentUser!!.email ?: "",
                            photoUrl = defaultImageUrl,
                            phoneNumber = auth.currentUser!!.phoneNumber ?: ""
                        )
                    }
                } catch (e: Exception) {
                    Log.e("UserProfileHeader", "Error loading profile", e)
                    // Create a basic profile from auth data
                    val displayName = auth.currentUser!!.displayName ?: ""
                    val nameParts = displayName.split(" ")
                    val firstName = if (nameParts.isNotEmpty()) nameParts[0] else ""

                    userProfile = UserProfile(
                        userId = auth.currentUser!!.uid,
                        firstName = firstName,
                        email = auth.currentUser!!.email ?: "",
                        photoUrl = defaultImageUrl
                    )
                }
            } else {
                // Not logged in, use default profile
                userProfile = UserProfile(firstName = "Guest", photoUrl = defaultImageUrl)
            }
            isLoading = false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello, ${if (isLoading) "..." else userProfile?.getDisplayName() ?: "User"}",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                text = "Let's Travel",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1EBFC3)
            )
        }

        // Use the profileImageUrl directly instead of relying on userProfile?.getAvatarUrl()
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(profileImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            onError = {
                // If loading fails, update to use default image
                profileImageUrl = defaultImageUrl
                Log.e("UserProfileHeader", "Error loading image, falling back to default")
            }
        )
    }
}