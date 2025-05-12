package com.example.travee.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SkyScannerApiService {

    private val countryToAirport = mapOf(
        "tunisia" to "TUN",
        "france" to "CDG",
        "italy" to "FCO",
        "germany" to "FRA",
        "spain" to "MAD"
        // ...add more countries as needed...
    )

    suspend fun searchFlights(
        originCountry: String,
        budget: Double,
        days: Int,
        departDate: String
    ): List<FlightResult> = withContext(Dispatchers.IO) {
        val origin = countryToAirport[originCountry.lowercase()]
            ?: throw IllegalArgumentException("Origin country not supported.")

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val depart = LocalDate.parse(departDate, formatter)
        val returnDate = depart.plusDays(days.toLong()).format(formatter)

        val urlStr = "https://api.travelpayouts.com/aviasales/v3/prices_for_dates" +
                "?origin=$origin&currency=tnd&depart_date=$departDate&return_date=$returnDate&trip_class=0&token=bb77b690cfa4857ad0a39521cb7bcc19"

        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"

        val response = conn.inputStream.bufferedReader().readText()
        val data = JSONObject(response)

        val results = mutableListOf<FlightResult>()
        if (data.has("data")) {
            val flights = data.getJSONArray("data")
            for (i in 0 until flights.length()) {
                val flight = flights.getJSONObject(i)
                val price = flight.optDouble("price", 0.0)
                if (price > 0 && price <= budget) {
                    results.add(
                        FlightResult(
                            destination = flight.optString("destination", "Unknown"),
                            destinationAirport = flight.optString("destination_airport", "Unknown"),
                            price = price,
                            airline = flight.optString("airline", "Unknown"),
                            departureAt = flight.optString("departure_at", "Unknown"),
                            returnAt = returnDate,
                            link = "https://www.aviasales.com" + flight.optString("link", "")
                        )
                    )
                }
            }
        }
        results
    }

    data class FlightResult(
        val destination: String,
        val destinationAirport: String,
        val price: Double,
        val airline: String,
        val departureAt: String,
        val returnAt: String,
        val link: String
    )
}