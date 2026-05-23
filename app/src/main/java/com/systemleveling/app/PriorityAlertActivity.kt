package com.systemleveling.app

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemleveling.core.notification.NotificationHelper
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * Full-screen alert activity shown on the locked screen when a priority quest is about to expire.
 * When the phone is awake, Android shows this as a heads-up notification instead.
 *
 * Launched by the fullScreenIntent in NotificationHelper.notifyPriorityQuestAlert().
 */
class PriorityAlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val questTitle = intent.getStringExtra(NotificationHelper.EXTRA_QUEST_TITLE) ?: "Nhiệm vụ đang chờ"
        val deadlineMs = intent.getLongExtra(NotificationHelper.EXTRA_DEADLINE_MS, 0L)

        setContent {
            PriorityAlertScreen(
                questTitle = questTitle,
                deadlineMs = deadlineMs,
                onAction = { finish() }
            )
        }
    }
}

@Composable
private fun PriorityAlertScreen(
    questTitle: String,
    deadlineMs: Long,
    onAction: () -> Unit
) {
    var timeLeftMs by remember { mutableLongStateOf(maxOf(0L, deadlineMs - System.currentTimeMillis())) }

    LaunchedEffect(Unit) {
        while (timeLeftMs > 0) {
            delay(1_000)
            timeLeftMs = maxOf(0L, deadlineMs - System.currentTimeMillis())
        }
    }

    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftMs) % 60
    val isOverdue = timeLeftMs == 0L
    val isCritical = minutes < 5

    val bgColor = when {
        isOverdue -> Color(0xFF6A0000)
        isCritical -> Color(0xFFB71C1C)
        else -> Color(0xFF1A237E)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = when {
                    isOverdue -> "⏰"
                    isCritical -> "🚨"
                    else -> "⚔️"
                },
                fontSize = 64.sp
            )

            Text(
                text = "NHIỆM VỤ BẮT BUỘC",
                color = Color(0xFFFFCC02),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )

            Text(
                text = questTitle,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )

            Text(
                text = if (isOverdue) "HẾT GIỜ" else "%02d:%02d".format(minutes, seconds),
                color = if (isCritical || isOverdue) Color(0xFFFF6B6B) else Color(0xFF64B5F6),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = when {
                    isOverdue -> "Hoàn thành ngay để tránh Debt Point!"
                    isCritical -> "Chỉ còn vài phút — tập trung ngay!"
                    else -> "Bắt đầu ngay để kịp deadline!"
                },
                color = Color(0xFFBBBBBB),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onAction,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Trì Hoãn 10p")
                }
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCritical || isOverdue) Color(0xFFFF5722) else Color(0xFF4CAF50)
                    )
                ) {
                    Text("⚔️ Bắt Đầu Ngay", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
