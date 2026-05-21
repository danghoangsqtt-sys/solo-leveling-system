package com.systemleveling.feature.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.designsystem.theme.md_theme_dark_primary
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.core.model.QuestType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onBack: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val quests by viewModel.questsForSelectedDate.collectAsState()
    
    // Generate 14 days (7 days before, 7 days after today)
    val days = remember {
        val list = mutableListOf<Long>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -7)
        for (i in 0..14) {
            list.add(cal.timeInMillis)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(md_theme_dark_background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).padding(top = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "◀ HOME",
                    color = md_theme_dark_primary,
                    modifier = Modifier.clickable { onBack() }
                )
                Text(
                    text = "📅 LỊCH TRÌNH",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Week Calendar Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Scroll to middle item (today) ideally, but keeping it simple
                items(days) { dayTimestamp ->
                    DayItem(
                        timestamp = dayTimestamp,
                        isSelected = isSameDay(dayTimestamp, selectedDate),
                        onClick = { viewModel.selectDate(dayTimestamp) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            Text(
                text = "Nhiệm Vụ Ngày ${sdf.format(Date(selectedDate))}",
                style = MaterialTheme.typography.titleMedium,
                color = md_theme_dark_primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Quest List
            if (quests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có nhiệm vụ nào cho ngày này.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(quests) { quest ->
                        CalendarQuestCard(quest = quest)
                    }
                }
            }
        }
    }
}

@Composable
fun DayItem(timestamp: Long, isSelected: Boolean, onClick: () -> Unit) {
    val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
    val dayOfMonth = SimpleDateFormat("dd", Locale.getDefault()).format(Date(timestamp))
    
    val bgColor = if (isSelected) md_theme_dark_primary else Color(0xFF1E1E2E)
    val textColor = if (isSelected) Color.Black else Color.White
    
    Column(
        modifier = Modifier
            .width(60.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, if (isSelected) md_theme_dark_primary else Color(0xFF2F2F40), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = dayOfWeek, style = MaterialTheme.typography.labelMedium, color = if(isSelected) Color.DarkGray else Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = dayOfMonth, style = MaterialTheme.typography.titleLarge, color = textColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CalendarQuestCard(quest: QuestEntity) {
    val statusColor = when (quest.status) {
        QuestStatus.COMPLETED -> Color(0xFF40E17E)
        QuestStatus.FAILED -> Color(0xFFFF5252)
        else -> Color(0xFF4A9EFF)
    }
    
    val typeIcon = when(quest.type) {
        QuestType.DAILY -> "🔄"
        QuestType.WEEKLY -> "📅"
        QuestType.BOSS -> "👑"
        QuestType.PENALTY -> "⚠️"
        QuestType.EVENT -> "🎁"
        QuestType.SIDE -> "👁️"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E2E))
            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(typeIcon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = quest.timeStart?.let { "$it - ${quest.timeEnd ?: "?"}" } ?: "All Day",
                        style = MaterialTheme.typography.labelSmall,
                        color = md_theme_dark_primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (quest.status == QuestStatus.COMPLETED) statusColor else Color.Transparent)
                    .border(2.dp, statusColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (quest.status == QuestStatus.COMPLETED) {
                    Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun isSameDay(ts1: Long, ts2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = ts1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = ts2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
