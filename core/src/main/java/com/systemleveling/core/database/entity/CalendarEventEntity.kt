package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.systemleveling.core.model.RecurrenceType
import java.util.UUID

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val emoji: String = "📌",
    val baseDateMs: Long,
    val timeStart: String? = null,
    val timeEnd: String? = null,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val reminderMinutesBefore: Int = 0,
    val colorHex: String = "#4A9EFF",
    val createdAt: Long = System.currentTimeMillis()
)
