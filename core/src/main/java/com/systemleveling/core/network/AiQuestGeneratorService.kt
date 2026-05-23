package com.systemleveling.core.network

import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.SkillDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.model.QuestRank
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.core.model.QuestType
import com.systemleveling.core.model.RewardConstants
import com.systemleveling.core.model.WorkPlanItem
import com.systemleveling.core.settings.SettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates daily quest generation using a priority-aware AI prompt.
 *
 * Priority algorithm (Eisenhower Matrix):
 *   CRITICAL (urgent+important) → multiple small sub-quests, placed first in the day
 *   HIGH (important)            → 1-2 focused quests during peak hours
 *   NORMAL (routine)            → interleaved between urgent quests
 *   LOW (optional)              → late afternoon, skippable
 *
 * Rewards are calibrated against RewardConstants to prevent power creep.
 * SP rewards reference actual child skill IDs from the DB.
 */
@Singleton
class AiQuestGeneratorService @Inject constructor(
    private val geminiApiService: GeminiApiService,
    private val userDao: UserDao,
    private val questDao: QuestDao,
    private val skillDao: SkillDao,
    private val settingsManager: SettingsManager
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun generateDailyQuests(apiKey: String, dayStart: Long): List<QuestEntity> {
        val dayEnd = dayStart + 86400000L
        val existingCount = questDao.getQuestCountByDate(dayStart, dayEnd)
        if (existingCount > 0) return emptyList()

        return try {
            val workPlan = settingsManager.workPlanItems.first()
            val quests = generateLocalQuests(workPlan, dayStart)

            if (quests.isNotEmpty()) {
                val fullList = injectHealthReminders(quests, dayStart)
                questDao.insertQuests(fullList)
                fullList
            } else {
                val fallback = generateFallbackQuests(dayStart)
                questDao.insertQuests(fallback)
                fallback
            }
        } catch (_: Exception) {
            val fallback = generateFallbackQuests(dayStart)
            questDao.insertQuests(fallback)
            fallback
        }
    }

    private fun generateLocalQuests(workPlan: List<WorkPlanItem>, dayStart: Long): List<QuestEntity> {
        if (workPlan.isEmpty()) return emptyList()
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayStart)
        
        return workPlan.sortedByDescending { it.workPriority.score }.mapIndexed { index, item ->
            val rank = when {
                item.workPriority.score >= 90 -> QuestRank.A
                item.workPriority.score >= 75 -> QuestRank.B
                item.workPriority.score >= 50 -> QuestRank.C
                item.workPriority.score >= 30 -> QuestRank.D
                else -> QuestRank.E
            }
            
            val lowerTitle = item.title.lowercase()
            val category = when {
                lowerTitle.contains("code") || lowerTitle.contains("bug") || lowerTitle.contains("dev") -> "coding"
                lowerTitle.contains("học") || lowerTitle.contains("đọc") || lowerTitle.contains("nghiên cứu") -> "study"
                lowerTitle.contains("tập") || lowerTitle.contains("chạy") || lowerTitle.contains("gym") -> "fitness"
                lowerTitle.contains("ngủ") || lowerTitle.contains("thiền") -> "health"
                lowerTitle.contains("tiền") || lowerTitle.contains("tài chính") -> "finance"
                else -> "creative"
            }
            
            val statReward = when (category) {
                "coding", "study" -> "{\"INT\": 1}"
                "fitness", "health" -> "{\"STR\": 1, \"VIT\": 1}"
                "finance" -> "{\"WIS\": 1}"
                else -> "{\"CHA\": 1}"
            }
            
            val startHour = 8 + (index * 2) // Basic time slot distribution
            val timeStart = String.format("%02d:00", startHour.coerceAtMost(22))
            val timeEnd = String.format("%02d:00", (startHour + 1).coerceAtMost(23))
            
            val exp = (item.workPriority.score * 2).coerceIn(10, 500)
            val gold = (item.workPriority.score / 2).coerceIn(5, 100)
            
            val subtasksStr = if (item.note.isNotBlank()) "[\"${item.note.replace("\"", "'")}\"]" else "[]"
            
            QuestEntity(
                id = "Q-$dateId-${String.format("%03d", index + 1)}",
                title = item.title,
                description = if (item.deadline.isNotBlank()) "Deadline: ${item.deadline}" else "Nhiệm vụ tự động",
                type = QuestType.DAILY,
                rank = rank,
                category = category,
                date = dayStart,
                timeStart = timeStart,
                timeEnd = timeEnd,
                durationMinutes = item.estimatedMinutes.takeIf { it > 0 } ?: 60,
                expReward = exp,
                goldReward = gold,
                status = QuestStatus.PENDING,
                subtasks = subtasksStr,
                skillPointRewards = "{}",
                statPointRewards = statReward,
                penaltyDebtPoints = if (rank == QuestRank.A || rank == QuestRank.B) 2 else 1,
                relatedSkillIds = "[]",
                isHealthReminder = false,
                priorityScore = item.workPriority.score
            )
        }
    }

    private suspend fun buildQuestPrompt(dayStart: Long, workPlan: List<WorkPlanItem>): String {
        val user = userDao.getUser().first()
        val stats = userDao.getStats().first()
        val weeklyPlan = settingsManager.weeklyPlanItems.first()
        val monthlyPlan = settingsManager.monthlyPlanItems.first()

        // Pass only child skills (parentId != null) so AI can reference real skill IDs
        val allSkills = skillDao.getAllSkillsSync()
        val childSkills = allSkills.filter { it.parentId != null }
        val parentSkills = allSkills.filter { it.parentId == null }

        val calendar = Calendar.getInstance().apply { timeInMillis = dayStart }
        val dayOfWeek = SimpleDateFormat("EEEE", Locale("vi")).format(calendar.time)
        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)

        // Format skill map for AI with IDs
        val skillsBlock = if (parentSkills.isEmpty()) "[]" else parentSkills.joinToString("\n") { parent ->
            val children = childSkills.filter { it.parentId == parent.id }
            val childBlock = children.joinToString(", ") { c ->
                """{"id":"${c.id}","name":"${c.name}","level":"${c.level.title}","sp":${c.currentSp},"maxSp":${c.level.maxSp},"category":"${c.category}"}"""
            }
            """Branch "${parent.name}": [$childBlock]"""
        }

        // Work plan with priority scores
        val workPlanBlock = if (workPlan.isEmpty()) {
            "Không có kế hoạch đặc biệt. Tạo quests dựa trên skill branches và thói quen phát triển."
        } else {
            workPlan.sortedByDescending { it.workPriority.score }.joinToString("\n") { item ->
                "- [${item.priority}] \"${item.title}\"" +
                (if (item.deadline.isNotBlank()) " | Deadline: ${item.deadline}" else "") +
                " | ~${item.estimatedMinutes}min" +
                (if (item.note.isNotBlank()) " | Note: ${item.note}" else "")
            }
        }

        val hasCritical = workPlan.any { it.workPriority.score >= 100 }
        val hasHigh = workPlan.any { it.workPriority.score >= 75 }

        val weeklyBlock = if (weeklyPlan.isEmpty()) "Chưa có kế hoạch tuần." else
            weeklyPlan.joinToString("\n") { "- [${it.priority}] ${it.title}${if (it.deadline.isNotBlank()) " (deadline: ${it.deadline})" else ""}" }
        val monthlyBlock = if (monthlyPlan.isEmpty()) "Chưa có kế hoạch tháng." else
            monthlyPlan.joinToString("\n") { "- [${it.priority}] ${it.title}" }

        return """
Bạn là AI Hệ Thống của ứng dụng Solo Leveling — một game nhập vai phát triển bản thân thực tế.
Nhiệm vụ của bạn: Tạo lịch quest CÁ NHÂN HÓA cho ngày $dayOfWeek, $dateStr.

════════════ HỒ SƠ NGƯỜI CHƠI ════════════
Nghề nghiệp: ${user?.characterClass ?: "Warrior"} | Cấp: ${user?.level ?: 1} | Streak: ${user?.streak ?: 0} ngày
Debt Points: ${user?.debtPoints ?: 0}
Stats: STR=${stats?.str ?: 10}, INT=${stats?.intStat ?: 10}, AGI=${stats?.agi ?: 10}, VIT=${stats?.vit ?: 10}, WIS=${stats?.wis ?: 10}, CHA=${stats?.cha ?: 10}

════════════ CÂY KỸ NĂNG (SKILL TREE) ════════════
$skillsBlock

════════════ KẾ HOẠCH HÔM NAY ════════════
$workPlanBlock

════════════ MỤC TIÊU TUẦN ════════════
$weeklyBlock

════════════ MỤC TIÊU THÁNG ════════════
$monthlyBlock

════════════ THUẬT TOÁN ƯU TIÊN ════════════
Phân loại mọi công việc theo ma trận Eisenhower (Urgent × Important):

CRITICAL [score 95-100] = Gấp + Quan trọng:
  → Chia thành 3-5 sub-quests nhỏ (20-45 min mỗi cái), đặt VÀO ĐẦU ngày (06:00-12:00)
  → Đặt rank B hoặc A, có deadline rõ ràng trong description
  → Subtasks phải CỤ THỂ và HÀNH ĐỘNG được ngay (không chung chung)

HIGH [score 75-94] = Quan trọng, không gấp:
  → 1-2 quest tập trung trong giờ đỉnh cao (09:00-12:00 hoặc 14:00-17:00)
  → Rank C hoặc B

NORMAL [score 40-74] = Thường xuyên, routine:
  → Đan xen với CRITICAL/HIGH: sau mỗi 2 quest gấp, chen 1 quest bình thường
  → Giúp người chơi "thở" và không bị burn out
  → Rank D hoặc C

LOW [score 0-39] = Tùy chọn, không gấp:
  → Cuối chiều (17:00-20:00)
  → Rank E hoặc D

NGUYÊN TẮC ĐAN XEN:
${if (hasCritical) "⚠️ Có CRITICAL tasks → Ưu tiên đặt vào slot sáng, chia nhỏ tối đa." else ""}
${if (hasHigh) "⚡ Có HIGH tasks → Đặt vào peak focus window 09:00-12:00." else ""}
- Sau mỗi 90 phút làm việc gấp, thêm 1 quest nhẹ (health/meditation) để recover
- Luôn kết thúc ngày với 1 quest review/journal (21:00-21:30)

════════════ BẢNG THƯỞNG CÂN BẰNG ════════════
${RewardConstants.toPromptTable()}

QUAN TRỌNG — Điểm thưởng phải CÂN BẰNG:
- KHÔNG thưởng quá nhiều: người chơi phát triển nhanh quá sẽ mất động lực vì cảm thấy "dễ quá"
- skillPointRewards: PHẢI dùng đúng child skill ID từ SKILL TREE ở trên, chỉ link skill phù hợp với nội dung quest
- statPointRewards: phân phối đúng stats phù hợp với category quest, không chia nhỏ < 1 điểm/stat

════════════ YÊU CẦU OUTPUT ════════════
Trả về MỘT JSON array duy nhất. KHÔNG có markdown (không có ```json).
Mỗi quest object CẦN ĐỦ các field sau:

{
  "title": "string ngắn gọn, style RPG",
  "description": "string tiếng Việt, CỤ THỂ và HÀNH ĐỘNG — mô tả rõ cần làm gì",
  "type": "DAILY",
  "rank": "E|D|C|B|A|S",
  "category": "fitness|health|study|reading|tech|coding|language|meditation|journal|finance|career|social|creative",
  "timeStart": "HH:mm",
  "timeEnd": "HH:mm",
  "durationMinutes": number,
  "expReward": number (theo bảng thưởng),
  "goldReward": number (theo bảng thưởng),
  "subtasks": ["bước cụ thể 1", "bước cụ thể 2"],
  "skillPointRewards": {"<child_skill_id_thực>": sp_amount},
  "statPointRewards": {"STR"|"INT"|"AGI"|"VIT"|"WIS"|"CHA": point},
  "penaltyDebtPoints": 1,
  "priorityScore": number (0-100, theo thứ tự ưu tiên)
}

Tổng số quest: 6-10 (không tính health reminders — sẽ inject sau).
Thời gian: từ 06:00 đến 22:00. Không trùng lặp time slot.
        """.trimIndent()
    }

    private fun parseAiQuests(aiResponse: String, dayStart: Long): List<QuestEntity> {
        return try {
            val quests = json.decodeFromString<List<AiQuestDto>>(aiResponse)
            quests.mapIndexed { index, dto ->
                QuestEntity(
                    id = "Q-${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayStart)}-${String.format("%03d", index + 1)}",
                    title = dto.title,
                    description = dto.description,
                    type = try { QuestType.valueOf(dto.type) } catch (_: Exception) { QuestType.DAILY },
                    rank = try { QuestRank.valueOf(dto.rank) } catch (_: Exception) { QuestRank.D },
                    category = dto.category,
                    date = dayStart,
                    timeStart = dto.timeStart,
                    timeEnd = dto.timeEnd,
                    durationMinutes = dto.durationMinutes ?: 30,
                    expReward = dto.expReward.coerceIn(10, 1200),
                    goldReward = dto.goldReward.coerceIn(3, 500),
                    status = QuestStatus.PENDING,
                    subtasks = json.encodeToString(
                        ListSerializer(String.serializer()), dto.subtasks ?: emptyList()
                    ),
                    skillPointRewards = json.encodeToString(
                        MapSerializer(String.serializer(), Int.serializer()),
                        dto.skillPointRewards ?: emptyMap()
                    ),
                    statPointRewards = json.encodeToString(
                        MapSerializer(String.serializer(), Int.serializer()),
                        dto.statPointRewards ?: emptyMap()
                    ),
                    penaltyDebtPoints = dto.penaltyDebtPoints ?: 1,
                    relatedSkillIds = "[]",
                    isHealthReminder = false,
                    priorityScore = dto.priorityScore?.coerceIn(0, 100) ?: 50
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun injectHealthReminders(quests: List<QuestEntity>, dayStart: Long): List<QuestEntity> {
        val result = quests.toMutableList()
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayStart)

        listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00")
            .forEachIndexed { i, time ->
                result.add(QuestEntity(
                    id = "H-$dateId-W${i + 1}",
                    title = "💧 Hydration Check",
                    description = "Uống 1 cốc nước (250ml) — Duy trì VIT stat!",
                    type = QuestType.DAILY, rank = QuestRank.E, category = "health",
                    date = dayStart, timeStart = time,
                    timeEnd = time.let {
                        val h = it.substringBefore(":").toInt()
                        "${String.format("%02d", h)}:${String.format("%02d", it.substringAfter(":").toInt() + 10)}"
                    },
                    durationMinutes = 5, expReward = 12, goldReward = 3,
                    status = QuestStatus.PENDING, penaltyDebtPoints = 0, isHealthReminder = true,
                    statPointRewards = "{}", skillPointRewards = "{}", priorityScore = 30
                ))
            }

        listOf("10:30", "14:30", "17:00").forEachIndexed { i, time ->
            result.add(QuestEntity(
                id = "H-$dateId-S${i + 1}",
                title = "🚶 Đứng Dậy & Vận Động",
                description = "Đứng dậy, đi lại 5 phút, giãn cơ. Cơ thể cần vận động sau khi ngồi lâu!",
                type = QuestType.DAILY, rank = QuestRank.E, category = "health",
                date = dayStart, timeStart = time,
                timeEnd = time.let {
                    val h = it.substringBefore(":").toInt()
                    "${String.format("%02d", h)}:${String.format("%02d", it.substringAfter(":").toInt() + 10)}"
                },
                durationMinutes = 5, expReward = 12, goldReward = 3,
                status = QuestStatus.PENDING, penaltyDebtPoints = 0, isHealthReminder = true,
                statPointRewards = "{\"VIT\":1}", skillPointRewards = "{}", priorityScore = 35
            ))
        }

        return result.sortedBy { it.timeStart ?: "99:99" }
    }

    private fun generateFallbackQuests(dayStart: Long): List<QuestEntity> {
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayStart)
        val quests = listOf(
            QuestEntity("Q-$dateId-001", "🌅 Morning Hydration", "Uống 500ml nước ngay khi thức dậy. Cơ thể mất nước sau 8 tiếng ngủ.",
                QuestType.DAILY, QuestRank.E, "health", dayStart, "06:00", "06:15",
                15, 20, 5, QuestStatus.PENDING, statPointRewards="{}", skillPointRewards="{}", priorityScore = 60),
            QuestEntity("Q-$dateId-002", "⚔️ Morning Warrior Training", "Tập luyện thể chất buổi sáng: 20 hít đất + 30 squat + 10 phút chạy bộ.",
                QuestType.DAILY, QuestRank.C, "fitness", dayStart, "06:30", "07:30",
                60, 120, 40, QuestStatus.PENDING,
                subtasks = "[\"20 hít đất\",\"30 squat\",\"10 phút chạy bộ\"]",
                statPointRewards="{\"STR\":1}", skillPointRewards="{}", priorityScore = 70),
            QuestEntity("Q-$dateId-003", "📖 Deep Focus: Study Session", "Tập trung học tập hoặc nghiên cứu chuyên sâu trong 90 phút, không điện thoại.",
                QuestType.DAILY, QuestRank.B, "study", dayStart, "08:30", "10:00",
                90, 200, 70, QuestStatus.PENDING,
                subtasks = "[\"Tắt thông báo\",\"Đặt timer Pomodoro 25min\",\"Ghi notes\"]",
                statPointRewards="{\"INT\":2}", skillPointRewards="{}", priorityScore = 80),
            QuestEntity("Q-$dateId-004", "💻 Afternoon Tech Sprint", "Lập trình hoặc thực hành kỹ thuật 2 tiếng.",
                QuestType.DAILY, QuestRank.C, "coding", dayStart, "14:00", "16:00",
                120, 140, 50, QuestStatus.PENDING,
                statPointRewards="{\"INT\":1}", skillPointRewards="{}", priorityScore = 65),
            QuestEntity("Q-$dateId-005", "📚 Knowledge Seeker", "Đọc sách hoặc tài liệu 30 phút — ưu tiên sách chuyên ngành.",
                QuestType.DAILY, QuestRank.D, "reading", dayStart, "17:00", "17:30",
                30, 50, 18, QuestStatus.PENDING,
                statPointRewards="{\"WIS\":1}", skillPointRewards="{}", priorityScore = 55),
            QuestEntity("Q-$dateId-006", "🧘 Evening Reflection", "Viết nhật ký ngắn: 3 điều tốt hôm nay, 1 điều cần cải thiện.",
                QuestType.DAILY, QuestRank.D, "journal", dayStart, "21:00", "21:30",
                30, 40, 15, QuestStatus.PENDING,
                statPointRewards="{\"WIS\":1}", skillPointRewards="{}", priorityScore = 60)
        )
        return injectHealthReminders(quests, dayStart)
    }
}

@Serializable
data class AiQuestDto(
    val title: String,
    val description: String,
    val type: String = "DAILY",
    val rank: String = "D",
    val category: String = "study",
    val timeStart: String? = null,
    val timeEnd: String? = null,
    val durationMinutes: Int? = 30,
    val expReward: Int = 50,
    val goldReward: Int = 20,
    val subtasks: List<String>? = null,
    val skillPointRewards: Map<String, Int>? = null,
    val statPointRewards: Map<String, Int>? = null,
    val penaltyDebtPoints: Int? = 1,
    val priorityScore: Int? = 50
)
