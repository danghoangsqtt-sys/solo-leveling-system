package com.systemleveling.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.systemleveling.core.model.PlanItem
import com.systemleveling.core.model.WorkPlanItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
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
    }

    val geminiApiKey: Flow<String> = dataStore.data.map { prefs ->
        prefs[GEMINI_API_KEY] ?: ""
    }

    suspend fun setGeminiApiKey(apiKey: String) {
        dataStore.edit { prefs -> prefs[GEMINI_API_KEY] = apiKey.trim() }
    }

    // ── Daily work plan items ────────────────────────────────────────────────

    val workPlanItems: Flow<List<WorkPlanItem>> = dataStore.data.map { prefs ->
        val raw = prefs[WORK_PLAN_ITEMS] ?: "[]"
        try { json.decodeFromString(ListSerializer(WorkPlanItem.serializer()), raw) }
        catch (_: Exception) { emptyList() }
    }

    suspend fun saveWorkPlanItems(items: List<WorkPlanItem>) {
        val encoded = json.encodeToString(ListSerializer(WorkPlanItem.serializer()), items)
        dataStore.edit { prefs -> prefs[WORK_PLAN_ITEMS] = encoded }
    }

    suspend fun addWorkPlanItem(item: WorkPlanItem) {
        dataStore.edit { prefs ->
            val raw = prefs[WORK_PLAN_ITEMS] ?: "[]"
            val current = try { json.decodeFromString(ListSerializer(WorkPlanItem.serializer()), raw) }
                          catch (_: Exception) { emptyList() }
            prefs[WORK_PLAN_ITEMS] = json.encodeToString(
                ListSerializer(WorkPlanItem.serializer()), current + item
            )
        }
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
        dataStore.edit { prefs ->
            val raw = prefs[WEEKLY_PLAN_ITEMS] ?: "[]"
            val current = try { json.decodeFromString(ListSerializer(PlanItem.serializer()), raw) }
                          catch (_: Exception) { emptyList() }
            prefs[WEEKLY_PLAN_ITEMS] = json.encodeToString(ListSerializer(PlanItem.serializer()), current + item)
        }
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
        dataStore.edit { prefs ->
            val raw = prefs[MONTHLY_PLAN_ITEMS] ?: "[]"
            val current = try { json.decodeFromString(ListSerializer(PlanItem.serializer()), raw) }
                          catch (_: Exception) { emptyList() }
            prefs[MONTHLY_PLAN_ITEMS] = json.encodeToString(ListSerializer(PlanItem.serializer()), current + item)
        }
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
