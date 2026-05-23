package com.systemleveling.feature.library.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.CourseDao
import com.systemleveling.core.database.dao.LessonDao
import com.systemleveling.core.database.entity.CourseEntity
import com.systemleveling.core.database.entity.LessonEntity
import com.systemleveling.core.model.CourseContentType
import com.systemleveling.core.model.ItemRarity
import com.systemleveling.core.settings.SettingsManager
import com.systemleveling.core.sync.AppwriteSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val courseDao: CourseDao,
    private val lessonDao: LessonDao,
    private val settingsManager: SettingsManager,
    private val appwriteSyncService: AppwriteSyncService
) : ViewModel() {

    private val _allCourses = MutableStateFlow<List<CourseEntity>>(emptyList())
    val selectedCategory = MutableStateFlow<CourseContentType?>(null)

    // ── Folder expand / collapse ──────────────────────────────────────────────
    val expandedFolderIds = MutableStateFlow<Set<String>>(emptySet())

    // Set of course IDs that have at least one child (are "folders")
    val folderIds: StateFlow<Set<String>> = _allCourses
        .map { courses -> courses.mapNotNull { it.parentId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // ── Multi-select ──────────────────────────────────────────────────────────
    val selectedCourseIds = MutableStateFlow<Set<String>>(emptySet())
    val isSelectMode: StateFlow<Boolean> = selectedCourseIds
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ── Sync state ────────────────────────────────────────────────────────────
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    // ── Course map for depth computation and cycle detection ──────────────────
    val courseMap: StateFlow<Map<String, CourseEntity>> = _allCourses
        .map { list -> list.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // ── Recursive display list — deep folder nesting ──────────────────────────
    val displayedCourses: StateFlow<List<CourseEntity>> = combine(
        _allCourses, selectedCategory, expandedFolderIds
    ) { all, category, expanded ->
        fun collectVisible(parentId: String?): List<CourseEntity> {
            val children = all.filter { it.parentId == parentId }
                .let { list ->
                    if (parentId == null && category != null)
                        list.filter { it.contentType == category }
                    else list
                }
                .sortedWith(
                    compareByDescending<CourseEntity> { it.isPinned }
                        .thenBy { it.isCompleted }
                        .thenBy { it.title }
                )
            return buildList {
                children.forEach { course ->
                    add(course)
                    if (course.id in expanded) addAll(collectVisible(course.id))
                }
            }
        }
        collectVisible(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            var hasSeeded = settingsManager.isLibrarySeeded()
            courseDao.getAllCourses().collect { dbCourses ->
                if (dbCourses.isEmpty() && !hasSeeded) {
                    seedDefaultCourses()
                    settingsManager.markLibrarySeeded()
                    hasSeeded = true
                } else {
                    _allCourses.value = dbCourses
                }
            }
        }
    }

    private suspend fun seedDefaultCourses() {
        appwriteSyncService.seedFromBundledAsset()
    }

    // ── Category filter ───────────────────────────────────────────────────────

    fun setCategory(category: CourseContentType?) {
        selectedCategory.value = category
    }

    // ── Folder expand / collapse ──────────────────────────────────────────────

    fun toggleFolderExpansion(courseId: String) {
        expandedFolderIds.value = expandedFolderIds.value.let {
            if (courseId in it) it - courseId else it + courseId
        }
    }

    // ── Multi-select ──────────────────────────────────────────────────────────

    fun toggleSelect(courseId: String) {
        selectedCourseIds.value = selectedCourseIds.value.let {
            if (courseId in it) it - courseId else it + courseId
        }
    }

    fun clearSelection() {
        selectedCourseIds.value = emptySet()
    }

    // ── Group selected courses into a folder ──────────────────────────────────

    fun groupSelectedIntoFolder(folderName: String) {
        val ids = selectedCourseIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val folderId = UUID.randomUUID().toString()
            courseDao.insertCourse(
                CourseEntity(
                    id = folderId,
                    title = folderName.trim(),
                    author = "Thư mục",
                    description = "${ids.size} khóa học",
                    totalModules = ids.size,
                    rewardExp = 0L,
                    rarity = ItemRarity.UNCOMMON,
                    contentType = CourseContentType.GENERAL,
                    category = ""
                )
            )
            ids.forEach { id -> courseDao.updateCourseParent(id, folderId) }
            clearSelection()
            expandedFolderIds.value = expandedFolderIds.value + folderId
        }
    }

    // Create an empty folder (no children required)
    fun createFolder(folderName: String) {
        viewModelScope.launch {
            val folderId = UUID.randomUUID().toString()
            courseDao.insertCourse(
                CourseEntity(
                    id = folderId,
                    title = folderName.trim(),
                    author = "Thư mục",
                    description = "",
                    totalModules = 0,
                    rewardExp = 0L,
                    rarity = ItemRarity.UNCOMMON,
                    contentType = CourseContentType.GENERAL,
                    category = ""
                )
            )
            expandedFolderIds.value = expandedFolderIds.value + folderId
        }
    }

    // Remove a child course from its folder (promote to top-level)
    fun ungroupCourse(course: CourseEntity) {
        viewModelScope.launch {
            courseDao.updateCourseParent(course.id, null)
        }
    }

    // Move items into a folder (or to root when targetFolderId == null); prevents cycles
    fun moveToFolder(ids: Set<String>, targetFolderId: String?) {
        viewModelScope.launch {
            if (targetFolderId != null) {
                val map = _allCourses.value.associateBy { it.id }
                for (id in ids) {
                    var cur = map[targetFolderId]
                    while (cur != null) {
                        if (cur.id == id) return@launch  // would create cycle
                        cur = cur.parentId?.let { map[it] }
                    }
                }
            }
            ids.forEach { id -> courseDao.updateCourseParent(id, targetFolderId) }
            clearSelection()
        }
    }

    // ── Edit course ───────────────────────────────────────────────────────────

    fun editCourse(course: CourseEntity, title: String, description: String, url: String) {
        viewModelScope.launch {
            courseDao.updateCourse(
                course.copy(
                    title = title.trim().ifBlank { course.title },
                    description = description.trim(),
                    contentUrl = url.trim()
                )
            )
        }
    }

    // ── Edit lesson ───────────────────────────────────────────────────────────

    fun editLesson(lesson: LessonEntity, title: String, url: String, notes: String) {
        viewModelScope.launch {
            lessonDao.updateLesson(
                lesson.copy(
                    title = title.trim().ifBlank { lesson.title },
                    contentUrl = url.trim(),
                    notes = notes.trim()
                )
            )
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    fun addCourse(
        title: String,
        author: String,
        description: String,
        contentUrl: String,
        contentType: CourseContentType,
        category: String
    ) {
        viewModelScope.launch {
            courseDao.insertCourse(
                CourseEntity(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    author = author.ifBlank { "Không rõ" },
                    description = description,
                    totalModules = 0,
                    rewardExp = 100,
                    contentUrl = contentUrl,
                    contentType = contentType,
                    category = category,
                    rarity = ItemRarity.COMMON
                )
            )
        }
    }

    fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            // Ungroup children before deleting the folder
            _allCourses.value.filter { it.parentId == courseId }
                .forEach { courseDao.updateCourseParent(it.id, null) }
            lessonDao.deleteLessonsForCourse(courseId)
            courseDao.deleteCourse(courseId)
        }
    }

    fun togglePin(course: CourseEntity) {
        viewModelScope.launch {
            courseDao.updateCourse(course.copy(isPinned = !course.isPinned))
        }
    }

    fun completeModule(course: CourseEntity) {
        if (course.completedModules < course.totalModules) {
            val newCompleted = course.completedModules + 1
            val isCompleted = newCompleted == course.totalModules
            viewModelScope.launch {
                courseDao.updateCourse(course.copy(completedModules = newCompleted, isCompleted = isCompleted))
            }
        }
    }

    fun syncFromAppwrite(apiKey: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = null
            settingsManager.setAppwriteApiKey(apiKey)
            val result = appwriteSyncService.syncCourses(apiKey)
            _syncMessage.value = result.fold(
                onSuccess = { count -> "Đồng bộ thành công: $count mục đã được import." },
                onFailure = { e -> "Lỗi đồng bộ: ${e.message}" }
            )
            _isSyncing.value = false
        }
    }

    fun importFromJson(jsonArray: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = null
            val result = appwriteSyncService.syncFromNodeArray(jsonArray)
            _syncMessage.value = result.fold(
                onSuccess = { count -> "Import thành công: $count mục đã được thêm vào thư viện." },
                onFailure = { e -> "Lỗi import: ${e.message}" }
            )
            _isSyncing.value = false
        }
    }

    fun clearSyncMessage() { _syncMessage.value = null }
}
