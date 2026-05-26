package com.systemleveling.core.network

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Calculates daily time slots based on the user's biological clock settings.
 *
 * Divides the day into logical blocks: pre-work, morning-work, afternoon-work,
 * workout, and evening — avoiding lunch and sleep times.
 */
class QuestTimeSlotCalculator {

    fun buildDaySlots(
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

    fun parseHourStart(s: String) =
        s.trim().split("-").first().trim().substringBefore(":").toIntOrNull() ?: 8

    fun parseHourEnd(s: String) =
        s.trim().split("-").last().trim().substringBefore(":").toIntOrNull() ?: 17
}
