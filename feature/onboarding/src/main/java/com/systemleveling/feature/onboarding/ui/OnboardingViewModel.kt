package com.systemleveling.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.ai.AiCompleteOnboardingResponse
import com.systemleveling.core.ai.AiSurveyData
import com.systemleveling.core.ai.AuraRepository
import com.systemleveling.core.database.dao.SkillDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.SkillEntity
import com.systemleveling.core.database.entity.StatEntity
import com.systemleveling.core.database.entity.UserEntity
import com.systemleveling.core.settings.SettingsManager
import com.systemleveling.core.model.SkillLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userDao: UserDao,
    private val skillDao: SkillDao,
    private val settingsManager: SettingsManager,
    private val auraRepository: AuraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Suppress("UNUSED_PARAMETER")
    fun generateRoadmapAndComplete(
        nickname: String,
        goal: String,
        surveyData: AiSurveyData,
        overrideApiKey: String = "",
        supabaseUrl: String = "",
        supabaseAnonKey: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = OnboardingUiState.Loading("Đang phân tích dữ liệu và tạo lộ trình...")

            try {
                if (overrideApiKey.isNotBlank()) {
                    settingsManager.setGeminiApiKey(overrideApiKey)
                }
                if (supabaseUrl.isNotBlank() && supabaseAnonKey.isNotBlank()) {
                    settingsManager.setSupabaseConfig(supabaseUrl, supabaseAnonKey)
                }
                val apiKey = settingsManager.geminiApiKey.first()
                if (apiKey.isBlank()) {
                    _uiState.value = OnboardingUiState.Error("Nhập Gemini API Key (bắt đầu bằng AIzaSy...) để khởi tạo.")
                    return@launch
                }

                val result = auraRepository.generateCompleteOnboarding(apiKey, surveyData, goal)
                result.fold(
                    onSuccess = { jsonStr ->
                        val cleanJson = jsonStr.trim().removePrefix("```json").removeSuffix("```").trim()
                        val roadmapData = json.decodeFromString<AiCompleteOnboardingResponse>(cleanJson)
                        _uiState.value = OnboardingUiState.Result(roadmapData)
                    },
                    onFailure = { error ->
                        _uiState.value = OnboardingUiState.Error(error.message ?: "Lỗi hệ thống")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = OnboardingUiState.Error("Lỗi kết nối AI: ${e.localizedMessage}")
            }
        }
    }

    fun resetToIdle() {
        _uiState.value = OnboardingUiState.Idle
    }

    fun acceptAndComplete(nickname: String, goal: String, selectedClassName: String, data: AiCompleteOnboardingResponse) {
        viewModelScope.launch {
            _uiState.value = OnboardingUiState.Loading("Đang lưu dữ liệu...")
            try {
                saveDataAndComplete(nickname, goal, selectedClassName, data)
            } catch (e: Exception) {
                _uiState.value = OnboardingUiState.Error("Lưu thất bại: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun saveDataAndComplete(nickname: String, goal: String, selectedClassName: String, data: AiCompleteOnboardingResponse) {
        val selectedJobClass = data.suggestedClasses.find { it.className == selectedClassName }

        // Save User
        userDao.insertUser(
            UserEntity(
                nickname = nickname.ifBlank { "Shadow Monarch" },
                characterClass = selectedClassName,
                avatarUri = selectedJobClass?.iconEmoji
            )
        )

        // Save Stats
        userDao.insertStats(
            StatEntity(
                str = data.stats.str,
                intStat = data.stats.intStat,
                agi = data.stats.agi,
                vit = data.stats.vit,
                wis = data.stats.wis,
                cha = data.stats.cha
            )
        )

        // Save Skills — group AI nodes by category into parent+child hierarchy
        val roadmapToUse = selectedJobClass?.roadmap ?: emptyList()

        val allSkillEntities = mutableListOf<SkillEntity>()

        // Group nodes by category → each category becomes a parent skill
        val grouped = roadmapToUse.groupBy { it.category }
        grouped.forEach { (categoryName, nodes) ->
            val parentId = UUID.randomUUID().toString()
            val parentIconEmoji = nodes.firstOrNull()?.iconEmoji ?: "⭐"

            // Parent skill: represents the entire skill branch
            allSkillEntities.add(
                SkillEntity(
                    id = parentId,
                    name = categoryName,
                    description = "Nhánh kỹ năng: $categoryName",
                    level = SkillLevel.NOVICE,
                    currentSp = 0,
                    parentId = null,
                    iconId = parentIconEmoji,
                    category = "parent",
                    goalDescription = goal,
                    isAiGenerated = true
                )
            )

            // Child skills: individual skill nodes under this category
            nodes.forEachIndexed { index, node ->
                allSkillEntities.add(
                    SkillEntity(
                        id = UUID.randomUUID().toString(),
                        name = node.name,
                        description = node.description,
                        level = SkillLevel.NOVICE,
                        currentSp = 0,
                        parentId = parentId,
                        iconId = node.iconEmoji,
                        category = node.category,
                        goalDescription = goal,
                        isAiGenerated = true,
                        yPos = (node.tier - 1) * 200f,
                        xPos = index * 250f
                    )
                )
            }
        }

        skillDao.replaceSkills(allSkillEntities)

        // Mark complete
        settingsManager.setOnboarded(true)

        _uiState.value = OnboardingUiState.Success
    }
}

sealed class OnboardingUiState {
    object Idle : OnboardingUiState()
    data class Loading(val message: String) : OnboardingUiState()
    data class Result(val data: AiCompleteOnboardingResponse) : OnboardingUiState()
    data class Error(val message: String) : OnboardingUiState()
    object Success : OnboardingUiState()
}
