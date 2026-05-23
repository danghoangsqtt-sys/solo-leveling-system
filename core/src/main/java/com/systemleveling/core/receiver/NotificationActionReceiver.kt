package com.systemleveling.core.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.systemleveling.core.notification.NotificationHelper

/**
 * Handles action buttons from priority/study notifications.
 * - SNOOZE: cancels the notification (PriorityQuestReminderWorker will re-fire in ~15 min)
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notifId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIF_ID, -1)
        if (notifId == -1) return

        when (intent.action) {
            NotificationHelper.ACTION_SNOOZE_QUEST -> {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(notifId)
            }
        }
    }
}
