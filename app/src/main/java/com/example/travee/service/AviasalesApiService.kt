package com.example.travee.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class AviasalesApiService {

    // Enhanced country to airport mapping with Tunisian focus
    private val countryToAirport = mapOf(
        "tunisia" to "TUN",
        "france" to "CDG",
        "italy" to "FCO",
        "germany" to "FRA",
        "spain" to "MAD",
        "algeria" to "ALG",
        "morocco" to "CMN",
        "libya" to "TIP",
        "egypt" to "CAI",
        "turkey" to "IST",
        "saudi arabia" to "RUH",
        "qatar" to "DOH",
        "uae" to "DXB",
        "uk" to "LHR",
        "usa" to "JFK",
        "canada" to "YYZ"
    )

    // Enhanced airline mapping with Tunisian focus
    private val airlineCodeToName = mapOf(
        // Tunisian Airlines
        "TU" to "Tunisair",
        "BJ" to "Nouvelair",
        "UG" to "Tunisair Express",

        // Major African Airlines
        "AH" to "Air Algérie",
        "AT" to "Royal Air Maroc",
        "MS" to "EgyptAir",
        "ET" to "Ethiopian Airlines",
        "KQ" to "Kenya Airways",
        "SA" to "South African Airways",

        // European Airlines that operate in Tunisia
        "AF" to "Air France",
        "BA" to "British Airways",
        "LH" to "Lufthansa",
        "AZ" to "ITA Airways",
        "IB" to "Iberia",
        "TK" to "Turkish Airlines",
        "KL" to "KLM",
        "SN" to "Brussels Airlines",
        "TP" to "TAP Portugal",
        "LX" to "Swiss International Air Lines",
        "A3" to "Aegean Airlines",
        "U2" to "easyJet",
        "FR" to "Ryanair",
        "W6" to "Wizz Air",

        // Middle Eastern Airlines
        "EK" to "Emirates",
        "QR" to "Qatar Airways",
        "EY" to "Etihad Airways",
        "SV" to "Saudia",
        "GF" to "Gulf Air",
        "FZ" to "flydubai",

        // Other major international airlines
        "DL" to "Delta Air Lines",
        "UA" to "United Airlines",
        "AA" to "American Airlines",
        "AC" to "Air Canada"
    )

    // Enhanced airport info with Tunisian airports and popular destinations from Tunisia
    private val airportInfo = mapOf(
        // Tunisian Airports
        "TUN" to Pair("Tunis", "Tunisia"),
        "NBE" to Pair("Enfidha", "Tunisia"),
        "DJE" to Pair("Djerba", "Tunisia"),
        "SFA" to Pair("Sfax", "Tunisia"),
        "TBJ" to Pair("Tabarka", "Tunisia"),
        "GAE" to Pair("Gabes", "Tunisia"),
        "TOE" to Pair("Tozeur", "Tunisia"),

        // North Africa
        "ALG" to Pair("Algiers", "Algeria"),
        "ORN" to Pair("Oran", "Algeria"),
        "CMN" to Pair("Casablanca", "Morocco"),
        "RAK" to Pair("Marrakech", "Morocco"),
        "TIP" to Pair("Tripoli", "Libya"),
        "CAI" to Pair("Cairo", "Egypt"),
        "HRG" to Pair("Hurghada", "Egypt"),


        // Europe - Added the requested airports here
        "NCE" to Pair("Nice", "France"),
        "BLQ" to Pair("Bologna", "Italy"),
        "PAR" to Pair("Paris", "France"), // Note: PAR is a city code covering both CDG and ORY
        "TGD" to Pair("Podgorica", "Montenegro"),
        // Europe
        "CDG" to Pair("Paris", "France"),
        "ORY" to Pair("Paris", "France"),
        "MRS" to Pair("Marseille", "France"),
        "LYS" to Pair("Lyon", "France"),
        "LHR" to Pair("London", "United Kingdom"),
        "LGW" to Pair("London", "United Kingdom"),
        "MAN" to Pair("Manchester", "United Kingdom"),
        "FRA" to Pair("Frankfurt", "Germany"),
        "MUC" to Pair("Munich", "Germany"),
        "FCO" to Pair("Rome", "Italy"),
        "MXP" to Pair("Milan", "Italy"),
        "MAD" to Pair("Madrid", "Spain"),
        "BCN" to Pair("Barcelona", "Spain"),
        "IST" to Pair("Istanbul", "Turkey"),
        "BRU" to Pair("Brussels", "Belgium"),
        "AMS" to Pair("Amsterdam", "Netherlands"),
        "GVA" to Pair("Geneva", "Switzerland"),
        "ZRH" to Pair("Zurich", "Switzerland"),

        // Middle East
        "DXB" to Pair("Dubai", "United Arab Emirates"),
        "AUH" to Pair("Abu Dhabi", "United Arab Emirates"),
        "DOH" to Pair("Doha", "Qatar"),
        "RUH" to Pair("Riyadh", "Saudi Arabia"),
        "JED" to Pair("Jeddah", "Saudi Arabia"),
        "BAH" to Pair("Manama", "Bahrain"),

        // Rest of the world
        "JFK" to Pair("New York", "United States"),
        "YYZ" to Pair("Toronto", "Canada"),
        "GRU" to Pair("São Paulo", "Brazil")
    )

    // List of preferred airlines (Tunisian and major partners)
    private val preferredAirlines = listOf(
        "Tunisair", "Nouvelair", "Tunisair Express",
        "Air France", "Lufthansa", "Turkish Airlines",
        "Emirates", "Qatar Airways", "Royal Air Maroc",
        "Air Algérie", "EgyptAir", "Ryanair", "easyJet"
    )

    suspend fun searchFlights(
        originCountry: String,
        budget: Double,
        days: Int,
        departDate: String
    ): List<FlightResult> = withContext(Dispatchers.IO) {
        val origin = countryToAirport[originCountry.lowercase()] ?: "TUN" // Default to Tunis

        // Validate and parse the departure date
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val validDepartDate = try {
            // Log the received departure date for debugging
            Log.d("AviasalesAPI", "Received departure date: $departDate")

            // Parse the date to validate it
            val parsedDate = LocalDate.parse(departDate, formatter)

            // Format it back to ensure consistent format
            parsedDate.format(formatter)
        } catch (e: DateTimeParseException) {
            // If parsing fails, log the error and use today's date as fallback
            Log.e("AviasalesAPI", "Error parsing departure date: $departDate", e)
            LocalDate.now().format(formatter)
        }

        // Calculate return date based on the validated departure date
        val returnDate = try {
            val parsedDepartDate = LocalDate.parse(validDepartDate, formatter)
            parsedDepartDate.plusDays(days.toLong()).format(formatter)
        } catch (e: Exception) {
            // Fallback if calculation fails
            Log.e("AviasalesAPI", "Error calculating return date", e)
            LocalDate.now().plusDays(days.toLong()).format(formatter)
        }

        Log.d("AviasalesAPI", "Using departure date: $validDepartDate, return date: $returnDate")

        try {
            val urlStr = "https://api.travelpayouts.com/aviasales/v3/prices_for_dates" +
                    "?origin=$origin&currency=tnd&depart_date=$validDepartDate&return_date=$returnDate&trip_class=0&token=bb77b690cfa4857ad0a39521cb7bcc19"

            Log.d("AviasalesAPI", "API URL: $urlStr")

            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            val response = conn.inputStream.bufferedReader().readText()
            val data = JSONObject(response)

            val results = mutableListOf<FlightResult>()
            if (data.has("data")) {
                val flights = data.getJSONArray("data")
                Log.d("AviasalesAPI", "Received ${flights.length()} flights from API")

                for (i in 0 until flights.length()) {
                    val flight = flights.getJSONObject(i)
                    val price = flight.optDouble("price", 0.0)

                    if (price > 0 && price <= budget) {
                        val destinationAirport = flight.optString("destination_airport", "Unknown")

                        // Get city and country from airport code
                        val (city, country) = airportInfo[destinationAirport] ?: Pair(
                            flight.optString("destination", "Unknown City"),
                            "Unknown Country"
                        )

                        // Get airline full name
                        val airlineCode = flight.optString("airline", "")
                        val airlineName = airlineCodeToName[airlineCode] ?: airlineCode

                        // Format destination as "City, Country"
                        val formattedDestination = "$city, $country"

                        // Get departure date from API response or use our validated date if missing
                        val flightDepartureAt = flight.optString("departure_at", null)
                        val actualDepartureAt = if (flightDepartureAt.isNullOrEmpty()) {
                            // If API doesn't return a departure date, use our validated date
                            "${validDepartDate}T12:00:00" // Add default time
                        } else {
                            // Use the date from API response
                            flightDepartureAt
                        }

                        // Get return date from API response or use our calculated return date
                        val flightReturnAt = flight.optString("return_at", null)
                        val actualReturnAt = if (flightReturnAt.isNullOrEmpty()) {
                            // If API doesn't return a return date, use our calculated date
                            "${returnDate}T12:00:00" // Add default time
                        } else {
                            // Use the date from API response
                            flightReturnAt
                        }

                        Log.d("AviasalesAPI", "Flight to $city: departure=$actualDepartureAt, return=$actualReturnAt")

                        // Prioritize preferred airlines
                        if (airlineName in preferredAirlines) {
                            results.add(
                                FlightResult(
                                    destination = city,
                                    destinationAirport = destinationAirport,
                                    destinationCountry = country,
                                    formattedDestination = formattedDestination,
                                    price = price,
                                    airline = airlineName,
                                    departureAt = actualDepartureAt,
                                    returnAt = actualReturnAt,
                                    link = "https://www.aviasales.com" + flight.optString("link", "")
                                )
                            )
                        }
                    }
                }
            } else {
                Log.d("AviasalesAPI", "No data field in API response")
            }

            // Sort by price and prioritize Tunisian airlines
            val sortedResults = results.sortedWith(compareBy(
                { if (it.airline.startsWith("Tunis")) 0 else 1 }, // Tunisian airlines first
                { it.price } // Then by price
            ))

            Log.d("AviasalesAPI", "Returning ${sortedResults.size} flight results")
            sortedResults
        } catch (e: Exception) {
            // If API fails, log the error and return empty list
            Log.e("AviasalesAPI", "Error searching flights", e)
            emptyList()
        }
    }

    data class FlightResult(
        val destination: String,          // City name
        val destinationAirport: String,   // IATA code
        val destinationCountry: String,   // Country name
        val formattedDestination: String, // "City, Country" format
        val price: Double,
        val airline: String,              // Full airline name
        val departureAt: String,
        val returnAt: String,
        val link: String
    )
}
