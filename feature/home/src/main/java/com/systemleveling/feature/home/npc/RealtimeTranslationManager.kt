package com.systemleveling.feature.home.npc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Wraps Android SpeechRecognizer for continuous real-time speech-to-text.
 *
 * All SpeechRecognizer operations are posted to the main thread via [mainHandler].
 * Callbacks ([onPartialResult], [onSentenceComplete], [onError]) are delivered on main thread.
 *
 * Call [start] to begin listening, [stop] to end the session.
 * The recognizer automatically restarts after each utterance while active.
 */
class RealtimeTranslationManager(
    private val context: Context,
    private val onPartialResult: (String) -> Unit,
    private val onSentenceComplete: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var shouldContinue = false
    private var sourceLocale: String = ""  // empty = system default (auto)

    /** @param locale BCP-47 code: "zh-CN", "en-US", "hi-IN", "ko-KR", or "" for auto. */
    fun start(locale: String = "") {
        sourceLocale = locale
        shouldContinue = true
        mainHandler.post { startListeningInternal() }
    }

    fun stop() {
        shouldContinue = false
        mainHandler.post {
            recognizer?.stopListening()
            recognizer?.destroy()
            recognizer = null
        }
    }

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    private fun startListeningInternal() {
        if (!shouldContinue) return
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).also {
            it.setRecognitionListener(buildListener())
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            if (sourceLocale.isNotBlank()) {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLocale)
            }
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        recognizer?.startListening(intent)
    }

    private fun buildListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0)
                ?: return
            if (partial.isNotBlank()) onPartialResult(partial)
        }

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0) ?: ""
            if (text.isNotBlank()) onSentenceComplete(text)
            if (shouldContinue) startListeningInternal()
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO          -> "Lỗi microphone"
                SpeechRecognizer.ERROR_NETWORK,
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Cần kết nối internet để nhận dạng giọng nói"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT  -> null  // silence — just restart
                SpeechRecognizer.ERROR_NO_MATCH        -> null  // no speech — just restart
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> null  // restart will fix it
                else                                   -> null
            }
            if (message != null) onError(message)
            if (shouldContinue && error != SpeechRecognizer.ERROR_AUDIO) {
                mainHandler.postDelayed({ if (shouldContinue) startListeningInternal() }, 400)
            }
        }
    }
}
