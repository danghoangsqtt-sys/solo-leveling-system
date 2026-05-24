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

/** Context mode for reply generation — controls tone/style of translated reply. */
enum class ReplyContext(val label: String, val promptHint: String) {
    NORMAL   ("Bình thường",  "giao tiếp thông thường, tự nhiên, thân thiện"),
    MEETING  ("Cuộc họp",     "họp công việc, lịch sự và chuyên nghiệp, súc tích"),
    INTERVIEW("Phỏng vấn",    "phỏng vấn xin việc, trang trọng, chuyên nghiệp, tự tin"),
    CABIN    ("Dịch cabin",   "dịch thuật chính xác hoàn toàn, từng từ, không thêm bớt"),
    FRIENDS  ("Bạn bè",       "bạn bè thân thiết, vui vẻ, thoải mái, có thể dùng tiếng lóng"),
    FORMAL   ("Trang trọng",  "giao tiếp lịch sự, trang trọng, văn phong chuẩn mực")
}

/** Represents a single recognized sentence and its translation (live mode). */
data class TranslationSegment(
    val original: String,
    val translation: String = ""  // empty = translation in progress
)

/** Source language options for the live translator. */
data class SourceLanguage(val label: String, val locale: String)

/** Target language with BCP-47 code for ML Kit and TTS. */
data class TargetLanguage(val label: String, val bcp47: String)

val SOURCE_LANGUAGES = listOf(
    SourceLanguage("Auto",  ""),
    SourceLanguage("中文",   "zh-CN"),
    SourceLanguage("English", "en-US"),
    SourceLanguage("English (IN)", "en-IN"),
    SourceLanguage("हिंदी",  "hi-IN"),
    SourceLanguage("한국어", "ko-KR")
)

val TARGET_LANGUAGES = listOf(
    TargetLanguage("Tiếng Việt", "vi-VN"),
    TargetLanguage("中文",        "zh-CN"),
    TargetLanguage("English",    "en-US"),
    TargetLanguage("हिंदी",      "hi-IN"),
    TargetLanguage("한국어",      "ko-KR")
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

    private val _targetLang = MutableStateFlow(TARGET_LANGUAGES[0])
    val targetLang: StateFlow<TargetLanguage> = _targetLang.asStateFlow()
    /** Convenience string label for audio transcription APIs */
    val targetLanguage: StateFlow<String> = kotlinx.coroutines.flow.MutableStateFlow("Tiếng Việt").also { flow ->
        viewModelScope.launch { _targetLang.collect { flow.value = it.label } }
    }

    private val _processingAudio = MutableStateFlow(false)
    val processingAudio: StateFlow<Boolean> = _processingAudio.asStateFlow()

    private val audioManager = AudioRecordingManager(context)
    private var recordingFile: File? = null
    private var timerJob: Job? = null

    // ── ML Kit offline translation ────────────────────────────────────────────
    private val mlKit = MlKitTranslationManager()

    // ── TTS ───────────────────────────────────────────────────────────────────
    private val _isTtsSpeaking = MutableStateFlow(false)
    val isTtsSpeaking: StateFlow<Boolean> = _isTtsSpeaking.asStateFlow()

    private val tts = AuraTtsManager(context) { speaking -> _isTtsSpeaking.value = speaking }

    // ── Reply suggestion state ────────────────────────────────────────────────
    private val _replyText = MutableStateFlow("")
    val replyText: StateFlow<String> = _replyText.asStateFlow()

    private val _replyContext = MutableStateFlow(ReplyContext.NORMAL)
    val replyContext: StateFlow<ReplyContext> = _replyContext.asStateFlow()

    private val _replyResult = MutableStateFlow("")
    val replyResult: StateFlow<String> = _replyResult.asStateFlow()

    private val _isGeneratingReply = MutableStateFlow(false)
    val isGeneratingReply: StateFlow<Boolean> = _isGeneratingReply.asStateFlow()

    /** Text captured from voice input (for confirmation before translate) */
    private val _voiceReplyDraft = MutableStateFlow("")
    val voiceReplyDraft: StateFlow<String> = _voiceReplyDraft.asStateFlow()

    private val _isVoiceReplying = MutableStateFlow(false)
    val isVoiceReplying: StateFlow<Boolean> = _isVoiceReplying.asStateFlow()

    private var voiceReplyManager: RealtimeTranslationManager? = null

    // ── Live notes transcription (NOTES mode — SpeechRecognizer, no AI) ─────────

    private val _isNotesTranscribing = MutableStateFlow(false)
    val isNotesTranscribing: StateFlow<Boolean> = _isNotesTranscribing.asStateFlow()

    /** Completed sentences accumulated during notes session */
    private val _notesLines = MutableStateFlow<List<String>>(emptyList())
    val notesLines: StateFlow<List<String>> = _notesLines.asStateFlow()

    /** Current partial utterance (before onResults fires) */
    private val _notesPartial = MutableStateFlow("")
    val notesPartial: StateFlow<String> = _notesPartial.asStateFlow()

    private var notesManager: RealtimeTranslationManager? = null

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

    fun setTargetLang(lang: TargetLanguage) { _targetLang.value = lang }

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
        val targetLang = _targetLang.value.label
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

    // ── Notes transcription (NOTES mode — SpeechRecognizer, zero API cost) ──────

    fun startNotesTranscription() {
        if (_isNotesTranscribing.value) return
        val manager = RealtimeTranslationManager(
            context = context,
            onPartialResult = { partial -> _notesPartial.value = partial },
            onSentenceComplete = { sentence ->
                _notesPartial.value = ""
                _notesLines.value = _notesLines.value + sentence
            },
            onError = { msg -> _error.value = msg }
        )
        if (!manager.isAvailable()) {
            _error.value = "Thiết bị không hỗ trợ nhận dạng giọng nói."
            return
        }
        notesManager = manager
        _notesLines.value = emptyList()
        _notesPartial.value = ""
        _error.value = null
        _isNotesTranscribing.value = true
        manager.start("")
    }

    fun stopNotesTranscription() {
        notesManager?.stop()
        notesManager = null
        _isNotesTranscribing.value = false
        _notesPartial.value = ""

        val lines = _notesLines.value
        if (lines.isNotEmpty()) {
            val content = buildString {
                append("📝 Ghi chú cuộc họp\n")
                append("━━━━━━━━━━━━━━━━━━━━\n")
                lines.forEachIndexed { i, line -> append("${i + 1}. $line\n") }
            }
            _messages.value = (_messages.value +
                ChatMessage(MessageRole.ASSISTANT, content)
            ).takeLast(100)
        }
        _notesLines.value = emptyList()
    }

    fun clearNotes() {
        _notesLines.value = emptyList()
        _notesPartial.value = ""
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

        val sourceBcp47 = _sourceLang.value.locale
        val targetBcp47 = _targetLang.value.bcp47

        viewModelScope.launch {
            val translation = if (mlKit.isSupported(sourceBcp47)) {
                // Offline ML Kit — zero token cost, ~100ms
                try {
                    mlKit.translate(sentence, sourceBcp47, targetBcp47)
                } catch (_: Exception) {
                    // Fallback to Gemini if ML Kit fails (model not yet downloaded, etc.)
                    auraRepository.translateText(sentence, _targetLang.value.label).getOrElse { "…" }
                }
            } else {
                // Source = "Auto": ML Kit can't auto-detect → use Gemini
                auraRepository.translateText(sentence, _targetLang.value.label).getOrElse { "…" }
            }
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
            val targetLang = _targetLang.value.label
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

    // ── Reply suggestion + TTS ────────────────────────────────────────────────

    fun setReplyText(text: String) { _replyText.value = text }

    fun setReplyContext(ctx: ReplyContext) { _replyContext.value = ctx }

    fun clearReply() {
        _replyText.value = ""
        _replyResult.value = ""
        _voiceReplyDraft.value = ""
    }

    /**
     * Generates a contextual translated reply for the user's Vietnamese text.
     * Uses Gemini for tone-aware translation (the one AI call in this feature).
     */
    fun generateReplySuggestion() {
        val text = _replyText.value.ifBlank { _voiceReplyDraft.value }
        if (text.isBlank() || _isGeneratingReply.value) return
        _isGeneratingReply.value = true
        _replyResult.value = ""
        viewModelScope.launch {
            val result = auraRepository.generateContextualReply(
                text = text,
                targetLanguage = _targetLang.value.label,
                contextLabel = _replyContext.value.promptHint
            )
            _replyResult.value = result.getOrElse { "Lỗi: ${it.message}" }
            _isGeneratingReply.value = false
        }
    }

    /** Reads the translated reply aloud via TTS. */
    fun speakReply() {
        val text = _replyResult.value
        if (text.isBlank()) return
        tts.speak(text, MlKitTranslationManager.toMlKitCode(_targetLang.value.bcp47))
    }

    /** Reads any text aloud in target language. */
    fun speakText(text: String) {
        tts.speak(text, MlKitTranslationManager.toMlKitCode(_targetLang.value.bcp47))
    }

    fun stopTts() = tts.stop()

    // ── Voice reply (speak Vietnamese → confirm → translate → TTS) ────────────

    /**
     * Starts one-shot Vietnamese speech recognition for composing a reply.
     * Result lands in [voiceReplyDraft] for user to review before sending.
     */
    fun startVoiceReply() {
        if (_isVoiceReplying.value) return
        val manager = RealtimeTranslationManager(
            context = context,
            onPartialResult = { partial -> _voiceReplyDraft.value = partial },
            onSentenceComplete = { sentence ->
                _voiceReplyDraft.value = sentence
                _replyText.value = sentence
                stopVoiceReply()
            },
            onError = { msg -> _error.value = msg; _isVoiceReplying.value = false }
        )
        if (!manager.isAvailable()) { _error.value = "Thiết bị không hỗ trợ nhận dạng giọng nói."; return }
        voiceReplyManager = manager
        _isVoiceReplying.value = true
        _voiceReplyDraft.value = ""
        manager.start("vi-VN")
    }

    fun stopVoiceReply() {
        voiceReplyManager?.stop()
        voiceReplyManager = null
        _isVoiceReplying.value = false
    }

    /** User confirmed the draft → copy to replyText for editing before sending. */
    fun confirmVoiceReply() {
        _replyText.value = _voiceReplyDraft.value
        _voiceReplyDraft.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        if (_isRecording.value) audioManager.cancelRecording()
        notesManager?.stop()
        liveTranslationManager?.stop()
        voiceReplyManager?.stop()
        mlKit.close()
        tts.close()
    }
}
