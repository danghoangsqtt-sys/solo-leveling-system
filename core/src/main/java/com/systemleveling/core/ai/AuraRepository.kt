package com.systemleveling.core.ai

import android.util.Base64
import android.util.Log
import com.systemleveling.core.settings.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuraRepository @Inject constructor(
    private val settingsManager: SettingsManager,
    private val auraService: AuraService
) {
    val apiKeyFlow: Flow<String> = settingsManager.geminiApiKey

    suspend fun saveApiKey(key: String) {
        settingsManager.setGeminiApiKey(key.trim())
    }

    suspend fun chat(history: List<ChatMessage>, userMessage: String): Result<String> {
        return try {
            val apiKey = settingsManager.geminiApiKey.first()
            if (apiKey.isBlank()) return Result.failure(Exception("Chưa cài đặt Gemini API Key."))
            auraService.chat(apiKey, history, userMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeSurvey(apiKey: String, surveyData: AiSurveyData): Result<String> {
        return auraService.analyzeSurvey(apiKey, surveyData)
    }

    suspend fun generateRoadmapV2(apiKey: String, goalInput: AiGoalInput): Result<String> {
        return auraService.generateRoadmapV2(apiKey, goalInput)
    }
    suspend fun generateCompleteOnboarding(apiKey: String, surveyData: AiSurveyData, goals: String): Result<String> {
        return auraService.generateCompleteOnboarding(apiKey, surveyData, goals)
    }

    suspend fun generateProactiveGreeting(apiKey: String, context: AuraPlayerContext): Result<String> {
        return auraService.generateProactiveGreeting(apiKey, context)
    }

    /** Translates user's Vietnamese [text] into [targetLanguage] with tone appropriate for [contextLabel]. */
    suspend fun generateContextualReply(
        text: String,
        targetLanguage: String,
        contextLabel: String
    ): Result<String> {
        return try {
            val apiKey = settingsManager.geminiApiKey.first()
            if (apiKey.isBlank()) return Result.failure(Exception("Chưa cài đặt Gemini API Key."))
            auraService.generateContextualReply(apiKey, text, targetLanguage, contextLabel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Translates [text] to [targetLanguage]. Used for real-time per-sentence translation. */
    suspend fun translateText(text: String, targetLanguage: String = "Tiếng Việt"): Result<String> {
        return try {
            val apiKey = settingsManager.geminiApiKey.first()
            if (apiKey.isBlank()) return Result.failure(Exception("Chưa cài đặt Gemini API Key."))
            auraService.translateText(apiKey, text, targetLanguage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Transcribes [audioFile] and saves a .txt companion file. Returns the transcript text. */
    suspend fun transcribeAudio(audioFile: File): Result<String> {
        val apiKey = settingsManager.geminiApiKey.first()
        if (apiKey.isBlank()) return Result.failure(Exception("Chưa cài đặt Gemini API Key."))
        val base64 = encodeFileToBase64(audioFile)
            ?: return Result.failure(Exception("Không thể đọc file âm thanh."))
        return auraService.transcribeAudio(apiKey, base64).also { result ->
            result.getOrNull()?.let { text ->
                saveCompanionText(audioFile, text)
            }
        }
    }

    /** Detects language in [audioFile], transcribes, and translates to [targetLanguage]. Saves a .txt companion file. */
    suspend fun translateAudio(audioFile: File, targetLanguage: String = "Tiếng Việt"): Result<String> {
        val apiKey = settingsManager.geminiApiKey.first()
        if (apiKey.isBlank()) return Result.failure(Exception("Chưa cài đặt Gemini API Key."))
        val base64 = encodeFileToBase64(audioFile)
            ?: return Result.failure(Exception("Không thể đọc file âm thanh."))
        return auraService.translateAudio(apiKey, base64, targetLanguage).also { result ->
            result.getOrNull()?.let { text ->
                saveCompanionText(audioFile, text)
            }
        }
    }

    private fun encodeFileToBase64(file: File): String? = try {
        Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
    } catch (_: Exception) { null }

    private fun saveCompanionText(audioFile: File, text: String) {
        try {
            val txtFile = File(audioFile.parent, audioFile.nameWithoutExtension + ".txt")
            txtFile.writeText(text)
        } catch (e: Exception) {
            Log.w("AuraRepository", "Failed to save companion transcript for ${audioFile.name}", e)
        }
    }
}
