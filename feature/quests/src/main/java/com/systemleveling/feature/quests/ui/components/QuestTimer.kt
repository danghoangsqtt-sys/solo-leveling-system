package com.systemleveling.feature.quests.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Calendar

// ── Quest timer ───────────────────────────────────────────────────────────────
@Composable
fun QuestTimer(timeStart: String, durationMinutes: Int, onExpired: () -> Unit = {}) {
    var remainingText by remember { mutableStateOf("--:--:--") }
    var isUrgent by remember { mutableStateOf(false) }
    var isExpired by remember { mutableStateOf(false) }

    LaunchedEffect(timeStart, durationMinutes) {
        val parts = timeStart.split(":")
        if (parts.size >= 2) {
            val hour = parts[0].toIntOrNull() ?: 0
            val min = parts[1].toIntOrNull() ?: 0
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, min)
            calendar.set(Calendar.SECOND, 0)
            calendar.add(Calendar.MINUTE, durationMinutes)
            val endTimeMs = calendar.timeInMillis

            while (true) {
                val now = System.currentTimeMillis()
                val diff = endTimeMs - now
                if (diff <= 0) {
                    remainingText = "HẾT GIỜ"
                    isExpired = true
                    isUrgent = false
                    onExpired()
                    break
                } else {
                    val h = (diff / (1000 * 60 * 60)) % 24
                    val m = (diff / (1000 * 60)) % 60
                    val s = (diff / 1000) % 60
                    remainingText = String.format("%02d:%02d:%02d", h, m, s)
                    isUrgent = diff < 15 * 60 * 1000
                }
                delay(1000)
            }
        }
    }

    val timerColor = when {
        isExpired -> Color(0xFFFF5252)
        isUrgent  -> Color(0xFFFFAB40)
        else      -> Color(0xFF4A9EFF)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(timerColor.copy(0.08f))
            .border(0.5.dp, timerColor.copy(0.3f), RoundedCornerShape(5.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (isExpired) "💀" else "⏳", fontSize = 10.sp)
        Spacer(Modifier.width(4.dp))
        Text(
            text = remainingText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = timerColor,
            letterSpacing = 0.04f.em
        )
    }
}
