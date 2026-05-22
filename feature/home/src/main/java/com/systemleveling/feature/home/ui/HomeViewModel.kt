package com.systemleveling.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.FinanceDao
import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.StatEntity
import com.systemleveling.core.database.entity.UserEntity
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.core.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class QuestSummary(val total: Int, val completed: Int)

@HiltViewModel
class HomeViewModel @Inject constructor(
    userDao: UserDao,
    questDao: QuestDao,
    financeDao: FinanceDao,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val todayStart: Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    private val todayEnd: Long = todayStart + 86_400_000L

    val user: StateFlow<UserEntity?> = userDao.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val stats: StateFlow<StatEntity?> = userDao.getStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val questSummary: StateFlow<QuestSummary> = questDao.getAllQuests()
        .map { quests ->
            QuestSummary(
                total = quests.size,
                completed = quests.count { it.status == QuestStatus.COMPLETED }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = QuestSummary(0, 0)
        )

    // True when ALL stats have reached the current stat cap — triggers class advancement
    val isAdvancementReady: StateFlow<Boolean> = combine(
        userDao.getUser(),
        userDao.getStats()
    ) { user, stats ->
        if (user == null || stats == null) return@combine false
        val cap = user.statCap
        stats.str >= cap && stats.intStat >= cap && stats.agi >= cap &&
            stats.vit >= cap && stats.wis >= cap && stats.cha >= cap
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val geminiApiKey: StateFlow<String> = settingsManager.geminiApiKey
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val totalBalance: StateFlow<Long> = combine(
        financeDao.getTotalIncome(),
        financeDao.getTotalExpense()
    ) { income, expense -> (income ?: 0L) - (expense ?: 0L) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val todayExpense: StateFlow<Long> = financeDao.getTodayExpense(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            settingsManager.setGeminiApiKey(key)
        }
    }
}
