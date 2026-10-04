package com.keephydrated.app.worker

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
        // e.g. Night shift: active from 20:00 to 06:00
        val startHour = 20
        val endHour = 6

        // At 22:00, within active period (not quiet)
        assertFalse(isQuietHour(currentHour = 22, startHour = startHour, endHour = endHour))

        // At 03:00, within active period (not quiet)
        assertFalse(isQuietHour(currentHour = 3, startHour = startHour, endHour = endHour))

        // At 12:00 PM, in quiet period (sleep time)
        assertTrue(isQuietHour(currentHour = 12, startHour = startHour, endHour = endHour))
    }
}
