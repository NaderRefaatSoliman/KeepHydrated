package com.keephydrated.app.presentation.ui.history

import com.keephydrated.app.domain.model.AchievementStatus
import com.keephydrated.app.domain.model.DailyHydrationSummary
import com.keephydrated.app.domain.model.HistoryPeriod
import com.keephydrated.app.domain.model.UserSettings

data class ChartBarData(
    val label: String,
    val amountMl: Int,
    val goalMl: Int,
    val percentage: Float,
    val status: AchievementStatus
)

data class HistoryUiState(
    val selectedPeriod: HistoryPeriod = HistoryPeriod.WEEK,
    val dailySummaries: List<DailyHydrationSummary> = emptyList(),
    val periodSummaries: List<DailyHydrationSummary> = emptyList(),
    val chartData: List<ChartBarData> = emptyList(),
    val averageIntakeMl: Int = 0,
    val daysGoalMetCount: Int = 0,
    val doneCount: Int = 0,
    val partialCount: Int = 0,
    val zeroCount: Int = 0,
    val isLoading: Boolean = true,
    val userSettings: UserSettings = UserSettings()
)
