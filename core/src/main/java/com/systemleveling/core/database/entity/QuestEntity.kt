package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.systemleveling.core.model.QuestRank
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.core.model.QuestType

@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: QuestType,
    val rank: QuestRank,
    val category: String,
    val date: Long, // timestamp of day start (midnight)
    val timeStart: String?, // "06:00"
    val timeEnd: String?, // "07:00"
    val durationMinutes: Int = 30, // time limit for the quest
    val expReward: Int,
    val goldReward: Int,
    val status: QuestStatus,
    // New fields
    val subtasks: String = "[]", // JSON array of subtask strings
    val skillPointRewards: String = "{}", // JSON map: {"skillId": spAmount}
    val penaltyDebtPoints: Int = 1, // debt points on failure
    val relatedSkillIds: String = "[]", // JSON array of skill IDs
    val relatedGoalId: String? = null,
    val droppedItemId: String? = null, // item ID if loot dropped
    val isHealthReminder: Boolean = false, // true for water/stand/sleep reminders
    val statPointRewards: String = "{}", // JSON map: {"STR": 1, "INT": 2} — per-quest specific stat gains
    val priorityScore: Int = 50 // 0-100: AI-assigned priority (100=critical, 0=optional)
)
