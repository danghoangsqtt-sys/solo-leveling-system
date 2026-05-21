package com.systemleveling.feature.library.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.CourseDao
import com.systemleveling.core.database.entity.CourseEntity
import com.systemleveling.core.model.ItemRarity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val courseDao: CourseDao
) : ViewModel() {

    private val _courses = MutableStateFlow<List<CourseEntity>>(emptyList())
    val courses: StateFlow<List<CourseEntity>> = _courses.asStateFlow()

    init {
        viewModelScope.launch {
            courseDao.getAllCourses().collect { dbCourses ->
                if (dbCourses.isEmpty()) {
                    seedMockData()
                } else {
                    _courses.value = dbCourses
                }
            }
        }
    }

    private suspend fun seedMockData() {
        val mockData = listOf(
            CourseEntity("C1", "Nhập môn AI - Thức Tỉnh", "Giáo sư X", "Hiểu cơ bản về AI & Machine Learning. Thức tỉnh năng lực lập trình dữ liệu.", 10, 3, 500, false, ItemRarity.RARE),
            CourseEntity("C2", "Mastering Kotlin Coroutines", "Google", "Chinh phục luồng thời gian và không gian trong Kotlin.", 25, 25, 2000, true, ItemRarity.EPIC),
            CourseEntity("C3", "Bí Quyết Giao Tiếp Cấp S - Cuốn Của Bậc Thầy", "Dale Carnegie", "Nâng cấp kỹ năng Charisma (Sức hút) lên cấp cao nhất.", 12, 1, 1000, false, ItemRarity.LEGENDARY),
            CourseEntity("C4", "Tiếng Anh Sinh Tồn", "System", "Hành trang tối thiểu để thám hiểm hầm ngục quốc tế.", 50, 42, 5000, false, ItemRarity.UNCOMMON)
        )
        courseDao.insertCourses(mockData)
    }

    fun completeModule(course: CourseEntity) {
        if (course.completedModules < course.totalModules) {
            val newCompleted = course.completedModules + 1
            val isCompleted = newCompleted == course.totalModules
            viewModelScope.launch {
                courseDao.updateCourse(course.copy(
                    completedModules = newCompleted,
                    isCompleted = isCompleted
                ))
            }
        }
    }
}
