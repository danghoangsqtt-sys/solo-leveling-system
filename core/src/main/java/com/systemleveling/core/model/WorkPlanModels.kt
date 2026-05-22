package com.systemleveling.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

enum class WorkPriority(val label: String, val score: Int) {
    CRITICAL("🔥 Cực kỳ gấp", 100),
    HIGH("⚡ Quan trọng", 75),
    NORMAL("📋 Thường xuyên", 50),
    LOW("💤 Có thể trì hoãn", 25)
}

/** Scope of a plan item — which planning horizon it belongs to. */
enum class PlanScope(val label: String, val icon: String) {
    DAILY("Ngày mai", "📅"),
    WEEKLY("Tuần này", "📆"),
    MONTHLY("Tháng này", "🗓️")
}

@Serializable
data class WorkPlanItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val note: String = "",
    val category: String = "general",
    val priority: String = "NORMAL", // WorkPriority.name
    val deadline: String = "", // "HH:mm" same-day OR "dd/MM" future date
    val estimatedMinutes: Int = 30
) {
    val workPriority: WorkPriority get() =
        try { WorkPriority.valueOf(priority) } catch (_: Exception) { WorkPriority.NORMAL }
}

/**
 * Plan item for weekly/monthly horizons.
 * Daily plans reuse [WorkPlanItem] stored as workPlanItems in SettingsManager.
 */
@Serializable
data class PlanItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val note: String = "",
    val priority: String = "NORMAL",  // WorkPriority.name
    val scope: String = "WEEKLY",     // PlanScope.name
    val deadline: String = "",        // "dd/MM" target date or week/month label
    val estimatedMinutes: Int = 60
) {
    val workPriority: WorkPriority get() =
        try { WorkPriority.valueOf(priority) } catch (_: Exception) { WorkPriority.NORMAL }
    val planScope: PlanScope get() =
        try { PlanScope.valueOf(scope) } catch (_: Exception) { PlanScope.WEEKLY }
}
