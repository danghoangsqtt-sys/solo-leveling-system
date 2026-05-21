package com.systemleveling.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.systemleveling.core.database.entity.DailySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySummaryDao {
    @Query("SELECT * FROM daily_summaries ORDER BY date DESC")
    fun getAllSummaries(): Flow<List<DailySummaryEntity>>

    @Query("SELECT * FROM daily_summaries WHERE date >= :dayStart AND date < :dayEnd LIMIT 1")
    fun getSummaryByDate(dayStart: Long, dayEnd: Long): Flow<DailySummaryEntity?>

    @Query("SELECT * FROM daily_summaries WHERE date >= :dayStart AND date < :dayEnd LIMIT 1")
    suspend fun getSummaryByDateSync(dayStart: Long, dayEnd: Long): DailySummaryEntity?

    @Query("SELECT * FROM daily_summaries ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentSummaries(limit: Int): List<DailySummaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: DailySummaryEntity)

    @Update
    suspend fun updateSummary(summary: DailySummaryEntity)
}
