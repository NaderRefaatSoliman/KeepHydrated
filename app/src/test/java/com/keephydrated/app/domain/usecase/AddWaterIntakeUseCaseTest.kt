package com.keephydrated.app.domain.usecase

import com.keephydrated.app.domain.model.WaterIntake
import com.keephydrated.app.domain.repository.HydrationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class AddWaterIntakeUseCaseTest {

    private lateinit var repository: HydrationRepository
    private lateinit var useCase: AddWaterIntakeUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = AddWaterIntakeUseCase(repository)
    }

    @Test
    fun `invoke with positive amount inserts intake into repository`() = runTest {
        val intakeSlot = slot<WaterIntake>()
        coEvery { repository.insertIntake(capture(intakeSlot)) } returns 1L

        val testTime = LocalDateTime.of(2026, 10, 4, 10, 0)
        val result = useCase(amountMl = 250, timestamp = testTime)

        assertEquals(1L, result)
        assertEquals(250, intakeSlot.captured.amountMl)
        assertEquals(testTime, intakeSlot.captured.timestamp)
        coVerify(exactly = 1) { repository.insertIntake(any()) }
    }

    @Test
    fun `invoke with zero or negative amount throws IllegalArgumentException`() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            runTest { useCase(amountMl = 0) }
        }

        assertThrows(IllegalArgumentException::class.java) {
            runTest { useCase(amountMl = -50) }
        }

        coVerify(exactly = 0) { repository.insertIntake(any()) }
    }
}
