package com.keephydrated.app.presentation.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keephydrated.app.domain.model.AchievementStatus
import com.keephydrated.app.domain.model.DailyHydrationSummary
import com.keephydrated.app.domain.model.HistoryPeriod
import com.keephydrated.app.domain.repository.SettingsRepository
import com.keephydrated.app.domain.usecase.GetHydrationHistoryUseCase
import com.keephydrated.app.util.LocalizationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHydrationHistoryUseCase: GetHydrationHistoryUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var allSummariesCache: List<DailyHydrationSummary> = emptyList()
    private var defaultGoalCache: Int = 2000
    private var languageCache: String = "en"
    private var userSettingsCache: com.keephydrated.app.domain.model.UserSettings = com.keephydrated.app.domain.model.UserSettings()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            combine(
                getHydrationHistoryUseCase(),
                settingsRepository.getUserSettings()
            ) { summaries, settings ->
                allSummariesCache = summaries
                defaultGoalCache = settings.dailyGoalMl
                languageCache = settings.language
                userSettingsCache = settings
                computeStateForPeriod(_uiState.value.selectedPeriod)
            }.collectLatest { newState ->
                _uiState.value = newState
            }
        }
    }

    fun selectPeriod(period: HistoryPeriod) {
        _uiState.update {
            computeStateForPeriod(period)
        }
    }

    private fun computeStateForPeriod(period: HistoryPeriod): HistoryUiState {
        val today = LocalDate.now()
        val summaryByDate = allSummariesCache.associateBy { it.date }
        val isArabic = languageCache == "ar"
        val locale = if (isArabic) Locale("ar") else Locale.ENGLISH

        when (period) {
            HistoryPeriod.WEEK -> {
                // Week starting on Saturday
                val daysSinceSaturday = (today.dayOfWeek.value % 7 + 1) % 7
                val startOfSaturdayWeek = today.minusDays(daysSinceSaturday.toLong())
                val days = (0..6).map { startOfSaturdayWeek.plusDays(it.toLong()) }
                val weekSummaries = days.map { date ->
                    summaryByDate[date] ?: DailyHydrationSummary(
                        date = date,
                        totalIntakeMl = 0,
                        goalMl = defaultGoalCache
                    )
                }

                val dayFormatter = DateTimeFormatter.ofPattern("EEE", locale)
                val chartData = weekSummaries.map { s ->
                    ChartBarData(
                        label = s.date.format(dayFormatter),
                        amountMl = s.totalIntakeMl,
                        goalMl = s.goalMl,
                        percentage = s.progressPercentage,
                        status = s.status
                    )
                }

                val done = weekSummaries.count { it.status == AchievementStatus.DONE }
                val partial = weekSummaries.count { it.status == AchievementStatus.PARTIAL }
                val zero = weekSummaries.count { it.status == AchievementStatus.ZERO }
                val avg = if (weekSummaries.isNotEmpty()) weekSummaries.sumOf { it.totalIntakeMl } / weekSummaries.size else 0

                return HistoryUiState(
                    selectedPeriod = period,
                    dailySummaries = allSummariesCache,
                    periodSummaries = weekSummaries,
                    chartData = chartData,
                    averageIntakeMl = avg,
                    daysGoalMetCount = done,
                    doneCount = done,
                    partialCount = partial,
                    zeroCount = zero,
                    isLoading = false,
                    userSettings = userSettingsCache
                )
            }

            HistoryPeriod.MONTH -> {
                // Last 30 days
                val days = (29 downTo 0).map { today.minusDays(it.toLong()) }
                val monthSummaries = days.map { date ->
                    summaryByDate[date] ?: DailyHydrationSummary(
                        date = date,
                        totalIntakeMl = 0,
                        goalMl = defaultGoalCache
                    )
                }

                // Chunk into 5 6-day periods
                val chartData = (0 until 5).map { i ->
                    val chunk = monthSummaries.subList(i * 6, (i + 1) * 6)
                    val startDay = chunk.first().date.dayOfMonth
                    val endDay = chunk.last().date.dayOfMonth
                    val total = chunk.sumOf { it.totalIntakeMl }
                    val goal = chunk.sumOf { it.goalMl }
                    val pct = if (goal > 0) total.toFloat() / goal.toFloat() else 0f
                    val status = when {
                        pct >= 1f -> AchievementStatus.DONE
                        total > 0 -> AchievementStatus.PARTIAL
                        else -> AchievementStatus.ZERO
                    }
                    val startStr = LocalizationUtils.formatNumber(startDay, isArabic)
                    val endStr = LocalizationUtils.formatNumber(endDay, isArabic)

                    ChartBarData(
                        label = "$startStr-$endStr",
                        amountMl = total / chunk.size,
                        goalMl = defaultGoalCache,
                        percentage = pct,
                        status = status
                    )
                }

                val done = monthSummaries.count { it.status == AchievementStatus.DONE }
                val partial = monthSummaries.count { it.status == AchievementStatus.PARTIAL }
                val zero = monthSummaries.count { it.status == AchievementStatus.ZERO }
                val avg = if (monthSummaries.isNotEmpty()) monthSummaries.sumOf { it.totalIntakeMl } / monthSummaries.size else 0

                return HistoryUiState(
                    selectedPeriod = period,
                    dailySummaries = allSummariesCache,
                    periodSummaries = monthSummaries.reversed(),
                    chartData = chartData,
                    averageIntakeMl = avg,
                    daysGoalMetCount = done,
                    doneCount = done,
                    partialCount = partial,
                    zeroCount = zero,
                    isLoading = false,
                    userSettings = userSettingsCache
                )
            }

            HistoryPeriod.YEAR -> {
                // 12 months of current year
                val currentYear = today.year
                val chartData = (1..12).map { monthNum ->
                    val month = Month.of(monthNum)
                    val monthSummaries = allSummariesCache.filter { it.date.year == currentYear && it.date.month == month }
                    val total = monthSummaries.sumOf { it.totalIntakeMl }
                    val avg = if (monthSummaries.isNotEmpty()) total / monthSummaries.size else 0
                    val pct = if (defaultGoalCache > 0) avg.toFloat() / defaultGoalCache.toFloat() else 0f
                    val status = when {
                        pct >= 1f -> AchievementStatus.DONE
                        avg > 0 -> AchievementStatus.PARTIAL
                        else -> AchievementStatus.ZERO
                    }
                    val label = month.getDisplayName(TextStyle.SHORT, locale)

                    ChartBarData(
                        label = label,
                        amountMl = avg,
                        goalMl = defaultGoalCache,
                        percentage = pct,
                        status = status
                    )
                }

                val done = allSummariesCache.count { it.date.year == currentYear && it.status == AchievementStatus.DONE }
                val partial = allSummariesCache.count { it.date.year == currentYear && it.status == AchievementStatus.PARTIAL }
                val totalDaysWithData = allSummariesCache.count { it.date.year == currentYear }
                val zero = (today.dayOfYear - totalDaysWithData).coerceAtLeast(0)
                val avg = if (allSummariesCache.isNotEmpty()) allSummariesCache.sumOf { it.totalIntakeMl } / allSummariesCache.size else 0

                return HistoryUiState(
                    selectedPeriod = period,
                    dailySummaries = allSummariesCache,
                    periodSummaries = allSummariesCache.filter { it.date.year == currentYear },
                    chartData = chartData,
                    averageIntakeMl = avg,
                    daysGoalMetCount = done,
                    doneCount = done,
                    partialCount = partial,
                    zeroCount = zero,
                    isLoading = false,
                    userSettings = userSettingsCache
                )
            }
        }
    }
}
