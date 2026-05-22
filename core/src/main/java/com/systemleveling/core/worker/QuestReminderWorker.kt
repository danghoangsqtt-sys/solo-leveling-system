package com.systemleveling.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import kotlin.math.abs

/**
 * Periodic reminder worker — runs every 30 minutes while quests are pending.
 *
 * Logic:
 *   1. Collect today's PENDING/IN_PROGRESS quests.
 *   2. For each quest whose timeEnd is ≤ 30 min away, fire a deadline warning.
 *   3. Fire a general focus-urge notification if any quests remain pending.
 */
@HiltWorker
class QuestReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val questDao: QuestDao,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val todayMidnight = getTodayMidnight()
            val todayEnd = todayMidnight + 86400000L

            val pendingQuests = questDao.getPendingQuestsByDateSync(todayMidnight, todayEnd)
                .filter { !it.isHealthReminder }

            if (pendingQuests.isEmpty()) return Result.success()

            val nowMinutes = currentMinuteOfDay()

            // Fire deadline warnings for quests expiring within 30 minutes
            pendingQuests.forEach { quest ->
                val endMin = parseTimeToMinutes(quest.timeEnd ?: return@forEach)
                val minutesLeft = endMin - nowMinutes
                if (minutesLeft in -5..30) {
                    val notifId = 5000 + (abs(quest.id.hashCode()) % 4000)
                    notificationHelper.notifyQuestDeadlineWarning(quest.title, minutesLeft, notifId)
                }
            }

            // General focus urge — use the quest with smallest timeEnd as the "most urgent"
            val mostUrgent = pendingQuests.minByOrNull { parseTimeToMinutes(it.timeEnd ?: "23:59") }
            notificationHelper.notifyQuestFocusUrge(pendingQuests.size, mostUrgent?.title ?: "")

            Result.success()
        } catch (_: Exception) {
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }

    private fun parseTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        if (parts.size < 2) return Int.MAX_VALUE
        val h = parts[0].toIntOrNull() ?: return Int.MAX_VALUE
        val m = parts[1].toIntOrNull() ?: return Int.MAX_VALUE
        return h * 60 + m
    }

    private fun currentMinuteOfDay(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }

    private fun getTodayMidnight(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    companion object {
        const val WORK_NAME = "quest_reminder"
    }
}
