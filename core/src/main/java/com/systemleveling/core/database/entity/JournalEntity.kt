package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.systemleveling.core.model.Mood

@Entity(tableName = "journals")
data class JournalEntity(
    @PrimaryKey val id: String,
    val content: String,
    val mood: Mood,
    val timestamp: Long = System.currentTimeMillis(),
    // New fields for AI integration
    val isAiGenerated: Boolean = false,
    val questSummary: String? = null, // Brief AI summary of today's quests
    val statSnapshot: String? = null, // JSON snapshot of stats at time of entry
    val dailySummaryId: String? = null // Link to DailySummaryEntity
)
