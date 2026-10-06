package com.keephydrated.app.presentation.ui.home

import com.keephydrated.app.domain.model.BottleStatus
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.domain.model.WaterIntake

data class HomeUiState(
    val currentIntakeMl: Int = 0,
    val dailyGoalMl: Int = 2000,
    val progressPercentage: Float = 0f,
    val isGoalAchieved: Boolean = false,
    val todayIntakes: List<WaterIntake> = emptyList(),
    val isLoading: Boolean = true,
    val userMessage: String? = null,
    val frequentIntakeMl: Int = 250,
    val bottleStatus: BottleStatus = BottleStatus(),
    val isBottleMode: Boolean = false,
    val userSettings: UserSettings = UserSettings()
)
