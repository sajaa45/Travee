package com.example.travee.service


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var resultText by remember { mutableStateOf("Fetching recommendations...") }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                resultText = callGroqAPI()
                isLoading = false
            }

            Surface(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Travel Recommendations:", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text(resultText)
                    }
                }
            }
        }
    }

    private suspend fun callGroqAPI(): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val jsonMediaType = "application/json".toMediaType()

        // Prepare Groq-compatible JSON payload
        val jsonBody = """
        {
            "model": "llama3-8b-8192",
            "messages": [
                {"role": "user", "content": "Suggest 3 budget activities in Rome"}
            ]
        }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer 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") // Replace with your Groq key
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                val json = JSONObject(body!!)
                val content = json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                content
            } else {
                "API error: ${response.code} - ${response.message}"
            }
        } catch (e: Exception) {
            "Network error: ${e.message}"
        }
    }
}
