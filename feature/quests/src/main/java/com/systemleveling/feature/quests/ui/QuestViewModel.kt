package com.systemleveling.feature.quests.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.engine.RewardEngine
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.core.model.RewardResult
import com.systemleveling.core.model.WorkPlanItem
import com.systemleveling.core.network.AiQuestGeneratorService
import com.systemleveling.core.settings.SettingsManager
import com.systemleveling.core.sync.CloudSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class QuestViewModel @Inject constructor(
    private val questDao: QuestDao,
    private val userDao: UserDao,
    private val rewardEngine: RewardEngine,
    private val aiQuestGenerator: AiQuestGeneratorService,
    private val settingsManager: SettingsManager,
    private val cloudSyncManager: CloudSyncManager
) : ViewModel() {

    val quests: StateFlow<List<QuestEntity>> = questDao.getAllQuests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val user: StateFlow<com.systemleveling.core.database.entity.UserEntity?> = userDao.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val workPlanItems: StateFlow<List<WorkPlanItem>> = settingsManager.workPlanItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _rewardResult = MutableSharedFlow<RewardResult>()
    val rewardResult: SharedFlow<RewardResult> = _rewardResult.asSharedFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private var pendingPushJob: Job? = null

    private fun schedulePush() {
        pendingPushJob?.cancel()
        pendingPushJob = viewModelScope.launch {
            delay(5_000L)
            withContext(Dispatchers.IO) { cloudSyncManager.push() }
        }
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { ensureTodayQuests() }
        }
    }

    private suspend fun ensureTodayQuests() {
        val todayStart = getTodayMidnight()
        val todayEnd = todayStart + 86400000L
        if (questDao.getQuestCountByDate(todayStart, todayEnd) == 0) {
            _isGenerating.value = true
            try {
                val apiKey = settingsManager.geminiApiKey.first()
                aiQuestGenerator.generateDailyQuests(apiKey, todayStart)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun completeQuest(quest: QuestEntity) {
        if (quest.status == QuestStatus.COMPLETED) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val result = rewardEngine.processQuestCompletion(quest)
                _rewardResult.emit(result)
            }
            schedulePush()
        }
    }

    fun regenerateQuests() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val todayStart = getTodayMidnight()
                questDao.deleteQuestsByDate(todayStart, todayStart + 86400000L)
                ensureTodayQuests()
            }
        }
    }

    fun addWorkPlanItem(item: WorkPlanItem) {
        viewModelScope.launch { settingsManager.addWorkPlanItem(item) }
    }

    fun removeWorkPlanItem(id: String) {
        viewModelScope.launch { settingsManager.removeWorkPlanItem(id) }
    }

    private fun getTodayMidnight(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
