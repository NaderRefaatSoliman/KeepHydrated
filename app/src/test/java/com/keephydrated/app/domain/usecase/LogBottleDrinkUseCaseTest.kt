package com.keephydrated.app.domain.usecase

import com.keephydrated.app.domain.repository.HydrationRepository
import com.keephydrated.app.domain.repository.SettingsRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LogBottleDrinkUseCaseTest {

    private lateinit var hydrationRepository: HydrationRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: LogBottleDrinkUseCase

    @Before
    fun setUp() {
        hydrationRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        useCase = LogBottleDrinkUseCase(hydrationRepository, settingsRepository)
    }

    @Test
    fun `invoke inserts intake into hydration repo and records bottle drink`() = runTest {
        useCase(250)

        coVerify(exactly = 1) { hydrationRepository.insertIntake(match { it.amountMl == 250 }) }
        coVerify(exactly = 1) { settingsRepository.recordBottleDrink(250) }
    }

    @Test
    fun `invoke with zero or negative amount does nothing`() = runTest {
        useCase(0)
        useCase(-50)

        coVerify(exactly = 0) { hydrationRepository.insertIntake(any()) }
        coVerify(exactly = 0) { settingsRepository.recordBottleDrink(any()) }
    }
}
