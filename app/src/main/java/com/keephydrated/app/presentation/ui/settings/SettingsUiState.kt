package com.keephydrated.app.presentation.ui.settings

data class SettingsUiState(
    val dailyGoalMl: Int = 2000,
    val reminderIntervalHours: Int = 2,
    val remindersEnabled: Boolean = true,
    val startHour: Int = 8,
    val endHour: Int = 22,
    val isLoading: Boolean = true,
    val userMessage: String? = null
)
