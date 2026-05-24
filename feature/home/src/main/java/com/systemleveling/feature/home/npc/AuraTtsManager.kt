package com.systemleveling.feature.home.npc

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.Locale

/**
 * Thin wrapper around Android TextToSpeech for reading translated replies aloud.
 * Call [close] when no longer needed (in ViewModel.onCleared).
 */
class AuraTtsManager(
    context: Context,
    private val onSpeakingChanged: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { onSpeakingChanged(true) }
            override fun onDone(utteranceId: String?) { onSpeakingChanged(false) }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { onSpeakingChanged(false) }
        })
    }

    fun speak(text: String, mlKitLangCode: String) {
        if (!ready || text.isBlank()) return
        val locale = MlKitTranslationManager.toTtsLocale(mlKitLangCode)
        tts?.language = locale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "aura_${System.currentTimeMillis()}")
    }

    fun speakVietnamese(text: String) = speak(text, TranslateLanguage.VIETNAMESE)

    fun stop() {
        tts?.stop()
        onSpeakingChanged(false)
    }

    fun close() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
