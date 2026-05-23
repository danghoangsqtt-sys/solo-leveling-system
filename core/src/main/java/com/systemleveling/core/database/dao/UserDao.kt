package com.systemleveling.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.systemleveling.core.database.entity.StatEntity
import com.systemleveling.core.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 'local_user'")
    fun getUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = 'local_user'")
    suspend fun getUserSync(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM stats WHERE id = 'local_stats'")
    fun getStats(): Flow<StatEntity?>

    @Query("SELECT * FROM stats WHERE id = 'local_stats'")
    suspend fun getStatsSync(): StatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: StatEntity)

    @Query("UPDATE users SET promotionTier = :tier, statCap = :cap WHERE id = 'local_user'")
    suspend fun updatePromotion(tier: Int, cap: Int)

    @Query("SELECT promotionTier FROM users WHERE id = 'local_user'")
    fun getPromotionTier(): Flow<Int?>
}
