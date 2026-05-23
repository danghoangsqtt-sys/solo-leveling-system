package com.systemleveling.core.network

import com.systemleveling.core.settings.SettingsManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiAvatarGeneratorService @Inject constructor(
    private val geminiApiService: GeminiApiService,
    private val settingsManager: SettingsManager
) {
    suspend fun generateAvatar(profession: String, description: String, promotionTier: Int): String? {
        val apiKey = settingsManager.geminiApiKey.first()
        if (apiKey.isBlank()) return null

        val tierTraits = when (promotionTier) {
            0    -> "glowing blue sapphire energy aura, piercing blue eyes, determined expression"
            1    -> "swirling violet mystical power, amethyst energy emanating, wise commanding expression"
            2    -> "radiant golden divine aura, blazing golden eyes, legendary heroic presence"
            3    -> "crimson flame power, intense burning red eyes, battle-scarred warrior"
            else -> "pure transcendent white energy halo, silver hair, otherworldly legendary hero"
        }

        val auraColor = when (promotionTier) {
            0    -> "sapphire blue"
            1    -> "deep violet"
            2    -> "radiant gold"
            3    -> "crimson red"
            else -> "pure white"
        }

        val classTerm = when {
            profession.contains("lập trình", ignoreCase = true) ||
            profession.contains("developer", ignoreCase = true) ||
            profession.contains("kỹ sư", ignoreCase = true) ||
            profession.contains("engineer", ignoreCase = true) -> "Arcane Technomancer"
            profession.contains("học sinh", ignoreCase = true) ||
            profession.contains("sinh viên", ignoreCase = true) ||
            profession.contains("student", ignoreCase = true) -> "Scholar Apprentice"
            profession.contains("giáo viên", ignoreCase = true) ||
            profession.contains("teacher", ignoreCase = true) -> "Sage Instructor"
            profession.contains("kinh doanh", ignoreCase = true) ||
            profession.contains("business", ignoreCase = true) -> "Merchant Prince"
            profession.contains("bác sĩ", ignoreCase = true) ||
            profession.contains("doctor", ignoreCase = true) -> "Divine Healer"
            profession.contains("thiết kế", ignoreCase = true) ||
            profession.contains("designer", ignoreCase = true) -> "Illusion Artisan"
            else -> "Shadow Warrior"
        }

        val descPart = if (description.isNotBlank()) "Character backstory: $description. " else ""

        val prompt = """
            Create a fantasy RPG character portrait in dark manhwa art style (Solo Leveling aesthetic).
            Character class: $classTerm.
            ${descPart}Visual traits: $tierTraits.
            $auraColor magical energy surrounds the character with glowing $auraColor runes and particles.
            Art direction: highly detailed digital painting, cinematic dramatic lighting, dark fantasy.
            Composition: close-up portrait facing viewer, heroic pose, face clearly visible.
            Background: dark void with swirling $auraColor energy particles and faint rune symbols.
            Style notes: sharp linework, deep shadows, vibrant energy effects, no text or watermarks.
        """.trimIndent()

        return geminiApiService.generateImageBase64(prompt, apiKey)
    }
}
