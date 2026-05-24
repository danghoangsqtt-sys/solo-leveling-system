package com.systemleveling.feature.inventory.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.systemleveling.core.database.entity.ItemEntity
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.designsystem.theme.md_theme_dark_primary
import com.systemleveling.core.model.ItemCategory
import com.systemleveling.core.model.ItemRarity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit,
    onNavigateToCompendium: () -> Unit = {}
) {
    val displayedItems by viewModel.displayedItems.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val activeCount by viewModel.activeCount.collectAsState()
    val storedCount by viewModel.storedCount.collectAsState()

    var selectedItem by remember { mutableStateOf<ItemEntity?>(null) }
    var deleteConfirmItem by remember { mutableStateOf<ItemEntity?>(null) }

    val categories = listOf(null) + ItemCategory.entries

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(md_theme_dark_background)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 32.dp)) {

            // ── Header ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "◀ HOME",
                    color = md_theme_dark_primary,
                    modifier = Modifier.clickable { onBack() },
                    fontSize = 13.sp
                )
                Text(
                    text = "🎒 KHO VẬT PHẨM",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "📖 THƯ VIỆN",
                    color = md_theme_dark_primary,
                    modifier = Modifier.clickable { onNavigateToCompendium() },
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Tab bar ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A2E))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                InventoryTab.entries.forEach { tab ->
                    val isSelected = activeTab == tab
                    val bgColor by animateColorAsState(
                        if (isSelected) md_theme_dark_primary else Color.Transparent,
                        animationSpec = tween(200), label = "tabBg"
                    )
                    val count = if (tab == InventoryTab.ACTIVE) activeCount else storedCount
                    val label = if (tab == InventoryTab.ACTIVE) "🎒 ĐANG CÓ" else "📦 LƯU TRỮ"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable { viewModel.setTab(tab) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$label ($count)",
                            color = if (isSelected) Color.Black else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Category chips ─────────────────────────────────────────────
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories, key = { it?.name ?: "ALL" }) { cat ->
                    val isSelected = selectedCategory == cat
                    val label = cat?.title ?: "TẤT CẢ"
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategory(cat) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = md_theme_dark_primary,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF1A1A2E),
                            labelColor = Color.Gray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = Color.Transparent,
                            borderColor = Color(0xFF2F2F50)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Grid / Empty state ─────────────────────────────────────────
            if (displayedItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (activeTab == InventoryTab.ACTIVE)
                                "Kho đồ trống\nHoàn thành nhiệm vụ để nhận vật phẩm!"
                            else
                                "Chưa có vật phẩm nào được lưu trữ",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedItems, key = { it.id }) { item ->
                        ItemCell(item = item) { selectedItem = item }
                    }
                }
            }
        }

        // ── Item detail bottom sheet ───────────────────────────────────────
        selectedItem?.let { item ->
            ModalBottomSheet(
                onDismissRequest = { selectedItem = null },
                containerColor = Color(0xFF1E1E2E)
            ) {
                ItemDetailSheet(
                    item = item,
                    onUse = {
                        viewModel.useItem(item)
                        selectedItem = null
                    },
                    onToggleStore = {
                        viewModel.toggleStore(item)
                        selectedItem = null
                    },
                    onDelete = {
                        deleteConfirmItem = item
                        selectedItem = null
                    }
                )
            }
        }

        // ── Delete confirmation dialog ─────────────────────────────────────
        deleteConfirmItem?.let { item ->
            DeleteConfirmDialog(
                itemName = item.name,
                onConfirm = {
                    viewModel.deleteItem(item)
                    deleteConfirmItem = null
                },
                onDismiss = { deleteConfirmItem = null }
            )
        }
    }
}

@Composable
fun ItemCell(item: ItemEntity, onClick: () -> Unit) {
    val borderColor = item.rarity.color()
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF121222))
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = item.iconId ?: "📦", fontSize = 28.sp)

        // Stored indicator
        if (item.isStored) {
            Box(
                modifier = Modifier.align(Alignment.TopStart).padding(3.dp)
                    .clip(RoundedCornerShape(3.dp)).background(Color(0xFF3A3A5A))
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text("📦", fontSize = 8.sp)
            }
        }

        // Quantity badge
        if (item.quantity > 1) {
            Box(
                modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp)
                    .clip(RoundedCornerShape(3.dp)).background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text("x${item.quantity}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ItemDetailSheet(
    item: ItemEntity,
    onUse: () -> Unit,
    onToggleStore: () -> Unit,
    onDelete: () -> Unit
) {
    val rarityColor = item.rarity.color()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon + Rarity glow
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(listOf(rarityColor.copy(0.25f), Color(0xFF12122A)))
                )
                .border(2.dp, rarityColor.copy(0.7f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = item.iconId ?: "📦", fontSize = 48.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(item.name, style = MaterialTheme.typography.headlineSmall, color = rarityColor, fontWeight = FontWeight.ExtraBold)
        Text(
            "${item.rarity.title} • ${item.category.title}",
            color = Color(0xFF888888), style = MaterialTheme.typography.bodySmall
        )

        if (item.quantity > 1) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Số lượng: ×${item.quantity}", color = Color(0xFF4A9EFF), fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = rarityColor.copy(0.2f))
        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = item.description,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        // Lore
        item.loreDescription?.takeIf { it.isNotBlank() }?.let { lore ->
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A2E))
                    .padding(12.dp)
            ) {
                Text("\"$lore\"", color = Color(0xFF888888), fontSize = 12.sp, textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth())
            }
        }

        // Effect
        val effectType = item.effectType
        val effectValue = item.effectValue
        if (effectType != null && effectValue != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(rarityColor.copy(0.12f))
                    .border(1.dp, rarityColor.copy(0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("⚡ ${effectLabel(effectType)} +$effectValue", color = rarityColor,
                    fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Origin quest
        item.fromQuestId?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("📜 Từ nhiệm vụ: $it", color = Color(0xFF555577), fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Action buttons ──
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Use / Equip
            Button(
                onClick = onUse,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = rarityColor.copy(0.85f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    if (item.category == ItemCategory.EQUIPMENT || item.category == ItemCategory.WEAPON) "TRANG BỊ" else "SỬ DỤNG",
                    fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black
                )
            }
            // Archive / Unarchive
            OutlinedButton(
                onClick = onToggleStore,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A9EFF)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A9EFF)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (item.isStored) "📤 HOÀN TRẢ" else "📦 LƯU KHO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            // Delete
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("🗑️ XÓA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(itemName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E2E))
                .border(1.dp, Color(0xFFFF5252).copy(0.5f), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚠️", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Xác nhận xóa?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Vật phẩm \"$itemName\" sẽ bị xóa vĩnh viễn\nkhỏi kho đồ của bạn.",
                    color = Color.Gray, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444466)),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("HỦY") }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("XÓA", fontWeight = FontWeight.Bold, color = Color.White) }
                }
            }
        }
    }
}

private fun effectLabel(type: String): String = when (type) {
    "EXP_BOOST" -> "EXP"
    "SP_BOOST" -> "SP"
    "STAT_BOOST" -> "STAT"
    "DEBT_CLEAR" -> "DEBT CLEAR"
    "ALL_BOOST" -> "ALL STATS"
    "SPEED_BOOST" -> "SPEED"
    else -> type
}

fun ItemRarity.color(): Color = when (this) {
    ItemRarity.COMMON -> Color(0xFFAAAAAA)
    ItemRarity.UNCOMMON -> Color(0xFF40E17E)
    ItemRarity.RARE -> Color(0xFF4A9EFF)
    ItemRarity.EPIC -> Color(0xFFE040FB)
    ItemRarity.LEGENDARY -> Color(0xFFFFAB40)
    ItemRarity.MYTHIC -> Color(0xFFFF5252)
}
