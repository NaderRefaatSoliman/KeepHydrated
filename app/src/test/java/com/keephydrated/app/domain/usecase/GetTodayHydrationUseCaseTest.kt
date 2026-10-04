package com.keephydrated.app.domain.usecase

import app.cash.turbine.test
import com.keephydrated.app.domain.model.WaterIntake
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.domain.repository.HydrationRepository
import com.keephydrated.app.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class GetTodayHydrationUseCaseTest {

    private lateinit var hydrationRepository: HydrationRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetTodayHydrationUseCase

    @Before
    fun setUp() {
        hydrationRepository = mockk()
        settingsRepository = mockk()
        useCase = GetTodayHydrationUseCase(hydrationRepository, settingsRepository)
    }

    @Test
    fun `invoke computes correct total intake and progress percentage`() = runTest {
        val today = LocalDate.of(2026, 10, 4)
        val intakes = listOf(
            WaterIntake(id = 1, amountMl = 500, timestamp = LocalDateTime.of(2026, 10, 4, 8, 30)),
            WaterIntake(id = 2, amountMl = 500, timestamp = LocalDateTime.of(2026, 10, 4, 11, 0))
        )
        val settings = UserSettings(dailyGoalMl = 2000)

        every { hydrationRepository.getIntakesForDate(today) } returns flowOf(intakes)
        every { settingsRepository.getUserSettings() } returns flowOf(settings)

        useCase(today).test {
            val summary = awaitItem()
            assertEquals(1000, summary.totalIntakeMl)
            assertEquals(2000, summary.goalMl)
            assertEquals(0.5f, summary.progressPercentage, 0.001f)
            assertFalse(summary.isGoalAchieved)
            awaitComplete()
        }
    }

    @Test
    fun `invoke reflects goal achieved when total exceeds or equals goal`() = runTest {
        val today = LocalDate.of(2026, 10, 4)
        val intakes = listOf(
            WaterIntake(id = 1, amountMl = 1500, timestamp = LocalDateTime.of(2026, 10, 4, 9, 0)),
            WaterIntake(id = 2, amountMl = 1000, timestamp = LocalDateTime.of(2026, 10, 4, 13, 0))
        )
        val settings = UserSettings(dailyGoalMl = 2000)

        every { hydrationRepository.getIntakesForDate(today) } returns flowOf(intakes)
        every { settingsRepository.getUserSettings() } returns flowOf(settings)

        useCase(today).test {
            val summary = awaitItem()
            assertEquals(2500, summary.totalIntakeMl)
            assertEquals(2000, summary.goalMl)
            assertEquals(1.25f, summary.progressPercentage, 0.001f)
            assertTrue(summary.isGoalAchieved)
            awaitComplete()
        }
    }
}
