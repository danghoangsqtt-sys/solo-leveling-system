package com.systemleveling.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.systemleveling.core.database.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY isCompleted ASC, id ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Query("SELECT * FROM courses WHERE id = :id")
    fun getCourseById(id: String): Flow<CourseEntity?>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseByIdSync(id: String): CourseEntity?

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourse(id: String)

    @Query("UPDATE courses SET parentId = :parentId WHERE id = :id")
    suspend fun updateCourseParent(id: String, parentId: String?)

    @Query("DELETE FROM courses")
    suspend fun deleteAllCourses()
}
