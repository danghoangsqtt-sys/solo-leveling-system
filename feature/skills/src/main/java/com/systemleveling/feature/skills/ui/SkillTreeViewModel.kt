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

@HiltViewModel
class SkillTreeViewModel @Inject constructor(
    private val skillDao: SkillDao
) : ViewModel() {

    private val _skills = MutableStateFlow<List<SkillEntity>>(emptyList())
    val skills: StateFlow<List<SkillEntity>> = _skills.asStateFlow()

    init {
        // Collect from DB
        viewModelScope.launch {
            skillDao.getAllSkills().collect { dbSkills ->
                if (dbSkills.isEmpty()) {
                    seedMockData()
                } else {
                    _skills.value = dbSkills
                }
            }
        }
    }

    private suspend fun seedMockData() {
        val list = listOf(
            SkillEntity("S1", "IELTS 7.0 Mastery", "Khả năng sử dụng tiếng Anh thành thạo", SkillLevel.ADVANCED, 600, null, "🎯", 0f, 0f),
            SkillEntity("S2", "Reading", "Đọc hiểu thần tốc", SkillLevel.INTERMEDIATE, 350, "S1", "📖", -150f, 150f),
            SkillEntity("S3", "Writing", "Viết luận sắc bén", SkillLevel.APPRENTICE, 150, "S1", "✍", 150f, 150f),
            SkillEntity("S4", "Vocabulary", "Từ vựng mở rộng", SkillLevel.EXPERT, 1200, "S2", "📚", -200f, 300f),
            SkillEntity("S5", "Task 2", "Cấu trúc bài luận", SkillLevel.NOVICE, 50, "S3", "📝", 100f, 300f),
            SkillEntity("S6", "Coherence", "Logic chặt chẽ", SkillLevel.NOVICE, 0, "S5", "🔗", 100f, 450f),
            SkillEntity("S7", "Listening", "Nghe như người bản xứ", SkillLevel.INTERMEDIATE, 450, "S1", "🎧", -400f, 100f)
        )
        skillDao.insertSkills(list)
    }
}
