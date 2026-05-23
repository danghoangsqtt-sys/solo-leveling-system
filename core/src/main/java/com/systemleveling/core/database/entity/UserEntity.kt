package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "local_user",
    val nickname: String,
    val avatarUri: String?,
    val characterClass: String,
    val level: Int = 1,
    val exp: Int = 0,
    val gold: Int = 0,
    val gem: Int = 0,
    val streak: Int = 0,
    val debtPoints: Int = 0,
    val promotionTier: Int = 0,
    val statCap: Int = 100,
    val profession: String = "",
    val personalDescription: String = "",
    val generatedAvatarBase64: String? = null
)
