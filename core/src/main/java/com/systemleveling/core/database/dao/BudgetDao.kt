package com.systemleveling.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.systemleveling.core.database.entity.BudgetEntity
import com.systemleveling.core.model.FinanceCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE category = :category")
    suspend fun deleteBudget(category: FinanceCategory)
}
