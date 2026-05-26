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
    private val slotCalculator = QuestTimeSlotCalculator()
    private val fallbackProvider = FallbackQuestProvider(slotCalculator)

    @Suppress("UNUSED_PARAMETER")
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
                val fullList = injectHealthReminders(quests, dayStart, wakeTime, lunchTime, workoutTime, sleepTime)
                questDao.insertQuests(fullList)
                fullList
            } else {
                val fallback = fallbackProvider.generateFallbackQuests(dayStart, wakeTime, workTime, lunchTime, workoutTime, sleepTime)
                val fullList = injectHealthReminders(fallback, dayStart, wakeTime, lunchTime, workoutTime, sleepTime)
                questDao.insertQuests(fullList)
                fullList
            }
        } catch (e: Exception) {
            val wakeTime    = try { settingsManager.wakeTime.first() }    catch (_: Exception) { "06:00" }
            val workTime    = try { settingsManager.workTime.first() }    catch (_: Exception) { "08:00 - 17:00" }
            val lunchTime   = try { settingsManager.lunchTime.first() }   catch (_: Exception) { "12:00 - 13:00" }
            val workoutTime = try { settingsManager.workoutTime.first() } catch (_: Exception) { "17:30 - 18:30" }
            val sleepTime   = try { settingsManager.sleepTime.first() }   catch (_: Exception) { "23:00" }
            val fallback = fallbackProvider.generateFallbackQuests(dayStart, wakeTime, workTime, lunchTime, workoutTime, sleepTime)
            val fullList = injectHealthReminders(fallback, dayStart, wakeTime, lunchTime, workoutTime, sleepTime)
            questDao.insertQuests(fullList)
            fullList
        }
    }

    private suspend fun generateLocalQuests(
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
        val daySlots = slotCalculator.buildDaySlots(wakeTime, workTime, lunchTime, workoutTime, sleepTime)

        // Lookup all skills from DB for SP assignment
        val allSkills = try { skillDao.getAllSkillsSync() } catch (_: Exception) { emptyList() }
        
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

            // Clamp stat rewards based on RewardConstants
            val rc = RewardConstants.forRank(rank)
            val statReward = when (category) {
                "coding" -> "{\"INT\": ${1.coerceAtMost(rc.statTotal.coerceAtLeast(1))}}"
                "study" -> if (rc.statTotal >= 2) "{\"INT\": 1, \"WIS\": 1}" else "{\"INT\": 1}"
                "fitness" -> if (rc.statTotal >= 2) "{\"STR\": 1, \"AGI\": 1}" else "{\"STR\": 1}"
                "health" -> "{\"VIT\": ${1.coerceAtMost(rc.statTotal.coerceAtLeast(1))}}"
                "finance" -> "{\"WIS\": ${1.coerceAtMost(rc.statTotal.coerceAtLeast(1))}}"
                "language" -> if (rc.statTotal >= 2) "{\"INT\": 1, \"CHA\": 1}" else "{\"INT\": 1}"
                else -> "{\"CHA\": ${1.coerceAtMost(rc.statTotal.coerceAtLeast(1))}}"
            }

            // Lookup matching skills for SP rewards
            val matchingSkills = allSkills.filter { skill ->
                val sName = skill.name.lowercase()
                when (category) {
                    "coding" -> sName.contains("code") || sName.contains("lập trình") || sName.contains("dev") || sName.contains("programming")
                    "study" -> sName.contains("học") || sName.contains("study") || sName.contains("research")
                    "fitness" -> sName.contains("gym") || sName.contains("cardio") || sName.contains("strength") || sName.contains("thể") || sName.contains("fitness")
                    "health" -> sName.contains("health") || sName.contains("sức khỏe") || sName.contains("meditation")
                    "finance" -> sName.contains("finance") || sName.contains("tài chính") || sName.contains("money")
                    "language" -> sName.contains("english") || sName.contains("tiếng") || sName.contains("language") || sName.contains("ielts")
                    else -> sName.contains(item.title.lowercase().take(6))
                }
            }.take(rc.maxLinkedSkills)

            val spPerSkill = ((rc.spMin + rc.spMax) / 2).coerceAtLeast(rc.spMin)
            val skillPointRewardsStr = if (matchingSkills.isNotEmpty()) {
                "{" + matchingSkills.joinToString(",") { "\"${it.id}\": $spPerSkill" } + "}"
            } else "{}"
            val relatedIds = if (matchingSkills.isNotEmpty()) {
                "[" + matchingSkills.joinToString(",") { "\"${it.id}\"" } + "]"
            } else "[]"
            
            val (timeStart, timeEnd) = daySlots.getOrElse(index) {
                String.format("%02d:00", (8 + index * 2).coerceAtMost(22)) to
                String.format("%02d:00", (9 + index * 2).coerceAtMost(23))
            }
            
            val exp = (item.workPriority.score * 2).coerceIn(rc.expMin, rc.expMax)
            val gold = (item.workPriority.score / 2).coerceIn(rc.goldMin, rc.goldMax)
            
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
                skillPointRewards = skillPointRewardsStr,
                statPointRewards = statReward,
                penaltyDebtPoints = if (rank == QuestRank.A || rank == QuestRank.B) 2 else 1,
                relatedSkillIds = relatedIds,
                isHealthReminder = false,
                priorityScore = item.workPriority.score
            )
        }
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

    private fun injectHealthReminders(
        quests: List<QuestEntity>,
        dayStart: Long,
        wakeTime: String = "06:00",
        lunchTime: String = "12:00 - 13:00",
        workoutTime: String = "17:30 - 18:30",
        sleepTime: String = "23:00"
    ): List<QuestEntity> {
        val result = quests.toMutableList()
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayStart)
        val meals = slotCalculator.getMealTimes(wakeTime, lunchTime, workoutTime)
        val sleepH = slotCalculator.parseHourStart(sleepTime).coerceAtLeast(20)

        // --- Hydration reminders (5 per day, spaced out) ---
        listOf("08:00", "10:00", "14:00", "16:00", "20:00")
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

        // --- Stretch/movement reminders ---
        listOf("10:30", "14:30").forEachIndexed { i, time ->
            result.add(QuestEntity(
                id = "H-$dateId-S${i + 1}",
                title = "🚶 Đứng Dậy & Vận Động",
                description = "Đứng dậy, đi lại 5 phút, giãn cơ.",
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

        // --- Meal reminders with finance tracking ---
        // Breakfast
        result.add(QuestEntity(
            id = "H-$dateId-M1",
            title = "🍳 Ăn Sáng & Ghi Chi Tiêu",
            description = "Ăn sáng đầy đủ dinh dưỡng. Sau khi ăn, mở app Finance ghi lại chi phí bữa sáng.",
            type = QuestType.DAILY, rank = QuestRank.E, category = "health",
            date = dayStart, timeStart = meals.breakfastStart, timeEnd = meals.breakfastEnd,
            durationMinutes = 30, expReward = 20, goldReward = 10,
            status = QuestStatus.PENDING, penaltyDebtPoints = 0, isHealthReminder = true,
            statPointRewards = "{\"VIT\":1}", skillPointRewards = "{}", priorityScore = 50,
            subtasks = "[\"Ăn sáng (protein + carb + rau/trái cây)\",\"📱 Mở Finance → ghi chi tiêu bữa sáng\"]"
        ))
        // Lunch
        result.add(QuestEntity(
            id = "H-$dateId-M2",
            title = "🍱 Ăn Trưa & Ghi Chi Tiêu",
            description = "Nghỉ trưa, ăn uống bổ sung năng lượng. Ghi chi tiêu bữa trưa vào Finance.",
            type = QuestType.DAILY, rank = QuestRank.E, category = "health",
            date = dayStart, timeStart = meals.lunchStart, timeEnd = meals.lunchEnd,
            durationMinutes = 45, expReward = 20, goldReward = 10,
            status = QuestStatus.PENDING, penaltyDebtPoints = 0, isHealthReminder = true,
            statPointRewards = "{\"VIT\":1}", skillPointRewards = "{}", priorityScore = 50,
            subtasks = "[\"Ăn trưa đầy đủ\",\"📱 Mở Finance → ghi chi tiêu bữa trưa\",\"Nghỉ ngơi 15 phút sau ăn\"]"
        ))
        // Dinner
        result.add(QuestEntity(
            id = "H-$dateId-M3",
            title = "🍽️ Ăn Tối & Ghi Chi Tiêu",
            description = "Ăn tối sau luyện tập, bổ sung protein phục hồi cơ bắp. Ghi chi tiêu bữa tối.",
            type = QuestType.DAILY, rank = QuestRank.E, category = "health",
            date = dayStart, timeStart = meals.dinnerStart, timeEnd = meals.dinnerEnd,
            durationMinutes = 60, expReward = 20, goldReward = 10,
            status = QuestStatus.PENDING, penaltyDebtPoints = 0, isHealthReminder = true,
            statPointRewards = "{\"VIT\":1}", skillPointRewards = "{}", priorityScore = 50,
            subtasks = "[\"Ăn tối (ưu tiên protein + rau)\",\"📱 Mở Finance → ghi chi tiêu bữa tối\",\"Nghỉ ngơi, không tập nặng sau ăn\"]"
        ))

        // --- Daily Finance Wrap-up ---
        result.add(QuestEntity(
            id = "H-$dateId-F1",
            title = "💰 Tổng Kết Chi Tiêu Hàng Ngày",
            description = "Kiểm tra tất cả giao dịch hôm nay, bổ sung những khoản chưa ghi. Đánh giá chi tiêu và đặt mục tiêu ngày mai.",
            type = QuestType.DAILY, rank = QuestRank.D, category = "finance",
            date = dayStart,
            timeStart = String.format("%02d:00", sleepH - 1),
            timeEnd = String.format("%02d:15", sleepH - 1),
            durationMinutes = 15, expReward = 40, goldReward = 25,
            status = QuestStatus.PENDING, penaltyDebtPoints = 0, isHealthReminder = true,
            statPointRewards = "{\"WIS\":1}", skillPointRewards = "{}", priorityScore = 60,
            subtasks = "[\"Mở tab Finance\",\"Kiểm tra & bổ sung giao dịch chưa ghi\",\"Đánh giá: hôm nay chi tiêu hợp lý chưa?\",\"Đặt budget cho ngày mai\"]"
        ))

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
