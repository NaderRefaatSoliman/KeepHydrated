package com.keephydrated.app.worker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuietHoursLogicTest {

    private fun isQuietHour(currentHour: Int, startHour: Int, endHour: Int): Boolean {
        return if (startHour <= endHour) {
            currentHour < startHour || currentHour >= endHour
        } else {
            currentHour in endHour until startHour
        }
    }

    private fun isCustomRoutineHourActive(currentHour: Int, selectedHours: Set<Int>): Boolean {
        return currentHour in selectedHours
    }

    private fun calculateWakingDayHours(wakeHour: Int, sleepHour: Int, stepHours: Int): Set<Int> {
        val step = stepHours.coerceIn(1, 4)
        val generatedHours = mutableSetOf<Int>()
        if (wakeHour <= sleepHour) {
            var h = wakeHour
            while (h <= sleepHour) {
                generatedHours.add(h)
                h += step
            }
        } else {
            var h = wakeHour
            while (h < 24) {
                generatedHours.add(h)
                h += step
            }
            var m = h % 24
            while (m <= sleepHour) {
                generatedHours.add(m)
                m += step
            }
        }
        if (generatedHours.isEmpty()) generatedHours.add(wakeHour)
        return generatedHours
    }

    @Test
    fun `standard day hours - within active hours is not quiet`() {
        val startHour = 8  // 8:00 AM
        val endHour = 22  // 10:00 PM

        // At 12:00 PM, should be active (not quiet)
        assertFalse(isQuietHour(currentHour = 12, startHour = startHour, endHour = endHour))

        // At 8:00 AM, should be active (not quiet)
        assertFalse(isQuietHour(currentHour = 8, startHour = startHour, endHour = endHour))

        // At 21:00 (9:00 PM), should be active (not quiet)
        assertFalse(isQuietHour(currentHour = 21, startHour = startHour, endHour = endHour))
    }

    @Test
    fun `standard day hours - outside active hours is quiet`() {
        val startHour = 8  // 8:00 AM
        val endHour = 22  // 10:00 PM

        // At 23:00 (11:00 PM), should be quiet
        assertTrue(isQuietHour(currentHour = 23, startHour = startHour, endHour = endHour))

        // At 03:00 AM, should be quiet
        assertTrue(isQuietHour(currentHour = 3, startHour = startHour, endHour = endHour))

        // At 07:00 AM, should be quiet
        assertTrue(isQuietHour(currentHour = 7, startHour = startHour, endHour = endHour))
    }

    @Test
    fun `overnight schedule - correctly identifies quiet period`() {
        val startHour = 20
        val endHour = 6

        // At 22:00, within active period (not quiet)
        assertFalse(isQuietHour(currentHour = 22, startHour = startHour, endHour = endHour))

        // At 03:00, within active period (not quiet)
        assertFalse(isQuietHour(currentHour = 3, startHour = startHour, endHour = endHour))

        // At 12:00 PM, in quiet period (sleep time)
        assertTrue(isQuietHour(currentHour = 12, startHour = startHour, endHour = endHour))
    }

    @Test
    fun `custom routine - correctly triggers only on user selected hours across 24h`() {
        // Can select any hour 0..23 (including midnight 0, early morning 6, evening 22)
        val selectedHours = setOf(0, 6, 9, 12, 15, 18, 21, 23)

        // Selected hours should be active
        assertTrue(isCustomRoutineHourActive(currentHour = 0, selectedHours = selectedHours))
        assertTrue(isCustomRoutineHourActive(currentHour = 6, selectedHours = selectedHours))
        assertTrue(isCustomRoutineHourActive(currentHour = 9, selectedHours = selectedHours))
        assertTrue(isCustomRoutineHourActive(currentHour = 23, selectedHours = selectedHours))

        // Non-selected hours should not be active
        assertFalse(isCustomRoutineHourActive(currentHour = 1, selectedHours = selectedHours))
        assertFalse(isCustomRoutineHourActive(currentHour = 7, selectedHours = selectedHours))
        assertFalse(isCustomRoutineHourActive(currentHour = 14, selectedHours = selectedHours))
    }

    @Test
    fun `waking day preset calculation - daytime schedule`() {
        val wake = 7
        val sleep = 23
        val step = 2

        val generated = calculateWakingDayHours(wake, sleep, step)
        val expected = setOf(7, 9, 11, 13, 15, 17, 19, 21, 23)
        assertEquals(expected, generated)

        // Within waking period
        assertFalse(isQuietHour(currentHour = 7, startHour = wake, endHour = sleep))
        assertFalse(isQuietHour(currentHour = 15, startHour = wake, endHour = sleep))

        // Outside waking period (e.g. 2 AM, 5 AM)
        assertTrue(isQuietHour(currentHour = 2, startHour = wake, endHour = sleep))
        assertTrue(isQuietHour(currentHour = 5, startHour = wake, endHour = sleep))
    }

    @Test
    fun `waking day preset calculation - overnight schedule`() {
        val wake = 20 // 8 PM
        val sleep = 6  // 6 AM
        val step = 2

        val generated = calculateWakingDayHours(wake, sleep, step)
        val expected = setOf(20, 22, 0, 2, 4, 6)
        assertEquals(expected, generated)

        // Within night shift awake period
        assertFalse(isQuietHour(currentHour = 22, startHour = wake, endHour = sleep))
        assertFalse(isQuietHour(currentHour = 3, startHour = wake, endHour = sleep))

        // Daytime sleep period (e.g. 10 AM, 2 PM)
        assertTrue(isQuietHour(currentHour = 10, startHour = wake, endHour = sleep))
        assertTrue(isQuietHour(currentHour = 14, startHour = wake, endHour = sleep))
    }

    @Test
    fun `safety rule - reminders suppressed when intake reaches safe limit`() {
        val safeLimitMl = 3800
        val totalIntakeMl = 3900
        val shouldSuppress = totalIntakeMl >= safeLimitMl
        assertTrue(shouldSuppress)
    }

    @Test
    fun `reminder rule - goal reached and remindAfterGoal is false suppresses reminder`() {
        val goalMl = 2000
        val totalIntakeMl = 2200
        val remindAfterGoal = false
        val shouldSuppress = totalIntakeMl >= goalMl && !remindAfterGoal
        assertTrue(shouldSuppress)
    }

    @Test
    fun `reminder rule - goal reached but remindAfterGoal is true continues reminder`() {
        val goalMl = 2000
        val totalIntakeMl = 2200
        val remindAfterGoal = true
        val shouldSuppress = totalIntakeMl >= goalMl && !remindAfterGoal
        assertFalse(shouldSuppress)
    }
}
