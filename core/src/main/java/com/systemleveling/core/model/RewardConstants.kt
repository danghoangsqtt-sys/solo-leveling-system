package com.systemleveling.core.model

/**
 * Calibrated reward ranges per quest rank.
 *
 * Design principle: progression should feel meaningful but never effortless.
 * - A NOVICE skill takes ~2-3 weeks of consistent daily quests to reach APPRENTICE.
 * - Stats grow slowly — capping a stat from baseline takes ~60-90 days of perfect play.
 * - Item drops are handled by LootTable (probability-based, not per-quest specified).
 */
object RewardConstants {

    data class RankReward(
        val expMin: Int,
        val expMax: Int,
        val goldMin: Int,
        val goldMax: Int,
        val spMin: Int,     // SP awarded to the linked child skill
        val spMax: Int,
        val statTotal: Int, // Total stat points the AI should distribute (e.g. 2 → "STR": 1, "INT": 1)
        val maxLinkedSkills: Int // How many child skills one quest should link to
    )

    val BY_RANK = mapOf(
        QuestRank.E to RankReward(10, 25, 3, 8, 2, 3, 0, 1),
        QuestRank.D to RankReward(30, 60, 8, 20, 4, 8, 1, 1),
        QuestRank.C to RankReward(80, 150, 25, 50, 8, 15, 1, 2),
        QuestRank.B to RankReward(180, 300, 60, 100, 15, 25, 2, 2),
        QuestRank.A to RankReward(350, 600, 120, 200, 25, 50, 3, 2),
        QuestRank.S to RankReward(700, 1200, 250, 500, 60, 100, 5, 3)
    )

    fun forRank(rank: QuestRank): RankReward = BY_RANK[rank] ?: BY_RANK[QuestRank.D]!!

    /**
     * Prompt-ready reward table for AI consumption.
     */
    fun toPromptTable(): String = """
| Rank | EXP       | Gold     | SP/skill | Stat pts | Max skills |
|------|-----------|----------|----------|----------|------------|
| E    | 10-25     | 3-8      | 2-3      | 0        | 1          |
| D    | 30-60     | 8-20     | 4-8      | 1        | 1          |
| C    | 80-150    | 25-50    | 8-15     | 1        | 2          |
| B    | 180-300   | 60-100   | 15-25    | 2        | 2          |
| A    | 350-600   | 120-200  | 25-50    | 3        | 2          |
| S    | 700-1200  | 250-500  | 60-100   | 5        | 3          |
Stat pts = total stat points distributed across ALL stats in statPointRewards.
SP/skill = SP per individual linked child skill in skillPointRewards.
    """.trimIndent()
}
