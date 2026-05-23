package com.systemleveling.core.sync

import com.systemleveling.core.database.entity.DailySummaryEntity
import com.systemleveling.core.database.entity.ItemEntity
import com.systemleveling.core.database.entity.SkillEntity
import com.systemleveling.core.database.entity.StatEntity
import com.systemleveling.core.database.entity.UserEntity
import com.systemleveling.core.model.ItemCategory
import com.systemleveling.core.model.ItemRarity
import com.systemleveling.core.model.SkillLevel
import kotlinx.serialization.Serializable

@Serializable
data class GameStateSyncPayload(
    val device_id: String,
    val player_json: String,
    val updated_at: String? = null
)

@Serializable
data class PlayerSnapshot(
    val user: UserSyncData,
    val stats: StatSyncData,
    val skills: List<SkillSyncData>,
    val items: List<ItemSyncData>
)

@Serializable
data class UserSyncData(
    val id: String,
    val nickname: String,
    val avatarUri: String?,
    val characterClass: String,
    val level: Int,
    val exp: Int,
    val gold: Int,
    val gem: Int,
    val streak: Int,
    val debtPoints: Int,
    val promotionTier: Int,
    val statCap: Int
)

@Serializable
data class StatSyncData(
    val id: String,
    val str: Int,
    val intStat: Int,
    val agi: Int,
    val vit: Int,
    val wis: Int,
    val cha: Int
)

@Serializable
data class SkillSyncData(
    val id: String,
    val name: String,
    val description: String,
    val level: String,
    val currentSp: Int,
    val parentId: String?,
    val iconId: String?,
    val xPos: Float,
    val yPos: Float,
    val category: String,
    val goalDescription: String?,
    val roadmapQuests: String,
    val totalQuestsCompleted: Int,
    val createdAt: Long,
    val isAiGenerated: Boolean
)

@Serializable
data class ItemSyncData(
    val id: String,
    val name: String,
    val description: String,
    val loreDescription: String?,
    val rarity: String,
    val category: String,
    val quantity: Int,
    val iconId: String?,
    val acquiredDate: Long,
    val effectType: String?,
    val effectValue: Int?,
    val effectTarget: String?,
    val fromQuestId: String?,
    val isStored: Boolean
)

// ── Entity → SyncData ────────────────────────────────────────────────────────

fun UserEntity.toSyncData() = UserSyncData(
    id = id, nickname = nickname, avatarUri = avatarUri,
    characterClass = characterClass, level = level, exp = exp,
    gold = gold, gem = gem, streak = streak, debtPoints = debtPoints,
    promotionTier = promotionTier, statCap = statCap
)

fun StatEntity.toSyncData() = StatSyncData(
    id = id, str = str, intStat = intStat, agi = agi,
    vit = vit, wis = wis, cha = cha
)

fun SkillEntity.toSyncData() = SkillSyncData(
    id = id, name = name, description = description, level = level.name,
    currentSp = currentSp, parentId = parentId, iconId = iconId,
    xPos = xPos, yPos = yPos, category = category, goalDescription = goalDescription,
    roadmapQuests = roadmapQuests, totalQuestsCompleted = totalQuestsCompleted,
    createdAt = createdAt, isAiGenerated = isAiGenerated
)

fun ItemEntity.toSyncData() = ItemSyncData(
    id = id, name = name, description = description, loreDescription = loreDescription,
    rarity = rarity.name, category = category.name, quantity = quantity,
    iconId = iconId, acquiredDate = acquiredDate, effectType = effectType,
    effectValue = effectValue, effectTarget = effectTarget,
    fromQuestId = fromQuestId, isStored = isStored
)

// ── SyncData → Entity ────────────────────────────────────────────────────────

fun UserSyncData.toEntity() = UserEntity(
    id = id, nickname = nickname, avatarUri = avatarUri,
    characterClass = characterClass, level = level, exp = exp,
    gold = gold, gem = gem, streak = streak, debtPoints = debtPoints,
    promotionTier = promotionTier, statCap = statCap
)

fun StatSyncData.toEntity() = StatEntity(
    id = id, str = str, intStat = intStat, agi = agi,
    vit = vit, wis = wis, cha = cha
)

fun SkillSyncData.toEntity() = SkillEntity(
    id = id, name = name, description = description,
    level = runCatching { SkillLevel.valueOf(level) }.getOrDefault(SkillLevel.NOVICE),
    currentSp = currentSp, parentId = parentId, iconId = iconId,
    xPos = xPos, yPos = yPos, category = category, goalDescription = goalDescription,
    roadmapQuests = roadmapQuests, totalQuestsCompleted = totalQuestsCompleted,
    createdAt = createdAt, isAiGenerated = isAiGenerated
)

fun ItemSyncData.toEntity() = ItemEntity(
    id = id, name = name, description = description, loreDescription = loreDescription,
    rarity = runCatching { ItemRarity.valueOf(rarity) }.getOrDefault(ItemRarity.COMMON),
    category = runCatching { ItemCategory.valueOf(category) }.getOrDefault(ItemCategory.COLLECTIBLE),
    quantity = quantity, iconId = iconId, acquiredDate = acquiredDate,
    effectType = effectType, effectValue = effectValue, effectTarget = effectTarget,
    fromQuestId = fromQuestId, isStored = isStored
)

// ── Daily History Sync ────────────────────────────────────────────────────────

/**
 * Supabase row payload for the `daily_history` table.
 * Primary key: (device_id, date_key). Upsert via Prefer: resolution=merge-duplicates.
 */
@Serializable
data class DailyHistoryPayload(
    val device_id: String,
    val date_key: String,       // "YYYY-MM-DD" for easy range queries
    val summary_json: String    // JSON-encoded DailySummaryHistoryItem
)

@Serializable
data class DailySummaryHistoryItem(
    val id: String,
    val date: Long,
    val totalQuests: Int,
    val completedQuests: Int,
    val failedQuests: Int,
    val completionRate: Double,
    val expEarned: Int,
    val goldEarned: Int,
    val itemsDropped: Int,
    val statChanges: String,
    val skillProgress: String,
    val debtPointsGained: Int,
    val currentDebtTotal: Int,
    val currentStreak: Int,
    val aiJournalContent: String,
    val userNotes: String,
    val tomorrowPlan: String,
    val generatedAt: Long
)

fun DailySummaryEntity.toHistoryItem() = DailySummaryHistoryItem(
    id = id, date = date, totalQuests = totalQuests, completedQuests = completedQuests,
    failedQuests = failedQuests, completionRate = completionRate, expEarned = expEarned,
    goldEarned = goldEarned, itemsDropped = itemsDropped, statChanges = statChanges,
    skillProgress = skillProgress, debtPointsGained = debtPointsGained,
    currentDebtTotal = currentDebtTotal, currentStreak = currentStreak,
    aiJournalContent = aiJournalContent, userNotes = userNotes,
    tomorrowPlan = tomorrowPlan, generatedAt = generatedAt
)

fun DailySummaryHistoryItem.toEntity() = DailySummaryEntity(
    id = id, date = date, totalQuests = totalQuests, completedQuests = completedQuests,
    failedQuests = failedQuests, completionRate = completionRate, expEarned = expEarned,
    goldEarned = goldEarned, itemsDropped = itemsDropped, statChanges = statChanges,
    skillProgress = skillProgress, debtPointsGained = debtPointsGained,
    currentDebtTotal = currentDebtTotal, currentStreak = currentStreak,
    aiJournalContent = aiJournalContent, userNotes = userNotes,
    tomorrowPlan = tomorrowPlan, generatedAt = generatedAt
)
