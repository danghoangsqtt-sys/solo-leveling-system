package com.systemleveling.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.systemleveling.core.worker.DailyQuestWorker
import com.systemleveling.core.worker.EndOfDayWorker
import com.systemleveling.core.worker.QuestReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class SystemLevelingApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleDailyWorkers()
    }

    private fun scheduleDailyWorkers() {
        val workManager = WorkManager.getInstance(this)

        // Daily Quest Generation — runs every 24 hours, first at next midnight
        val questGenerationRequest = PeriodicWorkRequestBuilder<DailyQuestWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(getDelayToNextMidnight(), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            DailyQuestWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            questGenerationRequest
        )

        // End of Day Processing — runs every 24 hours, first at 22:00 today
        val endOfDayRequest = PeriodicWorkRequestBuilder<EndOfDayWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(getDelayTo2200(), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            EndOfDayWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            endOfDayRequest
        )

        // Quest Reminder — runs every 30 minutes to urge completion of pending quests
        val reminderRequest = PeriodicWorkRequestBuilder<QuestReminderWorker>(
            30, TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            QuestReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }

    private fun getDelayToNextMidnight(): Long {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return midnight.timeInMillis - now.timeInMillis
    }

    private fun getDelayTo2200(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
