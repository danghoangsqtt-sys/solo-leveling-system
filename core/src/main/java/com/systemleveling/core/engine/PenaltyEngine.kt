package com.systemleveling.core.engine

import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.model.QuestStatus
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil
import kotlin.math.max

/**
 * Penalty engine: handles consequences for failed/expired quests.
 *
 * Thresholds (from V2 Prompt):
 * - Each failed quest: +penaltyDebtPoints, -30% EXP of that quest
 * - ≥3 Debt Points: Penalty Zone warning (UI red border)
 * - ≥5 Debt Points: Lose 10% Gold
 * - ≥10 Debt Points (14 days unpaid): Level Down + System Warning
 */
@Singleton
class PenaltyEngine @Inject constructor(
    private val userDao: UserDao,
    private val questDao: QuestDao
) {
    /**
     * Process a single quest failure.
     */
    suspend fun processQuestFailure(quest: QuestEntity) {
        val user = userDao.getUser().first() ?: return

        // Mark quest as FAILED
        questDao.updateQuest(quest.copy(status = QuestStatus.FAILED))

        // Add debt points
        val newDebt = user.debtPoints + quest.penaltyDebtPoints

        // Lose 30% EXP of the quest
        val expPenalty = ceil(quest.expReward * 0.3).toInt()
        val newExp = max(0, user.exp - expPenalty)

        // Gold penalty at threshold
        var newGold = user.gold
        if (newDebt >= 5 && user.debtPoints < 5) {
            // Just crossed the 5 threshold — lose 10% gold
            newGold = (user.gold * 0.9).toInt()
        }

        // Level down at threshold
        var newLevel = user.level
        if (newDebt >= 10 && user.debtPoints < 10) {
            newLevel = max(1, user.level - 1)
        }

        userDao.insertUser(user.copy(
            exp = newExp,
            gold = newGold,
            debtPoints = newDebt,
            level = newLevel
        ))
    }

    /**
     * Process end of day: find all uncompleted quests for today and apply penalties.
     * Called by EndOfDayWorker at 22:00.
     *
     * @param dayStart start of day timestamp (midnight)
     * @param dayEnd end of day timestamp (next midnight)
     * @return number of quests that failed
     */
    suspend fun processEndOfDay(dayStart: Long, dayEnd: Long): Int {
        val pendingQuests = questDao.getPendingQuestsByDateSync(dayStart, dayEnd)
        var failCount = 0

        for (quest in pendingQuests) {
            // Skip health reminders that don't carry penalties
            if (quest.isHealthReminder && quest.penaltyDebtPoints == 0) {
                questDao.updateQuest(quest.copy(status = QuestStatus.EXPIRED))
                continue
            }

            processQuestFailure(quest)
            failCount++
        }

        // Update streak
        val user = userDao.getUser().first() ?: return failCount
        val completedCount = questDao.getCompletedQuestsByDateSync(dayStart, dayEnd).size
        val totalCount = questDao.getQuestsByDateSync(dayStart, dayEnd)
            .filter { !it.isHealthReminder }.size

        // If completion rate >= 70%, keep streak; otherwise reset
        val completionRate = if (totalCount > 0) completedCount.toDouble() / totalCount else 0.0
        val newStreak = if (completionRate >= 0.7) user.streak + 1 else 0

        userDao.insertUser(user.copy(streak = newStreak))
        return failCount
    }

    /**
     * Reduce debt points (e.g., when completing penalty quests or using Phoenix Feather item).
     */
    suspend fun reduceDebt(amount: Int) {
        val user = userDao.getUser().first() ?: return
        val newDebt = max(0, user.debtPoints - amount)
        userDao.insertUser(user.copy(debtPoints = newDebt))
    }
}
