package com.example.travee.ui.components

import androidx.compose.foundation.Image
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

@Composable
fun UserProfileHeader(
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

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
                        // User profile exists in Firestore
                        val profile = document.toObject(UserProfile::class.java)
                            ?.copy(
                                userId = auth.currentUser!!.uid,
                                email = auth.currentUser!!.email ?: "",
                                photoUrl = auth.currentUser!!.photoUrl?.toString() ?: ""
                            )
                        userProfile = profile
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
                            photoUrl = auth.currentUser!!.photoUrl?.toString() ?: "",
                            phoneNumber = auth.currentUser!!.phoneNumber ?: ""
                        )
                    }
                } catch (e: Exception) {
                    // Create a basic profile from auth data
                    val displayName = auth.currentUser!!.displayName ?: ""
                    val nameParts = displayName.split(" ")
                    val firstName = if (nameParts.isNotEmpty()) nameParts[0] else ""

                    userProfile = UserProfile(
                        userId = auth.currentUser!!.uid,
                        firstName = firstName,
                        email = auth.currentUser!!.email ?: ""
                    )
                }
            } else {
                // Not logged in, use default profile
                userProfile = UserProfile(firstName = "Guest")
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

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(if (isLoading) null else userProfile?.getAvatarUrl())
                .crossfade(true)
                .build(),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}
