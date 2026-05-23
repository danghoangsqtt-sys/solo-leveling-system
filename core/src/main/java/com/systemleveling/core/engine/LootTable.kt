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
        ItemTemplate("Nhẫn Vĩnh Cửu", "Vật phẩm thần thoại không thể phá hủy", "Chiếc nhẫn tạo ra từ lõi của một ngôi sao đã tắt.", ItemRarity.MYTHIC, ItemCategory.COLLECTIBLE, "💍", "ALL_BOOST", 30),

        // ══════════════════════════════════════════════════════════════════════
        // TRANG BỊ — 50 items (EQUIPMENT)
        // ══════════════════════════════════════════════════════════════════════

        // ── EQUIPMENT · COMMON ──
        ItemTemplate("Bao Tay Vải Thô", "Găng tay đơn giản dành cho tân binh", "Bảo vệ bàn tay khỏi vết thương nhỏ trong hành trình đầu tiên.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "🧤", "STAT_BOOST", 2),
        ItemTemplate("Vòng Tay Gỗ", "Vòng tay bùa chú cơ bản", "Khắc từ cành cây linh mộc sống hàng trăm năm.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "📿", null, null),
        ItemTemplate("Thắt Lưng Da Thô", "Dây lưng cơ bản của lữ khách", "Một chiếc thắt lưng chắc chắn từ da thuộc thủ công.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "🔗", "STAT_BOOST", 1),
        ItemTemplate("Khăn Choàng Vải", "Bảo vệ nhẹ trước gió lạnh", "Khăn choàng đơn giản nhưng ấm áp trong gió lạnh vùng biên cương.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "🧣", "STAT_BOOST", 1),
        ItemTemplate("Mũ Vải Cũ", "Mũ bảo hộ cơ bản", "Chiếc mũ đã qua nhiều cuộc phiêu lưu, vẫn còn nguyên vẹn.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "🎩", "STAT_BOOST", 2),
        ItemTemplate("Ủng Đi Rừng", "Giày chắc chắn cho địa hình hiểm", "Phù hợp với địa hình rừng núi hiểm trở phía bắc.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "👢", "STAT_BOOST", 2),
        ItemTemplate("Áo Giáp Da Bò", "Giáp nhẹ bằng da thuộc", "Tuy mỏng nhưng đủ chịu đựng móng vuốt thú nhỏ.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "🥋", "STAT_BOOST", 3),
        ItemTemplate("Nhẫn Đồng", "Nhẫn đơn giản không phép thuật", "Một chiếc nhẫn đồng cũ kỹ nhưng vẫn còn bóng loáng.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "⭕", null, null),
        ItemTemplate("Dây Chuyền Xương", "Bùa hộ mệnh cơ bản", "Tự tay xâu từ xương kẻ thù đầu tiên — mang lại may mắn.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "🦴", "STAT_BOOST", 1),
        ItemTemplate("Mặt Nạ Gỗ", "Che giấu danh tính", "Mặt nạ đục thô từ gỗ thông già, dùng trong lễ hội bộ tộc.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "🎭", null, null),
        ItemTemplate("Vô Lăng Cổ Tay Thép", "Bảo vệ cổ tay", "Miếng thép mỏng quấn quanh cổ tay giúp đỡ đòn chuẩn hơn.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "⚙️", "STAT_BOOST", 2),
        ItemTemplate("Túi Đựng Đồ Vải", "Tăng sức chứa vật phẩm", "Túi vải thô nhưng rất bền, may bởi nghệ nhân làng.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "🎒", "EXP_BOOST", 5),
        ItemTemplate("Giáp Vai Sắt Vụn", "Trang bị vai cơ bản", "Ghép từ nhiều mảnh sắt vụn nhặt được trên chiến trường.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "🔩", "STAT_BOOST", 2),
        ItemTemplate("Kính Đọc Sách", "Tăng khả năng hấp thu tri thức", "Kính mắt cổ điển của những học giả lỗi lạc.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "👓", "SP_BOOST", 5),
        ItemTemplate("Áo Mưa Dầu", "Kháng thời tiết xấu", "Lớp vải dầu chống nước đơn giản giúp tiếp tục hành trình.", ItemRarity.COMMON, ItemCategory.EQUIPMENT, "🧥", "STAT_BOOST", 1),

        // ── EQUIPMENT · UNCOMMON ──
        ItemTemplate("Áo Choàng Bóng Tối", "Kháng phép yếu bậc thấp", "Dệt từ sợi mạng nhện thần mà không con nhện nào còn sống.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "🌑", "STAT_BOOST", 5),
        ItemTemplate("Nhẫn Ngọc Lạnh", "+5 INT khi đeo", "Viên ngọc phát ra hơi lạnh kỳ lạ khi nắm chặt vào ban đêm.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "💎", "STAT_BOOST", 5),
        ItemTemplate("Mũ Bảo Hiểm Sắt", "Giáp đầu cơ bản", "Chiếc mũ sắt được đóng đinh cẩn thận ở xưởng rèn biên cương.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "⛑️", "STAT_BOOST", 6),
        ItemTemplate("Giáp Ngực Đồng", "Bảo vệ tốt hơn da thuộc", "Giáp đồng được đúc tại xưởng thợ rèn nổi tiếng Đông Nam.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "🪖", "STAT_BOOST", 7),
        ItemTemplate("Giày Gió", "+8 AGI khi di chuyển", "Đôi giày được may từ da thú gió — chạy nhanh như gió thổi.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "💨", "STAT_BOOST", 8),
        ItemTemplate("Bao Tay Thép Bạc", "Tăng sức mạnh đòn đấm", "Bọc bằng bạc bên ngoài, thép bên trong — đẹp mà chết người.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "🤜", "STAT_BOOST", 7),
        ItemTemplate("Vòng Cổ Răng Nanh", "Bùa chiến binh tăng dũng khí", "Đeo răng nanh của quái vật đầu tiên bị đánh bại.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "🦷", "STAT_BOOST", 6),
        ItemTemplate("Áo Choàng Học Giả", "+WIS khi học bài", "Áo dài của các học giả giáo viên nổi tiếng học viện hoàng gia.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "🎓", "SP_BOOST", 15),
        ItemTemplate("Thắt Lưng Vàng", "Tăng EXP nhặt từ quest", "Thắt lưng có túi bí mật ở khắp nơi — không bao giờ hết chỗ.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "🥇", "EXP_BOOST", 10),
        ItemTemplate("Ủng Thép Nặng", "Giáp chân cứng chắc", "Đôi ủng nặng nhưng không thể xuyên thủng bởi bất kỳ vật sắc nào.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "🦶", "STAT_BOOST", 7),
        ItemTemplate("Mặt Nạ Bạch Kim", "Tăng CHA +5 khi đeo", "Mặt nạ của một diễn viên vang danh thiên hạ, nay lưu lạc.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "🎭", "STAT_BOOST", 5),
        ItemTemplate("Trâm Cài Phép Thuật", "Hỗ trợ tăng cường ma pháp", "Ghim thêm vào áo sẽ tăng hiệu quả bùa chú lên một bậc.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "📌", "SP_BOOST", 12),
        ItemTemplate("Vòng Tay Rồng Non", "Tăng tái sinh mana", "Vòng từ sừng rồng non chưa trưởng thành — đầy tiềm năng.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "🐲", "SP_BOOST", 10),
        ItemTemplate("Khiên Tay Nhỏ", "Phòng thủ tầm gần linh hoạt", "Khiên tay nhỏ gọn, phù hợp cho đấu tay đôi nhanh nhẹn.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "🛡️", "STAT_BOOST", 8),
        ItemTemplate("Kính Nhìn Xa Cổ", "Phát hiện mối nguy từ xa", "Ống nhòm cổ của một thuyền trưởng huyền thoại bị chìm tàu.", ItemRarity.UNCOMMON, ItemCategory.EQUIPMENT, "🔭", "EXP_BOOST", 12),

        // ── EQUIPMENT · RARE ──
        ItemTemplate("Giáp Tinh Thần", "Tăng WIS và INT đồng thời", "Giáp pha lê trong suốt, nhẹ hơn không khí nhưng cứng hơn thép.", ItemRarity.RARE, ItemCategory.EQUIPMENT, "✨", "STAT_BOOST", 12),
        ItemTemplate("Nhẫn Lửa Vĩnh Cửu", "Kháng hỏa hoàn toàn", "Nhẫn không bao giờ nguội lạnh, ngay cả khi chủ nhân đã qua đời.", ItemRarity.RARE, ItemCategory.EQUIPMENT, "🔥", "STAT_BOOST", 12),
        ItemTemplate("Áo Giáp Đêm", "Tàng hình trong bóng tối", "Giáp được nhuộm bằng mực của mực ảo ảnh từ biển sâu.", ItemRarity.RARE, ItemCategory.EQUIPMENT, "🌙", "STAT_BOOST", 13),
        ItemTemplate("Mũ Trí Huệ", "+15 INT khi đội", "Từng được đội bởi một Đại Pháp Sư bậc 9 — tỏa ra hào quang sáng.", ItemRarity.RARE, ItemCategory.EQUIPMENT, "🧠", "STAT_BOOST", 15),
        ItemTemplate("Giày Bay Bạc", "Bay nhảy linh hoạt như chim", "Đôi giày của ẩn sĩ sống trên núi mây — chưa ai chạm đất.", ItemRarity.RARE, ItemCategory.EQUIPMENT, "🦋", "STAT_BOOST", 14),
        ItemTemplate("Khiên Ngọc Thạch", "Kháng phép mạnh hơn thường", "Mặt khiên khắc rune phòng hộ cổ đại còn sáng mờ.", ItemRarity.RARE, ItemCategory.EQUIPMENT, "🔵", "STAT_BOOST", 13),
        ItemTemplate("Vòng Cổ Sao Rơi", "+15% EXP từ mọi hoạt động", "Đeo một mảnh thiên thạch nhỏ được mài tròn bởi tay tiên.", ItemRarity.RARE, ItemCategory.EQUIPMENT, "⭐", "EXP_BOOST", 15),
        ItemTemplate("Bao Tay Sấm Sét", "Tấn công kèm sát thương điện", "Tia sét nhỏ phóng ra mỗi khi nắm đấm chạm vào kẻ thù.", ItemRarity.RARE, ItemCategory.EQUIPMENT, "⚡", "STAT_BOOST", 13),
        ItemTemplate("Áo Choàng Sương Mai", "Hồi sinh HP chậm rãi", "Áo sương sớm tự động hồi phục vết thương nhỏ qua từng giờ.", ItemRarity.RARE, ItemCategory.EQUIPMENT, "🌫️", "SP_BOOST", 25),
        ItemTemplate("Thắt Lưng Chiến Thần", "Tăng STR tổng thể", "Từng thắt lưng của Đại Tướng Quân đời xưa trước khi ngã xuống.", ItemRarity.RARE, ItemCategory.EQUIPMENT, "⚔️", "STAT_BOOST", 15),

        // ── EQUIPMENT · EPIC ──
        ItemTemplate("Giáp Hoàng Long", "Kháng tất cả nguyên tố", "Rèn từ vảy Hoàng Long thần thánh — một trong bốn thần thú phương Đông.", ItemRarity.EPIC, ItemCategory.EQUIPMENT, "🐉", "STAT_BOOST", 20),
        ItemTemplate("Nhẫn Rỗng Vô Tận", "Triệu hồi năng lượng bóng tối", "Nhẫn không có viên ngọc, nhưng bên trong là khoảng trống vô tận.", ItemRarity.EPIC, ItemCategory.EQUIPMENT, "🕳️", "STAT_BOOST", 22),
        ItemTemplate("Mũ Vương Giả", "Oai phong tuyệt đỉnh +20 CHA", "Chỉ những ai được thiên mệnh lựa chọn mới có thể đội chiếc mũ này.", ItemRarity.EPIC, ItemCategory.EQUIPMENT, "👑", "STAT_BOOST", 20),
        ItemTemplate("Giày Thần Tốc", "Di chuyển với tốc độ cực cao", "Thần tốc bước chân, kẻ thù không kịp nhìn thấy hình dạng.", ItemRarity.EPIC, ItemCategory.EQUIPMENT, "🌪️", "STAT_BOOST", 20),
        ItemTemplate("Áo Choàng Pháp Sư Tối Thượng", "+50 SP cho mọi phép", "Áo choàng của 13 Đại Pháp Sư hội tụ năng lượng vào một chiếc.", ItemRarity.EPIC, ItemCategory.EQUIPMENT, "🔮", "SP_BOOST", 50),
        ItemTemplate("Khiên Anh Hùng", "Hấp thụ đòn tấn công tốt nhất", "Khiên mang hình mặt sư tử phun lửa — không ai vượt qua được.", ItemRarity.EPIC, ItemCategory.EQUIPMENT, "🦁", "STAT_BOOST", 22),
        ItemTemplate("Chuỗi Hổ Phách Cổ Đại", "+20 CHA vĩnh viễn", "Chuỗi hạt từ nhựa cây triệu năm tuổi — mang lại sức hút tự nhiên.", ItemRarity.EPIC, ItemCategory.EQUIPMENT, "🧡", "STAT_BOOST", 20),

        // ── EQUIPMENT · LEGENDARY ──
        ItemTemplate("Áo Giáp Thiên Mệnh", "Bất khả xâm phạm khi mặc", "Áo giáp được thiên mệnh ban cho người được chọn từ trước khi sinh.", ItemRarity.LEGENDARY, ItemCategory.EQUIPMENT, "⚜️", "STAT_BOOST", 30),
        ItemTemplate("Nhẫn Toàn Năng", "+30% tất cả stats cùng lúc", "Chiếc nhẫn sở hữu một tia sáng nằm bên trong mãi mãi không tắt.", ItemRarity.LEGENDARY, ItemCategory.EQUIPMENT, "🌟", "STAT_BOOST", 30),

        // ── EQUIPMENT · MYTHIC ──
        ItemTemplate("Áo Giáp Shadow Monarch", "Bất tử khi mặc — tuyệt đỉnh", "Giáp của Shadow Monarch — kẻ đứng trên đỉnh mọi thứ bậc quyền lực.", ItemRarity.MYTHIC, ItemCategory.EQUIPMENT, "🖤", "STAT_BOOST", 35),

        // ══════════════════════════════════════════════════════════════════════
        // THUỐC & TIÊU HAO — 50 items (CONSUMABLE)
        // ══════════════════════════════════════════════════════════════════════

        // ── CONSUMABLE · COMMON ──
        ItemTemplate("Thuốc Bột Phục Hồi", "Hồi phục cơ bản từ thảo mộc", "Bột thuốc đơn giản từ thảo mộc vùng núi — hiệu quả nhưng đắng.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "🌿", "EXP_BOOST", 20),
        ItemTemplate("Trà Thảo Mộc", "Tăng tập trung nhẹ khi uống", "Pha từ 7 loại lá trà cổ truyền được truyền qua ba thế hệ.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "🍵", "SP_BOOST", 8),
        ItemTemplate("Bánh Năng Lượng", "Phục hồi thể lực tức thì", "Bánh mì thô nhưng giàu năng lượng — lương thực của lính tuần.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "🍞", "EXP_BOOST", 15),
        ItemTemplate("Nước Suối Tinh Khiết", "Thanh lọc cơ thể hoàn toàn", "Từ suối đầu nguồn trên đỉnh núi thiêng chưa ai đặt chân đến.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "💦", "SP_BOOST", 5),
        ItemTemplate("Lá Cây Chữa Lành", "Băng bó dừng chảy máu", "Đặt lên vết thương sẽ dừng chảy máu ngay trong vài giây.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "🌱", "EXP_BOOST", 10),
        ItemTemplate("Thuốc Giảm Đau Cơ Bản", "Giảm mệt mỏi sau luyện tập", "Viên thuốc trắng nhỏ làm từ vỏ cây liễu cổ đại.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "💊", "EXP_BOOST", 15),
        ItemTemplate("Cháo Hồi Sức", "Phục hồi chậm nhưng bền vững", "Cháo nóng với gừng và lá thuốc — bà của mọi chiến binh đều nấu.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "🍲", "EXP_BOOST", 20),
        ItemTemplate("Dầu Thoa Cơ Bắp", "Tăng hiệu quả luyện tập thể chất", "Dầu thiên nhiên giúp cơ bắp phục hồi nhanh hơn sau tập luyện.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "🫙", "SP_BOOST", 8),
        ItemTemplate("Thuốc Ngủ Nhẹ", "Phục hồi qua giấc ngủ sâu", "Một giọt dầu lên gối sẽ giúp ngủ ngon và tỉnh dậy đầy năng lượng.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "😴", "EXP_BOOST", 25),
        ItemTemplate("Vitamin Nhà Giả Kim", "Bổ sung vi chất thiết yếu", "Viên tổng hợp của các nhà giả kim đại tài — không phụ tác dụng.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "🟡", "SP_BOOST", 10),
        ItemTemplate("Nước Táo Thần", "Phục hồi mana nhỏ", "Ép từ táo mọc ở vườn địa đàng — vị ngọt không tả được.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "🍎", "SP_BOOST", 8),
        ItemTemplate("Thạch Hồi Phục", "Phục hồi tức thì nhẹ", "Thạch đông đặc màu xanh, tan nhanh trong miệng như sương sớm.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "🍃", "EXP_BOOST", 20),
        ItemTemplate("Trái Cây Rừng Thánh", "Bổ sung thể lực tức thì", "Trái cây lạ nhặt từ rừng khu dungeon cấp 1 — chưa ai biết tên.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "🍇", "EXP_BOOST", 15),
        ItemTemplate("Thuốc Mỡ Trăn", "Bôi trơn khớp xương linh hoạt", "Dầu trăn giúp khớp xương linh hoạt hơn — thợ rèn hay dùng.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "🐍", "STAT_BOOST", 1),
        ItemTemplate("Bình Sương Lạnh", "Giải nhiệt tỉnh táo tức thì", "Phun sương lạnh giúp tỉnh táo ngay lập tức khi mệt mỏi.", ItemRarity.COMMON, ItemCategory.CONSUMABLE, "❄️", "SP_BOOST", 10),

        // ── CONSUMABLE · UNCOMMON ──
        ItemTemplate("Bình Mana Trung", "Phục hồi mana trung bình", "Bình pha lê đựng nước mana màu lam nhạt phát ánh sáng nhẹ.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "🔵", "SP_BOOST", 30),
        ItemTemplate("Thuốc Tăng Tốc", "+AGI trong suốt 1 nhiệm vụ", "Uống vào sẽ thấy thế giới chậm lại đột ngột — cảm giác kỳ diệu.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "⚡", "STAT_BOOST", 8),
        ItemTemplate("Thuốc Sức Mạnh", "+STR trong suốt 1 nhiệm vụ", "Thuốc sức mạnh truyền thống của chiến binh phương Bắc tuyết giá.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "💪", "STAT_BOOST", 10),
        ItemTemplate("Bình Thần Thánh", "Xua đuổi đen đủi và vận xấu", "Bình nước thánh được linh mục ban phước trong nghi lễ buổi sáng.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "✨", "EXP_BOOST", 30),
        ItemTemplate("Thuốc Trí Tuệ", "+INT trong vòng 24 giờ", "Uống vào đầu óc sáng lên — hiểu mọi thứ nhanh hơn gấp đôi.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "🧠", "STAT_BOOST", 8),
        ItemTemplate("Nước Thánh Trung Cấp", "Hồi phục mạnh hơn nước thánh nhỏ", "Nước thánh cấp cao từ đền thờ bậc 3 trên núi thánh địa.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "💧", "EXP_BOOST", 40),
        ItemTemplate("Tinh Chất Tập Trung", "Tăng tập trung khi học bài", "Tinh chất từ hoa Rosemary thần thánh được nhà giả kim chắt lọc.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "🌸", "SP_BOOST", 25),
        ItemTemplate("Thuốc Xóa Mệt Mỏi", "Phục hồi stamina hoàn toàn", "Uống một lần là quên đi mệt mỏi cả ngày — không cần ngủ.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "🌊", "EXP_BOOST", 35),
        ItemTemplate("Bột Phép Thuật", "Tăng hiệu quả phép thuật", "Bột phát sáng xanh khi rắc vào không gian — khuếch đại bùa chú.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "🌀", "SP_BOOST", 20),
        ItemTemplate("Trà Rồng Xanh", "Kích thích giác quan thứ 6", "Trà xanh đặc biệt từ Núi Rồng phương Đông — chỉ mùa thu mới có.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "🐉", "SP_BOOST", 20),
        ItemTemplate("Thuốc Bổ Xương Ngọc", "Tăng sức chịu đựng thể chất", "Bổ sung canxi tổng hợp từ xương rồng ngọc nghiền thành bột.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "🦴", "STAT_BOOST", 7),
        ItemTemplate("Viên Sáng Tạo", "Mở khóa ý tưởng sáng tạo ẩn", "Viên thuốc kỳ lạ khiến não bộ liên kết mọi thứ với nhau.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "💡", "SP_BOOST", 22),
        ItemTemplate("Bình Máu Quỷ", "Tăng giới hạn sức mạnh tạm thời", "Máu quỷ đỏ thẫm đóng gói trong ampule thủy tinh — nguy hiểm.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "🔴", "STAT_BOOST", 9),
        ItemTemplate("Kẹo Sức Mạnh", "Ngọt nhưng mạnh không ngờ", "Kẹo đúc từ mật ong ong quý trong rừng cổ đại nghìn năm.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "🍬", "STAT_BOOST", 8),
        ItemTemplate("Thuốc Tốc Hồi", "Hồi phục siêu nhanh khi nguy cấp", "Giải pháp khẩn cấp khi HP xuống thấp nguy hiểm — uống ngay.", ItemRarity.UNCOMMON, ItemCategory.CONSUMABLE, "🚑", "EXP_BOOST", 40),

        // ── CONSUMABLE · RARE ──
        ItemTemplate("Bình Elixir Xanh", "+200 SP tức thì", "Thuốc cường hóa tinh thần của các pháp sư bậc cao — hiếm có.", ItemRarity.RARE, ItemCategory.CONSUMABLE, "💠", "SP_BOOST", 200),
        ItemTemplate("Thuốc Đại Lực", "+20% STR trong nhiều ngày", "Chỉ dùng được 1 lần — sức mạnh khổng lồ dâng tràn toàn thân.", ItemRarity.RARE, ItemCategory.CONSUMABLE, "🏋️", "STAT_BOOST", 20),
        ItemTemplate("Tinh Chất Trí Tuệ", "+150 SP cho WIS", "Chắt lọc từ trí tuệ của ngàn trang sách cổ được hội pháp sư lưu giữ.", ItemRarity.RARE, ItemCategory.CONSUMABLE, "📚", "SP_BOOST", 150),
        ItemTemplate("Bình Phượng Hoàng", "Hồi phục hoàn toàn mọi vết thương", "Nước mắt phượng hoàng chữa lành mọi vết thương — cả tâm hồn.", ItemRarity.RARE, ItemCategory.CONSUMABLE, "🦅", "EXP_BOOST", 100),
        ItemTemplate("Thuốc Bất Tử Tạm Thời", "Miễn dịch 1 lần thất bại lớn", "Uống xong sẽ sống sót qua 1 thất bại tuyệt đối — cơ hội thứ hai.", ItemRarity.RARE, ItemCategory.CONSUMABLE, "🛡️", "DEBT_CLEAR", 1),
        ItemTemplate("Tinh Dầu Thiền Định", "Tăng WIS mạnh mẽ", "Dầu thơm của bậc thiền sư đắc đạo sau 40 năm ẩn tu.", ItemRarity.RARE, ItemCategory.CONSUMABLE, "🕯️", "SP_BOOST", 120),
        ItemTemplate("Máu Rồng Cô Đặc", "Tăng tất cả stats vừa phải", "Một giọt máu rồng đủ sức thay đổi vận mệnh của một con người.", ItemRarity.RARE, ItemCategory.CONSUMABLE, "🩸", "STAT_BOOST", 15),
        ItemTemplate("Thuốc Ký Ức", "Nhớ lại kiến thức đã quên", "Uống vào tất cả kiến thức học trước đây đều sống lại trong đầu.", ItemRarity.RARE, ItemCategory.CONSUMABLE, "🧩", "SP_BOOST", 130),
        ItemTemplate("Trái Thiên Đường", "+100 EXP ngay lập tức", "Trái cây kỳ lạ từ cõi thiên đàng — chỉ người có tâm mới thấy.", ItemRarity.RARE, ItemCategory.CONSUMABLE, "🍑", "EXP_BOOST", 100),
        ItemTemplate("Bình Tái Sinh", "Xóa 2 Debt Points tích lũy", "Bình nước tái sinh cuộc đời từ cổ tích — ai cũng chỉ được dùng 1 lần.", ItemRarity.RARE, ItemCategory.CONSUMABLE, "🌈", "DEBT_CLEAR", 2),

        // ── CONSUMABLE · EPIC ──
        ItemTemplate("Thuốc Thần Đại Pháp Sư", "+500 SP tức thì", "Chỉ 7 Đại Pháp Sư biết cách pha chế thuốc này — và cả 7 đều đã mất.", ItemRarity.EPIC, ItemCategory.CONSUMABLE, "🔮", "SP_BOOST", 500),
        ItemTemplate("Tinh Chất Rồng Vàng", "+25% tất cả stats đồng thời", "Tinh chất chiết xuất từ tim Rồng Vàng ngàn tuổi được tìm thấy.", ItemRarity.EPIC, ItemCategory.CONSUMABLE, "🌟", "STAT_BOOST", 25),
        ItemTemplate("Bình Thời Gian", "Hoàn tác 1 lần thất bại lớn", "Bình nước kỳ lạ có thể hoàn tác 1 lần thất bại — thay đổi lịch sử.", ItemRarity.EPIC, ItemCategory.CONSUMABLE, "⏳", "EXP_BOOST", 200),
        ItemTemplate("Thuốc Vô Song", "+30% EXP trong nhiều ngày liền", "Trạng thái vô song — mọi nỗ lực đều gấp đôi kết quả thực tế.", ItemRarity.EPIC, ItemCategory.CONSUMABLE, "🏆", "EXP_BOOST", 200),
        ItemTemplate("Trà Vũ Trụ", "Mở khóa tiềm năng ẩn bên trong", "Trà pha từ hoa trên các vì sao — chỉ thần mới biết cách thu hái.", ItemRarity.EPIC, ItemCategory.CONSUMABLE, "🌌", "STAT_BOOST", 22),
        ItemTemplate("Huyết Dược Sử Thi", "Đột phá qua giới hạn hiện tại", "Thuốc máu huyền thoại giúp người uống vượt qua ngưỡng giới hạn.", ItemRarity.EPIC, ItemCategory.CONSUMABLE, "🩺", "STAT_BOOST", 20),
        ItemTemplate("Bình Ánh Sáng", "Xóa 5 Debt Points một lúc", "Ánh sáng thanh tẩy mọi điều tiêu cực — tẩy trắng tâm hồn.", ItemRarity.EPIC, ItemCategory.CONSUMABLE, "☀️", "DEBT_CLEAR", 5),

        // ── CONSUMABLE · LEGENDARY ──
        ItemTemplate("Thuốc Trường Sinh", "+50 tất cả stats vĩnh viễn", "Thuốc của hoàng đế cổ đại tìm kiếm sự trường sinh bất tử.", ItemRarity.LEGENDARY, ItemCategory.CONSUMABLE, "💛", "STAT_BOOST", 50),
        ItemTemplate("Bình Sáng Thế", "+500 EXP và reset điểm yếu", "Nước từ buổi bình minh sáng thế, tái sinh mọi thứ từ đầu.", ItemRarity.LEGENDARY, ItemCategory.CONSUMABLE, "🌅", "EXP_BOOST", 500),

        // ── CONSUMABLE · MYTHIC ──
        ItemTemplate("Tiên Dược Tuyệt Đỉnh", "Vượt qua mọi giới hạn — bất tử", "Thuốc chỉ tồn tại trong truyền thuyết — uống vào trở thành bất tử.", ItemRarity.MYTHIC, ItemCategory.CONSUMABLE, "🌠", "STAT_BOOST", 50),

        // ══════════════════════════════════════════════════════════════════════
        // VẬT PHẨM KHÁC — 50 items (COLLECTIBLE, MATERIAL, SPECIAL, WEAPON)
        // ══════════════════════════════════════════════════════════════════════

        // ── COLLECTIBLE ──
        ItemTemplate("Đồng Tiền Vàng Cổ", "Đồng tiền từ đế chế đã biến mất", "Đồng tiền từ một đế chế đã bị xóa sổ khỏi sử sách ngàn năm.", ItemRarity.COMMON, ItemCategory.COLLECTIBLE, "🪙", null, null),
        ItemTemplate("Vỏ Sò Phát Sáng", "Vỏ sò phát ánh xanh đêm tối", "Vỏ sò kỳ lạ phát ra ánh sáng xanh trong đêm — loài sò tuyệt chủng.", ItemRarity.COMMON, ItemCategory.COLLECTIBLE, "🐚", null, null),
        ItemTemplate("Xương Quái Vật Cấp 1", "Kỷ niệm chiến thắng đầu tiên", "Xương của quái vật đầu tiên bị tiêu diệt — bắt đầu hành trình.", ItemRarity.COMMON, ItemCategory.COLLECTIBLE, "🦴", null, null),
        ItemTemplate("Lông Vũ Thần Điểu", "Lông màu bảy sắc cầu vồng", "Lông vũ có màu bảy sắc cầu vồng từ loài chim thần bí.", ItemRarity.COMMON, ItemCategory.COLLECTIBLE, "🪶", null, null),
        ItemTemplate("Đá Cuội Suối Thiêng", "Đá từ suối linh thiêng", "Đá tròn lấy từ suối thiêng — được người dân thờ phụng hàng đời.", ItemRarity.COMMON, ItemCategory.COLLECTIBLE, "🪨", null, null),
        ItemTemplate("Nanh Hổ Vàng", "Nanh của hổ vàng huyền thoại", "Nanh của con hổ vàng huyền thoại phương Đông ngàn năm trước.", ItemRarity.UNCOMMON, ItemCategory.COLLECTIBLE, "🐯", "EXP_BOOST", 10),
        ItemTemplate("Tai Nấm Linh Chi Trăm Tuổi", "Linh chi kéo dài tuổi thọ", "Tai nấm linh chi trăm tuổi mọc ở vách đá núi thiêng phương Bắc.", ItemRarity.UNCOMMON, ItemCategory.COLLECTIBLE, "🍄", "SP_BOOST", 15),
        ItemTemplate("Mắt Rắn Đỏ Thần", "Mắt rắn canh giữ kho báu", "Đôi mắt đỏ của con rắn thần canh giữ kho báu bị trộm mất.", ItemRarity.UNCOMMON, ItemCategory.COLLECTIBLE, "👁️", null, null),
        ItemTemplate("Ngà Voi Thiêng", "Mảnh ngà từ voi thánh", "Mảnh ngà nhỏ từ một con voi thánh đã qua đời được hỏa táng.", ItemRarity.UNCOMMON, ItemCategory.COLLECTIBLE, "🐘", null, null),
        ItemTemplate("Vảy Rồng Nước", "Vảy thần sông ngòi cổ đại", "Vảy của Rồng Nước — vị thần sông ngòi cổ đại phương Nam.", ItemRarity.UNCOMMON, ItemCategory.COLLECTIBLE, "🐠", null, null),
        ItemTemplate("Tâm Long", "Nguồn gốc mọi sức mạnh rồng", "Viên đỏ rực bên trong ngực rồng — nguồn gốc mọi sức mạnh phép thuật.", ItemRarity.RARE, ItemCategory.COLLECTIBLE, "❤️", "STAT_BOOST", 10),
        ItemTemplate("Mảnh Vảy Rồng Vàng", "Vảy hiếm từ Rồng Vàng", "Vảy hiếm rụng từ Rồng Vàng — đắt giá vô cùng trên thị trường đen.", ItemRarity.RARE, ItemCategory.COLLECTIBLE, "✨", "EXP_BOOST", 15),
        ItemTemplate("Lông Vũ Phượng Hoàng", "Biểu tượng bất tử vĩnh cửu", "Lông từ đuôi Phượng Hoàng — biểu tượng bất tử rực cháy mãi.", ItemRarity.RARE, ItemCategory.COLLECTIBLE, "🔥", "EXP_BOOST", 20),
        ItemTemplate("Ngọc Thiên Long", "Ngọc rồng từ cõi thiên đàng", "Viên ngọc của Thiên Long bị rơi xuống trần gian ngàn năm trước.", ItemRarity.EPIC, ItemCategory.COLLECTIBLE, "💎", "STAT_BOOST", 18),
        ItemTemplate("Ấn Quyền Năng", "Ấn của Thần Chiến Tranh", "Ấn của Thần Chiến Tranh — trao quyền lực tối cao cho người xứng.", ItemRarity.LEGENDARY, ItemCategory.COLLECTIBLE, "🔱", "STAT_BOOST", 25),

        // ── MATERIAL ──
        ItemTemplate("Sắt Thô Mỏ Cấp 1", "Quặng sắt đào từ mỏ cơ bản", "Quặng sắt đào từ mỏ sắt cấp 1 — nguyên liệu rèn vũ khí thấp.", ItemRarity.COMMON, ItemCategory.MATERIAL, "⛏️", null, null),
        ItemTemplate("Vải Lanh Thô", "Vải thô chưa qua gia công", "Vải thô chưa qua gia công — dùng may áo choàng cơ bản.", ItemRarity.COMMON, ItemCategory.MATERIAL, "🧶", null, null),
        ItemTemplate("Gỗ Thông Già", "Gỗ dẻo dễ uốn cong", "Gỗ thông dẻo, dễ uốn cong — dùng làm cán vũ khí và cung.", ItemRarity.COMMON, ItemCategory.MATERIAL, "🪵", null, null),
        ItemTemplate("Đá Lửa Biên Cương", "Đá tạo lửa khi chạm nhau", "Đá cuội có khả năng tạo lửa khi chạm vào nhau — người lữ hành cần.", ItemRarity.COMMON, ItemCategory.MATERIAL, "🪨", null, null),
        ItemTemplate("Da Thú Cơ Bản", "Da của quái thú cấp thấp", "Da của quái thú cấp thấp — chưa thuộc nhưng vẫn dùng được.", ItemRarity.COMMON, ItemCategory.MATERIAL, "🐾", null, null),
        ItemTemplate("Thép Cao Cấp", "Thép luyện nhiệt độ cao", "Thép được luyện ở nhiệt độ 1500°C — cứng hơn sắt thông thường.", ItemRarity.UNCOMMON, ItemCategory.MATERIAL, "🔩", null, null),
        ItemTemplate("Pha Lê Ma Thuật", "Pha lê khuếch đại phép thuật", "Pha lê có khả năng khuếch đại phép thuật lên gấp đôi cường độ.", ItemRarity.UNCOMMON, ItemCategory.MATERIAL, "💠", "SP_BOOST", 10),
        ItemTemplate("Vải Tơ Nhện Rừng Tối", "Tơ dệt bởi nhện khổng lồ", "Tơ nhện được dệt bởi nhện khổng lồ vùng rừng tối — nhẹ và dai.", ItemRarity.UNCOMMON, ItemCategory.MATERIAL, "🕸️", null, null),
        ItemTemplate("Xương Rồng Ma Giải Thể", "Xương rồng ma thuật đặc biệt", "Xương từ rồng ma thuật phân giải — vẫn còn nguyên khí mana.", ItemRarity.UNCOMMON, ItemCategory.MATERIAL, "💀", null, null),
        ItemTemplate("Đất Sét Thần Linh", "Đất sét mang năng lượng sáng tạo", "Đất sét từ cõi thần mang năng lượng sáng tạo — cái gì cũng đúc được.", ItemRarity.UNCOMMON, ItemCategory.MATERIAL, "🏺", null, null),
        ItemTemplate("Mảnh Thiên Thạch", "Năng lượng vũ trụ trong đá", "Mảnh đá trời rơi mang năng lượng vũ trụ — hiếm gặp ở trần gian.", ItemRarity.RARE, ItemCategory.MATERIAL, "☄️", "EXP_BOOST", 15),
        ItemTemplate("Vàng Nguyên Chất", "Vàng tinh khiết 99.99%", "Thanh vàng tinh khiết 99.99% được khai thác từ mỏ vàng bí ẩn.", ItemRarity.RARE, ItemCategory.MATERIAL, "🥇", null, null),
        ItemTemplate("Ngọc Lục Bảo Sâu", "Ngọc quý hiếm từ núi cao", "Ngọc quý hiếm từ mỏ sâu trong núi cao — ánh xanh rực rỡ.", ItemRarity.RARE, ItemCategory.MATERIAL, "🟢", "SP_BOOST", 20),
        ItemTemplate("Tinh Chất Linh Khí", "Linh khí cô đặc vùng thiêng", "Tinh chất linh khí cô đặc từ vùng đất linh thiêng ngàn năm.", ItemRarity.EPIC, ItemCategory.MATERIAL, "🌀", "STAT_BOOST", 15),
        ItemTemplate("Lõi Rồng Cổ Đại", "Nguyên liệu forge huyền thoại", "Lõi năng lượng bên trong tim rồng — nguyên liệu forge đỉnh nhất.", ItemRarity.LEGENDARY, ItemCategory.MATERIAL, "💛", "STAT_BOOST", 20),

        // ── SPECIAL ──
        ItemTemplate("Token Nhiệm Vụ Ẩn", "Mở khóa nhiệm vụ bí mật", "Token đặc biệt dùng để mở khóa nhiệm vụ ẩn chưa ai khám phá.", ItemRarity.COMMON, ItemCategory.SPECIAL, "🎫", null, null),
        ItemTemplate("Chìa Khóa Dungeon Bí Ẩn", "Mở cổng dungeon chưa khám phá", "Mở cổng vào dungeon bí ẩn chưa được bất kỳ hunter nào đặt chân.", ItemRarity.COMMON, ItemCategory.SPECIAL, "🗝️", null, null),
        ItemTemplate("Bản Đồ Kho Báu Vùng Đông", "Dẫn đến kho báu phía Đông", "Bản đồ dẫn đến nơi ẩn chứa vật phẩm quý giá vùng phía Đông.", ItemRarity.UNCOMMON, ItemCategory.SPECIAL, "🗺️", "EXP_BOOST", 20),
        ItemTemplate("Triệu Hồi Phù Linh Vật", "Triệu hồi linh vật trợ giúp", "Phong ấn triệu hồi linh vật — hóa giải nguy hiểm khi cần thiết.", ItemRarity.UNCOMMON, ItemCategory.SPECIAL, "📃", "SP_BOOST", 20),
        ItemTemplate("Bùa Trừ Tà Hộ Mệnh", "Bảo vệ khỏi ảnh hưởng tiêu cực", "Bùa chú bảo vệ khỏi ảnh hưởng tiêu cực — đeo 24/7 là an toàn.", ItemRarity.UNCOMMON, ItemCategory.SPECIAL, "🧧", "EXP_BOOST", 15),
        ItemTemplate("Cổng Truyền Dịch Tức Thì", "Teleport tức thì đến nơi muốn", "Phù chú mở cổng truyền dịch tức thì — không khoảng cách nào ngăn.", ItemRarity.RARE, ItemCategory.SPECIAL, "🌀", "SP_BOOST", 30),
        ItemTemplate("Quyển Sách Bí Ẩn Cổ Đại", "Tri thức chảy vào không cần đọc", "Sách không thể đọc được — nhưng cảm thấy tri thức chảy vào tự nhiên.", ItemRarity.RARE, ItemCategory.SPECIAL, "📕", "SP_BOOST", 80),
        ItemTemplate("Hòm Báu Phong Ấn", "Bên trong chứa bí ẩn quý giá", "Hòm gỗ khóa chặt — bên trong chứa điều gì đó cực kỳ quý giá.", ItemRarity.RARE, ItemCategory.SPECIAL, "📦", "EXP_BOOST", 80),
        ItemTemplate("Orb Thần Thánh Cõi Trên", "Cầu sáng từ cõi thần", "Cầu sáng rực tỏa ra từ cõi thần — ai cầm cũng thấy bình yên.", ItemRarity.EPIC, ItemCategory.SPECIAL, "🔮", "STAT_BOOST", 20),
        ItemTemplate("Thiên Mệnh Thư Phong Ấn", "Ghi lại thiên mệnh người cầm", "Cuốn sách ghi lại thiên mệnh của người cầm — không ai khác đọc được.", ItemRarity.LEGENDARY, ItemCategory.SPECIAL, "📜", "STAT_BOOST", 25),

        // ── WEAPON ──
        ItemTemplate("Búa Gỗ Thợ Mộc", "Búa gỗ đơn giản chắc chắn", "Búa gỗ đơn giản của thợ mộc — dùng chiến đấu cũng được lắm.", ItemRarity.COMMON, ItemCategory.WEAPON, "🔨", "STAT_BOOST", 2),
        ItemTemplate("Gậy Tre Luyện Võ", "Gậy tre dùng tập luyện", "Gậy tre dài dùng để tập luyện — nhẹ và linh hoạt.", ItemRarity.COMMON, ItemCategory.WEAPON, "🎋", "STAT_BOOST", 2),
        ItemTemplate("Đoản Kiếm Đồng", "Kiếm ngắn đúc bằng đồng", "Kiếm ngắn đúc bằng đồng — dễ rỉ nhưng đủ để chiến đấu cấp thấp.", ItemRarity.UNCOMMON, ItemCategory.WEAPON, "🗡️", "STAT_BOOST", 6),
        ItemTemplate("Cây Trượng Ma Thuật Cơ Bản", "Trượng gỗ khắc rune cơ bản", "Trượng bằng gỗ linh mộc khắc rune cơ bản — amplify phép thuật nhẹ.", ItemRarity.UNCOMMON, ItemCategory.WEAPON, "🪄", "SP_BOOST", 15),
        ItemTemplate("Búa Sắt Chiến Trường", "Búa sắt nặng của chiến binh", "Búa sắt nặng của chiến binh tiền tuyến — mỗi đòn như núi đổ.", ItemRarity.UNCOMMON, ItemCategory.WEAPON, "⚒️", "STAT_BOOST", 8),
        ItemTemplate("Côn Nhị Khúc Bạc", "Côn bạc của võ sư Thiếu Lâm", "Côn nhị khúc bọc bạc của võ sư Thiếu Lâm tặng lại trước khi mất.", ItemRarity.UNCOMMON, ItemCategory.WEAPON, "⛓️", "STAT_BOOST", 9),
        ItemTemplate("Kiếm Sắc Hiệu Năng", "Lưỡi sắc chém đôi viên đá", "Lưỡi kiếm sắc đủ chém đôi viên đá — thợ rèn cấp 7 rèn.", ItemRarity.RARE, ItemCategory.WEAPON, "⚔️", "STAT_BOOST", 14),
        ItemTemplate("Cung Bạch Ngân", "Cung thần khắc hoa văn bạc", "Cung thần khắc hoa văn bạc — chính xác tuyệt đỉnh trong tay người tài.", ItemRarity.RARE, ItemCategory.WEAPON, "🏹", "STAT_BOOST", 14),
        ItemTemplate("Trượng Pháp Sư Tối Thượng", "Trượng của đời Pháp Sư thứ 3", "Trượng của Tối Thượng Pháp Sư đời thứ 3 — chứa đựng ý chí học hỏi.", ItemRarity.EPIC, ItemCategory.WEAPON, "🔯", "STAT_BOOST", 22),
        ItemTemplate("Gươm Vạn Thắng", "Gươm chưa từng bại trận", "Gươm chưa từng bại trận — ý chí chiến thắng hóa thành hình hài kiếm.", ItemRarity.LEGENDARY, ItemCategory.WEAPON, "🌟", "STAT_BOOST", 30)
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
