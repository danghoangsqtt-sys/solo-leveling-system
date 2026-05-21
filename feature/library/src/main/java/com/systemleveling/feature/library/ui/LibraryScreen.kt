package com.systemleveling.feature.library.ui

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
import com.systemleveling.core.database.entity.CourseEntity
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.designsystem.theme.md_theme_dark_primary
import com.systemleveling.core.model.ItemRarity

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onBack: () -> Unit
) {
    val courses by viewModel.courses.collectAsState()

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
                    text = "📚 KHO HỌC THUẬT",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Course List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(courses) { course ->
                    CourseCard(course = course, onProgress = { viewModel.completeModule(course) })
                }
            }
        }
    }
}

@Composable
fun CourseCard(course: CourseEntity, onProgress: () -> Unit) {
    val rarityColor = when (course.rarity) {
        ItemRarity.COMMON -> Color(0xFFAAAAAA)
        ItemRarity.UNCOMMON -> Color(0xFF40E17E)
        ItemRarity.RARE -> Color(0xFF4A9EFF)
        ItemRarity.EPIC -> Color(0xFFE040FB)
        ItemRarity.LEGENDARY -> Color(0xFFFFAB40)
        ItemRarity.MYTHIC -> Color(0xFFFF5252)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E2E))
            .border(1.dp, if (course.isCompleted) rarityColor else Color(0xFF2F2F40), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = rarityColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Tác giả: ${course.author}", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                }
                
                if (course.isCompleted) {
                    Text("✅", fontSize = 24.sp)
                } else {
                    Text("📖", fontSize = 24.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = course.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            val progress = if (course.totalModules > 0) course.completedModules.toFloat() / course.totalModules else 0f
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = rarityColor,
                    trackColor = Color(0xFF121222)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${course.completedModules}/${course.totalModules}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Phần Thưởng: +${course.rewardExp} EXP",
                    color = Color(0xFFFFD700),
                    style = MaterialTheme.typography.labelMedium
                )
                
                if (!course.isCompleted) {
                    Button(
                        onClick = onProgress,
                        colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Đã Đọc", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
