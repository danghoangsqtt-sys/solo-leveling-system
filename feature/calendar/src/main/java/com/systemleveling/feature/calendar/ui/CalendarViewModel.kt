package com.systemleveling.feature.calendar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.entity.QuestEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val questDao: QuestDao
) : ViewModel() {

    private val _allQuests = MutableStateFlow<List<QuestEntity>>(emptyList())
    
    private val _selectedDate = MutableStateFlow<Long>(getStartOfDay(System.currentTimeMillis()))
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _questsForSelectedDate = MutableStateFlow<List<QuestEntity>>(emptyList())
    val questsForSelectedDate: StateFlow<List<QuestEntity>> = _questsForSelectedDate.asStateFlow()

    init {
        viewModelScope.launch {
            questDao.getAllQuests().collect { quests ->
                _allQuests.value = quests
                updateQuestsForSelectedDate(_selectedDate.value)
            }
        }
    }

    fun selectDate(timestamp: Long) {
        val startOfDay = getStartOfDay(timestamp)
        _selectedDate.value = startOfDay
        updateQuestsForSelectedDate(startOfDay)
    }

    private fun updateQuestsForSelectedDate(startOfDay: Long) {
        val endOfDay = startOfDay + 86400000 - 1
        _questsForSelectedDate.value = _allQuests.value.filter { quest ->
            quest.date in startOfDay..endOfDay
        }.sortedBy { it.timeStart ?: "23:59" }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
