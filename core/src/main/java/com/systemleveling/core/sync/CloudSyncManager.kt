package com.systemleveling.core.sync

import android.util.Log
import com.systemleveling.core.database.dao.DailySummaryDao
import com.systemleveling.core.database.dao.ItemDao
import com.systemleveling.core.database.dao.SkillDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.DailySummaryEntity
import com.systemleveling.core.settings.SettingsManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudSyncManager @Inject constructor(
    private val httpClient: HttpClient,
    private val json: Json,
    private val userDao: UserDao,
    private val skillDao: SkillDao,
    private val itemDao: ItemDao,
    private val dailySummaryDao: DailySummaryDao,
    private val settingsManager: SettingsManager
) {
    companion object {
        private const val TAG = "CloudSyncManager"
        private const val TABLE = "game_state"
        private const val HISTORY_TABLE = "daily_history"
        private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    }

    private fun Long.toDateKey(): String =
        LocalDate.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
            .format(DATE_FORMATTER)

    private fun String.normalizeUrl() = trimEnd('/')

    suspend fun push(): Boolean {
        val supabaseUrl = settingsManager.supabaseUrl.first().trim().normalizeUrl()
        val anonKey = settingsManager.supabaseAnonKey.first().trim()
        if (supabaseUrl.isBlank() || anonKey.isBlank()) {
            Log.d(TAG, "Supabase not configured — skip push")
            return false
        }

        val user = userDao.getUserSync() ?: run {
            Log.d(TAG, "No local user — skip push")
            return false
        }
        val stats = userDao.getStatsSync() ?: return false
        val deviceId = settingsManager.getOrCreateDeviceId()

        val endpoint = "$supabaseUrl/rest/v1/$TABLE"
        Log.d(TAG, "push → POST $endpoint (device=$deviceId)")

        val snapshot = PlayerSnapshot(
            user = user.toSyncData(),
            stats = stats.toSyncData(),
            skills = skillDao.getAllSkillsSync().map { it.toSyncData() },
            items = itemDao.getAllItemsSync().map { it.toSyncData() }
        )
        val playerJson = json.encodeToString(PlayerSnapshot.serializer(), snapshot)
        val payload = GameStateSyncPayload(device_id = deviceId, player_json = playerJson)

        return try {
            val response = httpClient.post(endpoint) {
                headers {
                    append("apikey", anonKey)
                    append("Authorization", "Bearer $anonKey")
                    append("Prefer", "resolution=merge-duplicates,return=minimal")
                }
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            val ok = response.status.value in 200..299
            if (ok) {
                Log.d(TAG, "push ✓ ${response.status}")
            } else {
                val body = runCatching { response.bodyAsText() }.getOrDefault("")
                Log.e(TAG, "push ✗ ${response.status} — $body")
            }
            ok
        } catch (e: Exception) {
            Log.e(TAG, "push exception", e)
            false
        }
    }

    suspend fun restoreIfEmpty(): Boolean {
        val localUser = userDao.getUserSync()
        if (localUser != null) return false  // Already has data

        val supabaseUrl = settingsManager.supabaseUrl.first().trim().normalizeUrl()
        val anonKey = settingsManager.supabaseAnonKey.first().trim()
        if (supabaseUrl.isBlank() || anonKey.isBlank()) return false

        val deviceId = settingsManager.getOrCreateDeviceId()
        val endpoint = "$supabaseUrl/rest/v1/$TABLE"
        Log.d(TAG, "restore → GET $endpoint (device=$deviceId)")

        return try {
            val response = httpClient.get(endpoint) {
                headers {
                    append("apikey", anonKey)
                    append("Authorization", "Bearer $anonKey")
                }
                url {
                    parameters.append("device_id", "eq.$deviceId")
                    parameters.append("select", "player_json")
                    parameters.append("limit", "1")
                }
            }

            if (response.status != HttpStatusCode.OK) {
                val body = runCatching { response.bodyAsText() }.getOrDefault("")
                Log.e(TAG, "restore ✗ ${response.status} — $body")
                return false
            }

            val rows: List<GameStateSyncPayload> = response.body()
            val playerJson = rows.firstOrNull()?.player_json ?: return false

            val snapshot = json.decodeFromString(PlayerSnapshot.serializer(), playerJson)

            userDao.insertUser(snapshot.user.toEntity())
            userDao.insertStats(snapshot.stats.toEntity())
            skillDao.insertSkills(snapshot.skills.map { it.toEntity() })
            itemDao.insertItems(snapshot.items.map { it.toEntity() })

            Log.d(TAG, "Restored ${snapshot.skills.size} skills, ${snapshot.items.size} items from cloud")
            true
        } catch (e: Exception) {
            Log.e(TAG, "restore failed", e)
            false
        }
    }

    // ── Daily History Sync ────────────────────────────────────────────────────

    suspend fun pushDailyHistory(summary: DailySummaryEntity): Boolean {
        val supabaseUrl = settingsManager.supabaseUrl.first().trim().normalizeUrl()
        val anonKey = settingsManager.supabaseAnonKey.first().trim()
        if (supabaseUrl.isBlank() || anonKey.isBlank()) {
            Log.d(TAG, "Supabase not configured — skip history push")
            return false
        }

        val deviceId = settingsManager.getOrCreateDeviceId()
        val summaryJson = json.encodeToString(DailySummaryHistoryItem.serializer(), summary.toHistoryItem())
        val payload = DailyHistoryPayload(
            device_id = deviceId,
            date_key = summary.date.toDateKey(),
            summary_json = summaryJson
        )

        val endpoint = "$supabaseUrl/rest/v1/$HISTORY_TABLE"
        Log.d(TAG, "history push → ${payload.date_key}")

        return try {
            val response = httpClient.post(endpoint) {
                headers {
                    append("apikey", anonKey)
                    append("Authorization", "Bearer $anonKey")
                    append("Prefer", "resolution=merge-duplicates,return=minimal")
                }
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            val ok = response.status.value in 200..299
            if (ok) Log.d(TAG, "history push ✓ ${payload.date_key}")
            else {
                val body = runCatching { response.bodyAsText() }.getOrDefault("")
                Log.e(TAG, "history push ✗ ${response.status} — $body")
            }
            ok
        } catch (e: Exception) {
            Log.e(TAG, "history push exception", e)
            false
        }
    }

    // Push the last [days] daily summaries — useful for catch-up after offline period
    suspend fun pushHistoryBatch(days: Int = 7): Int {
        val summaries = dailySummaryDao.getRecentSummaries(days)
        var successCount = 0
        summaries.forEach { if (pushDailyHistory(it)) successCount++ }
        Log.d(TAG, "history batch: $successCount/${summaries.size} pushed")
        return successCount
    }
}
