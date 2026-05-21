package com.systemleveling.core.network

import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.SkillDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.model.QuestRank
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.core.model.QuestType
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates daily quest generation:
 * 1. Gathers user data (profile, skills, history, calendar)
 * 2. Builds a prompt for the Gemini API
 * 3. Parses the AI response into QuestEntity objects
 * 4. Inserts generated quests into the database
 *
 * Quests are spread across the day with specific time windows,
 * respecting human biological cycles (eat, drink, sleep, stand up).
 */
@Singleton
class AiQuestGeneratorService @Inject constructor(
    private val geminiApiService: GeminiApiService,
    private val userDao: UserDao,
    private val questDao: QuestDao,
    private val skillDao: SkillDao
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Generate daily quests for today. Called at 00:00 or on first app open.
     * @param apiKey Gemini API key from BuildConfig
     * @param dayStart timestamp of today's midnight
     */
    suspend fun generateDailyQuests(apiKey: String, dayStart: Long): List<QuestEntity> {
        // Check if quests already exist for today
        val dayEnd = dayStart + 86400000L // +24h
        val existingCount = questDao.getQuestCountByDate(dayStart, dayEnd)
        if (existingCount > 0) return emptyList() // Already generated

        return try {
            // Build prompt from user data
            val prompt = buildQuestPrompt(dayStart)
            val aiResponse = geminiApiService.generateContent(prompt, apiKey)
            val quests = parseAiQuests(aiResponse, dayStart)

            if (quests.isNotEmpty()) {
                // Inject health reminders between AI quests
                val fullQuestList = injectHealthReminders(quests, dayStart)
                questDao.insertQuests(fullQuestList)
                fullQuestList
            } else {
                // Fallback to template quests
                val fallbackQuests = generateFallbackQuests(dayStart)
                questDao.insertQuests(fallbackQuests)
                fallbackQuests
            }
        } catch (e: Exception) {
            // API error — use fallback
            val fallbackQuests = generateFallbackQuests(dayStart)
            questDao.insertQuests(fallbackQuests)
            fallbackQuests
        }
    }

    private suspend fun buildQuestPrompt(dayStart: Long): String {
        val user = userDao.getUser().first()
        val stats = userDao.getStats().first()
        val skills = skillDao.getAllSkillsSync()

        val calendar = Calendar.getInstance().apply { timeInMillis = dayStart }
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.ENGLISH).format(calendar.time)
        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)

        val skillsJson = skills.joinToString(",\n") { skill ->
            """{"name":"${skill.name}","level":"${skill.level.title}","sp":${skill.currentSp}}"""
        }

        return """
You are the AI System of a Solo Leveling-style personal development app.
Generate 6-8 quests for the user's day. IMPORTANT RULES:

1. Quests MUST be spread across the day from wake-up (06:00) to bedtime (22:00)
2. Each quest MUST have a specific timeStart and timeEnd forming a clear deadline
3. Include health-conscious quests respecting human biological cycles:
   - Morning hydration (06:00-06:15)
   - At least 1 fitness/exercise quest
   - Meal reminders are handled separately — do NOT include them
   - Evening reflection/journal (21:00-21:30)
4. Mix quest difficulties: mostly D-C rank, with 1-2 B rank and maybe 1 A rank
5. Align quests with user's skills and goals
6. Quest titles should be motivating, short, RPG-flavored
7. Descriptions should be specific and actionable (in Vietnamese)

USER PROFILE:
- Class: ${user?.characterClass ?: "Warrior"}
- Level: ${user?.level ?: 1}
- Streak: ${user?.streak ?: 0} days
- Debt Points: ${user?.debtPoints ?: 0}
- Stats: STR=${stats?.str ?: 10}, INT=${stats?.intStat ?: 10}, AGI=${stats?.agi ?: 10}, VIT=${stats?.vit ?: 10}, WIS=${stats?.wis ?: 10}, CHA=${stats?.cha ?: 10}

SKILLS:
[$skillsJson]

CONTEXT:
- Day: $dayOfWeek $dateStr
- Wake time: 06:00
- Sleep time: 22:00

Respond with ONLY a JSON array of quest objects. Each object must have:
{
  "title": "string",
  "description": "string (Vietnamese)",
  "type": "DAILY",
  "rank": "E|D|C|B|A|S",
  "category": "fitness|health|study|reading|tech|coding|language|meditation|journal|finance|career|social|creative",
  "timeStart": "HH:mm",
  "timeEnd": "HH:mm",
  "durationMinutes": number,
  "expReward": number (20-800 based on rank),
  "goldReward": number (5-200 based on rank),
  "subtasks": ["step1", "step2"],
  "skillPointRewards": {"skillId": spAmount} or {},
  "penaltyDebtPoints": 1
}
        """.trimIndent()
    }

    private fun parseAiQuests(aiResponse: String, dayStart: Long): List<QuestEntity> {
        return try {
            val quests = json.decodeFromString<List<AiQuestDto>>(aiResponse)
            quests.mapIndexed { index, dto ->
                QuestEntity(
                    id = "Q-${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayStart)}-${String.format("%03d", index + 1)}",
                    title = dto.title,
                    description = dto.description,
                    type = try { QuestType.valueOf(dto.type) } catch (_: Exception) { QuestType.DAILY },
                    rank = try { QuestRank.valueOf(dto.rank) } catch (_: Exception) { QuestRank.D },
                    category = dto.category,
                    date = dayStart,
                    timeStart = dto.timeStart,
                    timeEnd = dto.timeEnd,
                    durationMinutes = dto.durationMinutes ?: 30,
                    expReward = dto.expReward.coerceIn(10, 2000),
                    goldReward = dto.goldReward.coerceIn(5, 500),
                    status = QuestStatus.PENDING,
                    subtasks = json.encodeToString(ListSerializer(String.serializer()), dto.subtasks ?: emptyList()),
                    skillPointRewards = json.encodeToString(MapSerializer(String.serializer(), Int.serializer()), dto.skillPointRewards ?: emptyMap()),
                    penaltyDebtPoints = dto.penaltyDebtPoints ?: 1,
                    relatedSkillIds = "[]",
                    isHealthReminder = false
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Inject health reminders (water, stand up) between AI-generated quests.
     */
    private fun injectHealthReminders(quests: List<QuestEntity>, dayStart: Long): List<QuestEntity> {
        val result = quests.toMutableList()
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayStart)

        // Water reminders every 2 hours from 08:00 to 20:00
        val waterHours = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00")
        waterHours.forEachIndexed { index, time ->
            result.add(QuestEntity(
                id = "H-$dateId-W${index + 1}",
                title = "💧 Hydration Check",
                description = "Uống 1 cốc nước (250ml) — Duy trì VIT stat!",
                type = QuestType.DAILY,
                rank = QuestRank.E,
                category = "health",
                date = dayStart,
                timeStart = time,
                timeEnd = time.let {
                    val h = it.substringBefore(":").toInt()
                    val m = it.substringAfter(":").toInt() + 10
                    String.format("%02d:%02d", h, m)
                },
                durationMinutes = 5,
                expReward = 15,
                goldReward = 5,
                status = QuestStatus.PENDING,
                penaltyDebtPoints = 0, // No penalty for water reminders
                isHealthReminder = true
            ))
        }

        // Stand up reminders (if sitting for long stretches)
        val standUpTimes = listOf("10:30", "14:30", "17:00")
        standUpTimes.forEachIndexed { index, time ->
            result.add(QuestEntity(
                id = "H-$dateId-S${index + 1}",
                title = "🚶 Đứng Dậy & Vận Động",
                description = "Đứng dậy, đi lại 5 phút, giãn cơ. Cơ thể cần vận động sau khi ngồi lâu!",
                type = QuestType.DAILY,
                rank = QuestRank.E,
                category = "health",
                date = dayStart,
                timeStart = time,
                timeEnd = time.let {
                    val h = it.substringBefore(":").toInt()
                    val m = it.substringAfter(":").toInt() + 10
                    String.format("%02d:%02d", h, m)
                },
                durationMinutes = 5,
                expReward = 15,
                goldReward = 5,
                status = QuestStatus.PENDING,
                penaltyDebtPoints = 0,
                isHealthReminder = true
            ))
        }

        // Sort by timeStart
        return result.sortedBy { it.timeStart ?: "99:99" }
    }

    /**
     * Fallback quest generation when AI API is unavailable.
     */
    private fun generateFallbackQuests(dayStart: Long): List<QuestEntity> {
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayStart)
        val quests = mutableListOf(
            QuestEntity(
                id = "Q-$dateId-001",
                title = "🌅 Morning Hydration",
                description = "Uống 500ml nước ngay khi thức dậy",
                type = QuestType.DAILY, rank = QuestRank.E, category = "health",
                date = dayStart, timeStart = "06:00", timeEnd = "06:15",
                durationMinutes = 15, expReward = 20, goldReward = 5,
                status = QuestStatus.PENDING
            ),
            QuestEntity(
                id = "Q-$dateId-002",
                title = "⚔️ Morning Warrior Training",
                description = "Tập luyện thể chất buổi sáng — chạy bộ hoặc tập gym",
                type = QuestType.DAILY, rank = QuestRank.C, category = "fitness",
                date = dayStart, timeStart = "06:30", timeEnd = "07:30",
                durationMinutes = 60, expReward = 150, goldReward = 50,
                status = QuestStatus.PENDING
            ),
            QuestEntity(
                id = "Q-$dateId-003",
                title = "📖 Deep Focus: Study Session",
                description = "Tập trung học tập/làm việc chuyên sâu",
                type = QuestType.DAILY, rank = QuestRank.B, category = "study",
                date = dayStart, timeStart = "08:30", timeEnd = "11:00",
                durationMinutes = 150, expReward = 250, goldReward = 80,
                status = QuestStatus.PENDING
            ),
            QuestEntity(
                id = "Q-$dateId-004",
                title = "🍽️ Lunch & Recovery",
                description = "Ăn trưa đầy đủ dinh dưỡng, nghỉ ngơi 30 phút",
                type = QuestType.DAILY, rank = QuestRank.E, category = "health",
                date = dayStart, timeStart = "12:00", timeEnd = "13:00",
                durationMinutes = 60, expReward = 20, goldReward = 5,
                status = QuestStatus.PENDING, isHealthReminder = true
            ),
            QuestEntity(
                id = "Q-$dateId-005",
                title = "💻 Afternoon Code Sprint",
                description = "Lập trình hoặc thực hành kỹ năng kỹ thuật",
                type = QuestType.DAILY, rank = QuestRank.C, category = "coding",
                date = dayStart, timeStart = "14:00", timeEnd = "16:00",
                durationMinutes = 120, expReward = 180, goldReward = 60,
                status = QuestStatus.PENDING
            ),
            QuestEntity(
                id = "Q-$dateId-006",
                title = "📚 Knowledge Seeker",
                description = "Đọc sách hoặc tài liệu 30 phút",
                type = QuestType.DAILY, rank = QuestRank.D, category = "reading",
                date = dayStart, timeStart = "17:00", timeEnd = "17:30",
                durationMinutes = 30, expReward = 60, goldReward = 20,
                status = QuestStatus.PENDING
            ),
            QuestEntity(
                id = "Q-$dateId-007",
                title = "🧘 Evening Reflection",
                description = "Viết nhật ký, đánh giá ngày hôm nay",
                type = QuestType.DAILY, rank = QuestRank.D, category = "journal",
                date = dayStart, timeStart = "21:00", timeEnd = "21:30",
                durationMinutes = 30, expReward = 40, goldReward = 15,
                status = QuestStatus.PENDING
            )
        )

        // Inject health reminders
        return injectHealthReminders(quests, dayStart)
    }
}

@Serializable
data class AiQuestDto(
    val title: String,
    val description: String,
    val type: String = "DAILY",
    val rank: String = "D",
    val category: String = "study",
    val timeStart: String? = null,
    val timeEnd: String? = null,
    val durationMinutes: Int? = 30,
    val expReward: Int = 50,
    val goldReward: Int = 20,
    val subtasks: List<String>? = null,
    val skillPointRewards: Map<String, Int>? = null,
    val penaltyDebtPoints: Int? = 1
)
