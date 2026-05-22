package com.systemleveling.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.systemleveling.core.database.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY acquiredDate DESC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE isStored = 0 ORDER BY acquiredDate DESC")
    fun getActiveItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE isStored = 1 ORDER BY acquiredDate DESC")
    fun getStoredItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE isStored = 0 AND category = :category ORDER BY acquiredDate DESC")
    fun getActiveItemsByCategory(category: String): Flow<List<ItemEntity>>

    @Query("SELECT COUNT(*) FROM items WHERE isStored = 0")
    fun getActiveItemCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM items WHERE isStored = 1")
    fun getStoredItemCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM items WHERE fromQuestId IS NOT NULL")
    suspend fun getQuestDropCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Query("DELETE FROM items WHERE id = :itemId")
    suspend fun deleteItem(itemId: String)

    @Query("DELETE FROM items")
    suspend fun deleteAllItems()
}
