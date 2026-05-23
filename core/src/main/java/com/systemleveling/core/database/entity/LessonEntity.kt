package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.systemleveling.core.model.CourseContentType
import java.util.UUID

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val courseId: String,
    val title: String,
    val contentUrl: String = "",
    val contentType: CourseContentType = CourseContentType.GENERAL,
    val orderIndex: Int = 0,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
