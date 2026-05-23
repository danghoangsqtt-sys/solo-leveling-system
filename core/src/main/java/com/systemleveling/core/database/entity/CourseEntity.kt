package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.systemleveling.core.model.CourseContentType
import com.systemleveling.core.model.ItemRarity

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val description: String,
    val totalModules: Int,
    val completedModules: Int = 0,
    val rewardExp: Long,
    val isCompleted: Boolean = false,
    val rarity: ItemRarity = ItemRarity.COMMON,
    val contentUrl: String = "",
    val contentType: CourseContentType = CourseContentType.GENERAL,
    val category: String = "",
    val isPinned: Boolean = false,
    val parentId: String? = null
)
