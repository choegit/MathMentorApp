package com.example.mathmentor2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mathmentor2.ui.theme.MathMentor2Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

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

class MathTutorViewModel : ViewModel() {
    var question by mutableStateOf("")
        private set
    var response by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun onQuestionChange(value: String) {
        question = value
    }

    fun askTutor() {
        if (loading) return
        loading = true
        response = ""
        viewModelScope.launch {
            response = fetchGPTResponse(question)
            loading = false
        }
    }

    private suspend fun fetchGPTResponse(userQuestion: String): String = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("model", "gpt-3.5-turbo")
                put("messages", JSONArray()
                    .put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
                    .put(JSONObject().put("role", "user").put("content", userQuestion))
                )
            }
            val body = json.toString().toRequestBody("application/json".toMediaType())

            val apiKey = BuildConfig.OPENAI_API_KEY

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .post(body)
                .build()

            client.newCall(request).execute().use { httpResponse ->
                val responseBody = httpResponse.body?.string()

                if (!httpResponse.isSuccessful) {
                    // Surface OpenAI's actual error message when present.
                    val apiMessage = responseBody
                        ?.let { runCatching { JSONObject(it).getJSONObject("error").getString("message") }.getOrNull() }
                    return@withContext "Error: ${apiMessage ?: "${httpResponse.code} ${httpResponse.message}"}"
                }

                if (responseBody.isNullOrBlank()) {
                    return@withContext "Error: empty response from server."
                }

                val jsonResponse = JSONObject(responseBody)
                if (!jsonResponse.has("choices")) {
                    return@withContext "Error: unexpected response (no 'choices')."
                }

                jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            }
        } catch (e: IOException) {
            // Network-level failures: no connectivity, DNS, timeouts.
            "Error: couldn't reach the tutor. Please check your internet connection and try again."
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }

    private companion object {
        const val SYSTEM_PROMPT =
            "You are a kind and patient math tutor for 8-year-old children. When a child asks a " +
            "math question, do not give the full answer right away. Instead, guide the child " +
            "step-by-step by asking simple follow-up questions, giving relatable examples, and " +
            "confirming understanding before moving forward. Use a warm tone. Make it fun and friendly."
    }
}

@Composable
fun MathTutorScreen(viewModel: MathTutorViewModel = viewModel()) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("📘 Math Mentor for Kids", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.question,
            onValueChange = viewModel::onQuestionChange,
            label = { Text("Ask a math question") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = viewModel::askTutor,
            enabled = !viewModel.loading
        ) {
            if (viewModel.loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Ask Tutor")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (viewModel.response.isNotEmpty()) {
            Text(text = viewModel.response, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
