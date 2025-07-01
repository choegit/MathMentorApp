package com.example.mathmentor2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mathmentor2.ui.theme.MathMentor2Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MathMentor2Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MathTutorScreen()
                }
            }
        }
    }
}

@Composable
fun MathTutorScreen() {
    var question by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("📘 Math Mentor for Kids", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            label = { Text("Ask a math question") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                loading = true
                response = ""
                CoroutineScope(Dispatchers.IO).launch {
                    val prompt = """
                        You are a kind and patient math tutor for 8-year-old children. When a child asks a math question, do not give the full answer right away. Instead, guide the child step-by-step by asking simple follow-up questions, giving relatable examples, and confirming understanding before moving forward. Use a warm tone. Make it fun and friendly.

                        Child's question: \"$question\"

                        Your response:
                    """.trimIndent()
                    val result = fetchGPTResponse(prompt)
                    response = result
                    loading = false
                }
            },
            enabled = !loading
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Ask Tutor")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (response.isNotEmpty()) {
            Text(text = response, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

suspend fun fetchGPTResponse(prompt: String): String {
    return try {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())

        val apiKey = BuildConfig.OPENAI_API_KEY

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey") // Replace with your real API key
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful || responseBody.isNullOrBlank()) {
            return "Error: ${response.code} - ${response.message}"
        }

        val jsonResponse = JSONObject(responseBody)
        if (!jsonResponse.has("choices")) {
            return jsonResponse.optString("error", "No 'choices' found in response.")
        }

        val content = jsonResponse
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        content.trim()
    } catch (e: Exception) {
        "Error: ${e.localizedMessage}"
    }
}
