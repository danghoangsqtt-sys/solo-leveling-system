package com.systemleveling.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.systemleveling.core.model.FinanceCategory

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val category: FinanceCategory,
    val limitAmount: Long
)
