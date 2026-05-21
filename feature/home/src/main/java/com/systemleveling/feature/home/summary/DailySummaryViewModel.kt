package com.systemleveling.feature.home.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.DailySummaryDao
import com.systemleveling.core.database.entity.DailySummaryEntity
import com.systemleveling.core.engine.DailySummaryService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DailySummaryViewModel @Inject constructor(
    private val dailySummaryDao: DailySummaryDao,
    private val dailySummaryService: DailySummaryService
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _summary = MutableStateFlow<DailySummaryEntity?>(null)
    val summary: StateFlow<DailySummaryEntity?> = _summary.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statChanges = MutableStateFlow<Map<String, Int>>(emptyMap())
    val statChanges: StateFlow<Map<String, Int>> = _statChanges.asStateFlow()

    private val _skillProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val skillProgress: StateFlow<Map<String, Int>> = _skillProgress.asStateFlow()

    private val _tomorrowPlan = MutableStateFlow<List<TodoItem>>(emptyList())
    val tomorrowPlan: StateFlow<List<TodoItem>> = _tomorrowPlan.asStateFlow()

    init {
        loadTodaySummary()
    }

    private fun loadTodaySummary() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val todayStart = getTodayMidnight()
                val todayEnd = todayStart + 86400000L

                // Try to load existing summary
                var existing = dailySummaryDao.getSummaryByDateSync(todayStart, todayEnd)

                if (existing == null) {
                    // Generate summary
                    _isLoading.value = true
                    existing = dailySummaryService.generateDailySummary(todayStart, todayEnd, "")
                }

                _summary.value = existing
                parseJsonFields(existing)
                _isLoading.value = false
            }
        }
    }

    fun updateTomorrowPlan(updatedPlan: List<TodoItem>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _tomorrowPlan.value = updatedPlan
                val summary = _summary.value ?: return@withContext
                val planJson = json.encodeToString(
                    kotlinx.serialization.builtins.ListSerializer(TodoItem.serializer()),
                    updatedPlan
                )
                dailySummaryDao.updateSummary(summary.copy(tomorrowPlan = planJson))
            }
        }
    }

    fun addUserNote(note: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val summary = _summary.value ?: return@withContext
                val updated = summary.copy(userNotes = note)
                dailySummaryDao.updateSummary(updated)
                _summary.value = updated
            }
        }
    }

    private fun parseJsonFields(entity: DailySummaryEntity) {
        try {
            _statChanges.value = json.decodeFromString(entity.statChanges)
        } catch (_: Exception) { }
        try {
            _skillProgress.value = json.decodeFromString(entity.skillProgress)
        } catch (_: Exception) { }
        try {
            _tomorrowPlan.value = json.decodeFromString(entity.tomorrowPlan)
        } catch (_: Exception) { }
    }

    private fun getTodayMidnight(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}

@kotlinx.serialization.Serializable
data class TodoItem(
    val title: String,
    val priority: String = "MEDIUM",
    val deadline: String = "",
    val category: String = "general",
    val isCompleted: Boolean = false
)
