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
        val lunchEnd    = slotCalculator.parseHourEnd(lunchTime)
        val workoutStart = slotCalculator.parseHourStart(workoutTime)
        val workoutEnd   = slotCalculator.parseHourEnd(workoutTime)
        val sleep       = slotCalculator.parseHourStart(sleepTime).coerceAtLeast(20)
        fun h(hour: Int) = String.format("%02d:00", hour.coerceIn(0, 23))
        fun hm(hour: Int, min: Int) = String.format("%02d:%02d", hour.coerceIn(0, 23), min.coerceIn(0, 59))

        // Morning fitness: wake → wake+1, capped at workStart
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

        return listOf(
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
    }
}
