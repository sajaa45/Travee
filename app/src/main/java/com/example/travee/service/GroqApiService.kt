package com.example.travee.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GroqApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json".toMediaType()

    /**
     * Get activity recommendations for a destination
     * @param destination The destination country or city
     * @param budget The budget in TND
     * @param days The number of days for the trip
     * @return A formatted string with 3 bullet point recommendations
     */
    suspend fun getActivityRecommendations(
        destination: String,
        budget: Double,
        days: Int
    ): String = withContext(Dispatchers.IO) {
        // Create a prompt that specifies exactly what we want
        val cleanDestination = destination.replace("\"", "").replace("\\", "")
        val promptText = "Suggest 3 budget-friendly activities in $cleanDestination for a $days-day trip with a budget of $budget TND. Format your response as 3 bullet points only, with no introduction or conclusion."

        try {
            // Create the messages array with the user prompt
            val messagesArray = JSONArray()
            val userMessage = JSONObject()
            userMessage.put("role", "user")
            userMessage.put("content", promptText)
            messagesArray.put(userMessage)

            // Create the complete request body
            val jsonBody = JSONObject()
            jsonBody.put("model", "llama3-8b-8192")
            jsonBody.put("messages", messagesArray)

            // Convert to string
            val jsonBodyString = jsonBody.toString()
            Log.d("GroqAPI", "Request body: $jsonBodyString")

            val request = Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Authorization", "Bearer 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")
                .addHeader("Content-Type", "application/json")
                .post(jsonBodyString.toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                Log.d("GroqAPI", "Successful response: $responseBody")
                val json = JSONObject(responseBody!!)
                val content = json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                content
            } else {
                Log.e("GroqAPI", "API error: ${response.code} - $responseBody")
                "Could not load activities. Please try again later.\n\n" +
                        "• Visit local landmarks and historical sites\n" +
                        "• Try local cuisine at affordable restaurants\n" +
                        "• Explore parks and natural attractions"
            }
        } catch (e: Exception) {
            Log.e("GroqAPI", "Exception: ${e.message}", e)
            "Network error. Please check your connection.\n\n" +
                    "• Visit local landmarks and historical sites\n" +
                    "• Try local cuisine at affordable restaurants\n" +
                    "• Explore parks and natural attractions"
        }
    }
}
