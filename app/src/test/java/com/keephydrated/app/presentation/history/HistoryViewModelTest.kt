package com.keephydrated.app.presentation.history

import com.keephydrated.app.domain.model.AchievementStatus
import com.keephydrated.app.domain.model.DailyHydrationSummary
import com.keephydrated.app.domain.model.HistoryPeriod
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.domain.repository.SettingsRepository
import com.keephydrated.app.domain.usecase.GetHydrationHistoryUseCase
import com.keephydrated.app.presentation.ui.history.HistoryViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getHydrationHistoryUseCase: GetHydrationHistoryUseCase
    private lateinit var settingsRepository: SettingsRepository

    private val historyFlow = MutableSharedFlow<List<DailyHydrationSummary>>(replay = 1)
    private val settingsFlow = MutableSharedFlow<UserSettings>(replay = 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getHydrationHistoryUseCase = mockk()
        settingsRepository = mockk(relaxed = true)

        every { getHydrationHistoryUseCase() } returns historyFlow
        every { settingsRepository.getUserSettings() } returns settingsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `history summaries correctly compute average intake and goals met`() = runTest {
        val summaries = listOf(
            DailyHydrationSummary(date = LocalDate.now(), totalIntakeMl = 2500, goalMl = 2000), // achieved
            DailyHydrationSummary(date = LocalDate.now().minusDays(1), totalIntakeMl = 1500, goalMl = 2000) // partial
        )
        historyFlow.emit(summaries)
        settingsFlow.emit(UserSettings(dailyGoalMl = 2000))

        val viewModel = HistoryViewModel(getHydrationHistoryUseCase, settingsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.dailySummaries.size)
        assertEquals(7, state.chartData.size) // 7 days in Week view
        assertEquals(1, state.doneCount)
        assertEquals(1, state.partialCount)
        assertEquals(5, state.zeroCount) // 5 remaining days in week are zero
    }

    @Test
    fun `switching to Month period computes monthly chart data`() = runTest {
        val summaries = listOf(
            DailyHydrationSummary(date = LocalDate.now(), totalIntakeMl = 2000, goalMl = 2000)
        )
        historyFlow.emit(summaries)
        settingsFlow.emit(UserSettings(dailyGoalMl = 2000))

        val viewModel = HistoryViewModel(getHydrationHistoryUseCase, settingsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectPeriod(HistoryPeriod.MONTH)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(HistoryPeriod.MONTH, state.selectedPeriod)
        assertEquals(5, state.chartData.size) // 5 6-day intervals in 30 days
    }

    @Test
    fun `switching to Year period computes 12 months chart data`() = runTest {
        val summaries = listOf(
            DailyHydrationSummary(date = LocalDate.now(), totalIntakeMl = 2000, goalMl = 2000)
        )
        historyFlow.emit(summaries)
        settingsFlow.emit(UserSettings(dailyGoalMl = 2000))

        val viewModel = HistoryViewModel(getHydrationHistoryUseCase, settingsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectPeriod(HistoryPeriod.YEAR)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(HistoryPeriod.YEAR, state.selectedPeriod)
        assertEquals(12, state.chartData.size) // 12 months
    }

    @Test
    fun `Arabic language setting formats chart day labels in Arabic`() = runTest {
        val summaries = listOf(
            DailyHydrationSummary(date = LocalDate.now(), totalIntakeMl = 2000, goalMl = 2000)
        )
        historyFlow.emit(summaries)
        settingsFlow.emit(UserSettings(dailyGoalMl = 2000, language = "ar"))

        val viewModel = HistoryViewModel(getHydrationHistoryUseCase, settingsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(7, state.chartData.size)
        val today = LocalDate.now()
        val daysSinceSaturday = (today.dayOfWeek.value % 7 + 1) % 7
        val saturday = today.minusDays(daysSinceSaturday.toLong())
        val expectedDay = saturday.format(DateTimeFormatter.ofPattern("EEE", Locale("ar")))
        assertEquals(expectedDay, state.chartData.first().label)
    }
}
