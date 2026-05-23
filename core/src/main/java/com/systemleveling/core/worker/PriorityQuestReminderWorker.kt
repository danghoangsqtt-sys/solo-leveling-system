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

private val LEARNING_KEYWORDS = setOf(
    "học", "tiếng", "english", "study", "library", "reading", "luyện", "nghe", "đọc", "viết"
)

/**
 * Runs every 15 minutes and fires priority notifications for quests whose deadline
 * is within 60 minutes.
 *
 * - Non-learning quests → full-screen heads-up + countdown, vibrates every cycle (continuous)
 * - Learning quests    → single gentle reminder (setOnlyAlertOnce), countdown shown silently
 */
@HiltWorker
class PriorityQuestReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val questDao: QuestDao,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val todayMidnight = getTodayMidnight()
            val todayEnd = todayMidnight + 86_400_000L
            val nowMin = currentMinuteOfDay()
            val nowMs = System.currentTimeMillis()

            val pendingQuests = questDao.getPendingQuestsByDateSync(todayMidnight, todayEnd)
                .filter { !it.isHealthReminder }

            if (pendingQuests.isEmpty()) return Result.success()

            pendingQuests.forEach { quest ->
                val endMin = parseTimeToMinutes(quest.timeEnd ?: return@forEach)
                val minutesLeft = endMin - nowMin

                // Only alert for quests expiring within 60 min (or already expired up to 5 min ago)
                if (minutesLeft !in -5..60) return@forEach

                val notifId = 6000 + (abs(quest.id.hashCode()) % 3000)
                val deadlineMs = nowMs + (minutesLeft * 60_000L)

                if (isLearningCategory(quest.category)) {
                    notificationHelper.notifyStudyReminder(
                        title = quest.title,
                        minutesLeft = minutesLeft,
                        notifId = notifId,
                        deadlineMs = deadlineMs
                    )
                } else {
                    notificationHelper.notifyPriorityQuestAlert(
                        title = quest.title,
                        minutesLeft = minutesLeft,
                        notifId = notifId,
                        deadlineMs = deadlineMs
                    )
                }
            }

            Result.success()
        } catch (_: Exception) {
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }

    private fun isLearningCategory(category: String): Boolean {
        val lower = category.lowercase()
        return LEARNING_KEYWORDS.any { lower.contains(it) }
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
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    companion object {
        const val WORK_NAME = "priority_quest_reminder"
    }
}
