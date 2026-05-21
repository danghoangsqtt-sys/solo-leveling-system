package com.systemleveling.feature.home.npc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.ai.AuraRepository
import com.systemleveling.core.ai.ChatMessage
import com.systemleveling.core.ai.MessageRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NpcChatViewModel @Inject constructor(
    private val auraRepository: AuraRepository
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
        _messages.value = _messages.value + userMsg
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = auraRepository.chat(_messages.value.dropLast(1), text)
            result.fold(
                onSuccess = { reply ->
                    _messages.value = _messages.value + ChatMessage(MessageRole.ASSISTANT, reply)
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
}
