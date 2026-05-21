package com.systemleveling.core.engine

import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.SkillDao
import com.systemleveling.core.database.dao.TitleDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.TitleEntity
import com.systemleveling.core.model.ItemRarity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Achievement Tracker Engine.
 * Automatically checks and updates title progress/unlock status
 * based on various player metrics (quests, streak, level, stats, etc.).
 *
 * Called after:
 * - Quest completion (RewardEngine)
 * - End of day (PenaltyEngine)
 * - Stat/Skill changes
 */
@Singleton
class AchievementTracker @Inject constructor(
    private val titleDao: TitleDao,
    private val userDao: UserDao,
    private val questDao: QuestDao,
    private val skillDao: SkillDao
) {
    /**
     * Check all unacquired titles and update progress.
     * Returns list of newly unlocked titles.
     */
    suspend fun checkAllAchievements(): List<TitleEntity> {
        val user = userDao.getUser().first() ?: return emptyList()
        val stats = userDao.getStats().first() ?: return emptyList()
        val unacquired = titleDao.getUnacquiredByType("QUEST_COUNT") +
                titleDao.getUnacquiredByType("STREAK") +
                titleDao.getUnacquiredByType("DEBT_ZERO") +
                titleDao.getUnacquiredByType("LEVEL") +
                titleDao.getUnacquiredByType("SKILL_LEVEL") +
                titleDao.getUnacquiredByType("STAT") +
                titleDao.getUnacquiredByType("CATEGORY_COUNT")

        val newlyUnlocked = mutableListOf<TitleEntity>()

        for (title in unacquired) {
            val currentProgress = when (title.conditionType) {
                "QUEST_COUNT" -> {
                    // Count total completed quests, optionally filtered by category
                    if (title.conditionTarget.isBlank()) {
                        questDao.getCompletedQuestCountAll()
                    } else {
                        questDao.getCompletedQuestCountByCategory(title.conditionTarget)
                    }
                }
                "STREAK" -> user.streak
                "DEBT_ZERO" -> {
                    // Progress = consecutive days with 0 debt
                    if (user.debtPoints == 0) title.progress + 1 else 0
                }
                "LEVEL" -> user.level
                "SKILL_LEVEL" -> {
                    val skill = skillDao.getSkillByIdSync(title.conditionTarget)
                    skill?.level?.ordinal ?: 0
                }
                "STAT" -> {
                    when (title.conditionTarget) {
                        "STR" -> stats.str
                        "INT" -> stats.intStat
                        "AGI" -> stats.agi
                        "VIT" -> stats.vit
                        "WIS" -> stats.wis
                        "CHA" -> stats.cha
                        else -> 0
                    }
                }
                "CATEGORY_COUNT" -> {
                    questDao.getCompletedQuestCountByCategory(title.conditionTarget)
                }
                else -> title.progress
            }

            val updated = title.copy(
                progress = currentProgress.coerceAtMost(title.maxProgress)
            )

            // Check if title should be unlocked
            if (currentProgress >= title.conditionValue && !title.isAcquired) {
                val unlocked = updated.copy(
                    isAcquired = true,
                    progress = title.maxProgress,
                    unlockedAt = System.currentTimeMillis()
                )
                titleDao.updateTitle(unlocked)
                newlyUnlocked.add(unlocked)
            } else if (updated.progress != title.progress) {
                titleDao.updateTitle(updated)
            }
        }

        return newlyUnlocked
    }

    /**
     * Seed the initial title definitions.
     * Called when no titles exist in DB.
     */
    suspend fun seedTitleDefinitions() {
        val count = titleDao.getAcquiredCount()
        // Only seed if less than 3 titles total (prevents duplicate seeding)
        val allTitles = titleDao.getUnacquiredTitles()
        if (allTitles.size > 5) return

        val titles = listOf(
            // Quest Count titles
            TitleEntity(
                id = "T-QC-10", name = "Chiến Binh Mới",
                description = "Bắt đầu hành trình trở thành kẻ mạnh nhất.",
                condition = "Hoàn thành 10 nhiệm vụ", rarity = ItemRarity.COMMON,
                conditionType = "QUEST_COUNT", conditionTarget = "", conditionValue = 10,
                maxProgress = 10, iconEmoji = "⚔️"
            ),
            TitleEntity(
                id = "T-QC-50", name = "Chiến Binh Trăm Trận",
                description = "Kinh nghiệm chiến đấu đáng nể.",
                condition = "Hoàn thành 50 nhiệm vụ", rarity = ItemRarity.UNCOMMON,
                conditionType = "QUEST_COUNT", conditionTarget = "", conditionValue = 50,
                maxProgress = 50, iconEmoji = "🗡️"
            ),
            TitleEntity(
                id = "T-QC-100", name = "Bách Chiến Bách Thắng",
                description = "Trải qua trăm trận chiến và vẫn đứng vững.",
                condition = "Hoàn thành 100 nhiệm vụ", rarity = ItemRarity.RARE,
                conditionType = "QUEST_COUNT", conditionTarget = "", conditionValue = 100,
                maxProgress = 100, iconEmoji = "🛡️",
                statBonus = """{"STR":5,"VIT":5}"""
            ),
            TitleEntity(
                id = "T-QC-500", name = "Huyền Thoại Sống",
                description = "500 nhiệm vụ — bạn là huyền thoại.",
                condition = "Hoàn thành 500 nhiệm vụ", rarity = ItemRarity.LEGENDARY,
                conditionType = "QUEST_COUNT", conditionTarget = "", conditionValue = 500,
                maxProgress = 500, iconEmoji = "👑",
                statBonus = """{"STR":10,"INT":10,"WIS":10}"""
            ),

            // Streak titles
            TitleEntity(
                id = "T-ST-7", name = "Kẻ Kiên Trì",
                description = "7 ngày liên tiếp không bỏ cuộc.",
                condition = "Streak 7 ngày", rarity = ItemRarity.COMMON,
                conditionType = "STREAK", conditionValue = 7,
                maxProgress = 7, iconEmoji = "🔥"
            ),
            TitleEntity(
                id = "T-ST-30", name = "Người Kỷ Luật Sắt",
                description = "30 ngày liên tiếp — ý chí bất khuất.",
                condition = "Streak 30 ngày", rarity = ItemRarity.RARE,
                conditionType = "STREAK", conditionValue = 30,
                maxProgress = 30, iconEmoji = "🔥",
                statBonus = """{"WIS":5}"""
            ),
            TitleEntity(
                id = "T-ST-100", name = "Ngọn Lửa Bất Diệt",
                description = "100 ngày — không gì có thể dập tắt bạn.",
                condition = "Streak 100 ngày", rarity = ItemRarity.MYTHIC,
                conditionType = "STREAK", conditionValue = 100,
                maxProgress = 100, iconEmoji = "🔥",
                statBonus = """{"STR":10,"INT":10,"AGI":10,"VIT":10,"WIS":10,"CHA":10}"""
            ),

            // Category specialization titles
            TitleEntity(
                id = "T-CAT-FIT", name = "Chiến Binh Thể Chất",
                description = "Rèn luyện thân thể không ngừng.",
                condition = "30 nhiệm vụ fitness", rarity = ItemRarity.UNCOMMON,
                conditionType = "CATEGORY_COUNT", conditionTarget = "fitness", conditionValue = 30,
                maxProgress = 30, iconEmoji = "💪",
                statBonus = """{"STR":5,"AGI":3}"""
            ),
            TitleEntity(
                id = "T-CAT-CODE", name = "Mã Thuật Sư",
                description = "Thành thạo ngôn ngữ của máy.",
                condition = "30 nhiệm vụ coding", rarity = ItemRarity.UNCOMMON,
                conditionType = "CATEGORY_COUNT", conditionTarget = "coding", conditionValue = 30,
                maxProgress = 30, iconEmoji = "💻",
                statBonus = """{"INT":5,"WIS":3}"""
            ),
            TitleEntity(
                id = "T-CAT-READ", name = "Hiền Giả Tri Thức",
                description = "Hấp thụ tri thức từ sách vở.",
                condition = "30 nhiệm vụ reading", rarity = ItemRarity.UNCOMMON,
                conditionType = "CATEGORY_COUNT", conditionTarget = "reading", conditionValue = 30,
                maxProgress = 30, iconEmoji = "📚",
                statBonus = """{"INT":3,"WIS":5}"""
            ),

            // Level titles
            TitleEntity(
                id = "T-LV-10", name = "Tân Binh Giác Ngộ",
                description = "Đạt Level 10 — con đường mới mở.",
                condition = "Đạt Level 10", rarity = ItemRarity.COMMON,
                conditionType = "LEVEL", conditionValue = 10,
                maxProgress = 10, iconEmoji = "⬆️"
            ),
            TitleEntity(
                id = "T-LV-25", name = "Dũng Sĩ Đẳng Cấp",
                description = "Level 25 — bạn thuộc top đầu.",
                condition = "Đạt Level 25", rarity = ItemRarity.RARE,
                conditionType = "LEVEL", conditionValue = 25,
                maxProgress = 25, iconEmoji = "⬆️",
                statBonus = """{"VIT":5}"""
            ),
            TitleEntity(
                id = "T-LV-50", name = "Bán Thần",
                description = "Level 50 — sức mạnh siêu phàm.",
                condition = "Đạt Level 50", rarity = ItemRarity.LEGENDARY,
                conditionType = "LEVEL", conditionValue = 50,
                maxProgress = 50, iconEmoji = "👑",
                statBonus = """{"STR":10,"INT":10}"""
            ),

            // Stat titles
            TitleEntity(
                id = "T-STAT-STR50", name = "Quả Đấm Sắt",
                description = "STR đạt 50 — sức mạnh kinh hoàng.",
                condition = "STR đạt 50", rarity = ItemRarity.RARE,
                conditionType = "STAT", conditionTarget = "STR", conditionValue = 50,
                maxProgress = 50, iconEmoji = "💪"
            ),
            TitleEntity(
                id = "T-STAT-INT50", name = "Bộ Não Thiên Tài",
                description = "INT đạt 50 — trí tuệ siêu phàm.",
                condition = "INT đạt 50", rarity = ItemRarity.RARE,
                conditionType = "STAT", conditionTarget = "INT", conditionValue = 50,
                maxProgress = 50, iconEmoji = "🧠"
            ),
            TitleEntity(
                id = "T-STAT-ALL30", name = "Chiến Binh Toàn Diện",
                description = "Tất cả stat đạt 30.",
                condition = "Tất cả stat ≥ 30", rarity = ItemRarity.EPIC,
                conditionType = "STAT", conditionTarget = "ALL", conditionValue = 30,
                maxProgress = 30, iconEmoji = "🌟",
                statBonus = """{"CHA":10}"""
            ),

            // Debt zero titles
            TitleEntity(
                id = "T-DZ-14", name = "Kẻ Trong Sạch",
                description = "14 ngày liên tiếp không nợ.",
                condition = "0 Debt Points 14 ngày", rarity = ItemRarity.UNCOMMON,
                conditionType = "DEBT_ZERO", conditionValue = 14,
                maxProgress = 14, iconEmoji = "✨"
            ),
            TitleEntity(
                id = "T-DZ-30", name = "Liêm Khiết Tuyệt Đối",
                description = "30 ngày không nợ — bạn là chuẩn mực.",
                condition = "0 Debt Points 30 ngày", rarity = ItemRarity.EPIC,
                conditionType = "DEBT_ZERO", conditionValue = 30,
                maxProgress = 30, iconEmoji = "✨",
                statBonus = """{"WIS":10}"""
            )
        )

        titleDao.insertTitles(titles)
    }
}
