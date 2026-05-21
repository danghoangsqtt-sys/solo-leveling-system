package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.systemleveling.core.model.FinanceCategory
import com.systemleveling.core.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Long,
    val type: TransactionType,
    val category: FinanceCategory,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)
