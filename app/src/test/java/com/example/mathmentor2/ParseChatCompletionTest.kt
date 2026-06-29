package com.example.mathmentor2

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [parseChatCompletion] — the pure mapping from an OpenAI Chat Completions
 * HTTP response to either the assistant's text or a user-facing error string.
 */
class ParseChatCompletionTest {

    private fun success(body: String?) =
        parseChatCompletion(isSuccessful = true, code = 200, httpMessage = "OK", body = body)

    private fun failure(code: Int, httpMessage: String, body: String?) =
        parseChatCompletion(isSuccessful = false, code = code, httpMessage = httpMessage, body = body)

    @Test
    fun `extracts assistant content on success`() {
        val body = """
            {"choices":[{"message":{"role":"assistant","content":"Great question! What is 2 + 2?"}}]}
        """.trimIndent()
        assertEquals("Great question! What is 2 + 2?", success(body))
    }

    @Test
    fun `trims surrounding whitespace in content`() {
        val body = """{"choices":[{"message":{"content":"  hello  "}}]}"""
        assertEquals("hello", success(body))
    }

    @Test
    fun `surfaces OpenAI error message on non-2xx`() {
        val body = """{"error":{"message":"You exceeded your current quota","type":"insufficient_quota"}}"""
        assertEquals("Error: You exceeded your current quota", failure(429, "Too Many Requests", body))
    }

    @Test
    fun `falls back to HTTP status when error body is not parseable`() {
        assertEquals("Error: 401 Unauthorized", failure(401, "Unauthorized", "not json"))
    }

    @Test
    fun `falls back to HTTP status when error body lacks error object`() {
        assertEquals("Error: 500 Internal Server Error", failure(500, "Internal Server Error", "{}"))
    }

    @Test
    fun `null error body falls back to HTTP status`() {
        assertEquals("Error: 503 Service Unavailable", failure(503, "Service Unavailable", null))
    }

    @Test
    fun `empty success body reports empty response`() {
        assertEquals("Error: empty response from server.", success(""))
        assertEquals("Error: empty response from server.", success("   "))
        assertEquals("Error: empty response from server.", success(null))
    }

    @Test
    fun `malformed success body reports parse error`() {
        assertEquals("Error: unexpected response (couldn't parse).", success("{not valid json"))
    }

    @Test
    fun `success body without choices reports missing choices`() {
        assertEquals("Error: unexpected response (no 'choices').", success("""{"id":"abc"}"""))
    }

    @Test
    fun `success body with empty choices array reports missing choices`() {
        assertEquals("Error: unexpected response (no 'choices').", success("""{"choices":[]}"""))
    }
}
