package com.systemleveling.feature.inventory.ui

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.designsystem.theme.md_theme_dark_primary
import com.systemleveling.core.engine.LootTable
import com.systemleveling.core.model.ItemCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCompendiumScreen(
    viewModel: ItemCompendiumViewModel,
    onBack: () -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val discoveredCount by viewModel.discoveredCount.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedEntry by remember { mutableStateOf<CompendiumEntry?>(null) }

    val progress = if (entries.isNotEmpty()) discoveredCount.toFloat() / LootTable.allTemplates.size else 0f

    Box(
        modifier = Modifier.fillMaxSize().background(md_theme_dark_background)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 32.dp)) {

            // ── Header ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("◀ KHO ĐỒ", color = md_theme_dark_primary,
                    modifier = Modifier.clickable { onBack() }, fontSize = 13.sp)
                Text("📖 THƯ VIỆN VẬT PHẨM", style = MaterialTheme.typography.titleLarge,
                    color = Color.White, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(60.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Discovery progress ─────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A2E)).padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Đã khám phá", color = Color.White, fontSize = 13.sp)
                        Text(
                            "$discoveredCount / ${LootTable.allTemplates.size}",
                            color = md_theme_dark_primary, fontWeight = FontWeight.Bold, fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = md_theme_dark_primary,
                        trackColor = Color(0xFF2F2F50)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        buildProgressLabel(progress),
                        color = Color(0xFF888888), fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Search ─────────────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearch(it) },
                placeholder = { Text("Tìm kiếm vật phẩm...", color = Color(0xFF555577)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = md_theme_dark_primary,
                    unfocusedBorderColor = Color(0xFF2F2F50),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = md_theme_dark_primary
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Category filter chips ──────────────────────────────────────
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val cats = listOf(null) + ItemCategory.entries
                items(cats) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategory(cat) },
                        label = { Text(cat?.title ?: "TỔNG HỢP", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = md_theme_dark_primary,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF1A1A2E),
                            labelColor = Color.Gray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = isSelected,
                            selectedBorderColor = Color.Transparent,
                            borderColor = Color(0xFF2F2F50)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Compendium grid ────────────────────────────────────────────
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(entries) { entry ->
                    CompendiumCell(entry = entry) { selectedEntry = entry }
                }
            }
        }

        // ── Entry detail bottom sheet ──────────────────────────────────────
        selectedEntry?.let { entry ->
            ModalBottomSheet(
                onDismissRequest = { selectedEntry = null },
                containerColor = Color(0xFF1E1E2E)
            ) {
                CompendiumEntryDetail(entry = entry)
            }
        }
    }
}

@Composable
private fun CompendiumCell(entry: CompendiumEntry, onClick: () -> Unit) {
    val rarityColor = entry.template.rarity.color()
    val alpha = if (entry.discovered) 1f else 0.35f
    val borderColor = if (entry.discovered) rarityColor else Color(0xFF333355)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (entry.discovered) Color(0xFF121222) else Color(0xFF0E0E1A))
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (entry.discovered) entry.template.icon else "❓",
            fontSize = 28.sp
        )
        // Rarity dot bottom-left
        Box(
            modifier = Modifier.align(Alignment.BottomStart).padding(3.dp).size(6.dp)
                .clip(RoundedCornerShape(3.dp)).background(if (entry.discovered) rarityColor else Color(0xFF333355))
        )
        // Owned count
        if (entry.discovered && entry.ownedCount > 0) {
            Box(
                modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp)
                    .clip(RoundedCornerShape(3.dp)).background(Color.Black.copy(0.7f))
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text("×${entry.ownedCount}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CompendiumEntryDetail(entry: CompendiumEntry) {
    val tmpl = entry.template
    val rarityColor = if (entry.discovered) tmpl.rarity.color() else Color(0xFF555577)

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp).padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(16.dp))
                .background(
                    if (entry.discovered)
                        Brush.radialGradient(listOf(rarityColor.copy(0.25f), Color(0xFF12122A)))
                    else
                        Brush.radialGradient(listOf(Color(0xFF1A1A2E), Color(0xFF0E0E18)))
                )
                .border(2.dp, rarityColor.copy(0.6f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (entry.discovered) tmpl.icon else "❓",
                fontSize = 48.sp,
                modifier = Modifier.alpha(if (entry.discovered) 1f else 0.4f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (entry.discovered) tmpl.name else "??? Vật phẩm chưa khám phá",
            style = MaterialTheme.typography.headlineSmall,
            color = rarityColor, fontWeight = FontWeight.ExtraBold
        )
        Text(
            "${tmpl.rarity.title} • ${tmpl.category.title}",
            color = Color(0xFF888888), style = MaterialTheme.typography.bodySmall
        )

        if (entry.discovered && entry.ownedCount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Đang sở hữu: ×${entry.ownedCount}", color = Color(0xFF4A9EFF), fontSize = 12.sp,
                fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = rarityColor.copy(0.2f))
        Spacer(modifier = Modifier.height(16.dp))

        if (entry.discovered) {
            Text(tmpl.description, color = Color.White, textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A2E)).padding(12.dp)
            ) {
                Text("\"${tmpl.lore}\"", color = Color(0xFF888888), fontSize = 12.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            val tmplEffectType = tmpl.effectType
            val tmplEffectValue = tmpl.effectValue
            if (tmplEffectType != null && tmplEffectValue != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        .background(rarityColor.copy(0.12f))
                        .border(1.dp, rarityColor.copy(0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("⚡ Hiệu ứng: +$tmplEffectValue ${effectLabelCompendium(tmplEffectType)}",
                        color = rarityColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = howToObtainHint(tmpl.rarity),
                color = Color(0xFF444466), fontSize = 11.sp, textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "Hoàn thành các nhiệm vụ để khám phá vật phẩm bí ẩn này.\nNó đang chờ bạn ở đâu đó trong thế giới...",
                color = Color(0xFF555577), textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(howToObtainHint(tmpl.rarity), color = Color(0xFF444466),
                fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    }
}

private fun buildProgressLabel(progress: Float): String = when {
    progress >= 1.0f -> "🏆 Huyền thoại! Bạn đã sưu tập đầy đủ bộ sưu tập!"
    progress >= 0.75f -> "⭐ Nhà sưu tập vĩ đại — chỉ còn một ít nữa thôi!"
    progress >= 0.5f -> "🔮 Nửa chặng đường — kho báu đang hé lộ..."
    progress >= 0.25f -> "🗺️ Hành trình mới bắt đầu — còn nhiều điều chờ đợi phía trước"
    progress > 0f -> "🌱 Mới bắt đầu khám phá — hãy hoàn thành nhiều nhiệm vụ hơn!"
    else -> "💤 Chưa có vật phẩm nào — hãy bắt đầu với nhiệm vụ đầu tiên!"
}

private fun howToObtainHint(rarity: com.systemleveling.core.model.ItemRarity): String = when (rarity) {
    com.systemleveling.core.model.ItemRarity.COMMON -> "📋 Hoàn thành bất kỳ nhiệm vụ Rank E-D nào để có cơ hội nhận"
    com.systemleveling.core.model.ItemRarity.UNCOMMON -> "📋 Hoàn thành nhiệm vụ Rank D-C hoặc streak dài hơn"
    com.systemleveling.core.model.ItemRarity.RARE -> "⚔️ Hoàn thành nhiệm vụ Rank C-B với chất lượng cao"
    com.systemleveling.core.model.ItemRarity.EPIC -> "🔥 Chinh phục nhiệm vụ Rank B-A liên tiếp không gián đoạn"
    com.systemleveling.core.model.ItemRarity.LEGENDARY -> "👑 Chỉ rơi từ nhiệm vụ Rank A-S — xứng đáng cho những chiến binh ưu tú"
    com.systemleveling.core.model.ItemRarity.MYTHIC -> "⚡ Cực hiếm — chỉ Rank S mới có cơ hội, tỷ lệ rất thấp"
}

private fun effectLabelCompendium(type: String): String = when (type) {
    "EXP_BOOST" -> "EXP"; "SP_BOOST" -> "SP"; "STAT_BOOST" -> "STAT"
    "DEBT_CLEAR" -> "Debt Points"; "ALL_BOOST" -> "ALL STATS"; "SPEED_BOOST" -> "SPEED"
    else -> type
}
