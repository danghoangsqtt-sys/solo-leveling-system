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
}
