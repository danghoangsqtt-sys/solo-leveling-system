package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.systemleveling.core.model.CourseContentType
import java.util.UUID

import androidx.room.Index

@Entity(
    tableName = "lessons",
    indices = [Index(value = ["courseId"], name = "idx_lessons_courseId")]
)
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
