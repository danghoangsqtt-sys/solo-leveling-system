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

        const val NOTIFICATION_QUEST_NEW = 1001
        const val NOTIFICATION_QUEST_DEADLINE = 1002
        const val NOTIFICATION_DAILY_SUMMARY = 2001
        const val NOTIFICATION_HEALTH_WATER = 3001
        const val NOTIFICATION_HEALTH_STANDUP = 3002
        const val NOTIFICATION_PENALTY = 4001
    }

    init {
        createNotificationChannels()
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

        manager.createNotificationChannels(
            listOf(questChannel, summaryChannel, healthChannel, penaltyChannel)
        )
    }

    /**
     * Show notification for new daily quests generated.
     */
    fun notifyNewQuests(questCount: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_QUEST)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("⚔️ Nhiệm Vụ Mới!")
            .setContentText("$questCount nhiệm vụ đang chờ bạn hôm nay. Sẵn sàng chiến đấu!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$questCount nhiệm vụ đang chờ bạn hôm nay.\n🔥 Hoàn thành tất cả để duy trì streak!\n⚡ Sẵn sàng chiến đấu, chiến binh!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
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

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_SUMMARY)
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .setContentTitle("$emoji Báo Cáo Ngày — Hạng $grade")
            .setContentText("Hoàn thành ${(completionRate * 100).toInt()}% | +${expEarned}EXP. Xem chi tiết!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Hoàn thành ${(completionRate * 100).toInt()}% nhiệm vụ\n⚡ +${expEarned} EXP\n📋 AI đã tạo kế hoạch cho ngày mai\nNhấn để xem chi tiết và lập kế hoạch!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
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
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("💧 Hydration Quest!")
            .setContentText(messages.random())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_HEALTH_WATER, notification)
    }

    /**
     * Show stand-up reminder notification.
     */
    fun notifyStandUpReminder() {
        val notification = NotificationCompat.Builder(context, CHANNEL_HEALTH)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("🚶 Đứng Dậy & Vận Động!")
            .setContentText("Ngồi quá lâu rồi! Đứng dậy đi lại 5 phút, giãn cơ để duy trì AGI!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_HEALTH_STANDUP, notification)
    }

    /**
     * Show penalty warning notification.
     */
    fun notifyPenaltyWarning(debtPoints: Int, failedQuests: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_PENALTY)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
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
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_PENALTY, notification)
    }
}
