package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.systemleveling.core.model.ItemRarity

@Entity(tableName = "titles")
data class TitleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val condition: String, // Human-readable condition
    val rarity: ItemRarity,
    val isAcquired: Boolean = false,
    val isEquipped: Boolean = false,
    val progress: Int = 0,
    val maxProgress: Int = 100,
    // New fields for automated tracking
    val conditionType: String = "MANUAL", // QUEST_COUNT, STREAK, DEBT_ZERO, LEVEL, SKILL_LEVEL, STAT, CATEGORY_COUNT
    val conditionTarget: String = "", // Target entity (e.g., "fitness", "STR", skill ID)
    val conditionValue: Int = 0, // Required value to unlock
    val iconEmoji: String = "🏆",
    val statBonus: String = "{}", // JSON: {"STR": 5} — permanent stat boost on equip
    val unlockedAt: Long? = null
)
