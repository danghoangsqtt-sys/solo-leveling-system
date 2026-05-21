package com.systemleveling.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.systemleveling.core.database.entity.TitleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TitleDao {
    @Query("SELECT * FROM titles ORDER BY isAcquired DESC, id ASC")
    fun getAllTitles(): Flow<List<TitleEntity>>

    @Query("SELECT * FROM titles WHERE isAcquired = 0 ORDER BY progress * 100 / maxProgress DESC")
    suspend fun getUnacquiredTitles(): List<TitleEntity>

    @Query("SELECT * FROM titles WHERE isAcquired = 0 AND progress >= maxProgress * 0.8")
    suspend fun getNearCompletionTitles(): List<TitleEntity>

    @Query("SELECT * FROM titles WHERE conditionType = :type AND isAcquired = 0")
    suspend fun getUnacquiredByType(type: String): List<TitleEntity>

    @Query("SELECT * FROM titles WHERE isEquipped = 1 LIMIT 1")
    suspend fun getEquippedTitle(): TitleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTitle(title: TitleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTitles(titles: List<TitleEntity>)

    @Update
    suspend fun updateTitle(title: TitleEntity)

    @Update
    suspend fun updateTitles(titles: List<TitleEntity>)

    @Query("UPDATE titles SET isEquipped = 0 WHERE isEquipped = 1")
    suspend fun unequipAll()

    @Query("UPDATE titles SET isEquipped = 1 WHERE id = :titleId")
    suspend fun equipTitle(titleId: String)

    @Query("SELECT COUNT(*) FROM titles WHERE isAcquired = 1")
    suspend fun getAcquiredCount(): Int
}
