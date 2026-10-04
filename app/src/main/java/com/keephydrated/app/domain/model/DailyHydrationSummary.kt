package com.keephydrated.app.domain.model

import java.time.LocalDate

data class DailyHydrationSummary(
    val date: LocalDate,
    val totalIntakeMl: Int,
    val goalMl: Int,
    val intakes: List<WaterIntake> = emptyList()
) {
    val progressPercentage: Float
        get() = if (goalMl > 0) {
            (totalIntakeMl.toFloat() / goalMl.toFloat()).coerceAtLeast(0f)
        } else {
            0f
        }

    val isGoalAchieved: Boolean
        get() = totalIntakeMl >= goalMl
}
