package com.systemleveling.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.model.QuestStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests ORDER BY date ASC, timeStart ASC")
    fun getAllQuests(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE date >= :dayStart AND date < :dayEnd ORDER BY timeStart ASC")
    fun getQuestsByDate(dayStart: Long, dayEnd: Long): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE date >= :dayStart AND date < :dayEnd AND status = :status ORDER BY timeStart ASC")
    fun getQuestsByDateAndStatus(dayStart: Long, dayEnd: Long, status: QuestStatus): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE date >= :dayStart AND date < :dayEnd AND status = 'COMPLETED'")
    suspend fun getCompletedQuestsByDateSync(dayStart: Long, dayEnd: Long): List<QuestEntity>

    @Query("SELECT * FROM quests WHERE date >= :dayStart AND date < :dayEnd AND (status = 'FAILED' OR status = 'EXPIRED')")
    suspend fun getFailedQuestsByDateSync(dayStart: Long, dayEnd: Long): List<QuestEntity>

    @Query("SELECT * FROM quests WHERE date >= :dayStart AND date < :dayEnd AND (status = 'PENDING' OR status = 'IN_PROGRESS')")
    suspend fun getPendingQuestsByDateSync(dayStart: Long, dayEnd: Long): List<QuestEntity>

    @Query("SELECT * FROM quests WHERE date >= :dayStart AND date < :dayEnd")
    suspend fun getQuestsByDateSync(dayStart: Long, dayEnd: Long): List<QuestEntity>

    @Query("SELECT COUNT(*) FROM quests WHERE date >= :dayStart AND date < :dayEnd")
    suspend fun getQuestCountByDate(dayStart: Long, dayEnd: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<QuestEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: QuestEntity)

    @Update
    suspend fun updateQuest(quest: QuestEntity)

    @Query("DELETE FROM quests WHERE date >= :dayStart AND date < :dayEnd")
    suspend fun deleteQuestsByDate(dayStart: Long, dayEnd: Long)

    @Query("DELETE FROM quests WHERE id = :questId")
    suspend fun deleteQuest(questId: String)

    @Query("SELECT COUNT(*) FROM quests WHERE status = 'COMPLETED'")
    suspend fun getCompletedQuestCountAll(): Int

    @Query("SELECT COUNT(*) FROM quests WHERE status = 'COMPLETED' AND category = :category")
    suspend fun getCompletedQuestCountByCategory(category: String): Int
}
