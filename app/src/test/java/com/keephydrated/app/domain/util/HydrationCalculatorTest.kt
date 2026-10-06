package com.keephydrated.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HydrationCalculatorTest {

    @Test
    fun `calculate returns appropriate recommendation for young active male`() {
        val rec = HydrationCalculator.calculate(
            age = 25,
            sex = "male",
            weightKg = 70f,
            heightCm = 175f,
            activityLevel = "moderate"
        )

        // Base: 70 * 35 = 2450. Sex male: 2450 * 1.10 = 2695. Moderate activity: +700 = 3395. Rounded: 3400 ml
        assertEquals(3400, rec.recommendedDailyMl)
        assertTrue(rec.safeMaxDailyMl >= 3400)
        assertEquals(1000, rec.maxHourlyMl)
        assertEquals("Normal", rec.bmiCategory)
    }

    @Test
    fun `calculate returns appropriate recommendation for older sedentary female`() {
        val rec = HydrationCalculator.calculate(
            age = 60,
            sex = "female",
            weightKg = 60f,
            heightCm = 160f,
            activityLevel = "sedentary"
        )

        // Base: 60 * 25 = 1500. Sex female: 1500. Sedentary: +0 = 1500. Rounded: 1500 ml
        assertEquals(1500, rec.recommendedDailyMl)
        assertTrue(rec.safeMaxDailyMl >= 3200)
        assertEquals(1000, rec.maxHourlyMl)
        assertEquals("Normal", rec.bmiCategory)
    }

    @Test
    fun `calculate handles intense athlete appropriately`() {
        val rec = HydrationCalculator.calculate(
            age = 22,
            sex = "male",
            weightKg = 80f,
            heightCm = 185f,
            activityLevel = "intense"
        )

        // Base: 80 * 35 = 2800. Male: 2800 * 1.1 = 3080. Intense: +1200 = 4280. Rounded: 4300 ml
        assertEquals(4300, rec.recommendedDailyMl)
        assertEquals(1000, rec.maxHourlyMl)
    }
}
