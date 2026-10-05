package com.keephydrated.app.presentation.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keephydrated.app.domain.usecase.AddWaterIntakeUseCase
import com.keephydrated.app.domain.usecase.DeleteWaterIntakeUseCase
import com.keephydrated.app.domain.usecase.GetTodayHydrationUseCase
import com.keephydrated.app.domain.usecase.UndoLastIntakeUseCase
import com.keephydrated.app.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayHydrationUseCase: GetTodayHydrationUseCase,
    private val addWaterIntakeUseCase: AddWaterIntakeUseCase,
    private val deleteWaterIntakeUseCase: DeleteWaterIntakeUseCase,
    private val undoLastIntakeUseCase: UndoLastIntakeUseCase,
    @ApplicationContext private val context: Context? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeTodayHydration()
    }

    private fun observeTodayHydration() {
        viewModelScope.launch {
            getTodayHydrationUseCase().collectLatest { summary ->
                _uiState.update { current ->
                    current.copy(
                        currentIntakeMl = summary.totalIntakeMl,
                        dailyGoalMl = summary.goalMl,
                        progressPercentage = summary.progressPercentage,
                        isGoalAchieved = summary.isGoalAchieved,
                        todayIntakes = summary.intakes,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            try {
                val previousTotal = _uiState.value.currentIntakeMl
                val goal = _uiState.value.dailyGoalMl
                addWaterIntakeUseCase(amountMl)
                val newTotal = previousTotal + amountMl

                if (previousTotal < goal && newTotal >= goal && goal > 0) {
                    context?.let { ctx ->
                        NotificationHelper.showCelebrationNotification(ctx, goal)
                    }
                }

                _uiState.update { it.copy(userMessage = "Added $amountMl ml") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to log water") }
            }
        }
    }

    fun deleteIntake(id: Long) {
        viewModelScope.launch {
            try {
                deleteWaterIntakeUseCase(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Failed to delete entry") }
            }
        }
    }

    fun undoLastIntake() {
        viewModelScope.launch {
            val undone = undoLastIntakeUseCase()
            if (undone) {
                _uiState.update { it.copy(userMessage = "Last entry undone") }
            } else {
                _uiState.update { it.copy(userMessage = "No entry to undo") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
