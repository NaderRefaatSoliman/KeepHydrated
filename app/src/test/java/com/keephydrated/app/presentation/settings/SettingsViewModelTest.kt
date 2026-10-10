package com.keephydrated.app.presentation.settings

import com.keephydrated.app.domain.model.CelebrationSound
import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.domain.repository.SettingsRepository
import com.keephydrated.app.presentation.ui.settings.SettingsViewModel
import com.keephydrated.app.util.LocalizationUtils
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
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var reminderScheduler: ReminderScheduler

    private val settingsFlow = MutableSharedFlow<UserSettings>(replay = 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mockk(relaxed = true)
        reminderScheduler = mockk(relaxed = true)

        every { settingsRepository.getUserSettings() } returns settingsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            settingsRepository,
            reminderScheduler,
            null
        )
    }

    @Test
    fun `initial state reflects settings from repository`() = runTest {
        val userSettings = UserSettings(
            dailyGoalMl = 2500,
            reminderIntervalHours = 3,
            reminderIntervalMinutes = 180,
            remindersEnabled = true,
            startHour = 7,
            endHour = 23,
            reminderMode = ReminderMode.CUSTOM_ROUTINE,
            customReminderHours = setOf(8, 12, 16, 20),
            notificationSound = NotificationSound.WATER_POUR.id,
            celebrationSound = CelebrationSound.VICTORY_SPLASH.id,
            defaultQuickAddMl = 350,
            quickAddOnNotificationClick = true,
            language = "ar",
            frequentIntakeMl = 330,
            bottleModeEnabled = true,
            bottleVolumeMl = 1000,
            bottleTargetDurationMinutes = 120
        )
        settingsFlow.emit(userSettings)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2500, state.dailyGoalMl)
        assertEquals(3, state.reminderIntervalHours)
        assertEquals(180, state.reminderIntervalMinutes)
        assertEquals(true, state.remindersEnabled)
        assertEquals("ar", state.language)
        assertEquals(330, state.frequentIntakeMl)
        assertEquals(true, state.bottleModeEnabled)
        assertEquals(1000, state.bottleVolumeMl)
        assertEquals(120, state.bottleTargetDurationMinutes)
    }

    @Test
    fun `updateLanguage delegates to repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateLanguage("ar")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.updateLanguage("ar") }
        assertEquals("ar", viewModel.uiState.value.language)
    }

    @Test
    fun `updateBottleModeEnabled delegates to repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateBottleModeEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.updateBottleModeEnabled(true) }
        assertEquals(true, viewModel.uiState.value.bottleModeEnabled)
    }

    @Test
    fun `updateBottleConfig delegates to repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateBottleConfig(1500, 240)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.updateBottleConfig(1500, 240) }
        assertEquals(1500, viewModel.uiState.value.bottleVolumeMl)
        assertEquals(240, viewModel.uiState.value.bottleTargetDurationMinutes)
    }

    @Test
    fun `updateFrequentIntakeMl delegates to repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateFrequentIntakeMl(500)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.updateFrequentIntakeMl(500) }
        assertEquals(500, viewModel.uiState.value.frequentIntakeMl)
    }

    @Test
    fun `updateDailyGoal delegates to repository and updates message`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateDailyGoal(2800)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.updateDailyGoal(2800) }
        assertEquals("Goal updated to 2800 ml", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `updateDailyGoal in Arabic updates Arabic userMessage`() = runTest {
        settingsFlow.emit(UserSettings(language = "ar"))
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateDailyGoal(2800)
        testDispatcher.scheduler.advanceUntilIdle()

        val expectedGoal = LocalizationUtils.formatNumber(2800, true)
        assertEquals("تم تحديث الهدف اليومي إلى $expectedGoal مل", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `updateUserProfile delegates to repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateUserProfile("Nader", 30, "male", 75f, 180f, "intense", 3200)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            settingsRepository.updateUserProfile("Nader", 30, "male", 75f, 180f, "intense", 3200)
        }
    }

    @Test
    fun `toggleReminders reschedules reminders in ReminderScheduler`() = runTest {
        val viewModel = createViewModel()

        viewModel.toggleReminders(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.updateRemindersEnabled(false) }
        verify(exactly = 1) { reminderScheduler.scheduleReminders(any(), false, any()) }
    }

    @Test
    fun `updateDroppyTipsEnabled delegates to repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateDroppyTipsEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.updateDroppyTipsEnabled(false) }
        assertEquals(false, viewModel.uiState.value.droppyTipsEnabled)
    }

    @Test
    fun `updateRemindAfterGoalReached delegates to repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateRemindAfterGoalReached(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.updateRemindAfterGoalReached(true) }
        assertEquals(true, viewModel.uiState.value.remindAfterGoalReached)
    }

    @Test
    fun `updateThemeMode delegates to repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateThemeMode("dark")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.updateThemeMode("dark") }
        assertEquals("dark", viewModel.uiState.value.themeMode)
    }
}
