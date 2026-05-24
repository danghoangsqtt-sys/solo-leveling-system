package com.systemleveling.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.systemleveling.core.database.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY orderIndex ASC, createdAt ASC")
    fun getLessonsForCourse(courseId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY orderIndex ASC, createdAt ASC")
    suspend fun getLessonsForCourseSync(courseId: String): List<LessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Query("DELETE FROM lessons WHERE id = :id")
    suspend fun deleteLesson(id: String)

    @Query("DELETE FROM lessons WHERE courseId = :courseId")
    suspend fun deleteLessonsForCourse(courseId: String)

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId AND isCompleted = 1")
    fun getCompletedCount(courseId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId")
    fun getTotalCount(courseId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId AND isCompleted = 1")
    suspend fun getCompletedCountSync(courseId: String): Int

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId")
    suspend fun getTotalCountSync(courseId: String): Int

    @Query("DELETE FROM lessons")
    suspend fun deleteAllLessons()
}
