package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Daily summary report — generated at end of day.
 * Contains aggregated stats, quest results, and AI-generated journal.
 */
@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    @PrimaryKey val id: String, // "DS-2026-05-21"
    val date: Long, // midnight timestamp

    // Quest stats
    val totalQuests: Int = 0,
    val completedQuests: Int = 0,
    val failedQuests: Int = 0,
    val completionRate: Double = 0.0,

    // Rewards earned today
    val expEarned: Int = 0,
    val goldEarned: Int = 0,
    val itemsDropped: Int = 0,

    // Stat changes today (JSON: {"STR": 5, "INT": 3})
    val statChanges: String = "{}",

    // Skill progress today (JSON: {"skillName": spGained})
    val skillProgress: String = "{}",

    // Penalty info
    val debtPointsGained: Int = 0,
    val currentDebtTotal: Int = 0,

    // Streak
    val currentStreak: Int = 0,

    // AI-generated journal/summary
    val aiJournalContent: String = "",
    val userNotes: String = "",

    // Tomorrow plan (JSON array of todo items)
    val tomorrowPlan: String = "[]",

    // Timestamp of when summary was generated
    val generatedAt: Long = System.currentTimeMillis()
)
