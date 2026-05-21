package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.systemleveling.core.model.SkillLevel

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val level: SkillLevel,
    val currentSp: Int,
    val parentId: String?, // For tree structure, null if root
    val iconId: String? = null,
    val xPos: Float = 0f,
    val yPos: Float = 0f,
    // New fields for roadmap
    val category: String = "general", // fitness, coding, language, etc.
    val goalDescription: String? = null, // What user wants to achieve
    val roadmapQuests: String = "[]", // JSON array of quest templates linked to this skill
    val totalQuestsCompleted: Int = 0, // Track how many quests contributed SP
    val createdAt: Long = System.currentTimeMillis(),
    val isAiGenerated: Boolean = false
)
