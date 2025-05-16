package com.example.travee.model

data class FavoriteFlight(
    val id: String = "", // Firestore document ID
    val userId: String = "", // User ID who favorited this flight
    val destination: String = "",
    val destinationAirport: String = "",
    val destinationCountry: String = "",
    val price: Double = 0.0,
    val airline: String = "",
    val departureAt: String = "",
    val returnAt: String = "",
    val link: String = "",
    val originCountry: String = "",
    val timestamp: Long = System.currentTimeMillis() // For sorting by most recently added
) {
    // Empty constructor for Firestore
    constructor() : this(
        id = "",
        userId = "",
        destination = "",
        destinationAirport = "",
        destinationCountry = "",
        price = 0.0,
        airline = "",
        departureAt = "",
        returnAt = "",
        link = "",
        originCountry = "",
        timestamp = 0L
    )

    // Generate a unique ID for this flight
    fun generateFlightId(): String {
        return "$destinationAirport-$departureAt-$returnAt".hashCode().toString()
    }
}
