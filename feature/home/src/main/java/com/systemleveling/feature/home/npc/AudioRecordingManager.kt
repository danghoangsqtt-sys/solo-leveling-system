package com.systemleveling.feature.home.npc

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecordingManager(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    /** Returns the output file. Throws if permission denied or hardware unavailable. */
    fun startRecording(): File {
        val dir = context.getExternalFilesDir("aura_notes") ?: context.filesDir
        dir.mkdirs()
        val file = File(dir, "aura_${System.currentTimeMillis()}.m4a")
        currentFile = file

        recorder = buildRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(16_000)
            setAudioEncodingBitRate(64_000)
            setOutputFile(file.absolutePath)
            setMaxDuration(MAX_DURATION_MS)
            prepare()
            start()
        }
        return file
    }

    /** Stops the recording and returns the saved file, or null on error. */
    fun stopRecording(): File? = try {
        recorder?.apply { stop(); release() }
        recorder = null
        currentFile
    } catch (e: Exception) {
        recorder?.release()
        recorder = null
        currentFile?.takeIf { it.length() > 0 }
    }

    /** Stops without saving — deletes the partial file. */
    fun cancelRecording() {
        try { recorder?.apply { stop(); release() } } catch (_: Exception) {}
        recorder = null
        currentFile?.delete()
        currentFile = null
    }

    @Suppress("DEPRECATION")
    private fun buildRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context)
        else MediaRecorder()

    companion object {
        const val MAX_DURATION_MS = 300_000 // 5 minutes
    }
}
