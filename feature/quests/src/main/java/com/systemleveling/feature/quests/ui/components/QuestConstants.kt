package com.systemleveling.feature.quests.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.systemleveling.core.model.QuestRank

// ── Rank meta ─────────────────────────────────────────────────────────────────
data class RankMeta(val color: Color, val bg: Color, val label: String, val glow: Boolean = false)

fun rankMeta(rank: QuestRank) = when (rank) {
    QuestRank.E -> RankMeta(Color(0xFFAAAAAA), Color(0x22AAAAAA), "E")
    QuestRank.D -> RankMeta(Color(0xFF2ED573), Color(0x222ED573), "D")
    QuestRank.C -> RankMeta(Color(0xFF4A9EFF), Color(0x224A9EFF), "C")
    QuestRank.B -> RankMeta(Color(0xFFE040FB), Color(0x22E040FB), "B")
    QuestRank.A -> RankMeta(Color(0xFFFFAB40), Color(0x22FFAB40), "A", glow = true)
    QuestRank.S -> RankMeta(Color(0xFFFF5252), Color(0x33FF5252), "S", glow = true)
}

// ── Shared colors ─────────────────────────────────────────────────────────────
val BG_DEEP  = Color(0xFF050508)
val PRIMARY  = Color(0xFF4A9EFF)
val GOLD     = Color(0xFFFFD700)
