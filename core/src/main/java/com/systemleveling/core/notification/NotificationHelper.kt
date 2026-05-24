package com.systemleveling.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import com.systemleveling.core.R

/**
 * Centralized notification helper for all System Leveling notifications.
 * Uses game-themed notification channels with custom sounds.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_QUEST = "quest_notifications"
        const val CHANNEL_DAILY_SUMMARY = "daily_summary"
        const val CHANNEL_HEALTH = "health_reminders"
        const val CHANNEL_PENALTY = "penalty_warnings"
        const val CHANNEL_PRIORITY = "priority_alerts"
        const val CHANNEL_STUDY = "study_reminders"

        const val NOTIFICATION_QUEST_NEW = 1001
        const val NOTIFICATION_QUEST_DEADLINE = 1002
        const val NOTIFICATION_QUEST_FOCUS_URGE = 1003
        const val NOTIFICATION_DAILY_SUMMARY = 2001
        const val NOTIFICATION_HEALTH_WATER = 3001
        const val NOTIFICATION_HEALTH_STANDUP = 3002
        const val NOTIFICATION_PENALTY = 4001

        const val ACTION_PRIORITY_ALERT = "com.systemleveling.app.PRIORITY_ALERT"
        const val ACTION_SNOOZE_QUEST = "com.systemleveling.core.SNOOZE_QUEST"
        const val EXTRA_NOTIF_ID = "extra_notif_id"
        const val EXTRA_QUEST_TITLE = "extra_quest_title"
        const val EXTRA_DEADLINE_MS = "extra_deadline_ms"
        const val EXTRA_MINUTES_LEFT = "extra_minutes_left"
    }

    init {
        createNotificationChannels()
    }

    private fun makePendingIntent(requestCode: Int, navDestination: String): PendingIntent {
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("nav_destination", navDestination)
            }
        return PendingIntent.getActivity(
            context, requestCode, launchIntent ?: Intent(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannels() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Quest Channel — game achievement sound
        val questChannel = NotificationChannel(
            CHANNEL_QUEST,
            "Nhiệm Vụ",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Thông báo nhiệm vụ mới và deadline"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200, 100, 200) // Double pulse
            setShowBadge(true)
        }

        // Daily Summary Channel
        val summaryChannel = NotificationChannel(
            CHANNEL_DAILY_SUMMARY,
            "Báo Cáo Ngày",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Thông báo tổng kết cuối ngày"
            enableVibration(true)
        }

        // Health Reminders Channel — gentle notification
        val healthChannel = NotificationChannel(
            CHANNEL_HEALTH,
            "Nhắc Nhở Sức Khỏe",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Nhắc uống nước, đứng dậy vận động"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 100) // Gentle single pulse
        }

        // Penalty Channel — urgent sound
        val penaltyChannel = NotificationChannel(
            CHANNEL_PENALTY,
            "Cảnh Báo Hình Phạt",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Cảnh báo khi sắp bị phạt"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300, 200, 300) // Triple pulse
        }

        // Priority Alerts — full-screen, alarm-level, forces attention
        val priorityChannel = NotificationChannel(
            CHANNEL_PRIORITY,
            "Nhắc Nhở Ưu Tiên",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Nhắc nhở bắt buộc cho nhiệm vụ sắp hết hạn"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 400, 200, 400) // Urgent quad pulse
            setShowBadge(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        // Study Reminders — single alert, focus-mode, no repeat buzz
        val studyChannel = NotificationChannel(
            CHANNEL_STUDY,
            "Nhắc Nhở Học Tập",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Nhắc nhở nhẹ nhàng khi bắt đầu học"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200, 100, 200) // Gentle double pulse
            setShowBadge(true)
        }

        manager.createNotificationChannels(
            listOf(questChannel, summaryChannel, healthChannel, penaltyChannel, priorityChannel, studyChannel)
        )
    }

    /**
     * Show notification for new daily quests generated.
     */
    fun notifyNewQuests(questCount: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_QUEST)
            .setSmallIcon(R.drawable.ic_quest)
            .setContentTitle("⚔️ Nhiệm Vụ Mới!")
            .setContentText("$questCount nhiệm vụ đang chờ bạn hôm nay. Sẵn sàng chiến đấu!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$questCount nhiệm vụ đang chờ bạn hôm nay.\n🔥 Hoàn thành tất cả để duy trì streak!\n⚡ Sẵn sàng chiến đấu, chiến binh!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(makePendingIntent(NOTIFICATION_QUEST_NEW, "quests"))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_QUEST_NEW, notification)
    }

    /**
     * Show notification for daily summary ready.
     */
    fun notifyDailySummary(grade: String, completionRate: Double, expEarned: Int) {
        val emoji = when (grade) {
            "S" -> "🏆"
            "A" -> "⭐"
            "B" -> "✅"
            "C" -> "📊"
            else -> "⚠️"
        }

        // Deep-link intent → opens MainActivity which navigates to daily_summary
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("nav_destination", "daily_summary")
            }
        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(context, NOTIFICATION_DAILY_SUMMARY, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else null

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_SUMMARY)
            .setSmallIcon(R.drawable.ic_quest)
            .setContentTitle("$emoji Báo Cáo Ngày — Hạng $grade")
            .setContentText("Hoàn thành ${(completionRate * 100).toInt()}% | +${expEarned}EXP. Xem chi tiết!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Hoàn thành ${(completionRate * 100).toInt()}% nhiệm vụ\n⚡ +${expEarned} EXP\n📋 AI đã tạo kế hoạch cho ngày mai\nNhấn để xem chi tiết và lập kế hoạch!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .apply { if (pendingIntent != null) setContentIntent(pendingIntent) }
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_DAILY_SUMMARY, notification)
    }

    /**
     * Show water reminder notification.
     */
    fun notifyWaterReminder() {
        val messages = listOf(
            "💧 Uống nước đi chiến binh! Duy trì VIT stat!",
            "💧 Hydration Check! Cơ thể cần nước để duy trì sức mạnh.",
            "💧 Đã đến lúc uống nước rồi! Đừng để VIT giảm.",
            "💧 Chiến binh mạnh nhờ cơ thể khỏe — uống nước ngay!"
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_HEALTH)
            .setSmallIcon(R.drawable.ic_health)
            .setContentTitle("💧 Hydration Quest!")
            .setContentText(messages.random())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(makePendingIntent(NOTIFICATION_HEALTH_WATER, "quests"))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_HEALTH_WATER, notification)
    }

    /**
     * Show stand-up reminder notification.
     */
    fun notifyStandUpReminder() {
        val notification = NotificationCompat.Builder(context, CHANNEL_HEALTH)
            .setSmallIcon(R.drawable.ic_health)
            .setContentTitle("🚶 Đứng Dậy & Vận Động!")
            .setContentText("Ngồi quá lâu rồi! Đứng dậy đi lại 5 phút, giãn cơ để duy trì AGI!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(makePendingIntent(NOTIFICATION_HEALTH_STANDUP, "quests"))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_HEALTH_STANDUP, notification)
    }

    /**
     * Urge user to complete a specific quest before its deadline.
     * notificationId is quest-specific so each quest gets its own slot.
     */
    fun notifyQuestDeadlineWarning(questTitle: String, minutesLeft: Int, notificationId: Int = NOTIFICATION_QUEST_DEADLINE) {
        val urgency = when {
            minutesLeft <= 0 -> "⏰ ĐÃ QUÁ HẠN!"
            minutesLeft <= 10 -> "🚨 CÒN $minutesLeft PHÚT!"
            minutesLeft <= 30 -> "⚡ Còn $minutesLeft phút!"
            else -> "⏳ Còn $minutesLeft phút"
        }
        val body = when {
            minutesLeft <= 0 -> "\"$questTitle\" đã hết hạn. Hoàn thành ngay hoặc nhận Debt Point!"
            minutesLeft <= 10 -> "\"$questTitle\" sắp hết hạn — tập trung ngay bây giờ!"
            else -> "\"$questTitle\" — đừng để thất bại vì thiếu tập trung!"
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_QUEST)
            .setSmallIcon(R.drawable.ic_system_alert)
            .setContentTitle("$urgency QUEST DEADLINE")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setContentIntent(makePendingIntent(notificationId, "quests"))
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(notificationId, notification)
    }

    /**
     * Periodic focus urge — fires every ~30 min while PENDING quests exist.
     */
    fun notifyQuestFocusUrge(pendingCount: Int, mostUrgentTitle: String) {
        val messages = listOf(
            "Chiến binh! Còn $pendingCount nhiệm vụ đang chờ. Đừng lãng phí thời gian!",
            "$pendingCount nhiệm vụ chưa hoàn thành — streak của bạn đang bị đe dọa!",
            "Tập trung! \"$mostUrgentTitle\" và $pendingCount nhiệm vụ khác đang chờ!",
            "Hệ thống nhắc nhở: $pendingCount nhiệm vụ → hoàn thành ngay để tránh phạt!"
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_QUEST)
            .setSmallIcon(R.drawable.ic_quest)
            .setContentTitle("⚔️ Tập Trung Chiến Đấu!")
            .setContentText(messages.random())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(makePendingIntent(NOTIFICATION_QUEST_FOCUS_URGE, "quests"))
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIFICATION_QUEST_FOCUS_URGE, notification)
    }

    /**
     * Priority alert for non-learning quests: full-screen intent + countdown timer + continuous buzz.
     * Fires every 15 min via PriorityQuestReminderWorker while deadline is within 60 min.
     */
    fun notifyPriorityQuestAlert(
        title: String,
        minutesLeft: Int,
        notifId: Int,
        deadlineMs: Long
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Full-screen intent → PriorityAlertActivity when phone is locked
        val fullScreenIntent = Intent(ACTION_PRIORITY_ALERT).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_QUEST_TITLE, title)
            putExtra(EXTRA_MINUTES_LEFT, minutesLeft)
            putExtra(EXTRA_DEADLINE_MS, deadlineMs)
            putExtra(EXTRA_NOTIF_ID, notifId)
        }
        val fullScreenPi = PendingIntent.getActivity(
            context, notifId, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action → NotificationActionReceiver (cancels for now; worker re-fires in 15 min)
        val snoozeIntent = Intent(ACTION_SNOOZE_QUEST).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_NOTIF_ID, notifId)
        }
        val snoozePi = PendingIntent.getBroadcast(
            context, notifId + 1000, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val urgencyText = when {
            minutesLeft <= 0 -> "⏰ ĐÃ HẾT GIỜ — hoàn thành ngay!"
            minutesLeft <= 5 -> "🚨 CÒN $minutesLeft PHÚT — TẬP TRUNG NGAY!"
            minutesLeft <= 15 -> "⚡ Còn $minutesLeft phút — đừng trì hoãn thêm!"
            else -> "⏳ Còn $minutesLeft phút — bắt đầu ngay để kịp!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_PRIORITY)
            .setSmallIcon(R.drawable.ic_system_alert)
            .setContentTitle("🔴 BẮT BUỘC: $title")
            .setContentText(urgencyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(urgencyText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 400, 200, 400))
            .setAutoCancel(false)
            .setOngoing(minutesLeft <= 0)
            .setWhen(deadlineMs)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setShowWhen(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPi, true)
            .setContentIntent(fullScreenPi)
            .addAction(R.drawable.ic_snooze, "⏱ Trì Hoãn 10p", snoozePi)
            .build()

        manager.notify(notifId, notification)
    }

    /**
     * Study reminder for learning quests: single gentle alert with countdown.
     * Uses setOnlyAlertOnce so only the first show vibrates — no repeated disturbance.
     */
    fun notifyStudyReminder(
        title: String,
        minutesLeft: Int,
        notifId: Int,
        deadlineMs: Long
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val dismissIntent = Intent(ACTION_SNOOZE_QUEST).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_NOTIF_ID, notifId)
        }
        val dismissPi = PendingIntent.getBroadcast(
            context, notifId + 2000, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = when {
            minutesLeft <= 0 -> "📚 Bắt đầu học ngay — kiến thức là sức mạnh!"
            else -> "📚 Còn $minutesLeft phút • Chuẩn bị và tập trung học tập"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_STUDY)
            .setSmallIcon(R.drawable.ic_health)
            .setContentTitle("📚 Học Tập: $title")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setWhen(deadlineMs)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setShowWhen(true)
            .addAction(R.drawable.ic_dismiss, "Đã Hiểu", dismissPi)
            .setContentIntent(makePendingIntent(notifId, "quests"))
            .build()

        manager.notify(notifId, notification)
    }

    /**
     * Show penalty warning notification.
     */
    fun notifyPenaltyWarning(debtPoints: Int, failedQuests: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_PENALTY)
            .setSmallIcon(R.drawable.ic_system_alert)
            .setContentTitle("⚠️ Cảnh Báo Penalty!")
            .setContentText("$failedQuests nhiệm vụ thất bại! Tổng nợ: $debtPoints Debt Points")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$failedQuests nhiệm vụ thất bại hôm nay!\n" +
                    "Tổng Debt Points: $debtPoints\n" +
                    when {
                        debtPoints >= 10 -> "🚨 NGUY HIỂM: Sắp bị Level Down!"
                        debtPoints >= 5 -> "⚠️ Đã mất 10% Gold!"
                        debtPoints >= 3 -> "⚡ Cẩn thận! Sắp đến ngưỡng phạt!"
                        else -> "Hoàn thành nhiệm vụ để giảm Debt Points!"
                    }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(makePendingIntent(NOTIFICATION_PENALTY, "quests"))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_PENALTY, notification)
    }
}
