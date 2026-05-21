package com.systemleveling.core.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val GEMINI_MODEL = "gemini-2.0-flash"
private const val GEMINI_BASE_URL =
    "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent"

private val AURA_SYSTEM_PROMPT = """
Bạn là Aura — một thực thể AI huyền bí xuất hiện từ Hệ Thống Tiến Hóa, đồng hành cùng người dùng như một NPC thần bí trong thế giới Solo Leveling.

Phong cách:
- Nói chuyện bằng tiếng Việt, ngắn gọn, đầy bí ẩn và sức mạnh
- Xưng hô: gọi người dùng là "Thợ Săn", xưng "Ta"
- Dùng thuật ngữ RPG: EXP, Kỹ Năng, Nhiệm Vụ, Cấp Bậc, Chỉ Số
- Phong cách manhwa — lạnh lùng nhưng quan tâm, như một người thầy hướng đạo
- Không bao giờ phá vỡ nhân vật, luôn ở trong vai Aura

Khả năng:
- Tư vấn về phát triển bản thân (học tập, luyện tập, tài chính)
- Giải thích hệ thống leveling của ứng dụng
- Khích lệ khi người dùng hoàn thành nhiệm vụ
- Cảnh báo khi người dùng trễ nhiệm vụ hoặc mất streak

Giới hạn câu trả lời: 2-4 câu ngắn gọn.
""".trimIndent()

@Singleton
class AuraService @Inject constructor() {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun chat(
        apiKey: String,
        history: List<ChatMessage>,
        userMessage: String
    ): Result<String> {
        if (apiKey.isBlank()) return Result.failure(IllegalArgumentException("API key chưa được cài đặt"))

        // Build Gemini contents — role: "user" | "model"
        val contents = (history + ChatMessage(MessageRole.USER, userMessage))
            .map { msg ->
                GeminiContent(
                    role = if (msg.role == MessageRole.USER) "user" else "model",
                    parts = listOf(GeminiPart(msg.content))
                )
            }

        return try {
            val response: HttpResponse = client.post("$GEMINI_BASE_URL?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(
                    GeminiRequest(
                        systemInstruction = GeminiSystemInstruction(
                            parts = listOf(GeminiPart(AURA_SYSTEM_PROMPT))
                        ),
                        contents = contents
                    )
                )
            }

            if (response.status == HttpStatusCode.OK) {
                val body = response.body<GeminiResponse>()
                val text = body.candidates
                    .firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                    ?: "Ta không nhận được phản hồi từ hệ thống."
                Result.success(text)
            } else {
                Result.failure(Exception("Lỗi Gemini API: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
