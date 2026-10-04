package com.keephydrated.app.presentation.home

import app.cash.turbine.test
import com.keephydrated.app.domain.model.DailyHydrationSummary
import com.keephydrated.app.domain.model.WaterIntake
import com.keephydrated.app.domain.usecase.AddWaterIntakeUseCase
import com.keephydrated.app.domain.usecase.DeleteWaterIntakeUseCase
import com.keephydrated.app.domain.usecase.GetTodayHydrationUseCase
import com.keephydrated.app.domain.usecase.UndoLastIntakeUseCase
import com.keephydrated.app.presentation.ui.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
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
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getTodayHydrationUseCase: GetTodayHydrationUseCase
    private lateinit var addWaterIntakeUseCase: AddWaterIntakeUseCase
    private lateinit var deleteWaterIntakeUseCase: DeleteWaterIntakeUseCase
    private lateinit var undoLastIntakeUseCase: UndoLastIntakeUseCase

    private val hydrationFlow = MutableSharedFlow<DailyHydrationSummary>(replay = 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getTodayHydrationUseCase = mockk()
        addWaterIntakeUseCase = mockk(relaxed = true)
        deleteWaterIntakeUseCase = mockk(relaxed = true)
        undoLastIntakeUseCase = mockk(relaxed = true)

        every { getTodayHydrationUseCase(any()) } returns hydrationFlow
        every { getTodayHydrationUseCase() } returns hydrationFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects today summary from use case`() = runTest {
        val summary = DailyHydrationSummary(
            date = LocalDate.now(),
            totalIntakeMl = 1200,
            goalMl = 2000,
            intakes = listOf(WaterIntake(1, 1200, LocalDateTime.now()))
        )
        hydrationFlow.emit(summary)

        val viewModel = HomeViewModel(
            getTodayHydrationUseCase,
            addWaterIntakeUseCase,
            deleteWaterIntakeUseCase,
            undoLastIntakeUseCase
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1200, state.currentIntakeMl)
        assertEquals(2000, state.dailyGoalMl)
        assertEquals(0.6f, state.progressPercentage, 0.001f)
    }

    @Test
    fun `addWater triggers AddWaterIntakeUseCase`() = runTest {
        val viewModel = HomeViewModel(
            getTodayHydrationUseCase,
            addWaterIntakeUseCase,
            deleteWaterIntakeUseCase,
            undoLastIntakeUseCase
        )

        viewModel.addWater(250)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { addWaterIntakeUseCase(250) }
    }

    @Test
    fun `undoLastIntake updates userMessage when successful`() = runTest {
        coEvery { undoLastIntakeUseCase() } returns true

        val viewModel = HomeViewModel(
            getTodayHydrationUseCase,
            addWaterIntakeUseCase,
            deleteWaterIntakeUseCase,
            undoLastIntakeUseCase
        )

        viewModel.undoLastIntake()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Last entry undone", viewModel.uiState.value.userMessage)
    }
}
