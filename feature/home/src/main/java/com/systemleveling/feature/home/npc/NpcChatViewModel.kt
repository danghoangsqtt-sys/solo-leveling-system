package com.systemleveling.feature.home.npc

import android.content.Context
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import javax.inject.Inject

enum class RecordMode { CHAT, NOTES, TRANSLATE }

/** Represents a single recognized sentence and its translation (live mode). */
data class TranslationSegment(
    val original: String,
    val translation: String = ""  // empty = translation in progress
)

/** Source language options for the live translator. */
data class SourceLanguage(val label: String, val locale: String)

val SOURCE_LANGUAGES = listOf(
    SourceLanguage("Auto",  ""),
    SourceLanguage("中文",   "zh-CN"),
    SourceLanguage("English", "en-US"),
    SourceLanguage("English (IN)", "en-IN"),
    SourceLanguage("हिंदी",  "hi-IN"),
    SourceLanguage("한국어", "ko-KR")
)

@HiltViewModel
class NpcChatViewModel @Inject constructor(
    private val auraRepository: AuraRepository,
    @ApplicationContext private val context: Context,
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

    // ── Audio recording state (NOTES mode) ───────────────────────────────────

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private val _recordMode = MutableStateFlow(RecordMode.CHAT)
    val recordMode: StateFlow<RecordMode> = _recordMode.asStateFlow()

    private val _targetLanguage = MutableStateFlow("Tiếng Việt")
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()

    private val _processingAudio = MutableStateFlow(false)
    val processingAudio: StateFlow<Boolean> = _processingAudio.asStateFlow()

    private val audioManager = AudioRecordingManager(context)
    private var recordingFile: File? = null
    private var timerJob: Job? = null

    // ── Live translation state (TRANSLATE mode) ───────────────────────────────

    private val _isLiveTranslating = MutableStateFlow(false)
    val isLiveTranslating: StateFlow<Boolean> = _isLiveTranslating.asStateFlow()

    /** Accumulated segments: (original sentence, translated sentence) */
    private val _liveSegments = MutableStateFlow<List<TranslationSegment>>(emptyList())
    val liveSegments: StateFlow<List<TranslationSegment>> = _liveSegments.asStateFlow()

    /** Current partial utterance being recognized (before onResults) */
    private val _livePartialText = MutableStateFlow("")
    val livePartialText: StateFlow<String> = _livePartialText.asStateFlow()

    private val _sourceLang = MutableStateFlow(SOURCE_LANGUAGES[0])
    val sourceLang: StateFlow<SourceLanguage> = _sourceLang.asStateFlow()

    private var liveTranslationManager: RealtimeTranslationManager? = null

    // ── Chat ──────────────────────────────────────────────────────────────────

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
                if (user == null) { _isLoading.value = false; return@launch }

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

                val ctx = AuraPlayerContext(
                    level = user.level,
                    exp = user.exp,
                    streak = user.streak,
                    pendingQuests = pendingQuests.map { it.title },
                    weakSkills = skills.map { it.name }
                )

                val result = auraRepository.generateProactiveGreeting(apiKey.value, ctx)
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

    // ── Audio recording (NOTES mode) ──────────────────────────────────────────

    fun setRecordMode(mode: RecordMode) { _recordMode.value = mode }

    fun setTargetLanguage(lang: String) { _targetLanguage.value = lang }

    fun setSourceLang(lang: SourceLanguage) {
        _sourceLang.value = lang
        if (_isLiveTranslating.value) {
            liveTranslationManager?.stop()
            liveTranslationManager?.start(lang.locale)
        }
    }

    fun startRecording() {
        if (_isRecording.value || _processingAudio.value) return
        try {
            recordingFile = audioManager.startRecording()
            _isRecording.value = true
            _recordingSeconds.value = 0
            _error.value = null
            timerJob = viewModelScope.launch {
                while (true) {
                    delay(1000L)
                    _recordingSeconds.value++
                    if (_recordingSeconds.value >= AudioRecordingManager.MAX_DURATION_MS / 1000) {
                        stopAndProcess()
                        break
                    }
                }
            }
        } catch (e: Exception) {
            _isRecording.value = false
            _error.value = when {
                e.message?.contains("permission", ignoreCase = true) == true ->
                    "Cần cấp quyền microphone để ghi âm."
                else -> "Không thể bắt đầu ghi âm: ${e.message}"
            }
        }
    }

    fun stopAndProcess() {
        if (!_isRecording.value) return
        timerJob?.cancel()
        timerJob = null
        val durationSec = _recordingSeconds.value
        val file = audioManager.stopRecording()
        _isRecording.value = false
        _recordingSeconds.value = 0

        if (file == null || !file.exists() || file.length() == 0L) {
            _error.value = "Ghi âm thất bại hoặc file trống."
            return
        }

        val mode = _recordMode.value
        val targetLang = _targetLanguage.value
        val durationLabel = "%d:%02d".format(durationSec / 60, durationSec % 60)

        _processingAudio.value = true
        val userLabel = when (mode) {
            RecordMode.NOTES     -> "🎙 Ghi âm [$durationLabel] — đang phân tích..."
            RecordMode.TRANSLATE -> "🌐 Ghi âm [$durationLabel] — đang dịch sang $targetLang..."
            RecordMode.CHAT      -> "🎙 [$durationLabel]"
        }
        _messages.value = (_messages.value + ChatMessage(MessageRole.USER, userLabel)).takeLast(100)

        viewModelScope.launch {
            val result = if (mode == RecordMode.NOTES) {
                auraRepository.transcribeAudio(file)
            } else {
                auraRepository.translateAudio(file, targetLang)
            }
            _messages.value = _messages.value.dropLast(1)
            val finalUserLabel = when (mode) {
                RecordMode.NOTES     -> "🎙 Ghi âm [$durationLabel]"
                RecordMode.TRANSLATE -> "🌐 Phiên dịch [$durationLabel] → $targetLang"
                RecordMode.CHAT      -> "🎙 [$durationLabel]"
            }
            result.fold(
                onSuccess = { text ->
                    _messages.value = (_messages.value +
                        ChatMessage(MessageRole.USER, finalUserLabel) +
                        ChatMessage(MessageRole.ASSISTANT, text)
                    ).takeLast(100)
                },
                onFailure = { e ->
                    _messages.value = (_messages.value +
                        ChatMessage(MessageRole.USER, finalUserLabel)
                    ).takeLast(100)
                    _error.value = e.message ?: "Xử lý âm thanh thất bại"
                }
            )
            _processingAudio.value = false
        }
    }

    fun cancelRecording() {
        if (!_isRecording.value) return
        timerJob?.cancel()
        timerJob = null
        audioManager.cancelRecording()
        _isRecording.value = false
        _recordingSeconds.value = 0
    }

    // ── Live translation (TRANSLATE mode) ─────────────────────────────────────

    fun startLiveTranslation() {
        if (_isLiveTranslating.value) return
        val manager = RealtimeTranslationManager(
            context = context,
            onPartialResult = { partial -> _livePartialText.value = partial },
            onSentenceComplete = { sentence -> onSentenceRecognized(sentence) },
            onError = { msg -> _error.value = msg }
        )
        if (!manager.isAvailable()) {
            _error.value = "Thiết bị không hỗ trợ nhận dạng giọng nói trực tiếp."
            return
        }
        liveTranslationManager = manager
        _liveSegments.value = emptyList()
        _livePartialText.value = ""
        _error.value = null
        _isLiveTranslating.value = true
        manager.start(_sourceLang.value.locale)
    }

    private fun onSentenceRecognized(sentence: String) {
        _livePartialText.value = ""
        val idx = _liveSegments.value.size
        _liveSegments.value = _liveSegments.value + TranslationSegment(original = sentence)

        viewModelScope.launch {
            val result = auraRepository.translateText(sentence, _targetLanguage.value)
            val translation = result.getOrElse { "…" }
            val current = _liveSegments.value.toMutableList()
            if (idx < current.size) {
                current[idx] = current[idx].copy(translation = translation)
                _liveSegments.value = current.toList()
            }
        }
    }

    /** Stops live translation and saves the session to chat history. */
    fun stopLiveTranslation() {
        liveTranslationManager?.stop()
        liveTranslationManager = null
        _isLiveTranslating.value = false
        _livePartialText.value = ""

        val segments = _liveSegments.value
        if (segments.isNotEmpty()) {
            val targetLang = _targetLanguage.value
            val sourceName = _sourceLang.value.label.ifBlank { "Auto" }
            val content = buildString {
                append("🌐 Phiên dịch trực tiếp ($sourceName → $targetLang)\n")
                append("━━━━━━━━━━━━━━━━━━━━\n")
                segments.forEachIndexed { i, seg ->
                    append("${i + 1}. ${seg.original}\n")
                    append("   → ${seg.translation.ifBlank { "…" }}\n")
                }
            }
            _messages.value = (_messages.value +
                ChatMessage(MessageRole.ASSISTANT, content)
            ).takeLast(100)
        }
        _liveSegments.value = emptyList()
    }

    /** Clears the live segment list (user pressed "Xoá" mid-session). */
    fun clearLiveSegments() {
        _liveSegments.value = emptyList()
        _livePartialText.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        if (_isRecording.value) audioManager.cancelRecording()
        liveTranslationManager?.stop()
    }
}
