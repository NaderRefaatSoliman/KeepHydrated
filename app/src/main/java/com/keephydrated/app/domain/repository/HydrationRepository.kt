package com.keephydrated.app.domain.repository

import com.keephydrated.app.domain.model.WaterIntake
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HydrationRepository {
    fun getIntakesForDate(date: LocalDate): Flow<List<WaterIntake>>
    fun getAllIntakes(): Flow<List<WaterIntake>>
    suspend fun insertIntake(intake: WaterIntake): Long
    suspend fun deleteIntake(id: Long)
    suspend fun deleteLatestIntakeForDate(date: LocalDate): Boolean
    suspend fun clearAll()
}
