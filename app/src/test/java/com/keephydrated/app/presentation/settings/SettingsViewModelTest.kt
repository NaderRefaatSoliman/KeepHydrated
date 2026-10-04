package com.keephydrated.app.presentation.settings

import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.domain.usecase.GetUserSettingsUseCase
import com.keephydrated.app.domain.usecase.SaveUserSettingsUseCase
import com.keephydrated.app.presentation.ui.settings.SettingsViewModel
import com.keephydrated.app.worker.ReminderScheduler
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getUserSettingsUseCase: GetUserSettingsUseCase
    private lateinit var saveUserSettingsUseCase: SaveUserSettingsUseCase
    private lateinit var reminderScheduler: ReminderScheduler

    private val settingsFlow = MutableSharedFlow<UserSettings>(replay = 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getUserSettingsUseCase = mockk()
        saveUserSettingsUseCase = mockk(relaxed = true)
        reminderScheduler = mockk(relaxed = true)

        every { getUserSettingsUseCase() } returns settingsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects settings from usecase`() = runTest {
        val userSettings = UserSettings(
            dailyGoalMl = 2500,
            reminderIntervalHours = 3,
            remindersEnabled = true
        )
        settingsFlow.emit(userSettings)

        val viewModel = SettingsViewModel(
            getUserSettingsUseCase,
            saveUserSettingsUseCase,
            reminderScheduler
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2500, state.dailyGoalMl)
        assertEquals(3, state.reminderIntervalHours)
        assertEquals(true, state.remindersEnabled)
    }

    @Test
    fun `updateDailyGoal delegates to usecase and updates message`() = runTest {
        val viewModel = SettingsViewModel(
            getUserSettingsUseCase,
            saveUserSettingsUseCase,
            reminderScheduler
        )

        viewModel.updateDailyGoal(2800)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveUserSettingsUseCase.updateGoal(2800) }
        assertEquals("Goal updated to 2800 ml", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `toggleReminders reschedules reminders in ReminderScheduler`() = runTest {
        val viewModel = SettingsViewModel(
            getUserSettingsUseCase,
            saveUserSettingsUseCase,
            reminderScheduler
        )

        viewModel.toggleReminders(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveUserSettingsUseCase.updateRemindersEnabled(false) }
        verify(exactly = 1) { reminderScheduler.scheduleReminders(any(), false) }
    }
}
