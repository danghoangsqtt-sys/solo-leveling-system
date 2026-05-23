package com.systemleveling.core.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Phase 1: Survey & Profile ---

@Serializable
data class AiSurveyData(
    val height: String = "",
    val weight: String = "",
    val pushUps: String = "",
    val lifting: String = "",
    val runningPace: String = "",
    val studyHours: String = "",
    val languageLevel: String = "",
    val sleepHours: String = "",
    val workStyle: String = ""
)

@Serializable
data class AiCompleteOnboardingResponse(
    val stats: AiStats = AiStats(),
    val suggestedClasses: List<AiJobClass> = emptyList()
)

@Serializable
data class AiProfileResponse(
    val stats: AiStats,
    val jobClasses: List<AiJobClass>
)

@Serializable
data class AiStats(
    val str: Int = 10,
    @SerialName("int") val intStat: Int = 10,
    val agi: Int = 10,
    val vit: Int = 10,
    val wis: Int = 10,
    val cha: Int = 10
)

@Serializable
data class AiJobClass(
    val className: String = "",
    val description: String = "",
    val iconEmoji: String = "⭐",
    val roadmap: List<AiSkillNode> = emptyList()
)

// --- Phase 2: Goals & Roadmap ---

@Serializable
data class AiGoalInput(
    val jobClass: String,
    val goal1: String,
    val goal2: String,
    val goal3: String
)

@Serializable
data class AiRoadmapResponse(
    val roadmap: List<AiSkillNode>
)

@Serializable
data class AiSkillNode(
    val name: String = "",
    val description: String = "",
    val tier: Int = 1,
    val category: String = "",
    val iconEmoji: String = "⭐"
)

// --- Phase 3: Aura Chat Context ---

@Serializable
data class AuraPlayerContext(
    val level: Int = 1,
    val exp: Int = 0,
    val streak: Int = 0,
    val pendingQuests: List<String> = emptyList(),
    val weakSkills: List<String> = emptyList()
)
