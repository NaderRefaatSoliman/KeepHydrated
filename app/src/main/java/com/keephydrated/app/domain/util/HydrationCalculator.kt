package com.keephydrated.app.domain.util

data class HydrationRecommendation(
    val recommendedDailyMl: Int,
    val safeMaxDailyMl: Int,
    val maxHourlyMl: Int = 1000,
    val baseWeightMl: Int,
    val activityBonusMl: Int,
    val bmi: Float,
    val bmiCategory: String
)

object HydrationCalculator {

    fun calculate(
        age: Int,
        sex: String,
        weightKg: Float,
        heightCm: Float,
        activityLevel: String
    ): HydrationRecommendation {
        val safeWeight = weightKg.coerceIn(30f, 250f)
        val safeHeight = heightCm.coerceIn(100f, 240f)

        // 1. Age-based multiplier (ml per kg according to EFSA & clinical physiology)
        val mlPerKg = when {
            age < 30 -> 35f
            age <= 55 -> 30f
            else -> 25f
        }

        // 2. Base weight hydration
        var baseMl = safeWeight * mlPerKg

        // 3. Sex adjustment (+10% for males due to higher lean body mass)
        if (sex.equals("male", ignoreCase = true)) {
            baseMl *= 1.10f
        }

        // 4. Physical activity & sports compensation
        val activityBonus = when (activityLevel.lowercase()) {
            "sedentary" -> 0
            "light" -> 350
            "moderate" -> 700
            "intense" -> 1200
            else -> 500
        }

        val totalRecommended = (baseMl + activityBonus).toInt()
        val roundedRecommended = ((totalRecommended + 25) / 50) * 50

        // Safe maximum daily intake to prevent hyponatremia
        val safeMaxDaily = minOf((safeWeight * 55f).toInt(), roundedRecommended + 1800).coerceAtLeast(3200)

        // Hourly kidney excretion rate ceiling (800 - 1000 ml/hr)
        val maxHourly = 1000

        // BMI calculation
        val heightM = safeHeight / 100f
        val bmi = safeWeight / (heightM * heightM)
        val bmiCategory = when {
            bmi < 18.5f -> "Underweight"
            bmi < 25f -> "Normal"
            bmi < 30f -> "Overweight"
            else -> "Obese"
        }

        return HydrationRecommendation(
            recommendedDailyMl = roundedRecommended.coerceIn(1200, 4500),
            safeMaxDailyMl = safeMaxDaily,
            maxHourlyMl = maxHourly,
            baseWeightMl = baseMl.toInt(),
            activityBonusMl = activityBonus,
            bmi = bmi,
            bmiCategory = bmiCategory
        )
    }
}
