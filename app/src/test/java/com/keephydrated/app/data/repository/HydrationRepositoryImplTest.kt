package com.keephydrated.app.data.repository

import app.cash.turbine.test
import com.keephydrated.app.data.local.dao.WaterIntakeDao
import com.keephydrated.app.data.local.entity.WaterIntakeEntity
import com.keephydrated.app.domain.model.WaterIntake
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class HydrationRepositoryImplTest {

    private lateinit var dao: WaterIntakeDao
    private lateinit var repository: HydrationRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        repository = HydrationRepositoryImpl(dao)
    }

    @Test
    fun `getIntakesForDate converts entity to domain models correctly`() = runTest {
        val today = LocalDate.of(2026, 10, 4)
        val time1 = LocalDateTime.of(2026, 10, 4, 9, 0)
        val entities = listOf(
            WaterIntakeEntity(id = 10, amountMl = 300, timestamp = time1)
        )

        every { dao.getIntakesBetween(any(), any()) } returns flowOf(entities)

        repository.getIntakesForDate(today).test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(10L, list[0].id)
            assertEquals(300, list[0].amountMl)
            assertEquals(time1, list[0].timestamp)
            awaitComplete()
        }
    }

    @Test
    fun `insertIntake delegates to dao with mapped entity`() = runTest {
        val intake = WaterIntake(amountMl = 250, timestamp = LocalDateTime.now())
        coEvery { dao.insertIntake(any()) } returns 42L

        val generatedId = repository.insertIntake(intake)

        assertEquals(42L, generatedId)
        coVerify(exactly = 1) { dao.insertIntake(match { it.amountMl == 250 }) }
    }

    @Test
    fun `deleteLatestIntakeForDate deletes latest when found and returns true`() = runTest {
        val today = LocalDate.of(2026, 10, 4)
        val latestEntity = WaterIntakeEntity(id = 99, amountMl = 200, timestamp = LocalDateTime.now())
        coEvery { dao.getLatestIntakeBetween(any(), any()) } returns latestEntity
        coEvery { dao.deleteById(99) } returns Unit

        val result = repository.deleteLatestIntakeForDate(today)

        assertTrue(result)
        coVerify(exactly = 1) { dao.deleteById(99) }
    }
}
