package com.systemleveling.feature.skills.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.SkillDao
import com.systemleveling.core.database.entity.SkillEntity
import com.systemleveling.core.model.SkillLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SkillGroup(
    val parent: SkillEntity,
    val children: List<SkillEntity>
) {
    val aggregateCurrentSp: Int get() = if (children.isEmpty()) parent.currentSp
        else children.sumOf { it.currentSp }

    val aggregateMaxSp: Int get() = if (children.isEmpty()) parent.level.maxSp
        else children.sumOf { it.level.maxSp }

    val aggregateProgress: Float get() =
        if (aggregateMaxSp == 0) 0f
        else (aggregateCurrentSp.toFloat() / aggregateMaxSp).coerceIn(0f, 1f)

    val masteryLevel: SkillLevel get() {
        if (children.isEmpty()) return parent.level
        val avgProgress = aggregateProgress
        return when {
            avgProgress >= 1f   -> SkillLevel.GRAND_MASTER
            avgProgress >= 0.8f -> SkillLevel.MASTER
            avgProgress >= 0.6f -> SkillLevel.EXPERT
            avgProgress >= 0.4f -> SkillLevel.ADVANCED
            avgProgress >= 0.2f -> SkillLevel.INTERMEDIATE
            avgProgress >= 0.05f -> SkillLevel.APPRENTICE
            else                 -> SkillLevel.NOVICE
        }
    }
}

@HiltViewModel
class SkillTreeViewModel @Inject constructor(
    private val skillDao: SkillDao
) : ViewModel() {

    private val _skillGroups = MutableStateFlow<List<SkillGroup>>(emptyList())
    val skillGroups: StateFlow<List<SkillGroup>> = _skillGroups.asStateFlow()

    // Kept for backward compat (empty — SkillTreeScreen uses skillGroups)
    val skills: StateFlow<List<SkillEntity>> = MutableStateFlow(emptyList())

    init {
        viewModelScope.launch {
            skillDao.getAllSkills().collect { all ->
                if (all.isEmpty()) {
                    seedMockData()
                } else {
                    buildGroups(all)
                }
            }
        }
    }

    private fun buildGroups(all: List<SkillEntity>) {
        val parents = all.filter { it.parentId == null }
        val childMap = all.filter { it.parentId != null }.groupBy { it.parentId }

        _skillGroups.value = parents.map { parent ->
            SkillGroup(
                parent = parent,
                children = childMap[parent.id] ?: emptyList()
            )
        }
    }

    private suspend fun seedMockData() {
        val mockGroups = listOf(
            Triple("tieng-anh", "Tiếng Anh IELTS", listOf(
                SkillEntity("S2", "Reading", "Đọc hiểu thần tốc", SkillLevel.INTERMEDIATE, 350, "tieng-anh", "📖"),
                SkillEntity("S3", "Writing", "Viết luận sắc bén", SkillLevel.APPRENTICE, 150, "tieng-anh", "✍️"),
                SkillEntity("S7", "Listening", "Nghe như người bản xứ", SkillLevel.INTERMEDIATE, 450, "tieng-anh", "🎧"),
                SkillEntity("S4", "Vocabulary", "Từ vựng mở rộng", SkillLevel.EXPERT, 1200, "tieng-anh", "📚")
            )),
            Triple("lap-trinh", "Lập Trình & Kỹ Thuật", listOf(
                SkillEntity("S8", "Kotlin Android", "Xây dựng ứng dụng native", SkillLevel.APPRENTICE, 200, "lap-trinh", "🤖"),
                SkillEntity("S9", "Clean Architecture", "Kiến trúc sạch, tách biệt", SkillLevel.NOVICE, 40, "lap-trinh", "🏗️")
            )),
            Triple("the-chat", "Thể Chất", listOf(
                SkillEntity("S10", "Cardio", "Sức bền tim mạch", SkillLevel.NOVICE, 80, "the-chat", "🏃"),
                SkillEntity("S11", "Sức Mạnh", "Luyện tạ, hít đất", SkillLevel.APPRENTICE, 160, "the-chat", "💪")
            ))
        )

        val toInsert = mutableListOf<SkillEntity>()
        mockGroups.forEach { (parentId, parentName, children) ->
            toInsert.add(
                SkillEntity(parentId, parentName, "Nhánh kỹ năng: $parentName",
                    SkillLevel.NOVICE, 0, null,
                    children.firstOrNull()?.iconId, 0f, 0f,
                    "parent", null, "[]", 0, System.currentTimeMillis(), true)
            )
            children.forEach { child ->
                toInsert.add(child)
            }
        }
        skillDao.insertSkills(toInsert)
    }
}
