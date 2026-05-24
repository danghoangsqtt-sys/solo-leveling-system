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

    @Suppress("UnusedParameter")
    suspend fun generateDailyQuests(apiKey: String, dayStart: Long): List<QuestEntity> {
        val dayEnd = dayStart + 86400000L
        val existingCount = questDao.getQuestCountByDate(dayStart, dayEnd)
        if (existingCount > 0) return emptyList()

        return try {
            val workPlan = settingsManager.workPlanItems.first()
            val wakeTime    = settingsManager.wakeTime.first()
            val workTime    = settingsManager.workTime.first()
            val lunchTime   = settingsManager.lunchTime.first()
            val workoutTime = settingsManager.workoutTime.first()
            val sleepTime   = settingsManager.sleepTime.first()
            val quests = generateLocalQuests(workPlan, dayStart, wakeTime, workTime, lunchTime, workoutTime, sleepTime)

            if (quests.isNotEmpty()) {
                val fullList = injectHealthReminders(quests, dayStart)
                questDao.insertQuests(fullList)
                fullList
            } else {
                val fallback = generateFallbackQuests(dayStart, wakeTime, workTime, lunchTime, workoutTime, sleepTime)
                questDao.insertQuests(fallback)
                fallback
            }
        } catch (e: Exception) {
            val wakeTime    = try { settingsManager.wakeTime.first() }    catch (_: Exception) { "06:00" }
            val workTime    = try { settingsManager.workTime.first() }    catch (_: Exception) { "08:00 - 17:00" }
            val lunchTime   = try { settingsManager.lunchTime.first() }   catch (_: Exception) { "12:00 - 13:00" }
            val workoutTime = try { settingsManager.workoutTime.first() } catch (_: Exception) { "17:30 - 18:30" }
            val sleepTime   = try { settingsManager.sleepTime.first() }   catch (_: Exception) { "23:00" }
            val fallback = generateFallbackQuests(dayStart, wakeTime, workTime, lunchTime, workoutTime, sleepTime)
            questDao.insertQuests(fallback)
            fallback
        }
    }

    private fun generateLocalQuests(
        workPlan: List<WorkPlanItem>,
        dayStart: Long,
        wakeTime: String = "06:00",
        workTime: String = "08:00 - 17:00",
        lunchTime: String = "12:00 - 13:00",
        workoutTime: String = "17:30 - 18:30",
        sleepTime: String = "23:00"
    ): List<QuestEntity> {
        if (workPlan.isEmpty()) return emptyList()
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayStart)
        val daySlots = buildDaySlots(wakeTime, workTime, lunchTime, workoutTime, sleepTime)
        
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
                lowerTitle.contains("code") || lowerTitle.contains("bug") || lowerTitle.contains("dev") ||
                lowerTitle.contains("lập trình") || lowerTitle.contains("app") || lowerTitle.contains("api") -> "coding"
                lowerTitle.contains("học") || lowerTitle.contains("đọc") || lowerTitle.contains("nghiên cứu") ||
                lowerTitle.contains("study") || lowerTitle.contains("khóa học") || lowerTitle.contains("course") -> "study"
                lowerTitle.contains("tập") || lowerTitle.contains("chạy") || lowerTitle.contains("gym") ||
                lowerTitle.contains("thể dục") || lowerTitle.contains("workout") || lowerTitle.contains("squat") ||
                lowerTitle.contains("yoga") || lowerTitle.contains("bơi") || lowerTitle.contains("fitness") ||
                lowerTitle.contains("thể thao") || lowerTitle.contains("hít đất") -> "fitness"
                lowerTitle.contains("ngủ") || lowerTitle.contains("thiền") || lowerTitle.contains("sức khỏe") -> "health"
                lowerTitle.contains("tiền") || lowerTitle.contains("tài chính") || lowerTitle.contains("đầu tư") -> "finance"
                lowerTitle.contains("tiếng") || lowerTitle.contains("ngôn ngữ") || lowerTitle.contains("english") -> "language"
                else -> "creative"
            }
            
            val statReward = when (category) {
                "coding" -> "{\"INT\": 1}"
                "study" -> "{\"INT\": 1, \"WIS\": 1}"
                "fitness" -> "{\"STR\": 1, \"AGI\": 1}"
                "health" -> "{\"VIT\": 1}"
                "finance" -> "{\"WIS\": 1}"
                "language" -> "{\"INT\": 1, \"CHA\": 1}"
                else -> "{\"CHA\": 1}"
            }
            
            val (timeStart, timeEnd) = daySlots.getOrElse(index) {
                String.format("%02d:00", (8 + index * 2).coerceAtMost(22)) to
                String.format("%02d:00", (9 + index * 2).coerceAtMost(23))
            }
            
            val exp = (item.workPriority.score * 2).coerceIn(10, 500)
            val gold = (item.workPriority.score / 2).coerceIn(5, 100)
            
            val description = buildCategoryDescription(category, item.title, item.deadline, item.note)
            val subtasksStr = buildCategorySubtasks(category, item.note)

            QuestEntity(
                id = "Q-$dateId-${String.format("%03d", index + 1)}",
                title = item.title,
                description = description,
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

        val wakeTime = settingsManager.wakeTime.first()
        val sleepTime = settingsManager.sleepTime.first()
        val workTime = settingsManager.workTime.first()
        val lunchTime = settingsManager.lunchTime.first()
        val workoutTime = settingsManager.workoutTime.first()

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
════════════ LỊCH TRÌNH SINH HỌC & LÀM VIỆC ════════════
Giờ thức dậy: $wakeTime
Giờ đi ngủ: $sleepTime
Giờ làm việc: $workTime
Nghỉ trưa: $lunchTime
Thời gian tập luyện: $workoutTime

* Yêu cầu BẮT BUỘC về thời gian:
- KHÔNG xếp nhiệm vụ nặng, học tập, hoặc làm việc vào Giờ đi ngủ hoặc Nghỉ trưa.
- Nhiệm vụ thể chất (fitness/workout) PHẢI ưu tiên xếp vào "Thời gian tập luyện" ($workoutTime) hoặc "Giờ thức dậy" ($wakeTime).
- Các nhiệm vụ học tập / làm việc phải nằm gọn trong "Giờ làm việc" ($workTime) hoặc thời gian rảnh rỗi.

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
- LUÔN thêm 1-2 quest fitness/thể chất (06:00-08:00) nếu skill tree có nhánh liên quan thể lực, sức khỏe, võ thuật hoặc thể thao — bất kể work plan có hay không
- Quest fitness PHẢI có subtasks cụ thể: số reps/sets, thời gian, cách ghi kết quả — KHÔNG chung chung như "tập thể dục"
- Sau mỗi 90 phút làm việc gấp, thêm 1 quest nhẹ (health/meditation) để recover
- Luôn kết thúc ngày với 1 quest review/journal (21:00-21:30)
- description của TỪNG quest PHẢI: nêu rõ cần làm GÌ, bắt đầu TỪ ĐÂU, và làm BAO NHIÊU — không được viết chung chung

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

    private fun buildCategoryDescription(category: String, title: String, deadline: String, note: String): String {
        val base = when (category) {
            "fitness" -> {
                val lc = title.lowercase()
                when {
                    lc.contains("chạy") -> "[$title] Chạy bộ theo đúng cự ly và nhịp tim mục tiêu. Ghi lại thời gian, quãng đường và nhịp tim sau khi hoàn thành."
                    lc.contains("gym") || lc.contains("tập") -> "[$title] Tập gym theo chương trình hôm nay — ghi số reps, sets và cân nặng từng bài tập. Không bỏ qua khởi động 5 phút."
                    lc.contains("yoga") -> "[$title] Thực hiện đầy đủ các tư thế yoga — chú ý hơi thở và giữ tư thế đúng ít nhất 30 giây/tư thế."
                    lc.contains("bơi") -> "[$title] Bơi đúng cự ly đặt ra — ghi lại kỹ thuật, số vòng và thời gian hoàn thành."
                    lc.contains("squat") -> "[$title] Hoàn thành đúng số squat đặt ra: lưng thẳng, đầu gối không vượt ngón chân, hông xuống thấp hơn đầu gối."
                    else -> "[$title] Thực hiện đầy đủ bài tập. Khởi động 5 phút → bài tập chính → giãn cơ 5 phút. Ghi lại số reps/sets/thời gian thực hiện."
                }
            }
            "study" -> "[$title] Học tập tập trung không bị gián đoạn — đọc tài liệu, ghi chú điểm mấu chốt, thực hành bài tập nếu có. Tắt mọi thông báo trước khi bắt đầu."
            "coding" -> "[$title] Đọc lại context và yêu cầu 5 phút → bắt đầu code → commit sau mỗi tính năng nhỏ hoàn chỉnh → kiểm tra output và test trước khi kết thúc."
            "language" -> "[$title] Luyện tập chủ động: nghe → nhắc lại ngay → ghi từ/cụm từ mới vào sổ. Tư duy trực tiếp bằng ngôn ngữ đó, không dịch từng chữ."
            "health" -> "[$title] Thực hiện đúng và đều đặn — cơ thể và tinh thần cần sự nhất quán mỗi ngày để duy trì VIT stat."
            "finance" -> "[$title] Mở app tài chính → kiểm tra số dư → ghi chép chi tiêu chưa ghi → đặt mục tiêu tiết kiệm cụ thể cho ngày mai."
            "meditation" -> "[$title] Ngồi yên tĩnh, lưng thẳng, nhắm mắt. Đặt timer đúng thời gian. Tập trung vào hơi thở — khi tâm trí phân tán, nhẹ nhàng kéo về."
            "journal" -> "[$title] Viết nhật ký thành thật trong ít nhất 10 phút: 3 điều tốt đã xảy ra hôm nay, 1 điều muốn cải thiện, và mục tiêu cụ thể cho ngày mai."
            "reading" -> "[$title] Đọc chủ động — dừng lại sau mỗi trang/đoạn và tóm tắt bằng lời của mình. Ghi ít nhất 3 ý tưởng có thể áp dụng ngay."
            "career" -> "[$title] Hoàn thành đúng yêu cầu, kiểm tra lại kết quả kỹ lưỡng trước khi nộp/báo cáo. Ghi chú những điểm cần cải thiện lần sau."
            else -> "[$title] Bắt đầu ngay bằng bước nhỏ nhất → duy trì tập trung → đánh dấu hoàn thành khi xong thực sự, không phải khi 'gần xong'."
        }
        val extras = buildList {
            if (note.isNotBlank()) add("Ghi chú: $note")
            if (deadline.isNotBlank()) add("⏰ Deadline: $deadline")
        }
        return if (extras.isEmpty()) base else "$base ${extras.joinToString(" | ")}"
    }

    private fun buildCategorySubtasks(category: String, note: String): String {
        val steps = when (category) {
            "fitness" -> listOf("Khởi động 5 phút", "Thực hiện bài tập chính", "Giãn cơ & phục hồi 5 phút", "Ghi lại kết quả (reps/thời gian)")
            "study" -> listOf("Tắt mọi thông báo", "Đọc/xem tài liệu tập trung", "Ghi chú điểm mấu chốt", "Ôn lại nhanh những gì đã học")
            "coding" -> listOf("Đọc lại code hôm qua 5 phút", "Viết code theo task", "Kiểm tra output / chạy test", "Commit với message rõ ràng")
            "language" -> listOf("Nghe/đọc 10 phút", "Luyện nói/viết 10 phút", "Ghi từ/cụm từ mới", "Ôn lại từ ngày hôm trước")
            "meditation" -> listOf("Tìm nơi yên tĩnh", "Đặt timer theo thời gian", "Tập trung vào hơi thở", "Ghi nhanh cảm nhận sau thiền")
            "journal" -> listOf("3 điều tốt hôm nay", "1 điều muốn cải thiện", "Mục tiêu nhỏ cho ngày mai")
            "reading" -> listOf("Đọc không bị gián đoạn", "Tóm tắt sau mỗi chương/đoạn", "Ghi 3 ý tưởng áp dụng được")
            else -> if (note.isNotBlank()) listOf(note) else emptyList()
        }
        return if (steps.isEmpty()) "[]"
        else "[${steps.joinToString(",") { "\"${it.replace("\"", "'")}\"" }}]"
    }

    private fun parseHourStart(s: String) =
        s.trim().split("-").first().trim().substringBefore(":").toIntOrNull() ?: 8

    private fun parseHourEnd(s: String) =
        s.trim().split("-").last().trim().substringBefore(":").toIntOrNull() ?: 17

    private fun buildDaySlots(
        wakeTime: String, workTime: String, lunchTime: String,
        workoutTime: String, sleepTime: String
    ): List<Pair<String, String>> {
        val wake = parseHourStart(wakeTime)
        val workStart = parseHourStart(workTime)
        val lunchStart = parseHourStart(lunchTime)
        val lunchEnd = parseHourEnd(lunchTime)
        val workoutStart = parseHourStart(workoutTime)
        val workoutEnd = parseHourEnd(workoutTime)
        val sleep = parseHourStart(sleepTime).coerceAtLeast(20)
        fun fmt(h: Int) = String.format("%02d:00", h.coerceIn(0, 23))
        val result = mutableListOf<Pair<String, String>>()
        // Pre-work: wake → workStart
        var h = wake
        while (h + 1 <= workStart && result.size < 3) { result.add(fmt(h) to fmt(h + 1)); h++ }
        // Morning work: workStart → lunchStart
        h = workStart
        while (h + 1 <= lunchStart) { result.add(fmt(h) to fmt(h + 1)); h++ }
        // Afternoon work: lunchEnd → workoutStart
        h = lunchEnd
        while (h + 1 <= workoutStart) { result.add(fmt(h) to fmt(h + 1)); h++ }
        // Workout slot
        if (workoutEnd > workoutStart) result.add(fmt(workoutStart) to fmt(workoutEnd))
        // Evening: workoutEnd → sleep-1
        h = workoutEnd
        while (h + 1 <= sleep - 1 && result.size < 16) { result.add(fmt(h) to fmt(h + 1)); h++ }
        return result.ifEmpty { (0..8).map { i -> fmt(8 + i) to fmt(9 + i) } }
    }

    private fun generateFallbackQuests(
        dayStart: Long,
        wakeTime: String = "06:00",
        workTime: String = "08:00 - 17:00",
        lunchTime: String = "12:00 - 13:00",
        workoutTime: String = "17:30 - 18:30",
        sleepTime: String = "23:00"
    ): List<QuestEntity> {
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayStart)

        val wake        = parseHourStart(wakeTime)
        val workStart   = parseHourStart(workTime)
        val lunchEnd    = parseHourEnd(lunchTime)
        val workoutStart = parseHourStart(workoutTime)
        val workoutEnd   = parseHourEnd(workoutTime)
        val sleep       = parseHourStart(sleepTime).coerceAtLeast(20)
        fun h(hour: Int) = String.format("%02d:00", hour.coerceIn(0, 23))
        fun hm(hour: Int, min: Int) = String.format("%02d:%02d", hour.coerceIn(0, 23), min.coerceIn(0, 59))

        // Morning fitness: wake → wake+1, capped at workStart. Falls back to pre-lunch if no pre-work time.
        val mornFitEnd = (wake + 1).coerceAtMost(workStart)
        val fitnessStart = if (mornFitEnd > wake) wake else (lunchEnd - 1).coerceAtLeast(workStart)
        val fitnessEnd   = if (mornFitEnd > wake) mornFitEnd else lunchEnd

        // Study: 1h into workday
        val studyStart = (workStart + 1).coerceAtMost(lunchEnd - 2)
        val studyEnd   = (studyStart + 1).coerceAtMost(lunchEnd)

        // Afternoon coding: 1h after lunch
        val codeStart = (lunchEnd + 1).coerceAtMost(workoutStart - 2)
        val codeEnd   = (codeStart + 2).coerceAtMost(workoutStart)

        // Reading after workout
        val readStart = workoutEnd
        val readEnd   = (readStart + 1).coerceAtMost(sleep - 2)

        // Journal: 1.5h before sleep
        val journalH = (sleep - 2).coerceAtLeast(readEnd + 1)

        val quests = listOf(
            QuestEntity(
                id = "Q-$dateId-001",
                title = "🌅 Khởi Đầu Ngày Mới",
                description = "Uống 500ml nước ngay khi thức dậy + 10 cái hít thở sâu. Cơ thể mất nước sau 8 tiếng ngủ — nạp năng lượng ngay!",
                type = QuestType.DAILY, rank = QuestRank.E, category = "health",
                date = dayStart, timeStart = h(wake), timeEnd = hm(wake, 20),
                durationMinutes = 20, expReward = 25, goldReward = 8,
                status = QuestStatus.PENDING,
                subtasks = "[\"Uống 500ml nước\",\"10 hít thở sâu (4s hít - 4s giữ - 4s thở)\",\"Ghi cảm xúc buổi sáng vào nhật ký\"]",
                statPointRewards = "{\"VIT\":1}", skillPointRewards = "{}", priorityScore = 65
            ),
            QuestEntity(
                id = "Q-$dateId-002",
                title = "⚔️ Sức Mạnh Buổi Sáng",
                description = "Bài tập thể chất buổi sáng: 3 vòng circuit training. Không bỏ qua khởi động và giãn cơ — injury sẽ phá streak của bạn!",
                type = QuestType.DAILY, rank = QuestRank.C, category = "fitness",
                date = dayStart, timeStart = hm(fitnessStart, 30), timeEnd = h(fitnessEnd),
                durationMinutes = ((fitnessEnd - fitnessStart) * 60 - 30).coerceAtLeast(30), expReward = 130, goldReward = 45,
                status = QuestStatus.PENDING,
                subtasks = "[\"Khởi động 5 phút (xoay khớp)\",\"Vòng 1: 20 hít đất + 30 squat + 20 bụng\",\"Vòng 2: 15 hít đất + 25 squat + 15 bụng\",\"Vòng 3: 10 hít đất + 20 squat + 10 bụng\",\"Giãn cơ 5 phút\",\"Ghi số reps thực tế hoàn thành\"]",
                statPointRewards = "{\"STR\":1,\"AGI\":1}", skillPointRewards = "{}", priorityScore = 75
            ),
            QuestEntity(
                id = "Q-$dateId-003",
                title = "🏃 Chiến Binh Cardio",
                description = "Chạy bộ hoặc đạp xe 30 phút. Duy trì nhịp tim 130-150 bpm. Nếu không ra ngoài được: 20 phút nhảy dây / tại chỗ.",
                type = QuestType.DAILY, rank = QuestRank.C, category = "fitness",
                date = dayStart, timeStart = h(workoutStart), timeEnd = h(workoutEnd),
                durationMinutes = (workoutEnd - workoutStart) * 60, expReward = 80, goldReward = 28,
                status = QuestStatus.PENDING,
                subtasks = "[\"Mang giày thể thao\",\"Chạy/đạp xe 30 phút (hoặc nhảy tại chỗ)\",\"Đo nhịp tim hoặc ước tính effort\",\"Ghi quãng đường / thời gian\"]",
                statPointRewards = "{\"AGI\":1,\"VIT\":1}", skillPointRewards = "{}", priorityScore = 70
            ),
            QuestEntity(
                id = "Q-$dateId-004",
                title = "📖 Phiên Học Tập Sâu",
                description = "Học tập / nghiên cứu tập trung 90 phút theo kỹ thuật Pomodoro. Không điện thoại, không mạng xã hội. Đọc chủ động — ghi chú, không chỉ đọc mắt.",
                type = QuestType.DAILY, rank = QuestRank.B, category = "study",
                date = dayStart, timeStart = h(studyStart), timeEnd = h(studyEnd),
                durationMinutes = (studyEnd - studyStart) * 60, expReward = 200, goldReward = 70,
                status = QuestStatus.PENDING,
                subtasks = "[\"Tắt tất cả thông báo\",\"Pomodoro 1 (25 phút): đọc & ghi chú\",\"Nghỉ 5 phút (đứng dậy, không điện thoại)\",\"Pomodoro 2 (25 phút): tiếp tục + luyện tập\",\"Ôn lại 10 phút: tóm tắt những gì đã học\"]",
                statPointRewards = "{\"INT\":2,\"WIS\":1}", skillPointRewards = "{}", priorityScore = 82
            ),
            QuestEntity(
                id = "Q-$dateId-005",
                title = "💻 Sprint Kỹ Thuật",
                description = "Lập trình hoặc thực hành kỹ thuật 2 tiếng. Bắt đầu bằng task nhỏ nhất để lấy đà, sau đó tackle task lớn. Commit code mỗi 30 phút.",
                type = QuestType.DAILY, rank = QuestRank.C, category = "coding",
                date = dayStart, timeStart = h(codeStart), timeEnd = h(codeEnd),
                durationMinutes = (codeEnd - codeStart) * 60, expReward = 150, goldReward = 52,
                status = QuestStatus.PENDING,
                subtasks = "[\"Đọc lại code/task hôm qua 5 phút\",\"Code task nhỏ trước để khởi động\",\"Tackle task chính (đặt timer 60 phút)\",\"Review & commit code\",\"Ghi note những gì cần làm tiếp\"]",
                statPointRewards = "{\"INT\":1}", skillPointRewards = "{}", priorityScore = 68
            ),
            QuestEntity(
                id = "Q-$dateId-006",
                title = "📚 Chiến Binh Tri Thức",
                description = "Đọc sách 30 phút — ưu tiên sách chuyên ngành hoặc phát triển bản thân. Đọc chủ động: tóm tắt ý chính sau mỗi đoạn.",
                type = QuestType.DAILY, rank = QuestRank.D, category = "reading",
                date = dayStart, timeStart = h(readStart), timeEnd = h(readEnd),
                durationMinutes = (readEnd - readStart) * 60, expReward = 55, goldReward = 20,
                status = QuestStatus.PENDING,
                subtasks = "[\"Chọn sách / chương cần đọc\",\"Đọc không bị gián đoạn 30 phút\",\"Ghi 3 ý tưởng quan trọng nhất\",\"1 điều sẽ áp dụng ngay hôm nay\"]",
                statPointRewards = "{\"WIS\":1,\"INT\":1}", skillPointRewards = "{}", priorityScore = 58
            ),
            QuestEntity(
                id = "Q-$dateId-007",
                title = "🧘 Phục Hồi & Nhật Ký",
                description = "Thiền 10 phút + viết nhật ký 10 phút. Xả stress, tổng kết ngày, đặt ý định cho ngày mai. Kết thúc ngày chiến binh đúng cách!",
                type = QuestType.DAILY, rank = QuestRank.D, category = "journal",
                date = dayStart, timeStart = h(journalH), timeEnd = hm(journalH, 30),
                durationMinutes = 30, expReward = 45, goldReward = 16,
                status = QuestStatus.PENDING,
                subtasks = "[\"Thiền 10 phút (tập trung hơi thở)\",\"3 điều tốt đã xảy ra hôm nay\",\"1 điều muốn cải thiện ngày mai\",\"Đặt giờ ngủ và giờ thức dậy\"]",
                statPointRewards = "{\"WIS\":1,\"VIT\":1}", skillPointRewards = "{}", priorityScore = 62
            )
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
