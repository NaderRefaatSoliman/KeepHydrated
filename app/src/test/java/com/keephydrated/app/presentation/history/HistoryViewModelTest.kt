package com.keephydrated.app.presentation.history

import com.keephydrated.app.domain.model.DailyHydrationSummary
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

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getHydrationHistoryUseCase: GetHydrationHistoryUseCase
    private val historyFlow = MutableSharedFlow<List<DailyHydrationSummary>>(replay = 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getHydrationHistoryUseCase = mockk()
        every { getHydrationHistoryUseCase() } returns historyFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `history summaries correctly compute average intake and goals met`() = runTest {
        val summaries = listOf(
            DailyHydrationSummary(date = LocalDate.now(), totalIntakeMl = 2500, goalMl = 2000), // achieved
            DailyHydrationSummary(date = LocalDate.now().minusDays(1), totalIntakeMl = 1500, goalMl = 2000) // not achieved
        )
        historyFlow.emit(summaries)

        val viewModel = HistoryViewModel(getHydrationHistoryUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.dailySummaries.size)
        assertEquals(2000, state.averageIntakeMl) // (2500+1500)/2
        assertEquals(1, state.daysGoalMetCount)
    }
}
