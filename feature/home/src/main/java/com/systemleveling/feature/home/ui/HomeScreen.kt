package com.systemleveling.feature.home.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import com.systemleveling.core.database.entity.StatEntity
import com.systemleveling.core.database.entity.UserEntity
import com.systemleveling.core.designsystem.components.GlassCard
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.delay

// ── Palette ──────────────────────────────────────────────────────────────────
private val BG           = Color(0xFF121222)
private val BG_DEEP      = Color(0xFF0A0A1A)
private val PRIMARY      = Color(0xFF4A9EFF)
private val PRIMARY_DIM  = Color(0xFFA4C9FF)
private val GOLD         = Color(0xFFFFD700)
private val GREEN        = Color(0xFF2ED573)
private val GLASS        = Color(0x1AFFFFFF)
private val GLASS_BORDER = Color(0x1FFFFFFF)
private val TEXT_MUTED   = Color(0xFFC0C7D4)

private val StatColors = mapOf(
    "STR" to Color(0xFF4A9EFF),
    "INT" to Color(0xFFFFD700),
    "AGI" to Color(0xFF2ED573),
    "VIT" to Color(0xFFFF6B6B),
    "WIS" to Color(0xFFB48EFF),
    "CHA" to Color(0xFFFF9F43)
)

// ── Root ─────────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToQuests: () -> Unit = {},
    onNavigateToSkills: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToFinance: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToNpc: () -> Unit = {},
    onNavigateToAdvancement: () -> Unit = {},
    onNavigateToDailySummary: () -> Unit = {}
) {
    val user by viewModel.user.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val questSummary by viewModel.questSummary.collectAsState()
    val isAdvancementReady by viewModel.isAdvancementReady.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val supabaseUrl by viewModel.supabaseUrl.collectAsState()
    val supabaseAnonKey by viewModel.supabaseAnonKey.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val todayExpense by viewModel.todayExpense.collectAsState()
    val otaUpdateInfo by viewModel.otaUpdateInfo.collectAsState()
    val otaDownloading by viewModel.otaDownloading.collectAsState()
    val auraGreeting by viewModel.auraGreeting.collectAsState()
    val syncId by viewModel.syncId.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF1A1A2E), BG_DEEP),
                    radius = 1400f
                )
            )
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            if (isTablet) {
                SideNavBar(
                    onNavigateToQuests = onNavigateToQuests,
                    onNavigateToSkills = onNavigateToSkills,
                    onNavigateToFinance = onNavigateToFinance,
                    onNavigateToAura = onNavigateToNpc
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                TopSystemBar(
                    user = user,
                    onSettingsClick = { showSettingsDialog = true }
                )

                if (isAdvancementReady) {
                    AdvancementBanner(onClick = onNavigateToAdvancement)
                }

                if (isTablet) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(Modifier.height(12.dp))
                            CharacterStatusPanel(
                                user = user,
                                stats = stats
                            )
                            FinanceSummaryCard(
                                totalBalance = totalBalance,
                                todayExpense = todayExpense,
                                onNavigateToFinance = onNavigateToFinance
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        // Right Column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(Modifier.height(12.dp))
                            QuestProgressCard(
                                questSummary = questSummary,
                                onNavigateToQuests = onNavigateToQuests
                            )
                            QuickActionsRow(
                                onCalendar = onNavigateToCalendar,
                                onJournal = onNavigateToJournal,
                                onLibrary = onNavigateToLibrary,
                                onInventory = onNavigateToInventory,
                                onDailySummary = onNavigateToDailySummary
                            )
                            MotivationalQuoteCard()
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Spacer(Modifier.height(12.dp))
                        CharacterStatusPanel(
                            user = user,
                            stats = stats
                        )
                        Spacer(Modifier.height(14.dp))
                        MotivationalQuoteCard()
                        Spacer(Modifier.height(14.dp))
                        QuestProgressCard(
                            questSummary = questSummary,
                            onNavigateToQuests = onNavigateToQuests
                        )
                        Spacer(Modifier.height(16.dp))
                        FinanceSummaryCard(
                            totalBalance = totalBalance,
                            todayExpense = todayExpense,
                            onNavigateToFinance = onNavigateToFinance
                        )
                        Spacer(Modifier.height(16.dp))
                        QuickActionsRow(
                            onCalendar = onNavigateToCalendar,
                            onJournal = onNavigateToJournal,
                            onLibrary = onNavigateToLibrary,
                            onInventory = onNavigateToInventory,
                            onDailySummary = onNavigateToDailySummary
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (!isTablet) {
                    BottomNavBar(
                        onNavigateToQuests = onNavigateToQuests,
                        onNavigateToSkills = onNavigateToSkills,
                        onNavigateToFinance = onNavigateToFinance,
                        onNavigateToAura = onNavigateToNpc
                    )
                }
            }
        }
        
        if (showSettingsDialog) {
            SettingsDialog(
                currentApiKey = geminiApiKey,
                currentSupabaseUrl = supabaseUrl,
                currentSupabaseAnonKey = supabaseAnonKey,
                currentSyncId = syncId,
                syncState = syncState,
                onDismiss = { showSettingsDialog = false },
                onForceSync = { viewModel.forceSync() },
                onSave = { apiKey, sbUrl, sbKey, newSyncId ->
                    viewModel.saveApiKey(apiKey)
                    viewModel.saveSupabaseConfig(sbUrl, sbKey)
                    viewModel.saveSyncId(newSyncId)
                    showSettingsDialog = false
                }
            )
        }



        otaUpdateInfo?.let { info ->
            OtaUpdateDialog(
                info = info,
                isDownloading = otaDownloading,
                onUpdate = { viewModel.downloadAndInstallUpdate() },
                onDismiss = { viewModel.dismissOtaUpdate() }
            )
        }

        auraGreeting?.let { greeting ->
            AuraGreetingBubble(
                message = greeting,
                onDismiss = { viewModel.dismissAuraGreeting() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ── Top app bar ───────────────────────────────────────────────────────────────
@Composable
private fun TopSystemBar(user: UserEntity?, onSettingsClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x99121222))
            .border(BorderStroke(0.5.dp, GLASS_BORDER), shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo + Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x331E3A5F))
                        .border(1.dp, PRIMARY.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("⚡", fontSize = 16.sp) }
                Spacer(Modifier.width(10.dp))
                Text(
                    "SYSTEM LEVELING",
                    color = PRIMARY_DIM,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.06f.em
                )
            }
            // Level badge & Settings
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GOLD.copy(alpha = 0.12f))
                        .border(1.dp, GOLD.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Lvl. ${user?.level ?: 1}",
                        color = GOLD,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .clickable { onSettingsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚙️", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingsDialog(
    currentApiKey: String,
    currentSupabaseUrl: String,
    currentSupabaseAnonKey: String,
    currentSyncId: String,
    syncState: SyncState,
    onDismiss: () -> Unit,
    onForceSync: () -> Unit,
    onSave: (apiKey: String, supabaseUrl: String, supabaseAnonKey: String, syncId: String) -> Unit
) {
    var apiKey by remember { mutableStateOf(currentApiKey) }
    var sbUrl by remember { mutableStateOf(currentSupabaseUrl) }
    var sbKey by remember { mutableStateOf(currentSupabaseAnonKey) }
    var sId by remember { mutableStateOf(currentSyncId) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = TEXT_MUTED,
        focusedBorderColor = PRIMARY,
        unfocusedBorderColor = GLASS_BORDER,
        cursorColor = PRIMARY
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BG_DEEP,
        title = {
            Text("⚙️ System Settings", color = PRIMARY_DIM, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ── AI Section ──────────────────────────────────────────
                Text("🤖 AI Configuration", color = PRIMARY_DIM, fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text("Gemini API Key:", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("AIzaSy...", color = TEXT_MUTED) }
                )
                Text(
                    "Used for AI Quest Generation, Skill Roadmaps, and Daily Summaries.",
                    color = TEXT_MUTED, fontSize = 11.sp
                )

                Spacer(Modifier.height(4.dp))
                HorizontalDivider(color = GLASS_BORDER)
                Spacer(Modifier.height(4.dp))

                // ── Cloud Backup Section ─────────────────────────────────
                Text("☁️ Cloud Backup (Supabase)", color = PRIMARY_DIM, fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text("Project URL:", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = sbUrl,
                    onValueChange = { sbUrl = it },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("https://xxxx.supabase.co", color = TEXT_MUTED) }
                )
                Spacer(Modifier.height(4.dp))
                Text("Anon Key:", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = sbKey,
                    onValueChange = { sbKey = it },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("eyJ...", color = TEXT_MUTED) }
                )
                Text(
                    "Data syncs to Supabase on quest completion and restores automatically on fresh install.",
                    color = TEXT_MUTED, fontSize = 11.sp
                )
                
                Spacer(Modifier.height(8.dp))
                Text("Device / Sync ID:", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = sId,
                    onValueChange = { sId = it },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text(
                    "Copy this ID to another device (e.g., Tablet) to sync both devices to the same cloud save.",
                    color = GOLD, fontSize = 11.sp
                )

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onForceSync,
                    colors = ButtonDefaults.buttonColors(containerColor = PRIMARY.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val statusText = when (syncState) {
                        SyncState.Syncing -> "Syncing..."
                        SyncState.Synced -> "Synced ✓"
                        SyncState.SyncFailed -> "Sync Failed ✗"
                        else -> "Sync Now (Force Push)"
                    }
                    Text(statusText, color = PRIMARY, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(apiKey, sbUrl, sbKey, sId) }) {
                Text("Save", color = PRIMARY, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TEXT_MUTED)
            }
        }
    )
}

// ── Character status panel ────────────────────────────────────────────────────
@Composable
private fun CharacterStatusPanel(
    user: UserEntity?,
    stats: StatEntity?
) {
    val u = user ?: UserEntity(
        nickname = "Shadow Monarch", characterClass = "Warrior",
        avatarUri = null
    )
    val s = stats ?: StatEntity()

    val classEmoji = u.avatarUri ?: when (u.characterClass) {
        "Warrior" -> "⚔️"
        "Mage"    -> "🔮"
        "Ranger"  -> "🏹"
        else      -> "⚔️"
    }
    val classNameVi = when (u.characterClass) {
        "Warrior" -> "CHIẾN BINH"
        "Mage"    -> "PHÁP SƯ"
        "Ranger"  -> "TỐC XẠ"
        else      -> u.characterClass.uppercase()
    }

    // Gold glowing border for character panel
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x1E1E2F66))
            .border(1.dp, GOLD.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column {
            // Header: avatar + info
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar with animated aura
                AuraProfileAvatar(
                    classEmoji = classEmoji,
                    promotionTier = u.promotionTier
                )

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Class badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PRIMARY.copy(alpha = 0.12f))
                            .border(0.5.dp, PRIMARY.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            classNameVi,
                            color = PRIMARY, fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 0.1f.em
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        u.nickname,
                        color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Level ${u.level}  ·  ${u.streak} ngày streak",
                            color = TEXT_MUTED, fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // EXP bar
            val maxExp = u.level * 1000
            val expProgress = (u.exp.toFloat() / maxExp).coerceIn(0f, 1f)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "EXP", color = GOLD, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 0.1f.em,
                    modifier = Modifier.width(36.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(GLASS_BORDER)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(expProgress)
                            .background(
                                Brush.horizontalGradient(listOf(GOLD, Color(0xFFFFE16D)))
                            )
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "${u.exp}/${maxExp}",
                    color = GOLD, fontSize = 10.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            // Divider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(0.5.dp).background(GLASS_BORDER))
                Text(
                    "  ATTRIBUTES  ",
                    color = TEXT_MUTED, fontSize = 10.sp, letterSpacing = 0.1f.em
                )
                Box(Modifier.weight(1f).height(0.5.dp).background(GLASS_BORDER))
            }

            Spacer(Modifier.height(12.dp))

            // Stat bars
            listOf(
                Triple("STR", s.str, StatColors["STR"]!!),
                Triple("INT", s.intStat, StatColors["INT"]!!),
                Triple("AGI", s.agi, StatColors["AGI"]!!),
                Triple("VIT", s.vit, StatColors["VIT"]!!),
                Triple("WIS", s.wis, StatColors["WIS"]!!),
                Triple("CHA", s.cha, StatColors["CHA"]!!)
            ).forEach { (name, value, color) ->
                StatBarRow(name = name, value = value, color = color)
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Currency row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CurrencyBadge("💰", "${u.gold}", GOLD)
                CurrencyBadge("🔥", "${u.streak} streak", Color(0xFFFF6B6B))
                CurrencyBadge("⚠️", "${u.debtPoints} debt", TEXT_MUTED)
            }
        }
    }
}

@Composable
private fun AuraProfileAvatar(
    classEmoji: String,
    promotionTier: Int
) {
    val auraColor = when (promotionTier) {
        0    -> Color(0xFF4A9EFF)
        1    -> Color(0xFFB48EFF)
        2    -> Color(0xFFFFD700)
        3    -> Color(0xFFFF4444)
        else -> Color(0xFFFFFFFF)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val rotation by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "rot"
    )
    val pulse by infiniteTransition.animateFloat(
        0.5f, 1f,
        infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(80.dp).clip(CircleShape)
                .background(auraColor.copy(alpha = pulse * 0.22f))
        )
        Canvas(Modifier.size(80.dp)) {
            val strokeWidth = 3.5.dp.toPx()
            val r = size.minDimension / 2f - strokeWidth
            rotate(degrees = rotation, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.4f to auraColor.copy(alpha = 0.4f * pulse),
                            0.7f to auraColor.copy(alpha = pulse),
                            0.85f to auraColor,
                            1f to Color.Transparent
                        ),
                        center = center
                    ),
                    radius = r,
                    style = Stroke(width = strokeWidth)
                )
            }
        }
        Box(
            Modifier.size(68.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF1E3A5F), Color(0xFF0A1628)))),
            contentAlignment = Alignment.Center
        ) {
            Text(classEmoji, fontSize = 28.sp)
        }
    }
}

@Composable
private fun StatBarRow(name: String, value: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            name, color = color, fontSize = 11.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 0.08f.em,
            modifier = Modifier.width(36.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(2.5.dp))
                .background(GLASS_BORDER)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((value / 100f).coerceIn(0f, 1f))
                    .background(
                        Brush.horizontalGradient(listOf(color.copy(alpha = 0.6f), color))
                    )
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            value.toString(),
            color = Color.White, fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(28.dp)
        )
    }
}

@Composable
private fun CurrencyBadge(icon: String, label: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f))
            .border(0.5.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 13.sp)
        Spacer(Modifier.width(5.dp))
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Quest progress card ───────────────────────────────────────────────────────
@Composable
private fun QuestProgressCard(
    questSummary: QuestSummary,
    onNavigateToQuests: () -> Unit
) {
    val progress = if (questSummary.total > 0)
        questSummary.completed.toFloat() / questSummary.total else 0f
    val remaining = questSummary.total - questSummary.completed
    val percent = (progress * 100).toInt()

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToQuests() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📋", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "NHIỆM VỤ HÔM NAY",
                        color = PRIMARY_DIM, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.08f.em
                    )
                }
                Text(
                    "${questSummary.completed} / ${questSummary.total} ›",
                    color = PRIMARY, fontSize = 12.sp, fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(GLASS_BORDER)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(Brush.horizontalGradient(listOf(PRIMARY, PRIMARY_DIM)))
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "$percent% hoàn thành · Còn $remaining nhiệm vụ",
                color = TEXT_MUTED, fontSize = 11.sp
            )
        }
    }
}

// ── Finance summary card ──────────────────────────────────────────────────────
@Composable
private fun FinanceSummaryCard(
    totalBalance: Long,
    todayExpense: Long,
    onNavigateToFinance: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val balanceColor = if (totalBalance >= 0) GREEN else Color(0xFFFF6B6B)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToFinance() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💰", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "TÀI CHÍNH",
                        color = GOLD, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.08f.em
                    )
                }
                Text("›", color = GOLD, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SỐ DƯ", color = TEXT_MUTED, fontSize = 10.sp, letterSpacing = 0.08f.em)
                    Text(
                        formatter.format(totalBalance),
                        color = balanceColor, fontSize = 16.sp, fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("CHI HÔM NAY", color = TEXT_MUTED, fontSize = 10.sp, letterSpacing = 0.08f.em)
                    Text(
                        "-${formatter.format(todayExpense)}",
                        color = Color(0xFFFF6B6B), fontSize = 16.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Quick action cards ────────────────────────────────────────────────────────
@Composable
private fun QuickActionsRow(
    onCalendar: () -> Unit,
    onJournal: () -> Unit,
    onLibrary: () -> Unit,
    onInventory: () -> Unit,
    onDailySummary: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            Triple("📅", "LỊCH TRÌNH", onCalendar),
            Triple("📖", "NHẬT KÝ", onJournal),
            Triple("📚", "HỌC TẬP", onLibrary),
            Triple("📊", "BÁO CÁO", onDailySummary),
            Triple("🎒", "KHO ĐỒ", onInventory)
        ).forEach { (icon, label, action) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GLASS.copy(alpha = 0.06f))
                    .border(0.5.dp, GLASS_BORDER, RoundedCornerShape(12.dp))
                    .clickable { action() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(icon, fontSize = 22.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        label, color = TEXT_MUTED,
                        fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 0.08f.em
                    )
                }
            }
        }
    }
}

// ── Advancement ready banner ──────────────────────────────────────────────────
@Composable
private fun AdvancementBanner(onClick: () -> Unit) {
    val glow by rememberInfiniteTransition(label = "adv_banner").animateFloat(
        0.5f, 1f,
        infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "g"
    )
    val GoldColor = Color(0xFFFFD700)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF2A1A00), Color(0xFF1A1000), Color(0xFF2A1A00))
                )
            )
            .border(BorderStroke(0.5.dp, GoldColor.copy(glow * 0.7f)))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚠", color = GoldColor.copy(glow), fontSize = 16.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "CHUYỂN NGHỀ SẴN SÀNG",
                        color = GoldColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.1f.em
                    )
                    Text(
                        "Tất cả chỉ số đã đạt giới hạn. Nhấn để thức tỉnh.",
                        color = GoldColor.copy(0.6f),
                        fontSize = 10.sp
                    )
                }
            }
            Text("›", color = GoldColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Bottom navigation bar ─────────────────────────────────────────────────────
@Composable
private fun BottomNavBar(
    onNavigateToQuests: () -> Unit,
    onNavigateToSkills: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToAura: () -> Unit
) {
    val auraGlow by rememberInfiniteTransition(label = "nav_aura").animateFloat(
        0.4f, 1f,
        infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "aura_glow"
    )
    val Purple = Color(0xFFB48EFF)

    data class NavItem(val icon: String, val label: String, val action: (() -> Unit)?, val isAura: Boolean = false)

    val items = listOf(
        NavItem("🏠", "HOME", null),
        NavItem("📋", "NHIỆM VỤ", onNavigateToQuests),
        NavItem("✦", "AURA", onNavigateToAura, isAura = true),
        NavItem("🌳", "KỸ NĂNG", onNavigateToSkills),
        NavItem("💰", "TÀI CHÍNH", onNavigateToFinance)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xCC0A0A1A))
            .border(BorderStroke(0.5.dp, GLASS_BORDER))
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { idx, item ->
                val isHome = idx == 0
                if (item.isAura) {
                    // Aura tab — elevated center button
                    Column(
                        modifier = Modifier
                            .clickable { item.action?.invoke() }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Purple.copy(auraGlow * 0.5f), Color(0xFF1A0A2E))
                                    )
                                )
                                .border(1.5.dp, Purple.copy(auraGlow), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✦", fontSize = 18.sp, color = Purple.copy(auraGlow))
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "AURA",
                            color = Purple.copy(auraGlow),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1f.em
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .clickable(enabled = item.action != null) { item.action?.invoke() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(item.icon, fontSize = 20.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            item.label,
                            color = if (isHome) PRIMARY else TEXT_MUTED,
                            fontSize = 8.sp,
                            fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal,
                            letterSpacing = 0.06f.em
                        )
                        if (isHome) {
                            Spacer(Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(PRIMARY)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Side navigation bar (Tablet) ──────────────────────────────────────────────
@Composable
private fun SideNavBar(
    onNavigateToQuests: () -> Unit,
    onNavigateToSkills: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToAura: () -> Unit
) {
    val auraGlow by rememberInfiniteTransition(label = "nav_aura_side").animateFloat(
        0.4f, 1f,
        infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "aura_glow_side"
    )
    val Purple = Color(0xFFB48EFF)

    data class NavItem(val icon: String, val label: String, val action: (() -> Unit)?, val isAura: Boolean = false)

    val items = listOf(
        NavItem("🏠", "HOME", null),
        NavItem("📋", "NHIỆM VỤ", onNavigateToQuests),
        NavItem("✦", "AURA", onNavigateToAura, isAura = true),
        NavItem("🌳", "KỸ NĂNG", onNavigateToSkills),
        NavItem("💰", "TÀI CHÍNH", onNavigateToFinance)
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(80.dp)
            .background(Color(0xCC0A0A1A))
            .border(BorderStroke(0.5.dp, GLASS_BORDER))
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.forEachIndexed { idx, item ->
                val isHome = idx == 0
                if (item.isAura) {
                    Column(
                        modifier = Modifier
                            .clickable { item.action?.invoke() }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Purple.copy(auraGlow * 0.5f), Color(0xFF1A0A2E))
                                    )
                                )
                                .border(1.5.dp, Purple.copy(auraGlow), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✦", fontSize = 18.sp, color = Purple.copy(auraGlow))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "AURA",
                            color = Purple.copy(auraGlow),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1f.em
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .clickable(enabled = item.action != null) { item.action?.invoke() }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(item.icon, fontSize = 22.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            item.label,
                            color = if (isHome) PRIMARY else TEXT_MUTED,
                            fontSize = 9.sp,
                            fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal,
                            letterSpacing = 0.06f.em
                        )
                        if (isHome) {
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(PRIMARY)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Motivational quotes ───────────────────────────────────────────────────────

private data class MotivationalQuote(val text: String, val source: String)

private val MOTIVATIONAL_QUOTES = listOf(
    MotivationalQuote("Không có con đường nào dẫn đến sự vĩ đại mà không đi qua khổ luyện.", "Hệ Thống"),
    MotivationalQuote("Kẻ mạnh không phải là người không bao giờ ngã, mà là người biết đứng dậy mỗi lần vấp.", "Triết Lý Hunter"),
    MotivationalQuote("Đừng so sánh hành trình của bạn với người khác. Bạn đang chiến đấu trận chiến của chính mình.", "Hệ Thống"),
    MotivationalQuote("Cấp độ không phải là con số — đó là phản ánh của tất cả những gì bạn đã vượt qua.", "Biên Niên Sử Hunter"),
    MotivationalQuote("Hệ Thống không quan tâm đến cảm xúc. Nó chỉ ghi nhận hành động.", "Hệ Thống"),
    MotivationalQuote("Sức mạnh thực sự không đến từ thể lực — nó đến từ ý chí không thể bị bẻ gãy.", "Sách Cổ"),
    MotivationalQuote("Hãy coi mỗi thất bại là kinh nghiệm, không phải bằng chứng của sự yếu đuối.", "Triết Lý Hunter"),
    MotivationalQuote("Người giỏi nhất không phải là người tài năng nhất — là người không bỏ cuộc.", "Hệ Thống"),
    MotivationalQuote("Dungeon khó nhất trong cuộc đời bạn chính là bản thân bạn.", "Biên Niên Sử"),
    MotivationalQuote("Kỷ luật là sự tự do thực sự. Lười biếng mới là nhà tù vô hình.", "Hệ Thống"),
    MotivationalQuote("Những gì bạn luyện tập trong bóng tối sẽ tỏa sáng dưới ánh đèn sân khấu.", "Triết Lý Hunter"),
    MotivationalQuote("Ai cũng muốn thành công, nhưng không phải ai cũng dám trả giá.", "Hệ Thống"),
    MotivationalQuote("Level up không xảy ra trong vùng thoải mái.", "Hệ Thống"),
    MotivationalQuote("Bắt đầu dù chưa sẵn sàng — sự hoàn hảo là kẻ thù của tiến bộ.", "Sách Cổ"),
    MotivationalQuote("Kiến thức là vũ khí sắc bén nhất. Hãy mài nó từng ngày.", "Biên Niên Sử Hunter"),
    MotivationalQuote("Ngày hôm nay bạn chịu đựng điều khó khăn là ngày mai bạn cảm ơn chính mình.", "Hệ Thống"),
    MotivationalQuote("Thất bại là giáo viên khắc nghiệt nhất và hiệu quả nhất.", "Triết Lý Hunter"),
    MotivationalQuote("Hệ thống đang ghi nhận từng bước tiến của bạn. Hãy tạo ra điều đáng nhớ.", "Hệ Thống"),
    MotivationalQuote("Sức mạnh không được tặng — nó được rèn từ mồ hôi, nước mắt và ý chí.", "Sách Cổ"),
    MotivationalQuote("Một giờ học tập hôm nay = 100 điểm kinh nghiệm. Đừng để ngày trôi qua vô nghĩa.", "Hệ Thống"),
    MotivationalQuote("Mỗi nhiệm vụ bạn hoàn thành là một bước tiến về phía con người bạn muốn trở thành.", "Biên Niên Sử"),
    MotivationalQuote("Không ai nhớ người đứng ở hàng dưới. Hãy leo lên và để lại dấu ấn.", "Triết Lý Hunter"),
    MotivationalQuote("Hôm nay bạn có thể chọn dễ dàng hoặc chọn mạnh mẽ. Chỉ một trong hai.", "Hệ Thống"),
    MotivationalQuote("Mỗi ngày bạn không tiến lên là ngày bạn đứng yên trong khi thế giới tiến về phía trước.", "Sách Cổ")
)

@Composable
private fun MotivationalQuoteCard() {
    var quoteIndex by remember { mutableStateOf(0) }
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(6_000L)
            visible = false
            delay(400L)
            quoteIndex = (quoteIndex + 1) % MOTIVATIONAL_QUOTES.size
            visible = true
        }
    }

    val quote = MOTIVATIONAL_QUOTES[quoteIndex]
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(350),
        label = "quoteAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "quote_glow")
    val borderGlow by infiniteTransition.animateFloat(
        0.25f, 0.55f,
        infiniteRepeatable(tween(2400, easing = EaseInOutSine), RepeatMode.Reverse), "qg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0x1A1E3A5F), Color(0x0F4A1E5F), Color(0x1A1E3A5F))
                )
            )
            .border(
                BorderStroke(0.5.dp, GOLD.copy(alpha = borderGlow)),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .alpha(alpha)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("✦", color = GOLD.copy(0.8f), fontSize = 14.sp,
                modifier = Modifier.padding(top = 2.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "\"${quote.text}\"",
                    color = Color(0xFFD4E4FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "— ${quote.source}",
                    color = GOLD.copy(0.65f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.06f.em
                )
            }
            Text("✦", color = GOLD.copy(0.4f), fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp))
        }
    }
}

// ── Aura NPC greeting bubble ──────────────────────────────────────────────────
@Composable
private fun AuraGreetingBubble(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(7_000L)
        visible = false
        delay(400L)
        onDismiss()
    }

    val slideOffset by animateIntAsState(
        targetValue = if (visible) 0 else 120,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "slide"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(350),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 80.dp)
            .offset(y = slideOffset.dp)
            .alpha(alpha)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xE6130D2E), Color(0xE61E0A3A))
                )
            )
            .border(1.dp, Color(0xFFB48EFF).copy(0.5f), RoundedCornerShape(16.dp))
            .clickable { visible = false }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Aura avatar icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFB48EFF).copy(0.4f), Color(0xFF6A2FBF).copy(0.2f))
                        )
                    )
                    .border(1.dp, Color(0xFFB48EFF).copy(0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🌟", fontSize = 18.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Aura NPC",
                    color = Color(0xFFB48EFF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.06f.em
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    message,
                    color = Color(0xFFE0D4FF),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Text("✕", color = Color(0xFFB48EFF).copy(0.4f), fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}

