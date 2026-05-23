package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class DebtType {
    BORROWED, // Bản thân nợ người khác (Cần trả)
    LENT      // Người khác nợ bản thân (Cần thu hồi)
}

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val personName: String,
    val amount: Long,
    val type: DebtType,
    val note: String = "",
    val isPaid: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
