package com.systemleveling.core.engine

import com.systemleveling.core.database.dao.SkillDao
import com.systemleveling.core.database.entity.SkillEntity
import com.systemleveling.core.model.SkillLevel
import com.systemleveling.core.network.GeminiApiService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI Skill Roadmap Builder.
 * Uses Gemini to analyze user goals and generate a skill tree
 * with sub-skills and quest templates for skill development.
 */
@Singleton
class AiSkillRoadmapService @Inject constructor(
    private val skillDao: SkillDao,
    private val geminiApiService: GeminiApiService
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Generate a skill roadmap from user's goal description.
     * Example: "Tôi muốn giỏi Android Development" → generates skill tree
     */
    suspend fun generateSkillRoadmap(
        goalDescription: String,
        category: String,
        apiKey: String
    ): List<SkillEntity> {
        if (apiKey.isBlank()) return generateFallbackRoadmap(goalDescription, category)

        val prompt = """
You are the AI Skill Architect of a Solo Leveling RPG personal development app.
The user wants to build a skill roadmap for their goal.

GOAL: $goalDescription
CATEGORY: $category

Generate a skill tree with 1 root skill and 3-5 sub-skills.
Each skill should have quest templates for leveling up.

Response MUST be a JSON array:
[
  {
    "name": "Root Skill Name",
    "description": "Description in Vietnamese",
    "parentId": null,
    "category": "$category",
    "questTemplates": [
      {"title": "quest title", "description": "Vietnamese desc", "spReward": 10, "rank": "D"},
      {"title": "quest title 2", "description": "Vietnamese desc", "spReward": 20, "rank": "C"}
    ]
  },
  {
    "name": "Sub-Skill Name",
    "description": "Description in Vietnamese",
    "parentId": "ROOT_ID_PLACEHOLDER",
    "category": "$category",
    "questTemplates": [...]
  }
]

Rules:
1. Root skill = the main goal, sub-skills = building blocks
2. Quest templates: 3-4 per skill, increasing difficulty D→B
3. All descriptions in Vietnamese
4. Skill names in English, RPG-style
5. Quest SP rewards: D=10, C=20, B=40, A=80
        """.trimIndent()

        return try {
            val response = geminiApiService.generateContent(prompt, apiKey)
            parseSkillRoadmap(response, goalDescription, category)
        } catch (_: Exception) {
            generateFallbackRoadmap(goalDescription, category)
        }
    }

    private suspend fun parseSkillRoadmap(
        response: String,
        goalDescription: String,
        category: String
    ): List<SkillEntity> {
        return try {
            val dtos = json.decodeFromString<List<SkillRoadmapDto>>(response)
            if (dtos.isEmpty()) return generateFallbackRoadmap(goalDescription, category)

            val rootId = "SK-${UUID.randomUUID().toString().take(8)}"
            val skills = mutableListOf<SkillEntity>()

            dtos.forEachIndexed { index, dto ->
                val skillId = if (index == 0) rootId else "SK-${UUID.randomUUID().toString().take(8)}"
                val parentId = if (index == 0) null else {
                    if (dto.parentId == "ROOT_ID_PLACEHOLDER" || dto.parentId == null) rootId
                    else dto.parentId
                }

                val questJson = try {
                    json.encodeToString(
                        kotlinx.serialization.builtins.ListSerializer(QuestTemplateDto.serializer()),
                        dto.questTemplates ?: emptyList()
                    )
                } catch (_: Exception) { "[]" }

                skills.add(
                    SkillEntity(
                        id = skillId,
                        name = dto.name,
                        description = dto.description,
                        level = SkillLevel.NOVICE,
                        currentSp = 0,
                        parentId = parentId,
                        iconId = getIconForCategory(category),
                        category = category,
                        goalDescription = goalDescription,
                        roadmapQuests = questJson,
                        isAiGenerated = true,
                        xPos = if (index == 0) 0.5f else (index.toFloat() / dtos.size),
                        yPos = if (index == 0) 0.1f else 0.4f + (index * 0.15f)
                    )
                )
            }

            // Save to DB
            skillDao.insertSkills(skills)
            skills
        } catch (_: Exception) {
            generateFallbackRoadmap(goalDescription, category)
        }
    }

    private suspend fun generateFallbackRoadmap(
        goalDescription: String,
        category: String
    ): List<SkillEntity> {
        val rootId = "SK-${UUID.randomUUID().toString().take(8)}"
        val icon = getIconForCategory(category)

        val skills = listOf(
            SkillEntity(
                id = rootId,
                name = "${category.replaceFirstChar { it.uppercase() }} Mastery",
                description = "Chinh phục con đường: $goalDescription",
                level = SkillLevel.NOVICE, currentSp = 0, parentId = null,
                iconId = icon, category = category,
                goalDescription = goalDescription, isAiGenerated = true,
                roadmapQuests = """[{"title":"Daily Practice","description":"Luyện tập cơ bản hàng ngày","spReward":10,"rank":"D"},{"title":"Deep Focus Session","description":"Tập trung chuyên sâu 2 giờ","spReward":20,"rank":"C"}]""",
                xPos = 0.5f, yPos = 0.1f
            ),
            SkillEntity(
                id = "SK-${UUID.randomUUID().toString().take(8)}",
                name = "Foundation",
                description = "Nền tảng cơ bản cho ${category}",
                level = SkillLevel.NOVICE, currentSp = 0, parentId = rootId,
                iconId = "📚", category = category, isAiGenerated = true,
                roadmapQuests = """[{"title":"Study Basics","description":"Học kiến thức nền tảng","spReward":10,"rank":"D"}]""",
                xPos = 0.2f, yPos = 0.4f
            ),
            SkillEntity(
                id = "SK-${UUID.randomUUID().toString().take(8)}",
                name = "Application",
                description = "Áp dụng vào thực tế",
                level = SkillLevel.NOVICE, currentSp = 0, parentId = rootId,
                iconId = "⚡", category = category, isAiGenerated = true,
                roadmapQuests = """[{"title":"Real Project","description":"Thực hành dự án thực tế","spReward":30,"rank":"C"}]""",
                xPos = 0.5f, yPos = 0.4f
            ),
            SkillEntity(
                id = "SK-${UUID.randomUUID().toString().take(8)}",
                name = "Advanced",
                description = "Kỹ năng nâng cao",
                level = SkillLevel.NOVICE, currentSp = 0, parentId = rootId,
                iconId = "🏆", category = category, isAiGenerated = true,
                roadmapQuests = """[{"title":"Master Challenge","description":"Thử thách bậc thầy","spReward":50,"rank":"B"}]""",
                xPos = 0.8f, yPos = 0.4f
            )
        )

        skillDao.insertSkills(skills)
        return skills
    }

    private fun getIconForCategory(category: String): String = when (category) {
        "coding" -> "💻"
        "fitness" -> "💪"
        "language" -> "🌍"
        "reading" -> "📚"
        "finance" -> "💰"
        "tech" -> "🔧"
        "study" -> "🎓"
        "creative" -> "🎨"
        "social" -> "🤝"
        "meditation" -> "🧘"
        else -> "⭐"
    }
}

@Serializable
data class SkillRoadmapDto(
    val name: String,
    val description: String,
    val parentId: String? = null,
    val category: String = "general",
    val questTemplates: List<QuestTemplateDto>? = null
)

@Serializable
data class QuestTemplateDto(
    val title: String,
    val description: String,
    val spReward: Int = 10,
    val rank: String = "D"
)
