package com.systemleveling.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.systemleveling.core.network.AiQuestGeneratorService
import com.systemleveling.core.notification.NotificationHelper
import com.systemleveling.core.settings.SettingsManager
import kotlinx.coroutines.flow.first
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

/**
 * WorkManager worker that generates daily quests.
 * Scheduled to run at 00:00 each day, or when app opens and no quests exist for today.
 * Sends a notification to wake the user up to their new quests.
 */
@HiltWorker
class DailyQuestWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val aiQuestGenerator: AiQuestGeneratorService,
    private val notificationHelper: NotificationHelper,
    private val settingsManager: SettingsManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val todayMidnight = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val apiKey = settingsManager.geminiApiKey.first()
            val quests = aiQuestGenerator.generateDailyQuests(apiKey, todayMidnight)

            if (quests.isNotEmpty()) {
                notificationHelper.notifyNewQuests(quests.size)
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "daily_quest_generation"
    }
}
