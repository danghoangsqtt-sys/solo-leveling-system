package com.systemleveling.core.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class MessageRole { USER, ASSISTANT }

data class ChatMessage(
    val role: MessageRole,
    val content: String
)

// ── Google Gemini API request/response DTOs ───────────────────────────────────

@Serializable
data class GeminiRequest(
    @SerialName("system_instruction") val systemInstruction: GeminiSystemInstruction,
    val contents: List<GeminiContent>,
    @SerialName("generationConfig") val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
)

@Serializable
data class GeminiSystemInstruction(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiContent(
    val role: String,   // "user" | "model"
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiGenerationConfig(
    @SerialName("maxOutputTokens") val maxOutputTokens: Int = 1024,
    val temperature: Double = 0.9
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
    @SerialName("finishReason") val finishReason: String = ""
)
