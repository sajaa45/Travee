package com.example.travee.data.models

data class UserProfile(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val phoneNumber: String = "",
    val preferredCurrency: String = "TND",
    val preferredLanguage: String = "English",
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false
) {
    // Empty constructor for Firestore
    constructor() : this(
        userId = "",
        firstName = "",
        lastName = "",
        email = "",
        photoUrl = "",
        phoneNumber = "",
        preferredCurrency = "TND",
        preferredLanguage = "English",
        notificationsEnabled = true,
        darkModeEnabled = false
    )

    // Get full name
    fun getFullName(): String {
        return if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
            "$firstName $lastName"
        } else if (firstName.isNotEmpty()) {
            firstName
        } else if (lastName.isNotEmpty()) {
            lastName
        } else {
            "User"
        }
    }

    // Get display name (first name or email prefix)
    fun getDisplayName(): String {
        return if (firstName.isNotEmpty()) {
            firstName
        } else if (email.isNotEmpty()) {
            email.split("@")[0]
        } else {
            "User"
        }
    }

    // Get avatar URL based on name or default to Yoda
    fun getAvatarUrl(): String {
        return if (photoUrl.isNotEmpty()) {
            photoUrl
        } else {
            // Default Yoda image
            "https://i.imgur.com/7Kky3Vu.jpg"
        }
    }
}
