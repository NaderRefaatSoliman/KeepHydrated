package com.keephydrated.app.domain.model

import java.time.LocalDate

enum class AchievementStatus {
    DONE,     // Target met: totalIntakeMl >= goalMl && goalMl > 0
    PARTIAL,  // Started drinking but below goal: totalIntakeMl in 1 until goalMl
    ZERO      // Zero drinking recorded
}

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
        get() = totalIntakeMl >= goalMl && goalMl > 0

    val status: AchievementStatus
        get() = when {
            isGoalAchieved -> AchievementStatus.DONE
            totalIntakeMl > 0 -> AchievementStatus.PARTIAL
            else -> AchievementStatus.ZERO
        }
}
