package com.systemleveling.feature.journal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.JournalDao
import com.systemleveling.core.database.entity.JournalEntity
import com.systemleveling.core.model.Mood
import com.systemleveling.core.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalDao: JournalDao,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _journals = MutableStateFlow<List<JournalEntity>>(emptyList())
    val journals: StateFlow<List<JournalEntity>> = _journals.asStateFlow()

    init {
        viewModelScope.launch {
            var hasSeeded = settingsManager.isJournalSeeded()
            journalDao.getAllJournals().collect { dbJournals ->
                if (dbJournals.isEmpty() && !hasSeeded) {
                    seedMockData()
                    settingsManager.markJournalSeeded()
                    hasSeeded = true
                } else {
                    _journals.value = dbJournals
                }
            }
        }
    }

    private suspend fun seedMockData() {
        val mockData = listOf(
            JournalEntity(UUID.randomUUID().toString(), "Hôm nay tôi đã chạy 5km không nghỉ, cảm giác cơ thể sắp vỡ tung nhưng rất sảng khoái.", Mood.EXCITED, System.currentTimeMillis() - 86400000 * 2),
            JournalEntity(UUID.randomUUID().toString(), "Tôi đã giải quyết được một bug khó nhằn sau 3 tiếng cày cuốc. Exp nhận được xứng đáng.", Mood.HAPPY, System.currentTimeMillis() - 86400000 * 1),
            JournalEntity(UUID.randomUUID().toString(), "Quá tải vì nhiều việc đổ dồn, nhưng tôi sẽ không gục ngã.", Mood.STRESSED, System.currentTimeMillis())
        )
        journalDao.insertJournals(mockData)
    }

    fun addJournal(content: String, mood: Mood) {
        viewModelScope.launch {
            journalDao.insertJournal(
                JournalEntity(
                    id = UUID.randomUUID().toString(),
                    content = content,
                    mood = mood
                )
            )
        }
    }
}
