package com.systemleveling.feature.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemleveling.core.database.entity.ItemEntity
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.designsystem.theme.md_theme_dark_primary
import com.systemleveling.core.model.ItemRarity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.items.collectAsState()
    var selectedItem by remember { mutableStateOf<ItemEntity?>(null) }

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
                    text = "🎒 KHO ĐỒ",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Inventory Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { item ->
                    ItemCell(item = item) {
                        selectedItem = item
                    }
                }
            }
        }

        // Bottom Sheet for detail
        if (selectedItem != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedItem = null },
                containerColor = Color(0xFF1E1E2E)
            ) {
                ItemDetailSheet(
                    item = selectedItem!!,
                    onAction = {
                        viewModel.useItem(selectedItem!!)
                        selectedItem = null
                    }
                )
            }
        }
    }
}

@Composable
fun ItemCell(item: ItemEntity, onClick: () -> Unit) {
    val borderColor = when (item.rarity) {
        ItemRarity.COMMON -> Color(0xFFAAAAAA)
        ItemRarity.UNCOMMON -> Color(0xFF40E17E)
        ItemRarity.RARE -> Color(0xFF4A9EFF)
        ItemRarity.EPIC -> Color(0xFFE040FB)
        ItemRarity.LEGENDARY -> Color(0xFFFFAB40)
        ItemRarity.MYTHIC -> Color(0xFFFF5252)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF121222))
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = item.iconId ?: "📦", fontSize = 32.sp)
        
        // Quantity badge
        if (item.quantity > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "x${item.quantity}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ItemDetailSheet(item: ItemEntity, onAction: () -> Unit = {}) {
    val rarityColor = when (item.rarity) {
        ItemRarity.COMMON -> Color(0xFFAAAAAA)
        ItemRarity.UNCOMMON -> Color(0xFF40E17E)
        ItemRarity.RARE -> Color(0xFF4A9EFF)
        ItemRarity.EPIC -> Color(0xFFE040FB)
        ItemRarity.LEGENDARY -> Color(0xFFFFAB40)
        ItemRarity.MYTHIC -> Color(0xFFFF5252)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = item.iconId ?: "📦", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = item.name,
            style = MaterialTheme.typography.headlineSmall,
            color = rarityColor,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "${item.rarity.title} • ${item.category.title}",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "\"${item.description}\"",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onAction,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary)
        ) {
            Text(if (item.category == com.systemleveling.core.model.ItemCategory.EQUIPMENT) "TRANG BỊ" else "SỬ DỤNG")
        }
    }
}
