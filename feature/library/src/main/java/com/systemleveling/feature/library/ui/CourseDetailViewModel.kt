package com.systemleveling.feature.library.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val lessonDao: LessonDao
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

    private suspend fun syncCourseProgress() {
        val currentCourse = course.value ?: return
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
