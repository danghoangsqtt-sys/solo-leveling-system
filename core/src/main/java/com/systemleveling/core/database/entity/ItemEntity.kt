package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.systemleveling.core.model.ItemCategory
import com.systemleveling.core.model.ItemRarity

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val loreDescription: String? = null, // RPG-style lore text
    val rarity: ItemRarity,
    val category: ItemCategory,
    val quantity: Int,
    val iconId: String? = null,
    val acquiredDate: Long = System.currentTimeMillis(),
    // Effect system
    val effectType: String? = null, // "EXP_BOOST", "SP_BOOST", "DEBT_CLEAR", "STAT_BOOST"
    val effectValue: Int? = null, // e.g. 50 for +50 EXP
    val effectTarget: String? = null, // stat name or skill ID for targeted effects
    // Origin tracking
    val fromQuestId: String? = null,
    // Storage state — false = active inventory, true = archived/stored
    val isStored: Boolean = false
)
