package com.systemleveling.feature.home.npc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.ai.AuraPlayerContext
import com.systemleveling.core.ai.AuraRepository
import com.systemleveling.core.ai.ChatMessage
import com.systemleveling.core.ai.MessageRole
import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.SkillDao
import com.systemleveling.core.database.dao.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class NpcChatViewModel @Inject constructor(
    private val auraRepository: AuraRepository,
    private val userDao: UserDao,
    private val questDao: QuestDao,
    private val skillDao: SkillDao
) : ViewModel() {

    val apiKey: StateFlow<String> = auraRepository.apiKeyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun saveApiKey(key: String) {
        viewModelScope.launch { auraRepository.saveApiKey(key) }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value) return
        val userMsg = ChatMessage(MessageRole.USER, text)
        _messages.value = (_messages.value + userMsg).takeLast(100)
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = auraRepository.chat(_messages.value.dropLast(1), text)
            result.fold(
                onSuccess = { reply ->
                    _messages.value = (_messages.value + ChatMessage(MessageRole.ASSISTANT, reply)).takeLast(100)
                },
                onFailure = { e ->
                    _error.value = e.message ?: "Kết nối thất bại"
                    _messages.value = _messages.value.dropLast(1)
                }
            )
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }

    fun clearHistory() {
        _messages.value = emptyList()
        _error.value = null
    }

    fun triggerProactiveGreeting() {
        if (_messages.value.isNotEmpty() || _isLoading.value) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val user = userDao.getUser().first()
                if (user == null) {
                    _isLoading.value = false
                    return@launch
                }
                
                // Get today's bounds
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val dayStart = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val dayEnd = calendar.timeInMillis
                
                val pendingQuests = questDao.getPendingQuestsByDateSync(dayStart, dayEnd)
                val skills = skillDao.getAllSkillsSync().sortedBy { it.level }.take(3)
                
                val context = AuraPlayerContext(
                    level = user.level,
                    exp = user.exp,
                    streak = user.streak,
                    pendingQuests = pendingQuests.map { it.title },
                    weakSkills = skills.map { it.name }
                )
                
                val key = apiKey.value
                val result = auraRepository.generateProactiveGreeting(key, context)
                result.fold(
                    onSuccess = { reply ->
                        _messages.value = listOf(ChatMessage(MessageRole.ASSISTANT, reply))
                    },
                    onFailure = { e ->
                        android.util.Log.w("NpcChatViewModel", "Proactive greeting failed: ${e.message}")
                    }
                )
            } catch (e: Exception) {
                android.util.Log.w("NpcChatViewModel", "Proactive greeting exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
