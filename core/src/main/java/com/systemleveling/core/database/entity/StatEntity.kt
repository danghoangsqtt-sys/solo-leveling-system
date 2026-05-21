package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stats")
data class StatEntity(
    @PrimaryKey val id: String = "local_stats",
    val str: Int = 10,
    val intStat: Int = 10,
    val agi: Int = 10,
    val vit: Int = 10,
    val wis: Int = 10,
    val cha: Int = 10
)
