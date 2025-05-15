package com.example.travee.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SkyScannerApiService {

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

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val depart = LocalDate.parse(departDate, formatter)
        val returnDate = depart.plusDays(days.toLong()).format(formatter)

        try {
            val urlStr = "https://api.travelpayouts.com/aviasales/v3/prices_for_dates" +
                    "?origin=$origin&currency=tnd&depart_date=$departDate&return_date=$returnDate&trip_class=0&token=bb77b690cfa4857ad0a39521cb7bcc19"

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
                                    departureAt = flight.optString("departure_at", departDate),
                                    returnAt = returnDate,
                                    link = "https://www.aviasales.com" + flight.optString("link", "")
                                )
                            )
                        }
                    }
                }
            }

            // Sort by price and prioritize Tunisian airlines
            results.sortedWith(compareBy(
                { if (it.airline.startsWith("Tunis")) 0 else 1 }, // Tunisian airlines first
                { it.price } // Then by price
            ))
        } catch (e: Exception) {
            // If API fails, return empty list
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