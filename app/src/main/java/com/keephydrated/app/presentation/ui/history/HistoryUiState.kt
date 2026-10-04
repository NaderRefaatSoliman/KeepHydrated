package com.keephydrated.app.presentation.ui.history

import com.keephydrated.app.domain.model.DailyHydrationSummary

data class HistoryUiState(
    val dailySummaries: List<DailyHydrationSummary> = emptyList(),
    val averageIntakeMl: Int = 0,
    val daysGoalMetCount: Int = 0,
    val isLoading: Boolean = true
)
