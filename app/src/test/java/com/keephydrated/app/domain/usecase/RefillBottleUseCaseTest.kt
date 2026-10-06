package com.keephydrated.app.domain.usecase

import com.keephydrated.app.domain.repository.SettingsRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RefillBottleUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: RefillBottleUseCase

    @Before
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase = RefillBottleUseCase(settingsRepository)
    }

    @Test
    fun `invoke delegates to settings repository refillBottle`() = runTest {
        useCase()

        coVerify(exactly = 1) { settingsRepository.refillBottle() }
    }
}
