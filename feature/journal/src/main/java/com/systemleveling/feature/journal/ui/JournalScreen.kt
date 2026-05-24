package com.systemleveling.feature.journal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.systemleveling.core.database.entity.JournalEntity
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.designsystem.theme.md_theme_dark_primary
import com.systemleveling.core.model.Mood
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    onBack: () -> Unit
) {
    val journals by viewModel.journals.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }

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
                    text = "📖 NHẬT KÝ",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = "✍️",
                    fontSize = 24.sp,
                    modifier = Modifier.clickable { showAddDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Journal List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(journals) { journal ->
                    JournalCard(journal = journal)
                }
            }
        }
        
        if (showAddDialog) {
            AddJournalDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { content, mood ->
                    viewModel.addJournal(content, mood)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun JournalCard(journal: JournalEntity) {
    val moodColor = when (journal.mood) {
        Mood.HAPPY -> Color(0xFF40E17E)
        Mood.EXCITED -> Color(0xFFFFD700)
        Mood.NEUTRAL -> Color(0xFFAAAAAA)
        Mood.SAD -> Color(0xFF4A9EFF)
        Mood.ANGRY -> Color(0xFFFF5252)
        Mood.STRESSED -> Color(0xFFE040FB)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E2E))
            .border(1.dp, moodColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(journal.mood.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = journal.mood.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = moodColor
                    )
                }
                
                val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
                Text(
                    text = sdf.format(Date(journal.timestamp)),
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = journal.content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}

@Composable
fun AddJournalDialog(onDismiss: () -> Unit, onAdd: (String, Mood) -> Unit) {
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(Mood.HAPPY) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2E),
        title = { Text("Viết Nhật Ký", color = Color.White) },
        text = {
            Column {
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Bạn đang cảm thấy thế nào?") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF121222),
                        unfocusedContainerColor = Color(0xFF121222),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Tâm Trạng", color = Color.White, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Mood.entries.forEach { mood ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedMood == mood) Color(0xFF4A9EFF) else Color(0xFF121222))
                                .clickable { selectedMood = mood }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(mood.emoji, fontSize = 20.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (content.isNotBlank()) onAdd(content, selectedMood) }) {
                Text("LƯU", color = md_theme_dark_primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("HỦY", color = Color.Gray)
            }
        }
    )
}
