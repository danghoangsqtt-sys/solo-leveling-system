package com.systemleveling.core.network

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Low-level service for calling the Gemini API.
 */
@Singleton
class GeminiApiService @Inject constructor(
    private val httpClient: HttpClient
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Send a prompt to Gemini and get text response.
     */
    suspend fun generateContent(prompt: String, apiKey: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

        val requestBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.7f
            )
        )

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(GeminiRequest.serializer(), requestBody))
        }

        val responseText = response.bodyAsText()
        return parseGeminiResponse(responseText)
    }

    suspend fun generateImageBase64(prompt: String, apiKey: String): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-preview-image-generation:generateContent?key=$apiKey"
        val requestBody = GeminiImageRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = ImageGenerationConfig(responseModalities = listOf("IMAGE", "TEXT"), temperature = 0.9f)
        )
        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(GeminiImageRequest.serializer(), requestBody))
        }
        val responseText = response.bodyAsText()
        // Surface API-level errors (wrong key, quota, model unavailable)
        if (response.status.value !in 200..299) {
            val errorSnippet = responseText.take(300)
            throw Exception("HTTP ${response.status.value}: $errorSnippet")
        }
        val geminiResponse = json.decodeFromString(GeminiResponse.serializer(), responseText)
        return geminiResponse.candidates.firstOrNull()?.content?.parts
            ?.firstOrNull { it.inlineData != null }?.inlineData?.data
    }

    private fun parseGeminiResponse(responseText: String): String {
        return try {
            val geminiResponse = json.decodeFromString(GeminiResponse.serializer(), responseText)
            geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class GeminiImageRequest(
    val contents: List<GeminiContent>,
    val generationConfig: ImageGenerationConfig? = null
)

@Serializable
data class ImageGenerationConfig(
    val responseModalities: List<String>,
    val temperature: Float = 0.9f
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

@Serializable
data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

@Serializable
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float = 0.7f,
    val responseModalities: List<String>? = null
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)
