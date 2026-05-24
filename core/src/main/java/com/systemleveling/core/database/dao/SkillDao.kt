package com.systemleveling.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.systemleveling.core.database.entity.SkillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills")
    fun getAllSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE category = :category")
    fun getSkillsByCategory(category: String): Flow<List<SkillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)

    @Update
    suspend fun updateSkill(skill: SkillEntity)

    @Query("SELECT * FROM skills WHERE id = :skillId")
    suspend fun getSkillByIdSync(skillId: String): SkillEntity?

    @Query("SELECT * FROM skills")
    suspend fun getAllSkillsSync(): List<SkillEntity>

    @Query("SELECT COUNT(*) FROM skills")
    suspend fun getSkillCount(): Int

    @Query("SELECT * FROM skills WHERE isAiGenerated = 1")
    suspend fun getAiGeneratedSkills(): List<SkillEntity>

    @Query("SELECT * FROM skills WHERE parentId IS NULL")
    fun getParentSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE parentId = :parentId")
    fun getChildSkills(parentId: String): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE parentId IS NULL")
    suspend fun getParentSkillsSync(): List<SkillEntity>

    @Query("SELECT * FROM skills WHERE parentId = :parentId")
    suspend fun getChildSkillsSync(parentId: String): List<SkillEntity>

    @Query("DELETE FROM skills")
    suspend fun deleteAllSkills()

    @Transaction
    suspend fun replaceSkills(skills: List<SkillEntity>) {
        deleteAllSkills()
        insertSkills(skills)
    }
}
