package com.systemleveling.core.network

import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.model.QuestRank
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.core.model.QuestType
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Generates a hardcoded set of well-rounded daily quests when the AI generator
 * is unavailable or the user has no work plan items.
 *
 * Each fallback quest is time-slotted based on the user's biological clock.
 * Includes: fitness (at correct workout time), meals, study, coding, reading, journal, finance.
 */
class FallbackQuestProvider(
    private val slotCalculator: QuestTimeSlotCalculator = QuestTimeSlotCalculator()
) {

    fun generateFallbackQuests(
        dayStart: Long,
        wakeTime: String = "06:00",
        workTime: String = "08:00 - 17:00",
        lunchTime: String = "12:00 - 13:00",
        workoutTime: String = "17:30 - 18:30",
        sleepTime: String = "23:00"
    ): List<QuestEntity> {
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayStart)

        val wake        = slotCalculator.parseHourStart(wakeTime)
        val workStart   = slotCalculator.parseHourStart(workTime)
        val lunchStart  = slotCalculator.parseHourStart(lunchTime)
        val lunchEnd    = slotCalculator.parseHourEnd(lunchTime)
        val workoutStart = slotCalculator.parseHourStart(workoutTime)
        val workoutEnd   = slotCalculator.parseHourEnd(workoutTime)
        val sleep       = slotCalculator.parseHourStart(sleepTime).coerceAtLeast(20)
        fun h(hour: Int) = String.format("%02d:00", hour.coerceIn(0, 23))
        fun hm(hour: Int, min: Int) = String.format("%02d:%02d", hour.coerceIn(0, 23), min.coerceIn(0, 59))

        // Dinner/rest window: right after workout
        val dinnerRestEnd = (workoutEnd + 1).coerceAtMost(sleep - 2)

        // Study block: first work hour
        val studyStart = (workStart + 1).coerceAtMost(lunchStart - 1)
        val studyEnd   = (studyStart + 1).coerceAtMost(lunchStart)

        // Coding block: afternoon after lunch
        val codeStart = (lunchEnd + 1).coerceAtMost(workoutStart - 1)
        val codeEnd   = (codeStart + 2).coerceAtMost(workoutStart)

        // Evening study: after dinner rest
        val eveningStudyStart = (dinnerRestEnd + 1).coerceAtMost(sleep - 2)
        val eveningStudyEnd   = (eveningStudyStart + 2).coerceAtMost(sleep - 1)

        // Journal: before sleep
        val journalH = (sleep - 1).coerceAtLeast(eveningStudyEnd)

        return listOf(
            // === QUEST 1: Morning Start ===
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

            // === QUEST 2: Deep Study (morning work hours) ===
            QuestEntity(
                id = "Q-$dateId-002",
                title = "📖 Phiên Học Tập Sâu",
                description = "Học tập / nghiên cứu tập trung 90 phút theo kỹ thuật Pomodoro. Không điện thoại, không mạng xã hội.",
                type = QuestType.DAILY, rank = QuestRank.B, category = "study",
                date = dayStart, timeStart = h(studyStart), timeEnd = h(studyEnd),
                durationMinutes = (studyEnd - studyStart) * 60, expReward = 200, goldReward = 70,
                status = QuestStatus.PENDING,
                subtasks = "[\"Tắt tất cả thông báo\",\"Pomodoro 1 (25 phút): đọc & ghi chú\",\"Nghỉ 5 phút\",\"Pomodoro 2 (25 phút): luyện tập\",\"Ôn lại 10 phút: tóm tắt\"]",
                statPointRewards = "{\"INT\":1,\"WIS\":1}", skillPointRewards = "{}", priorityScore = 82
            ),

            // === QUEST 3: Tech Sprint (afternoon) ===
            QuestEntity(
                id = "Q-$dateId-003",
                title = "💻 Sprint Kỹ Thuật",
                description = "Lập trình hoặc thực hành kỹ thuật 2 tiếng. Bắt đầu bằng task nhỏ nhất để lấy đà, sau đó tackle task lớn.",
                type = QuestType.DAILY, rank = QuestRank.C, category = "coding",
                date = dayStart, timeStart = h(codeStart), timeEnd = h(codeEnd),
                durationMinutes = (codeEnd - codeStart) * 60, expReward = 150, goldReward = 52,
                status = QuestStatus.PENDING,
                subtasks = "[\"Đọc lại code hôm qua 5 phút\",\"Code task nhỏ để khởi động\",\"Tackle task chính (60 phút)\",\"Review & commit code\"]",
                statPointRewards = "{\"INT\":1}", skillPointRewards = "{}", priorityScore = 68
            ),

            // === QUEST 4: Workout (at user's workout time!) ===
            QuestEntity(
                id = "Q-$dateId-004",
                title = "⚔️ Luyện Tập Thể Lực",
                description = "Bài tập thể chất theo lịch sinh học: circuit training hoặc cardio. Khởi động 5 phút → bài tập chính → giãn cơ 5 phút.",
                type = QuestType.DAILY, rank = QuestRank.C, category = "fitness",
                date = dayStart, timeStart = h(workoutStart), timeEnd = h(workoutEnd),
                durationMinutes = (workoutEnd - workoutStart) * 60, expReward = 130, goldReward = 45,
                status = QuestStatus.PENDING,
                subtasks = "[\"Khởi động 5 phút (xoay khớp)\",\"Bài tập chính (chạy/gym/circuit)\",\"Giãn cơ & phục hồi 5 phút\",\"Ghi kết quả: reps/thời gian/quãng đường\"]",
                statPointRewards = "{\"STR\":1,\"AGI\":1}", skillPointRewards = "{}", priorityScore = 75
            ),

            // === QUEST 5: Dinner & Rest (after workout) ===
            QuestEntity(
                id = "Q-$dateId-005",
                title = "🍽️ Nghỉ Ngơi & Ăn Tối",
                description = "Thời gian phục hồi sau luyện tập. Ăn tối đầy đủ dinh dưỡng, thư giãn. Đừng quên ghi lại chi tiêu bữa ăn!",
                type = QuestType.DAILY, rank = QuestRank.E, category = "health",
                date = dayStart, timeStart = h(workoutEnd), timeEnd = hm(dinnerRestEnd, 30),
                durationMinutes = 90, expReward = 20, goldReward = 5,
                status = QuestStatus.PENDING,
                subtasks = "[\"Tắm & thay đồ\",\"Ăn tối (protein + rau + carb)\",\"📱 Mở app Finance → ghi chi tiêu bữa tối\",\"Nghỉ ngơi 20 phút\"]",
                statPointRewards = "{\"VIT\":1}", skillPointRewards = "{}", priorityScore = 55
            ),

            // === QUEST 6: Evening Study (20h-22h30) ===
            QuestEntity(
                id = "Q-$dateId-006",
                title = "📚 Chiến Binh Tri Thức (Tối)",
                description = "Phiên học tập / làm việc buổi tối. Đọc sách chuyên ngành, ôn bài, hoặc tiếp tục project. Đọc chủ động — ghi chú, không chỉ đọc mắt.",
                type = QuestType.DAILY, rank = QuestRank.C, category = "study",
                date = dayStart, timeStart = h(eveningStudyStart), timeEnd = h(eveningStudyEnd),
                durationMinutes = (eveningStudyEnd - eveningStudyStart) * 60, expReward = 120, goldReward = 40,
                status = QuestStatus.PENDING,
                subtasks = "[\"Chọn tài liệu/sách cần đọc\",\"Đọc tập trung không bị gián đoạn\",\"Ghi 3 ý tưởng quan trọng nhất\",\"1 điều sẽ áp dụng ngay\"]",
                statPointRewards = "{\"INT\":1,\"WIS\":1}", skillPointRewards = "{}", priorityScore = 70
            ),

            // === QUEST 7: Daily Finance Wrap-up ===
            QuestEntity(
                id = "Q-$dateId-007",
                title = "💰 Tổng Kết Chi Tiêu Hàng Ngày",
                description = "Mở app Finance → kiểm tra tất cả giao dịch hôm nay → bổ sung những khoản chưa ghi. Đặt mục tiêu chi tiêu ngày mai.",
                type = QuestType.DAILY, rank = QuestRank.D, category = "finance",
                date = dayStart, timeStart = hm(eveningStudyEnd, 0), timeEnd = hm(eveningStudyEnd, 15),
                durationMinutes = 15, expReward = 40, goldReward = 25,
                status = QuestStatus.PENDING,
                subtasks = "[\"Mở tab Finance trong app\",\"Kiểm tra & bổ sung giao dịch hôm nay\",\"Đánh giá: hôm nay chi tiêu hợp lý chưa?\",\"Đặt budget cho ngày mai\"]",
                statPointRewards = "{\"WIS\":1}", skillPointRewards = "{}", priorityScore = 60
            ),

            // === QUEST 8: Journal & Sleep ===
            QuestEntity(
                id = "Q-$dateId-008",
                title = "🧘 Phục Hồi & Nhật Ký",
                description = "Thiền 10 phút + viết nhật ký 10 phút. Xả stress, tổng kết ngày, đặt ý định cho ngày mai.",
                type = QuestType.DAILY, rank = QuestRank.D, category = "journal",
                date = dayStart, timeStart = h(journalH), timeEnd = hm(journalH, 30),
                durationMinutes = 30, expReward = 45, goldReward = 16,
                status = QuestStatus.PENDING,
                subtasks = "[\"Thiền 10 phút (tập trung hơi thở)\",\"3 điều tốt đã xảy ra hôm nay\",\"1 điều muốn cải thiện ngày mai\",\"Đặt giờ ngủ\"]",
                statPointRewards = "{\"WIS\":1,\"VIT\":1}", skillPointRewards = "{}", priorityScore = 62
            )
        )
    }
}
