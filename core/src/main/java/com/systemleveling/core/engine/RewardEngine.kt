package com.systemleveling.core.engine

import com.systemleveling.core.database.dao.ItemDao
import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.SkillDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.model.DroppedItemInfo
import com.systemleveling.core.model.QuestCategoryStatMap
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.core.model.RewardResult
import com.systemleveling.core.model.SkillLevelUpInfo
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil

/**
 * Processes rewards when a quest is completed.
 * Handles: EXP, Gold, Stat gains, Skill Points, Item drops, Level ups.
 */
@Singleton
class RewardEngine @Inject constructor(
    private val userDao: UserDao,
    private val skillDao: SkillDao,
    private val itemDao: ItemDao,
    private val questDao: QuestDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Process quest completion: update user stats, skills, potentially drop an item.
     * Returns a RewardResult for the UI to display.
     */
    suspend fun processQuestCompletion(quest: QuestEntity): RewardResult {
        val user = userDao.getUser().first() ?: throw IllegalStateException("No user found")
        val stats = userDao.getStats().first() ?: throw IllegalStateException("No stats found")

        // 1. Calculate stat gains from quest category
        val affectedStats = QuestCategoryStatMap.getStatsForCategory(quest.category)
        val statGainPerStat = ceil(quest.expReward.toDouble() / 50.0 / affectedStats.size).toInt()
        val statChanges = mutableMapOf<String, Int>()

        var newStr = stats.str
        var newInt = stats.intStat
        var newAgi = stats.agi
        var newVit = stats.vit
        var newWis = stats.wis
        var newCha = stats.cha

        for (stat in affectedStats) {
            statChanges[stat] = statGainPerStat
            when (stat) {
                "STR" -> newStr += statGainPerStat
                "INT" -> newInt += statGainPerStat
                "AGI" -> newAgi += statGainPerStat
                "VIT" -> newVit += statGainPerStat
                "WIS" -> newWis += statGainPerStat
                "CHA" -> newCha += statGainPerStat
            }
        }

        // Cap stats at user's statCap
        val cap = user.statCap
        newStr = newStr.coerceAtMost(cap)
        newInt = newInt.coerceAtMost(cap)
        newAgi = newAgi.coerceAtMost(cap)
        newVit = newVit.coerceAtMost(cap)
        newWis = newWis.coerceAtMost(cap)
        newCha = newCha.coerceAtMost(cap)

        // Save stat changes
        userDao.insertStats(stats.copy(
            str = newStr, intStat = newInt, agi = newAgi,
            vit = newVit, wis = newWis, cha = newCha
        ))

        // 2. EXP and Gold
        val newExp = user.exp + quest.expReward
        val newGold = user.gold + quest.goldReward

        // 3. Check level up (exp >= level * 1000)
        val expForNextLevel = user.level * 1000
        val leveledUp = newExp >= expForNextLevel
        val newLevel = if (leveledUp) user.level + 1 else user.level
        val finalExp = if (leveledUp) newExp - expForNextLevel else newExp

        // 4. Update streak
        val newStreak = user.streak + (if (quest.type == com.systemleveling.core.model.QuestType.DAILY) 0 else 0) // streak managed by end-of-day

        userDao.insertUser(user.copy(
            exp = finalExp,
            gold = newGold,
            level = newLevel,
            streak = newStreak
        ))

        // 5. Skill Point rewards (from quest.skillPointRewards JSON)
        val skillPointChanges = mutableMapOf<String, Int>()
        val skillLevelUps = mutableListOf<SkillLevelUpInfo>()
        try {
            val spMap = json.decodeFromString<Map<String, Int>>(quest.skillPointRewards)
            for ((skillId, spGain) in spMap) {
                val skill = skillDao.getSkillByIdSync(skillId)
                if (skill != null) {
                    val newSp = skill.currentSp + spGain
                    skillPointChanges[skill.name] = spGain

                    // Check skill level up
                    val currentMaxSp = skill.level.maxSp
                    if (newSp >= currentMaxSp) {
                        val nextLevel = com.systemleveling.core.model.SkillLevel.entries
                            .getOrNull(skill.level.ordinal + 1)
                        if (nextLevel != null) {
                            skillDao.updateSkill(skill.copy(currentSp = newSp - currentMaxSp, level = nextLevel))
                            skillLevelUps.add(SkillLevelUpInfo(skillId, skill.name, skill.level, nextLevel))
                        } else {
                            skillDao.updateSkill(skill.copy(currentSp = currentMaxSp)) // Max level
                        }
                    } else {
                        skillDao.updateSkill(skill.copy(currentSp = newSp))
                    }
                }
            }
        } catch (_: Exception) {
            // Invalid JSON, no SP rewards
        }

        // 6. Loot drop
        var droppedItemInfo: DroppedItemInfo? = null
        val droppedItem = LootTable.rollDrop(quest.rank, quest.id)
        if (droppedItem != null) {
            itemDao.insertItem(droppedItem)
            // Update quest with dropped item ID
            questDao.updateQuest(quest.copy(
                status = QuestStatus.COMPLETED,
                droppedItemId = droppedItem.id
            ))
            droppedItemInfo = DroppedItemInfo(
                itemId = droppedItem.id,
                name = droppedItem.name,
                rarity = droppedItem.rarity,
                category = droppedItem.category,
                iconId = droppedItem.iconId ?: "📦",
                loreDescription = droppedItem.loreDescription ?: ""
            )
        } else {
            questDao.updateQuest(quest.copy(status = QuestStatus.COMPLETED))
        }

        return RewardResult(
            questTitle = quest.title,
            questRank = quest.rank,
            expGained = quest.expReward,
            goldGained = quest.goldReward,
            statChanges = statChanges,
            skillPointChanges = skillPointChanges,
            droppedItem = droppedItemInfo,
            leveledUp = leveledUp,
            newLevel = if (leveledUp) newLevel else null,
            skillLeveledUp = skillLevelUps
        )
    }
}
