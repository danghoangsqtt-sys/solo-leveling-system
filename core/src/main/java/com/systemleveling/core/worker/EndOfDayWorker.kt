package com.systemleveling.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.systemleveling.core.engine.DailySummaryService
import com.systemleveling.core.engine.PenaltyEngine
import com.systemleveling.core.notification.NotificationHelper
import com.systemleveling.core.settings.SettingsManager
import com.systemleveling.core.sync.CloudSyncManager
import kotlinx.coroutines.flow.first
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

/**
 * WorkManager worker that processes end-of-day:
 * 1. Marks uncompleted quests as FAILED via PenaltyEngine
 * 2. Generates Daily Summary report via DailySummaryService
 * 3. Sends notification to user to review summary + plan tomorrow
 *
 * Scheduled to run at 22:00 each day.
 */
@HiltWorker
class EndOfDayWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val penaltyEngine: PenaltyEngine,
    private val dailySummaryService: DailySummaryService,
    private val notificationHelper: NotificationHelper,
    private val settingsManager: SettingsManager,
    private val cloudSyncManager: CloudSyncManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val todayMidnight = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val tomorrowMidnight = todayMidnight + 86400000L

            // 1. Process penalties for failed quests
            val failCount = penaltyEngine.processEndOfDay(todayMidnight, tomorrowMidnight)

            val apiKey = settingsManager.geminiApiKey.first()

            // 2. Generate daily summary (includes AI journal + tomorrow plan)
            val summary = dailySummaryService.generateDailySummary(
                todayMidnight, tomorrowMidnight, apiKey
            )

            // 3. Push summary history to Supabase backend
            cloudSyncManager.pushDailyHistory(summary)

            // 4. Send notification
            val grade = when {
                summary.completionRate >= 0.95 -> "S"
                summary.completionRate >= 0.85 -> "A"
                summary.completionRate >= 0.70 -> "B"
                summary.completionRate >= 0.50 -> "C"
                summary.completionRate >= 0.30 -> "D"
                else -> "F"
            }

            notificationHelper.notifyDailySummary(grade, summary.completionRate, summary.expEarned)

            // Send penalty warning if applicable
            if (failCount > 0) {
                notificationHelper.notifyPenaltyWarning(summary.currentDebtTotal, failCount)
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "end_of_day_processing"
    }
}
