package com.systemleveling.feature.calendar.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.CalendarEventDao
import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.entity.CalendarEventEntity
import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.model.RecurrenceType
import com.systemleveling.core.receiver.CalendarReminderReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

sealed class CalendarItem {
    abstract val timeStart: String?

    data class QuestItem(val quest: QuestEntity) : CalendarItem() {
        override val timeStart get() = quest.timeStart
    }

    data class EventItem(val event: CalendarEventEntity) : CalendarItem() {
        override val timeStart get() = event.timeStart
    }
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val questDao: QuestDao,
    private val eventDao: CalendarEventDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _allQuests = MutableStateFlow<List<QuestEntity>>(emptyList())
    private val _allEvents = MutableStateFlow<List<CalendarEventEntity>>(emptyList())

    private val _selectedDate = MutableStateFlow(startOfDay(System.currentTimeMillis()))
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _itemsForDate = MutableStateFlow<List<CalendarItem>>(emptyList())
    val itemsForDate: StateFlow<List<CalendarItem>> = _itemsForDate.asStateFlow()

    init {
        viewModelScope.launch {
            questDao.getAllQuests().collect { quests ->
                _allQuests.value = quests
                rebuildItems()
            }
        }
        viewModelScope.launch {
            eventDao.getAllEvents().collect { events ->
                _allEvents.value = events
                rebuildItems()
            }
        }
    }

    fun selectDate(timestamp: Long) {
        _selectedDate.value = startOfDay(timestamp)
        rebuildItems()
    }

    private fun rebuildItems() {
        val day = _selectedDate.value
        val endOfDay = day + 86_400_000L - 1
        val targetCal = Calendar.getInstance().apply { timeInMillis = day }

        val questItems = _allQuests.value
            .filter { it.date in day..endOfDay && !it.isHealthReminder }
            .map { CalendarItem.QuestItem(it) }

        val eventItems = _allEvents.value
            .filter { isEventOnDate(it, targetCal) }
            .map { CalendarItem.EventItem(it) }

        _itemsForDate.value = (questItems + eventItems)
            .sortedBy { parseTimeToMinutes(it.timeStart) }
    }

    private fun parseTimeToMinutes(time: String?): Int {
        if (time == null) return 23 * 60 + 59
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 23
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 59
        return h * 60 + m
    }

    private fun isEventOnDate(event: CalendarEventEntity, targetCal: Calendar): Boolean {
        val baseCal = Calendar.getInstance().apply { timeInMillis = event.baseDateMs }
        val targetDay = dateKey(targetCal)
        val baseDay = dateKey(baseCal)
        if (targetDay < baseDay) return false
        return when (event.recurrenceType) {
            RecurrenceType.NONE -> targetDay == baseDay
            RecurrenceType.DAILY -> true
            RecurrenceType.WEEKLY ->
                targetCal.get(Calendar.DAY_OF_WEEK) == baseCal.get(Calendar.DAY_OF_WEEK)
            RecurrenceType.MONTHLY ->
                targetCal.get(Calendar.DAY_OF_MONTH) == baseCal.get(Calendar.DAY_OF_MONTH)
            RecurrenceType.YEARLY ->
                targetCal.get(Calendar.MONTH) == baseCal.get(Calendar.MONTH) &&
                targetCal.get(Calendar.DAY_OF_MONTH) == baseCal.get(Calendar.DAY_OF_MONTH)
        }
    }

    private fun dateKey(cal: Calendar) =
        cal.get(Calendar.YEAR) * 10000 + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DAY_OF_MONTH)

    fun addEvent(event: CalendarEventEntity) {
        viewModelScope.launch {
            eventDao.insertEvent(event)
            scheduleReminder(event)
        }
    }

    fun deleteEvent(id: String) {
        viewModelScope.launch {
            cancelReminder(id)
            eventDao.deleteById(id)
        }
    }

    private fun scheduleReminder(event: CalendarEventEntity) {
        if (event.reminderMinutesBefore <= 0) return
        val timeStart = event.timeStart ?: return
        val parts = timeStart.split(":")
        if (parts.size < 2) return
        val h = parts[0].toIntOrNull() ?: return
        val m = parts[1].toIntOrNull() ?: return

        val alarmMs = Calendar.getInstance().apply {
            timeInMillis = event.baseDateMs
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m - event.reminderMinutesBefore)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (alarmMs <= System.currentTimeMillis()) return

        val intent = Intent(context, CalendarReminderReceiver::class.java).apply {
            putExtra("eventId", event.id)
            putExtra("title", event.title)
            putExtra("emoji", event.emoji)
        }
        val pi = PendingIntent.getBroadcast(
            context, event.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmMs, pi)
    }

    private fun cancelReminder(eventId: String) {
        val intent = Intent(context, CalendarReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, eventId.hashCode(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pi)
    }

    private fun startOfDay(ts: Long) = Calendar.getInstance().apply {
        timeInMillis = ts
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
