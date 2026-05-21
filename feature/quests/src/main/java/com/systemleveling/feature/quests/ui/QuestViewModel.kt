package com.systemleveling.feature.quests.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.engine.RewardEngine
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.core.model.RewardResult
import com.systemleveling.core.network.AiQuestGeneratorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val aiQuestGenerator: AiQuestGeneratorService
) : ViewModel() {

    // Today's quests, sorted by timeStart
    val quests: StateFlow<List<QuestEntity>> = questDao.getAllQuests()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Reward result events for the completion popup
    private val _rewardResult = MutableSharedFlow<RewardResult>()
    val rewardResult: SharedFlow<RewardResult> = _rewardResult.asSharedFlow()

    // Loading state for AI quest generation
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    init {
        // On first load, generate quests if none exist for today
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                ensureTodayQuests()
            }
        }
    }

    /**
     * Generate quests for today if none exist yet.
     * Uses AI generator with fallback templates.
     */
    private suspend fun ensureTodayQuests() {
        val todayStart = getTodayMidnight()
        val todayEnd = todayStart + 86400000L
        val existingCount = questDao.getQuestCountByDate(todayStart, todayEnd)

        if (existingCount == 0) {
            _isGenerating.value = true
            try {
                // TODO: Read API key from BuildConfig when user sets it up
                val apiKey = "" // Will be populated from BuildConfig.GEMINI_API_KEY
                aiQuestGenerator.generateDailyQuests(apiKey, todayStart)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    /**
     * Complete a quest and process rewards through the RewardEngine.
     */
    fun completeQuest(quest: QuestEntity) {
        if (quest.status == QuestStatus.COMPLETED) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val result = rewardEngine.processQuestCompletion(quest)
                _rewardResult.emit(result)
            }
        }
    }

    /**
     * Manually trigger quest regeneration (e.g., user wants new quests).
     */
    fun regenerateQuests() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val todayStart = getTodayMidnight()
                val todayEnd = todayStart + 86400000L
                questDao.deleteQuestsByDate(todayStart, todayEnd)
                ensureTodayQuests()
            }
        }
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
