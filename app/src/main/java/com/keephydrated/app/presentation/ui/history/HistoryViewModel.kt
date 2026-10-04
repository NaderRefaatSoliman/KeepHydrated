package com.keephydrated.app.presentation.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keephydrated.app.domain.usecase.GetHydrationHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHydrationHistoryUseCase: GetHydrationHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getHydrationHistoryUseCase().collectLatest { summaries ->
                val avg = if (summaries.isNotEmpty()) {
                    summaries.sumOf { it.totalIntakeMl } / summaries.size
                } else {
                    0
                }
                val goalMetCount = summaries.count { it.isGoalAchieved }

                _uiState.update {
                    it.copy(
                        dailySummaries = summaries,
                        averageIntakeMl = avg,
                        daysGoalMetCount = goalMetCount,
                        isLoading = false
                    )
                }
            }
        }
    }
}
