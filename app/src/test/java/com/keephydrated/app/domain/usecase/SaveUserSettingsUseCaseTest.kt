package com.keephydrated.app.domain.usecase

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
    fun `updateReminderMode updates mode in repository`() = runTest {
        useCase.updateReminderMode(ReminderMode.CUSTOM_ROUTINE)
        coVerify(exactly = 1) { settingsRepository.updateReminderMode(ReminderMode.CUSTOM_ROUTINE) }
    }

    @Test
    fun `updateCustomReminderHours with valid hours updates repository`() = runTest {
        val validHours = setOf(8, 12, 16, 20)
        useCase.updateCustomReminderHours(validHours)
        coVerify(exactly = 1) { settingsRepository.updateCustomReminderHours(validHours) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `updateCustomReminderHours with invalid hour throws IllegalArgumentException`() = runTest {
        val invalidHours = setOf(8, 12, 25) // 25 is invalid
        useCase.updateCustomReminderHours(invalidHours)
    }
}
