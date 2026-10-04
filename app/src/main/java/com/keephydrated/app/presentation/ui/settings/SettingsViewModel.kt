package com.keephydrated.app.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keephydrated.app.domain.usecase.GetUserSettingsUseCase
import com.keephydrated.app.domain.usecase.SaveUserSettingsUseCase
import com.keephydrated.app.worker.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val saveUserSettingsUseCase: SaveUserSettingsUseCase,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            getUserSettingsUseCase().collectLatest { settings ->
                _uiState.update {
                    it.copy(
                        dailyGoalMl = settings.dailyGoalMl,
                        reminderIntervalHours = settings.reminderIntervalHours,
                        remindersEnabled = settings.remindersEnabled,
                        startHour = settings.startHour,
                        endHour = settings.endHour,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateDailyGoal(goalMl: Int) {
        viewModelScope.launch {
            try {
                saveUserSettingsUseCase.updateGoal(goalMl)
                _uiState.update { it.copy(userMessage = "Goal updated to $goalMl ml") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update goal") }
            }
        }
    }

    fun updateReminderInterval(hours: Int) {
        viewModelScope.launch {
            try {
                saveUserSettingsUseCase.updateReminderInterval(hours)
                reminderScheduler.scheduleReminders(hours, _uiState.value.remindersEnabled)
                _uiState.update { it.copy(userMessage = "Reminder interval updated") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update interval") }
            }
        }
    }

    fun toggleReminders(enabled: Boolean) {
        viewModelScope.launch {
            try {
                saveUserSettingsUseCase.updateRemindersEnabled(enabled)
                reminderScheduler.scheduleReminders(_uiState.value.reminderIntervalHours, enabled)
                val msg = if (enabled) "Reminders enabled" else "Reminders disabled"
                _uiState.update { it.copy(userMessage = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Failed to update reminders") }
            }
        }
    }

    fun updateActiveHours(startHour: Int, endHour: Int) {
        viewModelScope.launch {
            try {
                saveUserSettingsUseCase.updateActiveHours(startHour, endHour)
                _uiState.update { it.copy(userMessage = "Active hours updated") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update active hours") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
