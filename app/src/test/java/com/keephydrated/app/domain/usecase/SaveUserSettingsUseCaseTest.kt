package com.keephydrated.app.domain.usecase

import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.domain.repository.SettingsRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SaveUserSettingsUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SaveUserSettingsUseCase

    @Before
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase = SaveUserSettingsUseCase(settingsRepository)
    }

    @Test
    fun `updateGoal with valid value calls repository`() = runTest {
        useCase.updateGoal(2500)
        coVerify(exactly = 1) { settingsRepository.updateDailyGoal(2500) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `updateGoal with too low value throws IllegalArgumentException`() = runTest {
        useCase.updateGoal(300)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `updateGoal with too high value throws IllegalArgumentException`() = runTest {
        useCase.updateGoal(15000)
    }

    @Test
    fun `updateReminderInterval with valid hours calls repository`() = runTest {
        useCase.updateReminderInterval(3)
        coVerify(exactly = 1) { settingsRepository.updateReminderInterval(3) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `updateReminderInterval with zero hours throws IllegalArgumentException`() = runTest {
        useCase.updateReminderInterval(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `updateReminderInterval with too high hours throws IllegalArgumentException`() = runTest {
        useCase.updateReminderInterval(15)
    }

    @Test
    fun `updateReminderIntervalMinutes with valid minutes calls repository`() = runTest {
        useCase.updateReminderIntervalMinutes(45)
        coVerify(exactly = 1) { settingsRepository.updateReminderIntervalMinutes(45) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `updateReminderIntervalMinutes below 15 throws IllegalArgumentException`() = runTest {
        useCase.updateReminderIntervalMinutes(10)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `updateReminderIntervalMinutes above 720 throws IllegalArgumentException`() = runTest {
        useCase.updateReminderIntervalMinutes(800)
    }

    @Test
    fun `updateReminderMode updates mode in repository`() = runTest {
        useCase.updateReminderMode(ReminderMode.CUSTOM_ROUTINE)
        coVerify(exactly = 1) { settingsRepository.updateReminderMode(ReminderMode.CUSTOM_ROUTINE) }
    }

    @Test
    fun `updateCustomReminderHours with valid 24h range updates repository`() = runTest {
        val all24Hours = (0..23).toSet()
        useCase.updateCustomReminderHours(all24Hours)
        coVerify(exactly = 1) { settingsRepository.updateCustomReminderHours(all24Hours) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `updateCustomReminderHours with invalid hour throws IllegalArgumentException`() = runTest {
        val invalidHours = setOf(8, 12, 24) // 24 is invalid (valid range is 0..23)
        useCase.updateCustomReminderHours(invalidHours)
    }

    @Test
    fun `updateNotificationSound updates repository with valid sound id`() = runTest {
        useCase.updateNotificationSound(NotificationSound.WATER_POUR.id)
        coVerify(exactly = 1) { settingsRepository.updateNotificationSound(NotificationSound.WATER_POUR.id) }
    }

    @Test
    fun `updateDefaultQuickAddMl with valid amount updates repository`() = runTest {
        useCase.updateDefaultQuickAddMl(350)
        coVerify(exactly = 1) { settingsRepository.updateDefaultQuickAddMl(350) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `updateDefaultQuickAddMl with too low amount throws IllegalArgumentException`() = runTest {
        useCase.updateDefaultQuickAddMl(20)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `updateDefaultQuickAddMl with too high amount throws IllegalArgumentException`() = runTest {
        useCase.updateDefaultQuickAddMl(3000)
    }

    @Test
    fun `updateQuickAddOnNotificationClick updates repository`() = runTest {
        useCase.updateQuickAddOnNotificationClick(true)
        coVerify(exactly = 1) { settingsRepository.updateQuickAddOnNotificationClick(true) }
    }
}
