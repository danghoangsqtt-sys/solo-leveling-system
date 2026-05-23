package com.systemleveling.feature.library.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.systemleveling.core.database.AppDatabase
import com.systemleveling.core.database.dao.CourseDao
import com.systemleveling.core.database.dao.LessonDao
import com.systemleveling.core.database.entity.CourseEntity
import com.systemleveling.core.database.entity.LessonEntity
import com.systemleveling.core.model.CourseContentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseDao: CourseDao,
    private val lessonDao: LessonDao,
    private val database: AppDatabase
) : ViewModel() {

    private val courseId: String = checkNotNull(savedStateHandle["courseId"])

    val course: StateFlow<CourseEntity?> = courseDao.getCourseById(courseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val lessons: StateFlow<List<LessonEntity>> = lessonDao.getLessonsForCourse(courseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _viewingLesson = MutableStateFlow<LessonEntity?>(null)
    val viewingLesson: StateFlow<LessonEntity?> = _viewingLesson.asStateFlow()

    fun openLesson(lesson: LessonEntity) {
        _viewingLesson.value = lesson
        // Auto-mark as completed when opened
        if (!lesson.isCompleted) {
            viewModelScope.launch {
                lessonDao.updateLesson(lesson.copy(isCompleted = true))
                syncCourseProgress()
            }
        }
    }

    fun closeViewer() {
        _viewingLesson.value = null
    }

    fun addLesson(title: String, contentUrl: String, contentType: CourseContentType, notes: String = "") {
        viewModelScope.launch {
            val totalBefore = lessonDao.getTotalCountSync(courseId)
            lessonDao.insertLesson(
                LessonEntity(
                    id = UUID.randomUUID().toString(),
                    courseId = courseId,
                    title = title,
                    contentUrl = contentUrl,
                    contentType = contentType,
                    orderIndex = totalBefore,
                    notes = notes
                )
            )
            syncCourseProgress()
        }
    }

    fun deleteLesson(lesson: LessonEntity) {
        viewModelScope.launch {
            lessonDao.deleteLesson(lesson.id)
            syncCourseProgress()
        }
    }

    fun toggleLesson(lesson: LessonEntity) {
        viewModelScope.launch {
            lessonDao.updateLesson(lesson.copy(isCompleted = !lesson.isCompleted))
            syncCourseProgress()
        }
    }

    fun editLesson(lesson: LessonEntity, newTitle: String, newUrl: String, newType: CourseContentType, newNotes: String) {
        viewModelScope.launch {
            lessonDao.updateLesson(lesson.copy(
                title = newTitle.trim().ifBlank { lesson.title },
                contentUrl = newUrl.trim(),
                contentType = newType,
                notes = newNotes.trim()
            ))
        }
    }

    fun moveLessonUp(lesson: LessonEntity) {
        viewModelScope.launch {
            database.withTransaction {
                val current = lessonDao.getLessonsForCourseSync(courseId)
                val idx = current.indexOfFirst { it.id == lesson.id }
                if (idx <= 0) return@withTransaction
                val prev = current[idx - 1]
                lessonDao.updateLesson(lesson.copy(orderIndex = prev.orderIndex))
                lessonDao.updateLesson(prev.copy(orderIndex = lesson.orderIndex))
            }
        }
    }

    fun moveLessonDown(lesson: LessonEntity) {
        viewModelScope.launch {
            database.withTransaction {
                val current = lessonDao.getLessonsForCourseSync(courseId)
                val idx = current.indexOfFirst { it.id == lesson.id }
                if (idx < 0 || idx >= current.size - 1) return@withTransaction
                val next = current[idx + 1]
                lessonDao.updateLesson(lesson.copy(orderIndex = next.orderIndex))
                lessonDao.updateLesson(next.copy(orderIndex = lesson.orderIndex))
            }
        }
    }

    private suspend fun syncCourseProgress() {
        database.withTransaction {
            val currentCourse = courseDao.getCourseByIdSync(courseId) ?: return@withTransaction
            val completed = lessonDao.getCompletedCountSync(courseId)
            val total = lessonDao.getTotalCountSync(courseId)
            courseDao.updateCourse(
                currentCourse.copy(
                    completedModules = completed,
                    totalModules = total,
                    isCompleted = total > 0 && completed == total
                )
            )
        }
    }
}
