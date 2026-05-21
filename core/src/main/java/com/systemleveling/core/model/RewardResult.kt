package com.systemleveling.core.model

/**
 * Result returned after completing a quest, containing all changes to display in the reward popup.
 */
data class RewardResult(
    val questTitle: String,
    val questRank: QuestRank,
    val expGained: Int,
    val goldGained: Int,
    val statChanges: Map<String, Int>, // "STR" -> 3, "INT" -> 5
    val skillPointChanges: Map<String, Int>, // skillId -> SP gained
    val droppedItem: DroppedItemInfo? = null,
    val leveledUp: Boolean = false,
    val newLevel: Int? = null,
    val skillLeveledUp: List<SkillLevelUpInfo> = emptyList()
)

data class DroppedItemInfo(
    val itemId: String,
    val name: String,
    val rarity: ItemRarity,
    val category: ItemCategory,
    val iconId: String,
    val loreDescription: String
)

data class SkillLevelUpInfo(
    val skillId: String,
    val skillName: String,
    val oldLevel: SkillLevel,
    val newLevel: SkillLevel
)

/**
 * Categories that map quest types to stat gains.
 */
object QuestCategoryStatMap {
    val mapping: Map<String, List<String>> = mapOf(
        "fitness" to listOf("STR", "VIT"),
        "health" to listOf("VIT"),
        "language" to listOf("INT"),
        "study" to listOf("INT"),
        "reading" to listOf("INT", "WIS"),
        "tech" to listOf("INT", "AGI"),
        "coding" to listOf("INT", "AGI"),
        "social" to listOf("CHA"),
        "communication" to listOf("CHA"),
        "meditation" to listOf("WIS"),
        "journal" to listOf("WIS"),
        "finance" to listOf("WIS"),
        "career" to listOf("INT", "AGI"),
        "creative" to listOf("CHA", "WIS")
    )

    fun getStatsForCategory(category: String): List<String> {
        return mapping[category.lowercase()] ?: listOf("INT") // default to INT
    }
}
