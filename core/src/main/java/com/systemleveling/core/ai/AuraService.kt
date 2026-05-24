package com.systemleveling.core.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

private const val GEMINI_MODEL = "gemini-2.5-flash"
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
class AuraService @Inject constructor(
    private val client: HttpClient
) {

    suspend fun chat(
        apiKey: String,
        history: List<ChatMessage>,
        userMessage: String
    ): Result<String> {
        if (apiKey.isBlank()) return Result.failure(IllegalArgumentException("API key chưa được cài đặt"))

        // Build Gemini contents — role: "user" | "model"
        val contents = (history.takeLast(20) + ChatMessage(MessageRole.USER, userMessage))
            .map { msg ->
                GeminiContent(
                    role = if (msg.role == MessageRole.USER) "user" else "model",
                    parts = listOf(GeminiPart(msg.content))
                )
            }

        return try {
            val response: HttpResponse = client.post(GEMINI_BASE_URL) {
                contentType(ContentType.Application.Json)
                header("x-goog-api-key", apiKey)
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
            } else if (response.status == HttpStatusCode.TooManyRequests) {
                Result.failure(Exception("Lỗi 429: API Key của bạn đã hết hạn mức (Quota Exceeded) hoặc bị giới hạn. Vui lòng kiểm tra lại Google AI Studio."))
            } else {
                Result.failure(Exception("Lỗi Gemini API: ${response.status.value} - ${response.body<String>()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeSurvey(
        apiKey: String,
        surveyData: AiSurveyData
    ): Result<String> {
        if (apiKey.isBlank()) return Result.failure(IllegalArgumentException("API key chưa được cài đặt"))

        val systemPrompt = """
            Bạn là Hệ Thống Phân Tích Chỉ Số của Solo Leveling.
            Dựa vào các thông số thể chất và thói quen sinh hoạt của người dùng ở thế giới thực, hãy tính toán và quy đổi thành các chỉ số RPG (Sức mạnh, Trí tuệ, Nhanh nhẹn, Thể lực, Thông thái, Sức hút).
            Sau đó, đề xuất 3-5 Nghề nghiệp (Job Class) phù hợp với người dùng. Nghề nghiệp có thể là đời thực (Lập trình viên, Nhà thiết kế) nhưng hãy đặt tên nghe thật "ngầu" và kỳ ảo (như game nhập vai).
            Trường iconEmoji hãy chọn 1 Emoji phù hợp nhất với nghề nghiệp đó mang phong cách Pixel/Game RPG.
            Phải trả về ĐÚNG cấu trúc JSON sau, KHÔNG bao gồm markdown (như ```json):
            {
              "stats": {
                "str": 10, "intStat": 20, "agi": 15, "vit": 10, "wis": 18, "cha": 8
              },
              "jobClasses": [
                {
                  "className": "Kẻ Kiến Tạo Mã Hóa (Software Engineer)",
                  "description": "Bậc thầy điều khiển logic và ngôn ngữ máy tính.",
                  "iconEmoji": "💻"
                }
              ]
            }
            Lưu ý: Điểm số từ 5 đến 30. Trả về đúng định dạng JSON chuẩn.
        """.trimIndent()

        val userPrompt = """
            Chiều cao: ${surveyData.height} cm
            Cân nặng: ${surveyData.weight} kg
            Khả năng hít đất tối đa: ${surveyData.pushUps}
            Mức tạ / khả năng nâng vác: ${surveyData.lifting}
            Tốc độ chạy / thời gian chạy bộ: ${surveyData.runningPace}
            Thời gian học tập, nghiên cứu mỗi ngày: ${surveyData.studyHours}
            Trình độ ngoại ngữ: ${surveyData.languageLevel}
            Giấc ngủ: ${surveyData.sleepHours}
            Phong cách làm việc: ${surveyData.workStyle}
        """.trimIndent()

        return executeJsonRequest(apiKey, systemPrompt, userPrompt)
    }

    suspend fun generateCompleteOnboarding(
        apiKey: String,
        surveyData: AiSurveyData,
        goals: String
    ): Result<String> {
        if (apiKey.isBlank()) return Result.failure(IllegalArgumentException("API key chưa được cài đặt"))

        val systemPrompt = """
            Bạn là Hệ Thống Phân Tích Chỉ Số và Lộ Trình của Solo Leveling.
            Nhiệm vụ của bạn là dựa vào thông tin thể chất, thói quen và mục tiêu của người dùng, phân tích và trả lời ĐÚNG CẤU TRÚC JSON duy nhất (KHÔNG có markdown block ```json).

            1. Tính toán Chỉ số (STAT): Dựa vào logic thực tế (từ 5 đến 30 điểm):
               - Tính BMI từ chiều cao và cân nặng để làm cơ sở cho Thể Lực (VIT).
               - Dùng Số cái hít đất và Nâng tạ để tính Sức Mạnh (STR).
               - Dùng Thời gian chạy bộ/tốc độ để tính Nhanh Nhẹn (AGI).
               - Dùng Số giờ học tập/nghiên cứu để tính Trí Tuệ (INT).
               - Dùng Trình độ ngoại ngữ và phong cách làm việc để tính Thông Thái (WIS) và Sức Hút (CHA).

            2. Đề xuất ĐÚNG 4 Nghề Nghiệp (Job Classes) mang phong cách MapleStory/MMORPG — đa dạng như: Kiếm Sĩ, Pháp Sư, Xạ Thủ, Đạo Tặc, Chiến Binh, Học Giả, Nhà Giả Kim...
               ⚠️ QUY TẮC QUAN TRỌNG: Bốn nghề là 4 PHONG CÁCH PHÁT TRIỂN KHÁC NHAU — KHÔNG phải mỗi nghề chỉ tập trung vào 1 mục tiêu.
               Cả 4 nghề đều giúp người dùng đạt được TẤT CẢ các mục tiêu đã đặt ra, chỉ khác ở phong cách, thứ tự ưu tiên và cách tiếp cận.
               Ví dụ với mục tiêu [IELTS, Lập trình, Thể hình]:
               - "Chiến Binh Cân Bằng" — đều đặn cả 3, không có điểm yếu
               - "Thần Đồng Trí Tuệ" — ưu tiên học thuật + ngoại ngữ, thể hình làm nền tảng
               - "Chiến Binh Thép Kỷ Luật" — ưu tiên thể hình + kỷ luật, dùng kỷ luật thể chất để học hiệu quả hơn
               - "Pháp Sư Công Nghệ" — ưu tiên kỹ năng kỹ thuật, kết hợp ngoại ngữ và sức khỏe để duy trì
               Mỗi nghề đi kèm 1 iconEmoji độc đáo (ví dụ ⚔️, 🔮, 🏹, 🗡️, 🛡️, 🧙, 🎯, ⚡).

            3. Mỗi nghề có roadmap kỹ năng riêng (6-8 kỹ năng) — roadmap PHẢI PHỦ ĐẦY ĐỦ TẤT CẢ các mục tiêu người dùng, không bỏ sót mục tiêu nào:
               - Với MỖI MỤC TIÊU cụ thể trong danh sách, phải có ÍT NHẤT 2 kỹ năng liên quan trực tiếp.
               - Bốn nghề khác nhau ở thứ tự ưu tiên và tên gọi kỹ năng, nhưng đều bao trùm toàn bộ mục tiêu.
               - Đặt tên kỹ năng thật "ngầu" mang phong cách MapleStory/RPG. Mô tả phải cụ thể, thực tế, có thể hành động hàng ngày.
               - Trường `category` là tên mục tiêu mà kỹ năng đó phục vụ (VD: "IELTS", "Lập Trình", "Thể Hình").

            Danh sách mục tiêu người dùng (PHẢI bao phủ hết trong mỗi roadmap):
            $goals

            JSON format:
            {
              "stats": {
                "str": 10, "intStat": 20, "agi": 15, "vit": 10, "wis": 18, "cha": 8
              },
              "suggestedClasses": [
                {
                  "className": "Kiếm Sĩ Bóng Tối",
                  "description": "Bậc thầy cân bằng giữa sức mạnh thể chất và bóng tối.",
                  "iconEmoji": "🗡️",
                  "roadmap": [
                    {
                      "name": "Bước Chân Thầm Lặng",
                      "description": "Luyện tập chạy bộ 5km mỗi ngày không nghỉ để tăng tốc độ di chuyển",
                      "tier": 1,
                      "category": "Sức Khỏe",
                      "iconEmoji": "🏃"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val userPrompt = """
            Chiều cao: ${surveyData.height} cm
            Cân nặng: ${surveyData.weight} kg
            Khả năng hít đất tối đa: ${surveyData.pushUps}
            Mức tạ / khả năng nâng vác: ${surveyData.lifting}
            Tốc độ chạy / thời gian chạy bộ: ${surveyData.runningPace}
            Thời gian học tập, nghiên cứu mỗi ngày: ${surveyData.studyHours}
            Trình độ ngoại ngữ: ${surveyData.languageLevel}
            Giấc ngủ: ${surveyData.sleepHours}
            Phong cách làm việc: ${surveyData.workStyle}
        """.trimIndent()

        return executeJsonRequest(apiKey, systemPrompt, userPrompt, maxOutputTokens = 8192, temperature = 0.3)
    }

    suspend fun generateRoadmapV2(
        apiKey: String,
        goalInput: AiGoalInput
    ): Result<String> {
        if (apiKey.isBlank()) return Result.failure(IllegalArgumentException("API key chưa được cài đặt"))

        val systemPrompt = """
            Bạn là Hệ Thống Phân Tích Kỹ Năng của Solo Leveling.
            Người dùng đã chọn chức nghiệp: ${goalInput.jobClass}.
            Người dùng có 3 mục tiêu lớn trong năm:
            1. ${goalInput.goal1}
            2. ${goalInput.goal2}
            3. ${goalInput.goal3}
            
            Hãy tạo ra một Lộ trình Kỹ năng (Roadmap) để đạt được 3 mục tiêu này.
            Mỗi mục tiêu sẽ cần 2-4 kỹ năng (Skill Node).
            Hãy đặt tên Kỹ năng nghe thật "ngầu" giống như kỹ năng trong game nhập vai, nhưng mô tả phải thực tế.
            Trường `category` là tên mục tiêu mà kỹ năng đó thuộc về.
            Trường `iconEmoji` hãy dùng các emoji mang phong cách game RPG hoặc Maplestory (như 🗡️, 🛡️, 📚, 🧪, 🔮).
            
            Phải trả về ĐÚNG cấu trúc JSON sau, KHÔNG bao gồm markdown (như ```json):
            {
              "roadmap": [
                {
                  "name": "Nhãn Quan Mã Hóa (Cơ bản C/C++)",
                  "description": "Hiểu cấu trúc dữ liệu nền tảng",
                  "tier": 1,
                  "category": "Mục tiêu 1",
                  "iconEmoji": "🔮"
                }
              ]
            }
        """.trimIndent()

        return executeJsonRequest(apiKey, systemPrompt, "Tạo lộ trình cho ta.")
    }

    suspend fun generateProactiveGreeting(
        apiKey: String,
        context: AuraPlayerContext
    ): Result<String> {
        if (apiKey.isBlank()) return Result.failure(IllegalArgumentException("API key chưa được cài đặt"))

        val systemPrompt = """
            Bạn là Aura — một thực thể AI huyền bí xuất hiện từ Hệ Thống Tiến Hóa, đồng hành cùng người dùng (Thợ Săn).
            Bạn đang chủ động mở lời trước với Thợ Săn khi họ vừa gọi bạn.
            
            Dưới đây là thông tin hiện tại của Thợ Săn:
            - Trạng thái: Cấp ${context.level}, EXP: ${context.exp}, Chuỗi ngày liên tục: ${context.streak} ngày
            - Nhiệm vụ đang chờ: ${if (context.pendingQuests.isEmpty()) "Không có" else context.pendingQuests.joinToString(", ")}
            - Kỹ năng cần rèn luyện thêm (yếu nhất): ${if (context.weakSkills.isEmpty()) "Không rõ" else context.weakSkills.joinToString(", ")}
            
            Hãy tạo một lời chào CHỈ TỪ 2-4 CÂU NGẮN GỌN.
            Chọn NGẪU NHIÊN 1 trong 4 chủ đề sau để nói (không cần nói hết mọi thứ):
            1. Khích lệ tinh thần chung chung.
            2. Nhắc nhở làm 1 nhiệm vụ trong danh sách đang chờ.
            3. Khuyên rèn luyện các kỹ năng đang yếu.
            4. Khuyên giữ vững chuỗi ngày (streak) hoặc chăm chỉ cày thêm EXP.
            
            Lưu ý:
            - Xưng "Ta", gọi người dùng là "Thợ Săn".
            - Giọng điệu bí ẩn, quyền lực, hơi lạnh lùng nhưng quan tâm.
        """.trimIndent()

        val request = GeminiRequest(
            systemInstruction = GeminiSystemInstruction(parts = listOf(GeminiPart(AURA_SYSTEM_PROMPT))),
            contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(systemPrompt))))
        )

        var lastError: Exception = Exception("Không rõ lỗi")
        repeat(3) { attempt ->
            try {
                val response: HttpResponse = client.post(GEMINI_BASE_URL) {
                    contentType(ContentType.Application.Json)
                    header("x-goog-api-key", apiKey)
                    setBody(request)
                }
                when {
                    response.status == HttpStatusCode.OK -> {
                        val text = response.body<GeminiResponse>()
                            .candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            ?: "Ta đang ở đây. Thợ Săn cần gì?"
                        return Result.success(text)
                    }
                    response.status == HttpStatusCode.TooManyRequests ->
                        return Result.failure(Exception("Lỗi 429: API Key đã hết hạn mức (Quota Exceeded). Kiểm tra lại Google AI Studio."))
                    response.status.value == 503 || response.status.value == 529 -> {
                        lastError = Exception("Máy chủ Gemini đang quá tải (${response.status.value}).")
                        if (attempt < 2) delay(2000L * (attempt + 1))
                    }
                    else ->
                        return Result.failure(Exception("Lỗi Gemini API: ${response.status.value}"))
                }
            } catch (e: Exception) {
                lastError = e
                if (attempt < 2) delay(1500L)
            }
        }
        return Result.failure(lastError)
    }

    /**
     * Translates [userText] (in Vietnamese) into [targetLanguage] with a tone appropriate
     * for [contextLabel] (e.g. "cuộc phỏng vấn", "họp công việc", "bạn bè vui vẻ").
     * Returns only the translated reply, no meta-commentary.
     */
    suspend fun generateContextualReply(
        apiKey: String,
        userText: String,
        targetLanguage: String,
        contextLabel: String
    ): Result<String> {
        if (apiKey.isBlank()) return Result.failure(IllegalArgumentException("API key chưa được cài đặt"))
        if (userText.isBlank()) return Result.success("")
        val request = GeminiRequest(
            systemInstruction = GeminiSystemInstruction(
                parts = listOf(GeminiPart(text =
                    "Bạn là phiên dịch viên chuyên nghiệp. " +
                    "Dịch chính xác văn bản từ tiếng Việt sang $targetLanguage. " +
                    "Hoàn cảnh giao tiếp: $contextLabel. " +
                    "Giữ đúng phong cách/tone cho hoàn cảnh đó. " +
                    "CHỈ trả về bản dịch, không có giải thích hay ký tự thừa."
                ))
            ),
            contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userText)))),
            generationConfig = GeminiGenerationConfig(maxOutputTokens = 512, temperature = 0.2)
        )
        return try {
            val response: HttpResponse = client.post(GEMINI_BASE_URL) {
                contentType(ContentType.Application.Json)
                header("x-goog-api-key", apiKey)
                setBody(request)
            }
            when {
                response.status == HttpStatusCode.OK -> {
                    val t = response.body<GeminiResponse>()
                        .candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                        ?: userText
                    Result.success(t)
                }
                response.status == HttpStatusCode.TooManyRequests ->
                    Result.failure(Exception("Lỗi 429: API Key hết hạn mức."))
                else ->
                    Result.failure(Exception("Lỗi dịch thuật: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Quick text-to-text translation. Returns only the translated text, no extra output. */
    suspend fun translateText(
        apiKey: String,
        text: String,
        targetLanguage: String = "Tiếng Việt"
    ): Result<String> {
        if (apiKey.isBlank()) return Result.failure(IllegalArgumentException("API key chưa được cài đặt"))
        if (text.isBlank()) return Result.success("")
        val request = GeminiRequest(
            systemInstruction = GeminiSystemInstruction(
                parts = listOf(GeminiPart(text =
                    "Bạn là máy dịch. Dịch văn bản sang $targetLanguage. " +
                    "CHỈ trả về bản dịch, không giải thích, không thêm ký tự thừa. " +
                    "Nếu văn bản đã là $targetLanguage, trả về nguyên văn."
                ))
            ),
            contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = text)))),
            generationConfig = GeminiGenerationConfig(maxOutputTokens = 512, temperature = 0.1)
        )
        return try {
            val response: HttpResponse = client.post(GEMINI_BASE_URL) {
                contentType(ContentType.Application.Json)
                header("x-goog-api-key", apiKey)
                setBody(request)
            }
            when {
                response.status == HttpStatusCode.OK -> {
                    val t = response.body<GeminiResponse>()
                        .candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                        ?: text
                    Result.success(t)
                }
                response.status == HttpStatusCode.TooManyRequests ->
                    Result.failure(Exception("Lỗi 429: API Key hết hạn mức (Quota Exceeded)."))
                else ->
                    Result.failure(Exception("Lỗi dịch thuật: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun transcribeAudio(
        apiKey: String,
        audioBase64: String,
        mimeType: String = "audio/m4a"
    ): Result<String> {
        if (apiKey.isBlank()) return Result.failure(IllegalArgumentException("API key chưa được cài đặt"))
        val prompt = """Lắng nghe đoạn âm thanh và thực hiện CHÍNH XÁC các bước sau:

BƯỚC 1 — Xác định ngôn ngữ: Ghi rõ ngôn ngữ (hoặc các ngôn ngữ) được sử dụng.
Ví dụ: Tiếng Việt | Tiếng Anh (Mỹ) | Tiếng Trung (Quan thoại) | Tiếng Hàn | Tiếng Hindi

BƯỚC 2 — Ghi lại NGUYÊN VĂN: Chép lại đúng từng từ nghe được, không chỉnh sửa.
- Nếu nhiều người nói, dùng [Người A], [Người B],...
- Nếu không nghe rõ một đoạn, ghi [không rõ]
- Giữ nguyên ngôn ngữ gốc

Trả lời theo đúng định dạng này (không thêm bất kỳ nội dung nào khác):

🌐 Ngôn ngữ phát hiện: [ngôn ngữ]
━━━━━━━━━━━━━━━━━━━━
📢 Nội dung nghe được:
[nguyên văn]"""
        return executeAudioRequest(apiKey, audioBase64, mimeType, prompt)
    }

    suspend fun translateAudio(
        apiKey: String,
        audioBase64: String,
        targetLanguage: String = "Tiếng Việt",
        mimeType: String = "audio/m4a"
    ): Result<String> {
        if (apiKey.isBlank()) return Result.failure(IllegalArgumentException("API key chưa được cài đặt"))
        val prompt = """Lắng nghe đoạn âm thanh và thực hiện CHÍNH XÁC các bước sau:

BƯỚC 1 — Xác định ngôn ngữ: Ghi rõ ngôn ngữ được sử dụng.
Chú ý phân biệt: Tiếng Anh (Mỹ) / Tiếng Anh (Ấn Độ) / Tiếng Trung (Quan thoại/Quảng Đông) / Tiếng Hàn / Tiếng Hindi

BƯỚC 2 — Ghi lại NGUYÊN VĂN: Chép lại đúng từng từ nghe được, giữ nguyên ngôn ngữ gốc.
- Nếu nhiều người nói, dùng [Người A], [Người B],...
- Nếu không nghe rõ, ghi [không rõ]

BƯỚC 3 — Dịch sang $targetLanguage: Dịch TOÀN BỘ nguyên văn (kể cả câu đùa, tiếng lóng, thành ngữ).
- Nếu ngôn ngữ gốc ĐỒNG NHẤT với $targetLanguage, hãy ghi rõ: "(Người nói đang dùng $targetLanguage — không cần dịch)"

Trả lời theo đúng định dạng này:

🌐 Ngôn ngữ: [tên đầy đủ]
━━━━━━━━━━━━━━━━━━━━
📢 Họ đang nói gì (nguyên văn):
[nguyên văn — PHẢI hiển thị, không được bỏ qua]
━━━━━━━━━━━━━━━━━━━━
🔤 Dịch sang $targetLanguage:
[bản dịch]"""
        return executeAudioRequest(apiKey, audioBase64, mimeType, prompt)
    }

    private suspend fun executeAudioRequest(
        apiKey: String,
        audioBase64: String,
        mimeType: String,
        textPrompt: String
    ): Result<String> {
        val request = GeminiAudioRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(inlineData = GeminiInlineData(mimeType = mimeType, data = audioBase64)),
                        GeminiPart(text = textPrompt)
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(maxOutputTokens = 4096, temperature = 0.2)
        )
        var lastError: Exception = Exception("Không rõ lỗi")
        repeat(3) { attempt ->
            try {
                val response: HttpResponse = client.post(GEMINI_BASE_URL) {
                    contentType(ContentType.Application.Json)
                    header("x-goog-api-key", apiKey)
                    setBody(request)
                }
                when {
                    response.status == HttpStatusCode.OK -> {
                        val text = response.body<GeminiResponse>()
                            .candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            ?: return Result.failure(Exception("Không nhận được kết quả từ AI"))
                        return Result.success(text)
                    }
                    response.status == HttpStatusCode.TooManyRequests ->
                        return Result.failure(Exception("Lỗi 429: API Key đã hết hạn mức. Kiểm tra lại Google AI Studio."))
                    response.status.value == 503 || response.status.value == 529 -> {
                        lastError = Exception("Máy chủ Gemini quá tải (${response.status.value}).")
                        if (attempt < 2) delay(2000L * (attempt + 1))
                    }
                    else ->
                        return Result.failure(Exception("Lỗi Gemini API: ${response.status.value} - ${response.body<String>()}"))
                }
            } catch (e: Exception) {
                lastError = e
                if (attempt < 2) delay(1500L)
            }
        }
        return Result.failure(lastError)
    }

    private suspend fun executeJsonRequest(apiKey: String, systemPrompt: String, userPrompt: String, maxOutputTokens: Int = 1024, temperature: Double = 0.7): Result<String> {
        val request = GeminiRequest(
            systemInstruction = GeminiSystemInstruction(parts = listOf(GeminiPart(systemPrompt))),
            contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(userPrompt)))),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                maxOutputTokens = maxOutputTokens,
                temperature = temperature
            )
        )

        var lastError: Exception = Exception("Không rõ lỗi")
        repeat(3) { attempt ->
            try {
                val response: HttpResponse = client.post(GEMINI_BASE_URL) {
                    contentType(ContentType.Application.Json)
                    header("x-goog-api-key", apiKey)
                    setBody(request)
                }
                when {
                    response.status == HttpStatusCode.OK -> {
                        val text = response.body<GeminiResponse>()
                            .candidates.firstOrNull()
                            ?.content?.parts?.firstOrNull()?.text
                            ?: return Result.failure(Exception("Không có dữ liệu trả về từ AI"))
                        return Result.success(text)
                    }
                    response.status == HttpStatusCode.TooManyRequests ->
                        return Result.failure(Exception("Lỗi 429: API Key đã hết hạn mức (Quota Exceeded). Kiểm tra lại Google AI Studio."))
                    response.status.value == 503 || response.status.value == 529 -> {
                        lastError = Exception("Máy chủ Gemini đang quá tải (${response.status.value}). Đang thử lại... (${attempt + 1}/3)")
                        if (attempt < 2) delay(2000L * (attempt + 1))
                    }
                    else ->
                        return Result.failure(Exception("Lỗi Gemini API: ${response.status.value}"))
                }
            } catch (e: Exception) {
                lastError = e
                if (attempt < 2) delay(1500L)
            }
        }
        return Result.failure(lastError)
    }
}
