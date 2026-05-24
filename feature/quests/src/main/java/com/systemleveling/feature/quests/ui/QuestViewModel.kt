package com.systemleveling.feature.quests.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.engine.PenaltyEngine
import com.systemleveling.core.engine.RewardEngine
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.core.model.RewardResult
import com.systemleveling.core.model.WorkPlanItem
import com.systemleveling.core.network.AiQuestGeneratorService
import com.systemleveling.core.notification.NotificationHelper
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
import kotlin.math.ceil
import kotlin.math.max

@HiltViewModel
class QuestViewModel @Inject constructor(
    private val questDao: QuestDao,
    private val userDao: UserDao,
    private val rewardEngine: RewardEngine,
    private val penaltyEngine: PenaltyEngine,
    private val notificationHelper: NotificationHelper,
    private val aiQuestGenerator: AiQuestGeneratorService,
    private val settingsManager: SettingsManager,
    private val cloudSyncManager: CloudSyncManager
) : ViewModel() {

    /** Fired when a quest expires in real-time — displayed as in-app penalty banner. */
    data class PenaltyEvent(val questTitle: String, val expLost: Int, val debtAdded: Int)

    val quests: StateFlow<List<QuestEntity>> = questDao.getAllQuests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val user: StateFlow<com.systemleveling.core.database.entity.UserEntity?> = userDao.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val workPlanItems: StateFlow<List<WorkPlanItem>> = settingsManager.workPlanItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _rewardResult = MutableSharedFlow<RewardResult>()
    val rewardResult: SharedFlow<RewardResult> = _rewardResult.asSharedFlow()

    private val _penaltyEvent = MutableSharedFlow<PenaltyEvent>()
    val penaltyEvent: SharedFlow<PenaltyEvent> = _penaltyEvent.asSharedFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError: StateFlow<String?> = _generationError

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
                if (apiKey.isBlank()) {
                    _generationError.value = "Chưa cài Gemini API key — vào Cài đặt để nhập key"
                    return
                }
                aiQuestGenerator.generateDailyQuests(apiKey, todayStart)
                _generationError.value = null
            } catch (e: Exception) {
                android.util.Log.e("QuestViewModel", "Quest generation failed: ${e.message}", e)
                _generationError.value = "Không thể tạo nhiệm vụ hôm nay: ${e.message?.take(80)}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun completeQuest(quest: QuestEntity) {
        // Guard: only allow completing PENDING or IN_PROGRESS quests
        if (quest.status != QuestStatus.PENDING && quest.status != QuestStatus.IN_PROGRESS) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val result = rewardEngine.processQuestCompletion(quest)
                _rewardResult.emit(result)
            }
            schedulePush()
        }
    }

    /**
     * Called when a quest's countdown timer reaches zero in real-time.
     * Immediately applies penalty and emits a penalty banner event.
     */
    fun failExpiredQuest(quest: QuestEntity) {
        if (quest.status != QuestStatus.PENDING && quest.status != QuestStatus.IN_PROGRESS) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                penaltyEngine.processQuestFailure(quest)
                val expLost = ceil(quest.expReward * 0.3).toInt()
                _penaltyEvent.emit(PenaltyEvent(quest.title, expLost, quest.penaltyDebtPoints))
                // System notification for penalty
                if (quest.penaltyDebtPoints > 0) {
                    val user = userDao.getUser().first()
                    notificationHelper.notifyPenaltyWarning(
                        debtPoints = max(0, (user?.debtPoints ?: 0)),
                        failedQuests = 1
                    )
                }
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
