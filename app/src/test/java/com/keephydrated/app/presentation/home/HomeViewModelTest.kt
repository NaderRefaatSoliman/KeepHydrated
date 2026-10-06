package com.keephydrated.app.presentation.home

import com.keephydrated.app.domain.model.DailyHydrationSummary
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.domain.model.WaterIntake
import com.keephydrated.app.domain.repository.SettingsRepository
import com.keephydrated.app.domain.usecase.AddWaterIntakeUseCase
import com.keephydrated.app.domain.usecase.DeleteWaterIntakeUseCase
import com.keephydrated.app.domain.usecase.GetTodayHydrationUseCase
import com.keephydrated.app.domain.usecase.LogBottleDrinkUseCase
import com.keephydrated.app.domain.usecase.RefillBottleUseCase
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
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var logBottleDrinkUseCase: LogBottleDrinkUseCase
    private lateinit var refillBottleUseCase: RefillBottleUseCase

    private val hydrationFlow = MutableSharedFlow<DailyHydrationSummary>(replay = 1)
    private val settingsFlow = MutableSharedFlow<UserSettings>(replay = 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getTodayHydrationUseCase = mockk()
        addWaterIntakeUseCase = mockk(relaxed = true)
        deleteWaterIntakeUseCase = mockk(relaxed = true)
        undoLastIntakeUseCase = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        logBottleDrinkUseCase = mockk(relaxed = true)
        refillBottleUseCase = mockk(relaxed = true)

        every { getTodayHydrationUseCase(any()) } returns hydrationFlow
        every { getTodayHydrationUseCase() } returns hydrationFlow
        every { settingsRepository.getUserSettings() } returns settingsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            getTodayHydrationUseCase,
            addWaterIntakeUseCase,
            deleteWaterIntakeUseCase,
            undoLastIntakeUseCase,
            settingsRepository,
            logBottleDrinkUseCase,
            refillBottleUseCase
        )
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
        settingsFlow.emit(UserSettings(dailyGoalMl = 2000, frequentIntakeMl = 250))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1200, state.currentIntakeMl)
        assertEquals(2000, state.dailyGoalMl)
        assertEquals(0.6f, state.progressPercentage, 0.001f)
        assertEquals(250, state.frequentIntakeMl)
    }

    @Test
    fun `addWater triggers AddWaterIntakeUseCase`() = runTest {
        val summary = DailyHydrationSummary(
            date = LocalDate.now(),
            totalIntakeMl = 500,
            goalMl = 2000
        )
        hydrationFlow.emit(summary)
        settingsFlow.emit(UserSettings(dailyGoalMl = 2000))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addWater(250)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { addWaterIntakeUseCase(amountMl = 250, timestamp = any()) }
    }

    @Test
    fun `drinkFromBottle triggers LogBottleDrinkUseCase`() = runTest {
        hydrationFlow.emit(DailyHydrationSummary(date = LocalDate.now(), totalIntakeMl = 0, goalMl = 2000))
        settingsFlow.emit(UserSettings(dailyGoalMl = 2000, frequentIntakeMl = 250, bottleModeEnabled = true))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.drinkFromBottle(250)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { logBottleDrinkUseCase(250) }
    }

    @Test
    fun `refillBottle triggers RefillBottleUseCase`() = runTest {
        hydrationFlow.emit(DailyHydrationSummary(date = LocalDate.now(), totalIntakeMl = 0, goalMl = 2000))
        settingsFlow.emit(UserSettings(dailyGoalMl = 2000, bottleModeEnabled = true))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.refillBottle()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { refillBottleUseCase() }
    }

    @Test
    fun `undoLastIntake updates userMessage when successful`() = runTest {
        coEvery { undoLastIntakeUseCase() } returns true
        hydrationFlow.emit(DailyHydrationSummary(date = LocalDate.now(), totalIntakeMl = 500, goalMl = 2000))
        settingsFlow.emit(UserSettings(dailyGoalMl = 2000))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.undoLastIntake()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Last entry undone", viewModel.uiState.value.userMessage)
    }
}
