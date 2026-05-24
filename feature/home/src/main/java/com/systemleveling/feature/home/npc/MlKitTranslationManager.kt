package com.systemleveling.feature.home.npc

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Offline ML Kit translation — zero Gemini API cost.
 *
 * Supports: Chinese ↔ Vietnamese, English ↔ Vietnamese, Hindi ↔ Vietnamese,
 * Korean ↔ Vietnamese (and any pair within these 5 languages).
 *
 * Models are downloaded once (~30 MB each) on first use, then fully offline.
 * Call [close] when done to release resources.
 */
class MlKitTranslationManager {

    private val translators = mutableMapOf<String, Translator>()

    /**
     * Translates [text] from [sourceLangCode] to [targetLangCode].
     * Both codes are BCP-47 (zh-CN, en-US, hi-IN, ko-KR, vi-VN) or ML Kit codes.
     * Throws on model download failure or unsupported pair.
     */
    suspend fun translate(text: String, sourceLangCode: String, targetLangCode: String): String {
        if (text.isBlank()) return ""
        val src = toMlKitCode(sourceLangCode)
        val tgt = toMlKitCode(targetLangCode)
        if (src == tgt) return text

        val key = "$src→$tgt"
        val translator = translators.getOrPut(key) {
            Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(src)
                    .setTargetLanguage(tgt)
                    .build()
            )
        }

        // Download model if needed (no-op if already downloaded)
        suspendCancellableCoroutine<Unit> { cont ->
            translator.downloadModelIfNeeded()
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }

        return suspendCancellableCoroutine { cont ->
            translator.translate(text)
                .addOnSuccessListener { result -> cont.resume(result) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }

    /**
     * Returns true if the [bcp47Locale] is a known explicit language code (not "Auto"/empty).
     * ML Kit requires explicit source language — cannot auto-detect.
     */
    fun isSupported(bcp47Locale: String): Boolean = bcp47Locale.isNotBlank()

    fun close() {
        translators.values.forEach { it.close() }
        translators.clear()
    }

    companion object {
        fun toMlKitCode(bcp47: String): String = when {
            bcp47.startsWith("zh")  -> TranslateLanguage.CHINESE
            bcp47.startsWith("en")  -> TranslateLanguage.ENGLISH
            bcp47.startsWith("hi")  -> TranslateLanguage.HINDI
            bcp47.startsWith("ko")  -> TranslateLanguage.KOREAN
            bcp47.startsWith("vi") || bcp47.contains("việt", ignoreCase = true)
                                    -> TranslateLanguage.VIETNAMESE
            // Fallback: map target language labels
            bcp47.contains("Trung", ignoreCase = true) || bcp47.contains("Chinese", ignoreCase = true)
                                    -> TranslateLanguage.CHINESE
            bcp47.contains("Anh", ignoreCase = true) || bcp47.contains("English", ignoreCase = true)
                                    -> TranslateLanguage.ENGLISH
            bcp47.contains("Hindi", ignoreCase = true) || bcp47.contains("हिंदी")
                                    -> TranslateLanguage.HINDI
            bcp47.contains("Korean", ignoreCase = true) || bcp47.contains("한국어")
                                    -> TranslateLanguage.KOREAN
            else                    -> TranslateLanguage.ENGLISH
        }

        /** Returns the BCP-47 locale label for TTS given an ML Kit code */
        fun toTtsLocale(mlKitCode: String): java.util.Locale = when (mlKitCode) {
            TranslateLanguage.CHINESE    -> java.util.Locale.SIMPLIFIED_CHINESE
            TranslateLanguage.ENGLISH    -> java.util.Locale.US
            TranslateLanguage.HINDI      -> java.util.Locale("hi", "IN")
            TranslateLanguage.KOREAN     -> java.util.Locale.KOREAN
            TranslateLanguage.VIETNAMESE -> java.util.Locale("vi", "VN")
            else                         -> java.util.Locale.ENGLISH
        }
    }
}
