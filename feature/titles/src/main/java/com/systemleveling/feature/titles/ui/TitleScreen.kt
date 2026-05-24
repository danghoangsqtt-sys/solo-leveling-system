package com.systemleveling.feature.titles.ui

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.systemleveling.core.database.entity.TitleEntity
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.designsystem.theme.md_theme_dark_primary
import com.systemleveling.core.model.ItemRarity

@Composable
fun TitleScreen(
    viewModel: TitleViewModel,
    onBack: () -> Unit
) {
    val titles by viewModel.titles.collectAsState()

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
                    text = "🏆 DANH HIỆU",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(titles, key = { it.id }) { title ->
                    TitleCard(title = title, onEquip = { viewModel.equipTitle(title.id) })
                }
            }
        }
    }
}

@Composable
fun TitleCard(title: TitleEntity, onEquip: () -> Unit) {
    val rarityColor = when (title.rarity) {
        ItemRarity.COMMON -> Color(0xFFAAAAAA)
        ItemRarity.UNCOMMON -> Color(0xFF40E17E)
        ItemRarity.RARE -> Color(0xFF4A9EFF)
        ItemRarity.EPIC -> Color(0xFFE040FB)
        ItemRarity.LEGENDARY -> Color(0xFFFFAB40)
        ItemRarity.MYTHIC -> Color(0xFFFF5252)
    }

    val backgroundBrush = if (title.isAcquired) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF1E1E2E), rarityColor.copy(alpha = 0.2f))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF121222), Color(0xFF121222))
        )
    }

    val borderColor = if (title.isAcquired) rarityColor else Color(0xFF2F2F40)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundBrush)
            .border(if (title.isEquipped) 2.dp else 1.dp, if (title.isEquipped) md_theme_dark_primary else borderColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (title.isAcquired) rarityColor else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                
                if (title.isEquipped) {
                    Text(
                        text = "EQUIPPED",
                        color = md_theme_dark_primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\"${title.description}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = if (title.isAcquired) Color.White else Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Điều kiện: ${title.condition}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { if (title.maxProgress > 0) title.progress.toFloat() / title.maxProgress else 1f },
                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = if (title.isAcquired) rarityColor else Color.Gray,
                    trackColor = Color(0xFF2F2F40)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${title.progress}/${title.maxProgress}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
            
            if (title.isAcquired && !title.isEquipped) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onEquip,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F2F40))
                ) {
                    Text("Trang Bị", color = Color.White)
                }
            }
        }
    }
}
