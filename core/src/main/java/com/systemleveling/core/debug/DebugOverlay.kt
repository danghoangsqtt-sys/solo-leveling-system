package com.systemleveling.core.debug

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

/**
 * Debug overlay panel — only renders in DEBUG builds.
 * Place inside the root Box of MainActivity with Alignment.TopEnd.
 * Toggle visibility with shake or long-press on a debug button.
 */
@Composable
fun DebugOverlay(modifier: Modifier = Modifier) {
    if (!DebugLogger.isEnabled) return

    val logs by DebugLogger.logs.collectAsState()
    val dbCount by DebugLogger.dbQueryCount.collectAsState()
    val lastApi by DebugLogger.lastApiCall.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.padding(8.dp)) {
        if (expanded) {
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xEE0A0A1A))
                    .border(1.dp, Color(0x334A9EFF), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙ DEBUG", color = Color(0xFF4A9EFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "CLEAR", color = Color(0xFFFF6B6B), fontSize = 9.sp,
                            modifier = Modifier.clickable { DebugLogger.clear() }
                        )
                        Text(
                            "✕", color = Color.White, fontSize = 12.sp,
                            modifier = Modifier.clickable { expanded = false }
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Stats row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatChip("DB", dbCount.toString(), Color(0xFF2ED573))
                    lastApi?.let { StatChip("API", it.take(20), Color(0xFFFFD700)) }
                }

                Spacer(Modifier.height(6.dp))

                // Log list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(logs.reversed()) { entry ->
                        LogRow(entry)
                    }
                }
            }
        } else {
            // Collapsed pill button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xEE0A0A1A))
                    .border(1.dp, Color(0x334A9EFF), RoundedCornerShape(16.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("⚙ DBG", color = Color(0xFF4A9EFF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(0.1f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text("$label: ", color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun LogRow(entry: LogEntry) {
    val color = when (entry.level) {
        LogLevel.ERROR -> Color(0xFFFF6B6B)
        LogLevel.WARN  -> Color(0xFFFFD700)
        LogLevel.INFO  -> Color(0xFF2ED573)
        LogLevel.DEBUG -> Color(0xFFC0C7D4)
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            timeFmt.format(Date(entry.timestamp)),
            color = Color(0xFF666680), fontSize = 8.sp, fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(52.dp)
        )
        Text(
            "[${entry.tag}] ",
            color = color.copy(0.7f), fontSize = 8.sp, fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(36.dp)
        )
        Text(
            entry.message,
            color = color, fontSize = 8.sp, fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}
