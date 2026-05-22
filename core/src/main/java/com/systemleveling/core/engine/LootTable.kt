package com.systemleveling.core.engine

import com.systemleveling.core.database.entity.ItemEntity
import com.systemleveling.core.model.ItemCategory
import com.systemleveling.core.model.ItemRarity
import com.systemleveling.core.model.QuestRank
import java.util.UUID
import kotlin.random.Random

/**
 * Loot table for item drops from quest completion.
 * Drop rates and rarity pools scale with quest rank.
 * Items are themed as weapons, collectibles, and potions for a gamer feel.
 */
object LootTable {

    // Drop chance by quest rank (0.0 to 1.0) — tuned for rewarding feel
    private val dropRates = mapOf(
        QuestRank.E to 0.40,
        QuestRank.D to 0.55,
        QuestRank.C to 0.65,
        QuestRank.B to 0.75,
        QuestRank.A to 0.85,
        QuestRank.S to 0.95
    )

    // Rarity distribution by quest rank (cumulative weights)
    private val rarityWeights = mapOf(
        QuestRank.E to listOf(
            ItemRarity.COMMON to 80,
            ItemRarity.UNCOMMON to 20
        ),
        QuestRank.D to listOf(
            ItemRarity.COMMON to 60,
            ItemRarity.UNCOMMON to 30,
            ItemRarity.RARE to 10
        ),
        QuestRank.C to listOf(
            ItemRarity.COMMON to 40,
            ItemRarity.UNCOMMON to 30,
            ItemRarity.RARE to 20,
            ItemRarity.EPIC to 10
        ),
        QuestRank.B to listOf(
            ItemRarity.COMMON to 20,
            ItemRarity.UNCOMMON to 30,
            ItemRarity.RARE to 25,
            ItemRarity.EPIC to 20,
            ItemRarity.LEGENDARY to 5
        ),
        QuestRank.A to listOf(
            ItemRarity.COMMON to 10,
            ItemRarity.UNCOMMON to 20,
            ItemRarity.RARE to 30,
            ItemRarity.EPIC to 25,
            ItemRarity.LEGENDARY to 12,
            ItemRarity.MYTHIC to 3
        ),
        QuestRank.S to listOf(
            ItemRarity.UNCOMMON to 10,
            ItemRarity.RARE to 20,
            ItemRarity.EPIC to 30,
            ItemRarity.LEGENDARY to 25,
            ItemRarity.MYTHIC to 15
        )
    )

    // Item pool definitions — weapons & collectibles feel
    data class ItemTemplate(
        val name: String,
        val description: String,
        val lore: String,
        val rarity: ItemRarity,
        val category: ItemCategory,
        val icon: String,
        val effectType: String?,
        val effectValue: Int?
    )

    val allTemplates = listOf(
        // ── COMMON ──
        ItemTemplate("Nước Thánh Nhỏ", "Phục hồi năng lượng cơ bản", "Một lọ nước thánh được tìm thấy tại cổng dungeon tầng 1.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "🧪", "EXP_BOOST", 30),
        ItemTemplate("Mảnh Sắt Cũ", "Mảnh vỡ từ vũ khí cổ đại", "Dù đã rỉ sét, vẫn toát ra một sức mạnh kỳ lạ...", ItemRarity.COMMON, ItemCategory.COLLECTIBLE, "⚙️", null, null),
        ItemTemplate("Đá Rune Vỡ", "Mảnh đá rune không còn năng lượng", "Từng là một phần của hệ thống phong ấn thượng cổ.", ItemRarity.COMMON, ItemCategory.COLLECTIBLE, "🪨", null, null),
        ItemTemplate("Bình Mana Nhỏ", "Phục hồi một ít năng lượng tinh thần", "Nước xanh phát sáng nhẹ trong bóng tối.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "💧", "SP_BOOST", 10),

        // ── UNCOMMON ──
        ItemTemplate("Tinh Thể Tập Trung", "+20% SP cho skill tiếp theo", "Viên pha lê trong suốt phát ra ánh sáng ấm áp khi chạm vào.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "🔮", "SP_BOOST", 20),
        ItemTemplate("Dao Găm Bạc", "Vũ khí tầm gần cơ bản", "Lưỡi dao được rèn từ bạc nguyên chất — khắc tinh của bóng tối.", ItemRarity.UNCOMMON, ItemCategory.WEAPON, "🗡️", "STAT_BOOST", 5),
        ItemTemplate("Giày Tốc Hành", "Tăng tốc hoàn thành nhiệm vụ", "Đôi giày enchanted mang đến cảm giác nhẹ như gió.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "👟", "SPEED_BOOST", 10),
        ItemTemplate("Huy Chương Chiến Binh", "Biểu tượng danh dự", "Ban cho kẻ đã vượt qua thử thách đầu tiên.", ItemRarity.UNCOMMON, ItemCategory.COLLECTIBLE, "🎖️", null, null),

        // ── RARE ──
        ItemTemplate("Cuộn Tri Thức", "+100 SP cho 1 skill bất kỳ", "Cuộn giấy cổ đại ghi chép kiến thức của Hiền Giả.", ItemRarity.RARE, ItemCategory.CONSUMABLE, "📜", "SP_BOOST", 100),
        ItemTemplate("Găng Tay Chiến Binh", "+10% STR bonus cho quest fitness", "Găng tay bọc thép rèn bởi thợ rèn vùng biên cương.", ItemRarity.RARE, ItemCategory.WEAPON, "🧤", "STAT_BOOST", 10),
        ItemTemplate("Cung Dài Gỗ Sồi", "Vũ khí tầm xa chính xác", "Mỗi mũi tên bắn ra đều mang theo ý chí của người cầm cung.", ItemRarity.RARE, ItemCategory.WEAPON, "🏹", "STAT_BOOST", 12),
        ItemTemplate("Ngọc Mắt Rồng", "Viên ngọc phát sáng kỳ lạ", "Người ta nói nó là mắt của một con rồng cổ đại đã ngủ.", ItemRarity.RARE, ItemCategory.COLLECTIBLE, "💎", null, null),

        // ── EPIC ──
        ItemTemplate("Sách Cổ Đại", "+300 SP cho 1 skill", "Trang sách vẫn tỏa ra năng lượng sau hàng nghìn năm.", ItemRarity.EPIC, ItemCategory.CONSUMABLE, "📖", "SP_BOOST", 300),
        ItemTemplate("Lông Phượng Hoàng", "Xóa 3 Debt Points", "Chiếc lông rực cháy với ngọn lửa bất diệt — biểu tượng của sự tái sinh.", ItemRarity.EPIC, ItemCategory.SPECIAL, "🔥", "DEBT_CLEAR", 3),
        ItemTemplate("Kiếm Sấm Sét", "Vũ khí mang sức mạnh sấm sét", "Lưỡi kiếm phóng ra tia sét mỗi khi chém xuống.", ItemRarity.EPIC, ItemCategory.WEAPON, "⚡", "STAT_BOOST", 20),
        ItemTemplate("Khiên Băng Giá", "Phòng thủ tối thượng", "Tấm khiên đông cứng mọi đòn tấn công khi chạm vào.", ItemRarity.EPIC, ItemCategory.WEAPON, "🛡️", "STAT_BOOST", 18),

        // ── LEGENDARY ──
        ItemTemplate("Vương Miện Trí Tuệ", "+15% INT, WIS bonus", "Vương miện của Đại Pháp Sư thời kỳ hoàng kim.", ItemRarity.LEGENDARY, ItemCategory.EQUIPMENT, "👑", "STAT_BOOST", 15),
        ItemTemplate("Excalibur", "+20% EXP cho tất cả quest", "Thanh kiếm huyền thoại — chỉ người được chọn mới nhấc nổi.", ItemRarity.LEGENDARY, ItemCategory.WEAPON, "⚔️", "EXP_BOOST", 20),
        ItemTemplate("Áo Giáp Rồng", "Trang bị phòng thủ tối thượng", "Áo giáp rèn từ vảy rồng — nhẹ như lông vũ, cứng như kim cương.", ItemRarity.LEGENDARY, ItemCategory.WEAPON, "🐉", "STAT_BOOST", 25),

        // ── MYTHIC ──
        ItemTemplate("Dao Găm Shadow Monarch", "+25% tất cả stats", "Vũ khí của Shadow Monarch — mang sức mạnh đỉnh cao của bóng tối.", ItemRarity.MYTHIC, ItemCategory.WEAPON, "🗡️", "STAT_BOOST", 25),
        ItemTemplate("Nhẫn Vĩnh Cửu", "Vật phẩm thần thoại không thể phá hủy", "Chiếc nhẫn tạo ra từ lõi của một ngôi sao đã tắt.", ItemRarity.MYTHIC, ItemCategory.COLLECTIBLE, "💍", "ALL_BOOST", 30)
    )

    /**
     * Roll for a possible item drop based on quest rank.
     * @return ItemEntity if drop successful, null otherwise.
     */
    fun rollDrop(questRank: QuestRank, questId: String): ItemEntity? {
        val dropChance = dropRates[questRank] ?: 0.1
        if (Random.nextDouble() > dropChance) return null // No drop

        val rarity = rollRarity(questRank)
        val candidates = allTemplates.filter { it.rarity == rarity }
        if (candidates.isEmpty()) return null

        val template = candidates.random()
        return ItemEntity(
            id = UUID.randomUUID().toString(),
            name = template.name,
            description = template.description,
            loreDescription = template.lore,
            rarity = template.rarity,
            category = template.category,
            quantity = 1,
            iconId = template.icon,
            effectType = template.effectType,
            effectValue = template.effectValue,
            fromQuestId = questId
        )
    }

    /** Always returns a COMMON item — used for first-quest guarantee. */
    fun rollGuaranteedDrop(questId: String): ItemEntity {
        val template = allTemplates.filter { it.rarity == ItemRarity.COMMON }.random()
        return ItemEntity(
            id = UUID.randomUUID().toString(),
            name = template.name,
            description = template.description,
            loreDescription = template.lore,
            rarity = template.rarity,
            category = template.category,
            quantity = 1,
            iconId = template.icon,
            effectType = template.effectType,
            effectValue = template.effectValue,
            fromQuestId = questId
        )
    }

    private fun rollRarity(questRank: QuestRank): ItemRarity {
        val weights = rarityWeights[questRank] ?: return ItemRarity.COMMON
        val total = weights.sumOf { it.second }
        var roll = Random.nextInt(total)
        for ((rarity, weight) in weights) {
            roll -= weight
            if (roll < 0) return rarity
        }
        return weights.last().first
    }
}
