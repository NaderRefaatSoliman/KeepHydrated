package com.keephydrated.app.data.repository

import com.keephydrated.app.data.local.dao.WaterIntakeDao
import com.keephydrated.app.data.local.entity.WaterIntakeEntity
import com.keephydrated.app.domain.model.WaterIntake
import com.keephydrated.app.domain.repository.HydrationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HydrationRepositoryImpl @Inject constructor(
    private val dao: WaterIntakeDao
) : HydrationRepository {

    override fun getIntakesForDate(date: LocalDate): Flow<List<WaterIntake>> {
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.atTime(LocalTime.MAX)
        return dao.getIntakesBetween(startOfDay, endOfDay).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllIntakes(): Flow<List<WaterIntake>> {
        return dao.getAllIntakes().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertIntake(intake: WaterIntake): Long {
        return dao.insertIntake(intake.toEntity())
    }

    override suspend fun deleteIntake(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun deleteLatestIntakeForDate(date: LocalDate): Boolean {
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.atTime(LocalTime.MAX)
        val latest = dao.getLatestIntakeBetween(startOfDay, endOfDay)
        return if (latest != null) {
            dao.deleteById(latest.id)
            true
        } else {
            false
        }
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }

    private fun WaterIntakeEntity.toDomain() = WaterIntake(
        id = id,
        amountMl = amountMl,
        timestamp = timestamp
    )

    private fun WaterIntake.toEntity() = WaterIntakeEntity(
        id = id,
        amountMl = amountMl,
        timestamp = timestamp
    )
}
