package com.systemleveling.feature.home.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.DailySummaryDao
import com.systemleveling.core.database.dao.FinanceDao
import com.systemleveling.core.database.entity.DailySummaryEntity
import com.systemleveling.core.engine.DailySummaryService
import com.systemleveling.core.model.PlanItem
import com.systemleveling.core.model.PlanScope
import com.systemleveling.core.model.WorkPlanItem
import com.systemleveling.core.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DailySummaryViewModel @Inject constructor(
    private val dailySummaryDao: DailySummaryDao,
    private val dailySummaryService: DailySummaryService,
    private val settingsManager: SettingsManager,
    financeDao: FinanceDao
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val todayStart: Long = getTodayMidnight()
    private val todayEnd: Long = todayStart + 86_400_000L

    val todayFinanceIncome: StateFlow<Long> = financeDao.getTodayIncome(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val todayFinanceExpense: StateFlow<Long> = financeDao.getTodayExpense(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _summary = MutableStateFlow<DailySummaryEntity?>(null)
    val summary: StateFlow<DailySummaryEntity?> = _summary.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isBefore22hAndEmpty = MutableStateFlow(false)
    val isBefore22hAndEmpty: StateFlow<Boolean> = _isBefore22hAndEmpty.asStateFlow()

    private val _statChanges = MutableStateFlow<Map<String, Int>>(emptyMap())
    val statChanges: StateFlow<Map<String, Int>> = _statChanges.asStateFlow()

    private val _skillProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val skillProgress: StateFlow<Map<String, Int>> = _skillProgress.asStateFlow()

    private val _tomorrowPlan = MutableStateFlow<List<TodoItem>>(emptyList())
    val tomorrowPlan: StateFlow<List<TodoItem>> = _tomorrowPlan.asStateFlow()

    // ── Editable plan items for tomorrow (WorkPlanItem for quest gen) ─────────
    private val _tomorrowWorkPlan = MutableStateFlow<List<WorkPlanItem>>(emptyList())
    val tomorrowWorkPlan: StateFlow<List<WorkPlanItem>> = _tomorrowWorkPlan.asStateFlow()

    // ── Weekly and monthly plans from DataStore ────────────────────────────────
    val weeklyPlanItems: StateFlow<List<PlanItem>> = settingsManager.weeklyPlanItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyPlanItems: StateFlow<List<PlanItem>> = settingsManager.monthlyPlanItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _plansSaved = MutableStateFlow(false)
    val plansSaved: StateFlow<Boolean> = _plansSaved.asStateFlow()

    init {
        loadTodaySummary()
        loadExistingTomorrowPlan()
    }

    private fun isBefore22h(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour < 22
    }

    private fun loadTodaySummary() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val todayStart = getTodayMidnight()
                val todayEnd = todayStart + 86400000L

                val existing = dailySummaryDao.getSummaryByDateSync(todayStart, todayEnd)
                if (existing == null) {
                    if (isBefore22h()) {
                        _isBefore22hAndEmpty.value = true
                        _isLoading.value = false
                        return@withContext
                    }
                    _isLoading.value = true
                    val generated = dailySummaryService.generateDailySummary(todayStart, todayEnd, "")
                    _summary.value = generated
                    parseJsonFields(generated)
                } else {
                    _summary.value = existing
                    parseJsonFields(existing)
                }
                _isBefore22hAndEmpty.value = false
                _isLoading.value = false
            }
        }
    }

    fun generateSummaryEarly() {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                val todayStart = getTodayMidnight()
                val todayEnd = todayStart + 86400000L
                val generated = dailySummaryService.generateDailySummary(todayStart, todayEnd, "")
                _summary.value = generated
                parseJsonFields(generated)
                _isBefore22hAndEmpty.value = false
            }
            _isLoading.value = false
        }
    }

    private fun loadExistingTomorrowPlan() {
        viewModelScope.launch {
            _tomorrowWorkPlan.value = settingsManager.workPlanItems.first()
        }
    }

    // ── Tomorrow plan (AI-suggested list editing) ─────────────────────────────

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

    // ── Tomorrow WorkPlanItem CRUD (used for AI quest gen next day) ──────────

    fun addTomorrowItem(item: WorkPlanItem) {
        _tomorrowWorkPlan.value = _tomorrowWorkPlan.value + item
    }

    fun removeTomorrowItem(id: String) {
        _tomorrowWorkPlan.value = _tomorrowWorkPlan.value.filter { it.id != id }
    }

    // ── Weekly plan CRUD ──────────────────────────────────────────────────────

    fun addWeeklyItem(item: PlanItem) {
        viewModelScope.launch { settingsManager.addWeeklyPlanItem(item.copy(scope = PlanScope.WEEKLY.name)) }
    }

    fun removeWeeklyItem(id: String) {
        viewModelScope.launch { settingsManager.removeWeeklyPlanItem(id) }
    }

    // ── Monthly plan CRUD ─────────────────────────────────────────────────────

    fun addMonthlyItem(item: PlanItem) {
        viewModelScope.launch { settingsManager.addMonthlyPlanItem(item.copy(scope = PlanScope.MONTHLY.name)) }
    }

    fun removeMonthlyItem(id: String) {
        viewModelScope.launch { settingsManager.removeMonthlyPlanItem(id) }
    }

    // ── Save all plans at once ────────────────────────────────────────────────

    fun saveAllPlans() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                settingsManager.saveWorkPlanItems(_tomorrowWorkPlan.value)
                _plansSaved.value = true
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
        try { _statChanges.value  = json.decodeFromString(entity.statChanges)  } catch (e: Exception) { android.util.Log.w("DailySummaryVM", "statChanges parse failed: ${e.message}") }
        try { _skillProgress.value= json.decodeFromString(entity.skillProgress) } catch (e: Exception) { android.util.Log.w("DailySummaryVM", "skillProgress parse failed: ${e.message}") }
        try { _tomorrowPlan.value = json.decodeFromString(entity.tomorrowPlan)  } catch (e: Exception) { android.util.Log.w("DailySummaryVM", "tomorrowPlan parse failed: ${e.message}") }
    }

    private fun getTodayMidnight(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

@kotlinx.serialization.Serializable
data class TodoItem(
    val title: String,
    val priority: String = "MEDIUM",
    val deadline: String = "",
    val category: String = "general",
    val isCompleted: Boolean = false
)
