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
import com.systemleveling.core.model.SkillLevel
import com.systemleveling.core.model.SkillLevelUpInfo
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Processes rewards when a quest is completed.
 *
 * Stat resolution order:
 *   1. quest.statPointRewards (AI-specified per-quest — precise and calibrated)
 *   2. QuestCategoryStatMap fallback (legacy/health quests) with capped minimal gain
 *
 * SP resolution:
 *   - quest.skillPointRewards maps child skill IDs → SP amount
 *   - Handles skill level-up when SP crosses SkillLevel.maxSp threshold
 */
@Singleton
class RewardEngine @Inject constructor(
    private val userDao: UserDao,
    private val skillDao: SkillDao,
    private val itemDao: ItemDao,
    private val questDao: QuestDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun processQuestCompletion(quest: QuestEntity): RewardResult {
        val user = userDao.getUser().first()
            ?: com.systemleveling.core.database.entity.UserEntity(
                id = "local_user", nickname = "Player", avatarUri = null, characterClass = "Unknown"
            )
        val stats = userDao.getStats().first()
            ?: com.systemleveling.core.database.entity.StatEntity(id = "local_stats")

        // ── 1. Stat gains ────────────────────────────────────────────────────
        val statChanges = mutableMapOf<String, Int>()
        var newStr = stats.str; var newInt = stats.intStat; var newAgi = stats.agi
        var newVit = stats.vit; var newWis = stats.wis; var newCha = stats.cha

        val perQuestStats = parseStatRewards(quest.statPointRewards)
        val statSource = if (perQuestStats.isNotEmpty()) {
            // AI-specified — trust and apply directly
            perQuestStats
        } else {
            // Fallback: category map but cap at 1 point total (health reminders, legacy)
            val affected = QuestCategoryStatMap.getStatsForCategory(quest.category)
            if (affected.isNotEmpty() && !quest.isHealthReminder) {
                mapOf(affected.first() to 1) // only 1 point to 1 stat max
            } else {
                emptyMap()
            }
        }

        for ((stat, gain) in statSource) {
            statChanges[stat] = gain
            when (stat.uppercase()) {
                "STR" -> newStr += gain
                "INT" -> newInt += gain
                "AGI" -> newAgi += gain
                "VIT" -> newVit += gain
                "WIS" -> newWis += gain
                "CHA" -> newCha += gain
            }
        }

        // Cap at user's statCap
        val cap = user.statCap
        newStr = newStr.coerceAtMost(cap); newInt = newInt.coerceAtMost(cap)
        newAgi = newAgi.coerceAtMost(cap); newVit = newVit.coerceAtMost(cap)
        newWis = newWis.coerceAtMost(cap); newCha = newCha.coerceAtMost(cap)

        userDao.insertStats(stats.copy(
            str = newStr, intStat = newInt, agi = newAgi,
            vit = newVit, wis = newWis, cha = newCha
        ))

        // ── 2. EXP, Gold, Level ──────────────────────────────────────────────
        val newExp = user.exp + quest.expReward
        val newGold = user.gold + quest.goldReward
        val expForNextLevel = user.level * 1000
        val leveledUp = newExp >= expForNextLevel
        val newLevel = if (leveledUp) user.level + 1 else user.level
        val finalExp = if (leveledUp) newExp - expForNextLevel else newExp

        userDao.insertUser(user.copy(exp = finalExp, gold = newGold, level = newLevel))

        // ── 3. Skill Point awards ────────────────────────────────────────────
        val skillPointChanges = mutableMapOf<String, Int>()
        val skillLevelUps = mutableListOf<SkillLevelUpInfo>()
        try {
            val spMap = json.decodeFromString(
                MapSerializer(String.serializer(), Int.serializer()), quest.skillPointRewards
            )
            for ((skillId, spGain) in spMap) {
                val skill = skillDao.getSkillByIdSync(skillId) ?: continue
                val newSp = skill.currentSp + spGain
                skillPointChanges[skill.name] = spGain

                if (newSp >= skill.level.maxSp) {
                    val nextLevel = SkillLevel.entries.getOrNull(skill.level.ordinal + 1)
                    if (nextLevel != null) {
                        skillDao.updateSkill(skill.copy(
                            currentSp = newSp - skill.level.maxSp,
                            level = nextLevel,
                            totalQuestsCompleted = skill.totalQuestsCompleted + 1
                        ))
                        skillLevelUps.add(SkillLevelUpInfo(skillId, skill.name, skill.level, nextLevel))
                    } else {
                        skillDao.updateSkill(skill.copy(
                            currentSp = skill.level.maxSp,
                            totalQuestsCompleted = skill.totalQuestsCompleted + 1
                        ))
                    }
                } else {
                    skillDao.updateSkill(skill.copy(
                        currentSp = newSp,
                        totalQuestsCompleted = skill.totalQuestsCompleted + 1
                    ))
                }
            }
        } catch (_: Exception) { /* malformed JSON, skip SP */ }

        // ── 4. Loot drop ─────────────────────────────────────────────────────
        var droppedItemInfo: DroppedItemInfo? = null
        val noPriorQuestDrops = itemDao.getQuestDropCount() == 0
        // First-quest guarantee: always drop on first ever completion
        val droppedItem = if (noPriorQuestDrops) {
            LootTable.rollGuaranteedDrop(quest.id)
        } else {
            LootTable.rollDrop(quest.rank, quest.id)
        }
        if (droppedItem != null) {
            itemDao.insertItem(droppedItem)
            questDao.updateQuest(quest.copy(status = QuestStatus.COMPLETED, droppedItemId = droppedItem.id))
            droppedItemInfo = DroppedItemInfo(
                itemId = droppedItem.id, name = droppedItem.name, rarity = droppedItem.rarity,
                category = droppedItem.category, iconId = droppedItem.iconId ?: "📦",
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

    private fun parseStatRewards(raw: String): Map<String, Int> {
        if (raw.isBlank() || raw == "{}") return emptyMap()
        return try {
            json.decodeFromString(MapSerializer(String.serializer(), Int.serializer()), raw)
                .filter { (_, v) -> v > 0 }
        } catch (_: Exception) { emptyMap() }
    }
}
