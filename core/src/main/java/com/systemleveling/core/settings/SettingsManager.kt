package com.systemleveling.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.systemleveling.core.model.PlanItem
import com.systemleveling.core.model.WorkPlanItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        private val WORK_PLAN_ITEMS = stringPreferencesKey("work_plan_items")
        private val WEEKLY_PLAN_ITEMS = stringPreferencesKey("weekly_plan_items")
        private val MONTHLY_PLAN_ITEMS = stringPreferencesKey("monthly_plan_items")
        private val DEVICE_UUID = stringPreferencesKey("device_uuid")
        private val SUPABASE_URL = stringPreferencesKey("supabase_url")
        private val SUPABASE_ANON_KEY = stringPreferencesKey("supabase_anon_key")
        private val IS_ONBOARDED = booleanPreferencesKey("isOnboarded")
        private val IS_JOURNAL_SEEDED = booleanPreferencesKey("is_journal_seeded")
        private val IS_LIBRARY_SEEDED = booleanPreferencesKey("is_library_seeded")

        const val DEFAULT_APPWRITE_ENDPOINT = "https://sgp.cloud.appwrite.io/v1"
    }

    private val encryptedPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "secret_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ── Onboarding state ─────────────────────────────────────────────────────

    val isOnboarded: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[IS_ONBOARDED] ?: false
    }

    suspend fun setOnboarded(value: Boolean) {
        dataStore.edit { prefs -> prefs[IS_ONBOARDED] = value }
    }

    // ── One-time seed flags ──────────────────────────────────────────────────

    suspend fun isJournalSeeded(): Boolean = dataStore.data.first()[IS_JOURNAL_SEEDED] ?: false
    suspend fun markJournalSeeded() { dataStore.edit { prefs -> prefs[IS_JOURNAL_SEEDED] = true } }

    suspend fun isLibrarySeeded(): Boolean = dataStore.data.first()[IS_LIBRARY_SEEDED] ?: false
    suspend fun markLibrarySeeded() { dataStore.edit { prefs -> prefs[IS_LIBRARY_SEEDED] = true } }

    // ── Appwrite sync ────────────────────────────────────────────────────────

    private val _appwriteApiKey = MutableStateFlow(encryptedPrefs.getString("appwrite_api_key", "") ?: "")
    val appwriteApiKey: Flow<String> = _appwriteApiKey.asStateFlow()

    suspend fun setAppwriteApiKey(key: String) {
        encryptedPrefs.edit().putString("appwrite_api_key", key.trim()).apply()
        _appwriteApiKey.value = key.trim()
    }

    private val _appwriteEndpoint = MutableStateFlow(encryptedPrefs.getString("appwrite_endpoint", DEFAULT_APPWRITE_ENDPOINT) ?: DEFAULT_APPWRITE_ENDPOINT)
    val appwriteEndpoint: Flow<String> = _appwriteEndpoint.asStateFlow()

    private val _appwriteProjectId = MutableStateFlow(encryptedPrefs.getString("appwrite_project_id", "") ?: "")
    val appwriteProjectId: Flow<String> = _appwriteProjectId.asStateFlow()

    private val _appwriteDatabaseId = MutableStateFlow(encryptedPrefs.getString("appwrite_database_id", "") ?: "")
    val appwriteDatabaseId: Flow<String> = _appwriteDatabaseId.asStateFlow()

    private val _appwriteCollectionId = MutableStateFlow(encryptedPrefs.getString("appwrite_collection_id", "") ?: "")
    val appwriteCollectionId: Flow<String> = _appwriteCollectionId.asStateFlow()

    suspend fun setAppwriteConfig(endpoint: String, projectId: String, databaseId: String, collectionId: String) {
        encryptedPrefs.edit()
            .putString("appwrite_endpoint", endpoint.trim())
            .putString("appwrite_project_id", projectId.trim())
            .putString("appwrite_database_id", databaseId.trim())
            .putString("appwrite_collection_id", collectionId.trim())
            .apply()
        _appwriteEndpoint.value = endpoint.trim()
        _appwriteProjectId.value = projectId.trim()
        _appwriteDatabaseId.value = databaseId.trim()
        _appwriteCollectionId.value = collectionId.trim()
    }

    private val _geminiApiKey = MutableStateFlow(encryptedPrefs.getString("gemini_api_key", "") ?: "")
    val geminiApiKey: Flow<String> = _geminiApiKey.asStateFlow()

    suspend fun setGeminiApiKey(apiKey: String) {
        encryptedPrefs.edit().putString("gemini_api_key", apiKey.trim()).apply()
        _geminiApiKey.value = apiKey.trim()
    }

    // ── Device identity & cloud sync ─────────────────────────────────────────

    suspend fun getOrCreateDeviceId(): String {
        val existing = dataStore.data.first()[DEVICE_UUID]
        if (!existing.isNullOrBlank()) return existing
        val newId = UUID.randomUUID().toString()
        dataStore.edit { prefs -> prefs[DEVICE_UUID] = newId }
        return newId
    }

    private val _supabaseUrl = MutableStateFlow(encryptedPrefs.getString("supabase_url", "") ?: "")
    val supabaseUrl: Flow<String> = _supabaseUrl.asStateFlow()

    private val _supabaseAnonKey = MutableStateFlow(encryptedPrefs.getString("supabase_anon_key", "") ?: "")
    val supabaseAnonKey: Flow<String> = _supabaseAnonKey.asStateFlow()

    suspend fun setSupabaseConfig(url: String, anonKey: String) {
        encryptedPrefs.edit()
            .putString("supabase_url", url.trim())
            .putString("supabase_anon_key", anonKey.trim())
            .apply()
        _supabaseUrl.value = url.trim()
        _supabaseAnonKey.value = anonKey.trim()
    }

    // ── Generic list helper ──────────────────────────────────────────────────

    private suspend fun <T> appendToListPref(
        key: Preferences.Key<String>,
        item: T,
        listSerializer: KSerializer<List<T>>
    ) {
        dataStore.edit { prefs ->
            val current = try {
                json.decodeFromString(listSerializer, prefs[key] ?: "[]")
            } catch (_: Exception) { emptyList() }
            prefs[key] = json.encodeToString(listSerializer, current + item)
        }
    }

    // ── Daily work plan items ────────────────────────────────────────────────

    val workPlanItems: Flow<List<WorkPlanItem>> = dataStore.data.map { prefs ->
        val raw = prefs[WORK_PLAN_ITEMS] ?: "[]"
        try { json.decodeFromString(ListSerializer(WorkPlanItem.serializer()), raw) }
        catch (_: Exception) { emptyList() }
    }

    suspend fun saveWorkPlanItems(items: List<WorkPlanItem>) {
        dataStore.edit { prefs ->
            prefs[WORK_PLAN_ITEMS] = json.encodeToString(ListSerializer(WorkPlanItem.serializer()), items)
        }
    }

    suspend fun addWorkPlanItem(item: WorkPlanItem) {
        appendToListPref(WORK_PLAN_ITEMS, item, ListSerializer(WorkPlanItem.serializer()))
    }

    suspend fun removeWorkPlanItem(id: String) {
        dataStore.edit { prefs ->
            val raw = prefs[WORK_PLAN_ITEMS] ?: "[]"
            val filtered = try {
                json.decodeFromString(ListSerializer(WorkPlanItem.serializer()), raw).filter { it.id != id }
            } catch (_: Exception) { emptyList() }
            prefs[WORK_PLAN_ITEMS] = json.encodeToString(ListSerializer(WorkPlanItem.serializer()), filtered)
        }
    }

    // ── Weekly plan items ────────────────────────────────────────────────────

    val weeklyPlanItems: Flow<List<PlanItem>> = dataStore.data.map { prefs ->
        val raw = prefs[WEEKLY_PLAN_ITEMS] ?: "[]"
        try { json.decodeFromString(ListSerializer(PlanItem.serializer()), raw) }
        catch (_: Exception) { emptyList() }
    }

    suspend fun saveWeeklyPlanItems(items: List<PlanItem>) {
        dataStore.edit { prefs ->
            prefs[WEEKLY_PLAN_ITEMS] = json.encodeToString(ListSerializer(PlanItem.serializer()), items)
        }
    }

    suspend fun addWeeklyPlanItem(item: PlanItem) {
        appendToListPref(WEEKLY_PLAN_ITEMS, item, ListSerializer(PlanItem.serializer()))
    }

    suspend fun removeWeeklyPlanItem(id: String) {
        dataStore.edit { prefs ->
            val raw = prefs[WEEKLY_PLAN_ITEMS] ?: "[]"
            val filtered = try {
                json.decodeFromString(ListSerializer(PlanItem.serializer()), raw).filter { it.id != id }
            } catch (_: Exception) { emptyList() }
            prefs[WEEKLY_PLAN_ITEMS] = json.encodeToString(ListSerializer(PlanItem.serializer()), filtered)
        }
    }

    // ── Monthly plan items ───────────────────────────────────────────────────

    val monthlyPlanItems: Flow<List<PlanItem>> = dataStore.data.map { prefs ->
        val raw = prefs[MONTHLY_PLAN_ITEMS] ?: "[]"
        try { json.decodeFromString(ListSerializer(PlanItem.serializer()), raw) }
        catch (_: Exception) { emptyList() }
    }

    suspend fun saveMonthlyPlanItems(items: List<PlanItem>) {
        dataStore.edit { prefs ->
            prefs[MONTHLY_PLAN_ITEMS] = json.encodeToString(ListSerializer(PlanItem.serializer()), items)
        }
    }

    suspend fun addMonthlyPlanItem(item: PlanItem) {
        appendToListPref(MONTHLY_PLAN_ITEMS, item, ListSerializer(PlanItem.serializer()))
    }

    suspend fun removeMonthlyPlanItem(id: String) {
        dataStore.edit { prefs ->
            val raw = prefs[MONTHLY_PLAN_ITEMS] ?: "[]"
            val filtered = try {
                json.decodeFromString(ListSerializer(PlanItem.serializer()), raw).filter { it.id != id }
            } catch (_: Exception) { emptyList() }
            prefs[MONTHLY_PLAN_ITEMS] = json.encodeToString(ListSerializer(PlanItem.serializer()), filtered)
        }
    }
}
