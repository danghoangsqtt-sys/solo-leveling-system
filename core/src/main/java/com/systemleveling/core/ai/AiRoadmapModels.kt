package com.systemleveling.core.ai

import kotlinx.serialization.Serializable

// --- Phase 1: Survey & Profile ---

@Serializable
data class AiSurveyData(
    val height: String,
    val weight: String,
    val pushUps: String,
    val lifting: String,
    val runningPace: String,
    val studyHours: String,
    val languageLevel: String,
    val sleepHours: String,
    val workStyle: String
)

@Serializable
data class AiCompleteOnboardingResponse(
    val stats: AiStats,
    val suggestedClasses: List<AiJobClass>
)

@Serializable
data class AiProfileResponse(
    val stats: AiStats,
    val jobClasses: List<AiJobClass>
)

@Serializable
data class AiStats(
    val str: Int, // Sức mạnh (Strength)
    val intStat: Int, // Trí tuệ (Intelligence)
    val agi: Int, // Nhanh nhẹn (Agility)
    val vit: Int, // Thể lực (Vitality)
    val wis: Int, // Thông thái (Wisdom)
    val cha: Int // Sức hút (Charisma)
)

@Serializable
data class AiJobClass(
    val className: String,
    val description: String,
    val iconEmoji: String, // Maplestory style vibe emoji
    val roadmap: List<AiSkillNode> // Kỹ năng riêng của nghề này
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
    val name: String,
    val description: String,
    val tier: Int,
    val category: String, // Which goal this belongs to
    val iconEmoji: String // e.g. 🗡️, 🛡️, 📚, 💻
)
