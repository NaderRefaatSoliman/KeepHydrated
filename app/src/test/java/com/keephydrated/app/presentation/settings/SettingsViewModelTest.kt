package com.keephydrated.app.presentation.settings

import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.ReminderMode
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
import org.junit.Assert.assertTrue
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
            reminderIntervalMinutes = 180,
            remindersEnabled = true,
            startHour = 7,
            endHour = 23,
            reminderMode = ReminderMode.CUSTOM_ROUTINE,
            customReminderHours = setOf(8, 12, 16, 20),
            notificationSound = NotificationSound.WATER_POUR.id
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
        assertEquals(180, state.reminderIntervalMinutes)
        assertEquals(true, state.remindersEnabled)
        assertEquals(7, state.startHour)
        assertEquals(23, state.endHour)
        assertEquals(ReminderMode.CUSTOM_ROUTINE, state.reminderMode)
        assertEquals(setOf(8, 12, 16, 20), state.customReminderHours)
        assertEquals(NotificationSound.WATER_POUR.id, state.notificationSound)
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
    fun `updateReminderInterval delegates to usecase and reschedules reminders`() = runTest {
        val viewModel = SettingsViewModel(
            getUserSettingsUseCase,
            saveUserSettingsUseCase,
            reminderScheduler
        )

        viewModel.updateReminderInterval(4)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveUserSettingsUseCase.updateReminderInterval(4) }
        verify(exactly = 1) { reminderScheduler.scheduleReminders(240, any(), any()) }
        assertEquals("Reminder interval updated to 4 hours", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `updateReminderIntervalMinutes delegates to usecase and reschedules reminders`() = runTest {
        val viewModel = SettingsViewModel(
            getUserSettingsUseCase,
            saveUserSettingsUseCase,
            reminderScheduler
        )

        viewModel.updateReminderIntervalMinutes(45)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveUserSettingsUseCase.updateReminderIntervalMinutes(45) }
        verify(exactly = 1) { reminderScheduler.scheduleReminders(45, any(), any()) }
        assertEquals("Reminder interval updated to 45 minutes", viewModel.uiState.value.userMessage)
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
        verify(exactly = 1) { reminderScheduler.scheduleReminders(any(), false, any()) }
    }

    @Test
    fun `updateReminderMode delegates to usecase and updates scheduler`() = runTest {
        val viewModel = SettingsViewModel(
            getUserSettingsUseCase,
            saveUserSettingsUseCase,
            reminderScheduler
        )

        viewModel.updateReminderMode(ReminderMode.CUSTOM_ROUTINE)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveUserSettingsUseCase.updateReminderMode(ReminderMode.CUSTOM_ROUTINE) }
        verify(exactly = 1) { reminderScheduler.scheduleReminders(any(), any(), ReminderMode.CUSTOM_ROUTINE) }
    }

    @Test
    fun `toggleCustomReminderHour toggles hour in usecase`() = runTest {
        val userSettings = UserSettings(
            customReminderHours = setOf(9, 12, 15)
        )
        settingsFlow.emit(userSettings)

        val viewModel = SettingsViewModel(
            getUserSettingsUseCase,
            saveUserSettingsUseCase,
            reminderScheduler
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Toggle hour 18 on (not currently in set)
        viewModel.toggleCustomReminderHour(18)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveUserSettingsUseCase.updateCustomReminderHours(setOf(9, 12, 15, 18)) }
    }

    @Test
    fun `setCustomReminderHours updates custom hours and userMessage`() = runTest {
        val viewModel = SettingsViewModel(
            getUserSettingsUseCase,
            saveUserSettingsUseCase,
            reminderScheduler
        )

        val preset = setOf(9, 11, 13, 15, 17)
        viewModel.setCustomReminderHours(preset)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveUserSettingsUseCase.updateCustomReminderHours(preset) }
        assertEquals("Preset routine applied (5 reminders)", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `updateNotificationSound updates sound in usecase and sets message`() = runTest {
        val viewModel = SettingsViewModel(
            getUserSettingsUseCase,
            saveUserSettingsUseCase,
            reminderScheduler
        )

        viewModel.updateNotificationSound(NotificationSound.OCEAN_WAVE.id)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveUserSettingsUseCase.updateNotificationSound(NotificationSound.OCEAN_WAVE.id) }
        assertEquals("Notification sound: Ocean Wave", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `applyWakingDayPreset updates active hours and custom reminder routine`() = runTest {
        val viewModel = SettingsViewModel(
            getUserSettingsUseCase,
            saveUserSettingsUseCase,
            reminderScheduler
        )

        // Wake at 7 AM, sleep at 11 PM (23), every 2h
        viewModel.applyWakingDayPreset(wakeHour = 7, sleepHour = 23, stepHours = 2)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveUserSettingsUseCase.updateActiveHours(7, 23) }
        val expectedHours = setOf(7, 9, 11, 13, 15, 17, 19, 21, 23)
        coVerify(exactly = 1) { saveUserSettingsUseCase.updateCustomReminderHours(expectedHours) }
        assertEquals("Awake schedule applied: 9 reminders set", viewModel.uiState.value.userMessage)
    }
}
