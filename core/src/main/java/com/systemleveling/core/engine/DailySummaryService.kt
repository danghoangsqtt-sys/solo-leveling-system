package com.systemleveling.core.engine

import com.systemleveling.core.database.dao.DailySummaryDao
import com.systemleveling.core.database.dao.ItemDao
import com.systemleveling.core.database.dao.JournalDao
import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.SkillDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.DailySummaryEntity
import com.systemleveling.core.database.entity.JournalEntity
import com.systemleveling.core.model.Mood
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.core.network.GeminiApiService
import com.systemleveling.core.settings.SettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates the Daily Summary report at end of day.
 * Aggregates quest results, stat changes, skill progress, and generates AI journal.
 */
@Singleton
class DailySummaryService @Inject constructor(
    private val questDao: QuestDao,
    private val userDao: UserDao,
    private val skillDao: SkillDao,
    private val itemDao: ItemDao,
    private val journalDao: JournalDao,
    private val dailySummaryDao: DailySummaryDao,
    private val geminiApiService: GeminiApiService,
    private val settingsManager: SettingsManager
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Generate the complete daily summary for a given day.
     * Called by EndOfDayWorker at 22:00 or manually by user.
     */
    suspend fun generateDailySummary(dayStart: Long, dayEnd: Long, apiKey: String): DailySummaryEntity {
        // Check if summary already exists
        val existing = dailySummaryDao.getSummaryByDateSync(dayStart, dayEnd)
        if (existing != null) return existing

        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayStart)

        // 1. Aggregate quest stats
        val allQuests = questDao.getQuestsByDateSync(dayStart, dayEnd)
        val completed = allQuests.filter { it.status == QuestStatus.COMPLETED }
        val failed = allQuests.filter { it.status == QuestStatus.FAILED || it.status == QuestStatus.EXPIRED }
        val totalNonHealth = allQuests.filter { !it.isHealthReminder }
        val completionRate = if (totalNonHealth.isNotEmpty()) {
            completed.filter { !it.isHealthReminder }.size.toDouble() / totalNonHealth.size
        } else 0.0

        // 2. Calculate rewards earned today
        val expEarned = completed.sumOf { it.expReward }
        val goldEarned = completed.sumOf { it.goldReward }
        val itemsDropped = completed.count { it.droppedItemId != null }

        // 3. Stat changes — read from quest.statPointRewards (matches RewardEngine precision)
        val statChanges = mutableMapOf<String, Int>()
        for (quest in completed) {
            try {
                val statMap = json.decodeFromString<Map<String, Int>>(quest.statPointRewards)
                for ((stat, gain) in statMap) {
                    if (gain > 0) statChanges[stat] = (statChanges[stat] ?: 0) + gain
                }
            } catch (_: Exception) {
                // Fallback for legacy/health quests with no statPointRewards
            }
        }

        // 4. Debt info
        val user = userDao.getUser().first()
        val debtGained = failed.sumOf { it.penaltyDebtPoints }

        // 5. Skill progress (aggregate SP from completed quests)
        val skillProgress = mutableMapOf<String, Int>()
        for (quest in completed) {
            try {
                val spMap = json.decodeFromString<Map<String, Int>>(quest.skillPointRewards)
                for ((skillId, sp) in spMap) {
                    val skill = skillDao.getSkillByIdSync(skillId)
                    val name = skill?.name ?: skillId
                    skillProgress[name] = (skillProgress[name] ?: 0) + sp
                }
            } catch (_: Exception) { }
        }

        // 6. Generate AI Journal
        val aiJournal = generateAiJournal(dayStart, completed.size, failed.size, expEarned, statChanges, skillProgress, apiKey)

        // 7. Generate Tomorrow Plan
        val tomorrowPlan = generateTomorrowPlan(dayStart, completed, failed, apiKey)

        val summary = DailySummaryEntity(
            id = "DS-$dateId",
            date = dayStart,
            totalQuests = totalNonHealth.size,
            completedQuests = completed.filter { !it.isHealthReminder }.size,
            failedQuests = failed.filter { !it.isHealthReminder }.size,
            completionRate = completionRate,
            expEarned = expEarned,
            goldEarned = goldEarned,
            itemsDropped = itemsDropped,
            statChanges = json.encodeToString(MapSerializer(String.serializer(), Int.serializer()), statChanges),
            skillProgress = json.encodeToString(MapSerializer(String.serializer(), Int.serializer()), skillProgress),
            debtPointsGained = debtGained,
            currentDebtTotal = user?.debtPoints ?: 0,
            currentStreak = user?.streak ?: 0,
            aiJournalContent = aiJournal,
            tomorrowPlan = tomorrowPlan
        )

        dailySummaryDao.insertSummary(summary)

        // Also create an AI journal entry
        val journalEntry = JournalEntity(
            id = "J-AI-$dateId",
            content = aiJournal,
            mood = determineMood(completionRate),
            timestamp = System.currentTimeMillis(),
            isAiGenerated = true,
            questSummary = "Hoàn thành ${completed.size}/${totalNonHealth.size} nhiệm vụ. +${expEarned}EXP, +${goldEarned}G",
            dailySummaryId = summary.id
        )
        journalDao.insertJournal(journalEntry)

        return summary
    }

    private suspend fun generateAiJournal(
        dayStart: Long,
        completedCount: Int,
        failedCount: Int,
        expEarned: Int,
        statChanges: Map<String, Int>,
        skillProgress: Map<String, Int>,
        apiKey: String
    ): String {
        if (apiKey.isBlank()) return generateFallbackJournal(completedCount, failedCount, expEarned)

        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dayStart)
        val statsText = statChanges.entries.joinToString(", ") { "${it.key}+${it.value}" }
        val skillsText = skillProgress.entries.joinToString(", ") { "${it.key}+${it.value}SP" }

        val prompt = """
You are the AI narrator of a Solo Leveling RPG personal development app.
Write a short, motivating end-of-day journal entry (150-200 words) in Vietnamese.
Use RPG/manhwa language style — dramatic, poetic, empowering.

TODAY'S RESULTS ($dateStr):
- Quests completed: $completedCount, failed: $failedCount
- EXP earned: $expEarned
- Stat gains: $statsText
- Skill progress: $skillsText

Write the journal as if narrating the player's adventure today.
Include:1. A dramatic opening line2. Mention specific achievements3. Encouraging words for tomorrow4. End with a power quote

Respond with ONLY the journal text, no JSON wrapping.
        """.trimIndent()

        return try {
            val response = geminiApiService.generateContent(prompt, apiKey)
            // Clean up any JSON wrapping the AI might add
            response.trim().removeSurrounding("\"")
        } catch (_: Exception) {
            generateFallbackJournal(completedCount, failedCount, expEarned)
        }
    }

    private fun generateFallbackJournal(completedCount: Int, failedCount: Int, expEarned: Int): String {
        return """
⚔️ Nhật Ký Chiến Binh

Hôm nay, chiến binh đã hoàn thành $completedCount nhiệm vụ và thu được ${expEarned} EXP.
${if (failedCount > 0) "Có $failedCount nhiệm vụ chưa hoàn thành — nhưng thất bại chỉ là bước đệm cho thành công." else "Không có nhiệm vụ nào thất bại — một ngày hoàn hảo!"}

Mỗi ngày trôi qua, sức mạnh của bạn lại tăng thêm.
Hãy tiếp tục con đường trở thành Vua Bóng Tối.

"Kẻ mạnh không phải là kẻ không bao giờ ngã, mà là kẻ luôn đứng dậy."
        """.trimIndent()
    }

    private suspend fun generateTomorrowPlan(
        dayStart: Long,
        completed: List<com.systemleveling.core.database.entity.QuestEntity>,
        failed: List<com.systemleveling.core.database.entity.QuestEntity>,
        apiKey: String
    ): String {
        if (apiKey.isBlank()) return generateFallbackTomorrowPlan(failed)

        val failedTitles = failed.joinToString(", ") { it.title }
        val completedCategories = completed.map { it.category }.distinct().joinToString(", ")

        val weeklyPlan = settingsManager.weeklyPlanItems.first()
        val monthlyPlan = settingsManager.monthlyPlanItems.first()
        val weeklyBlock = if (weeklyPlan.isEmpty()) "Chưa có" else
            weeklyPlan.joinToString("; ") { "[${it.priority}] ${it.title}" }
        val monthlyBlock = if (monthlyPlan.isEmpty()) "Chưa có" else
            monthlyPlan.joinToString("; ") { "[${it.priority}] ${it.title}" }

        val prompt = """
You are the AI Quest Master of an RPG personal development app.
Based on today's results and the user's multi-horizon plans, suggest 5-7 todo items for tomorrow.

TODAY'S RESULTS:
- Completed categories: $completedCategories
- Failed quests: $failedTitles

WEEKLY GOALS (use to align tomorrow's plan):
$weeklyBlock

MONTHLY GOALS (use to align long-term focus):
$monthlyBlock

Generate a JSON array of todo items for tomorrow's plan:
[
  {"title": "string", "priority": "HIGH|MEDIUM|LOW", "deadline": "HH:mm", "category": "string"},
  ...
]

Rules:
1. Retry any failed quests from today (mark HIGH priority)
2. Include 1-2 items that advance the weekly goals
3. Balance fitness, study, and rest
4. Align at least 1 item with monthly goals if relevant

Respond with ONLY the JSON array.
        """.trimIndent()

        return try {
            geminiApiService.generateContent(prompt, apiKey)
        } catch (_: Exception) {
            generateFallbackTomorrowPlan(failed)
        }
    }

    private fun generateFallbackTomorrowPlan(
        failed: List<com.systemleveling.core.database.entity.QuestEntity>
    ): String {
        val items = mutableListOf(
            """{"title":"Morning Workout","priority":"HIGH","deadline":"07:30","category":"fitness"}""",
            """{"title":"Deep Focus Session","priority":"HIGH","deadline":"11:00","category":"study"}""",
            """{"title":"Reading 30min","priority":"MEDIUM","deadline":"17:30","category":"reading"}""",
            """{"title":"Evening Journal","priority":"MEDIUM","deadline":"21:30","category":"journal"}"""
        )
        // Add retry items for failed quests
        for (quest in failed.take(3)) {
            items.add("""{"title":"Retry: ${quest.title}","priority":"HIGH","deadline":"${quest.timeEnd ?: "18:00"}","category":"${quest.category}"}""")
        }
        return "[${items.joinToString(",")}]"
    }

    private fun determineMood(completionRate: Double): Mood {
        return when {
            completionRate >= 0.9 -> Mood.EXCITED
            completionRate >= 0.7 -> Mood.HAPPY
            completionRate >= 0.5 -> Mood.NEUTRAL
            completionRate >= 0.3 -> Mood.SAD
            else -> Mood.STRESSED
        }
    }
}
