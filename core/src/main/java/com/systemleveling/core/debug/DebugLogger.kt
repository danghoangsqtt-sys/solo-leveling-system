package com.systemleveling.core.debug

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LogEntry(
    val tag: String,
    val message: String,
    val level: LogLevel,
    val timestamp: Long = System.currentTimeMillis()
)

enum class LogLevel { DEBUG, INFO, WARN, ERROR }

object DebugLogger {

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _dbQueryCount = MutableStateFlow(0)
    val dbQueryCount: StateFlow<Int> = _dbQueryCount.asStateFlow()

    private val _lastApiCall = MutableStateFlow<String?>(null)
    val lastApiCall: StateFlow<String?> = _lastApiCall.asStateFlow()

    // Only active in debug builds — no-op in release
    val isEnabled: Boolean = isDebugBuild()

    fun event(tag: String, message: String) {
        if (!isEnabled) return
        Log.d("SL/$tag", message)
        append(LogEntry(tag, message, LogLevel.DEBUG))
    }

    fun info(tag: String, message: String) {
        if (!isEnabled) return
        Log.i("SL/$tag", message)
        append(LogEntry(tag, message, LogLevel.INFO))
    }

    fun warn(tag: String, message: String) {
        if (!isEnabled) return
        Log.w("SL/$tag", message)
        append(LogEntry(tag, message, LogLevel.WARN))
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        if (!isEnabled) return
        Log.e("SL/$tag", message, throwable)
        val full = if (throwable != null) "$message — ${throwable.message}" else message
        append(LogEntry(tag, full, LogLevel.ERROR))
    }

    fun db(operation: String, entity: String, durationMs: Long = 0) {
        if (!isEnabled) return
        _dbQueryCount.value++
        val msg = "$operation $entity (${durationMs}ms)"
        Log.d("SL/DB", msg)
        append(LogEntry("DB", msg, LogLevel.DEBUG))
    }

    fun api(endpoint: String, status: Int, durationMs: Long = 0) {
        if (!isEnabled) return
        val msg = "[$status] $endpoint (${durationMs}ms)"
        _lastApiCall.value = msg
        Log.d("SL/API", msg)
        append(LogEntry("API", msg, if (status in 200..299) LogLevel.INFO else LogLevel.ERROR))
    }

    fun nav(route: String) {
        if (!isEnabled) return
        Log.d("SL/NAV", "→ $route")
        append(LogEntry("NAV", "→ $route", LogLevel.DEBUG))
    }

    fun clear() {
        _logs.value = emptyList()
        _dbQueryCount.value = 0
    }

    private fun append(entry: LogEntry) {
        _logs.value = (_logs.value + entry).takeLast(200)
    }

    private fun isDebugBuild(): Boolean = try {
        Class.forName("com.systemleveling.app.BuildConfig")
            .getField("DEBUG").getBoolean(null)
    } catch (e: Exception) {
        false
    }
}
