package com.systemleveling.core.ai

import com.systemleveling.core.settings.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        val apiKey = settingsManager.geminiApiKey.first()
        return auraService.chat(apiKey, history, userMessage)
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
}
