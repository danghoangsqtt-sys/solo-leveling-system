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

@Singleton
class SettingsManager @Inject constructor(
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
        private val APPWRITE_API_KEY = stringPreferencesKey("appwrite_api_key")
        private val APPWRITE_ENDPOINT = stringPreferencesKey("appwrite_endpoint")
        private val APPWRITE_PROJECT_ID = stringPreferencesKey("appwrite_project_id")
        private val APPWRITE_DATABASE_ID = stringPreferencesKey("appwrite_database_id")
        private val APPWRITE_COLLECTION_ID = stringPreferencesKey("appwrite_collection_id")

        const val DEFAULT_APPWRITE_ENDPOINT = "https://sgp.cloud.appwrite.io/v1"
        const val DEFAULT_APPWRITE_PROJECT_ID = "698fe17d00005a81c862"
        const val DEFAULT_APPWRITE_DATABASE_ID = "698fe1b90013010bd402"
        const val DEFAULT_APPWRITE_COLLECTION_ID = "69c4e168003b8a09bff8"
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

    val appwriteApiKey: Flow<String> = dataStore.data.map { prefs -> prefs[APPWRITE_API_KEY] ?: "" }

    suspend fun setAppwriteApiKey(key: String) {
        dataStore.edit { prefs -> prefs[APPWRITE_API_KEY] = key.trim() }
    }

    val appwriteEndpoint: Flow<String> = dataStore.data.map { prefs ->
        prefs[APPWRITE_ENDPOINT] ?: DEFAULT_APPWRITE_ENDPOINT
    }
    val appwriteProjectId: Flow<String> = dataStore.data.map { prefs ->
        prefs[APPWRITE_PROJECT_ID] ?: DEFAULT_APPWRITE_PROJECT_ID
    }
    val appwriteDatabaseId: Flow<String> = dataStore.data.map { prefs ->
        prefs[APPWRITE_DATABASE_ID] ?: DEFAULT_APPWRITE_DATABASE_ID
    }
    val appwriteCollectionId: Flow<String> = dataStore.data.map { prefs ->
        prefs[APPWRITE_COLLECTION_ID] ?: DEFAULT_APPWRITE_COLLECTION_ID
    }

    suspend fun setAppwriteConfig(endpoint: String, projectId: String, databaseId: String, collectionId: String) {
        dataStore.edit { prefs ->
            prefs[APPWRITE_ENDPOINT] = endpoint.trim()
            prefs[APPWRITE_PROJECT_ID] = projectId.trim()
            prefs[APPWRITE_DATABASE_ID] = databaseId.trim()
            prefs[APPWRITE_COLLECTION_ID] = collectionId.trim()
        }
    }

    val geminiApiKey: Flow<String> = dataStore.data.map { prefs ->
        prefs[GEMINI_API_KEY] ?: ""
    }

    suspend fun setGeminiApiKey(apiKey: String) {
        dataStore.edit { prefs -> prefs[GEMINI_API_KEY] = apiKey.trim() }
    }

    // ── Device identity & cloud sync ─────────────────────────────────────────

    suspend fun getOrCreateDeviceId(): String {
        val existing = dataStore.data.first()[DEVICE_UUID]
        if (!existing.isNullOrBlank()) return existing
        val newId = UUID.randomUUID().toString()
        dataStore.edit { prefs -> prefs[DEVICE_UUID] = newId }
        return newId
    }

    val supabaseUrl: Flow<String> = dataStore.data.map { prefs ->
        prefs[SUPABASE_URL] ?: ""
    }

    val supabaseAnonKey: Flow<String> = dataStore.data.map { prefs ->
        prefs[SUPABASE_ANON_KEY] ?: ""
    }

    suspend fun setSupabaseConfig(url: String, anonKey: String) {
        dataStore.edit { prefs ->
            prefs[SUPABASE_URL] = url.trim()
            prefs[SUPABASE_ANON_KEY] = anonKey.trim()
        }
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
