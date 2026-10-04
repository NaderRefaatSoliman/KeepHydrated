package com.keephydrated.app.domain.usecase

import com.keephydrated.app.domain.model.WaterIntake
import com.keephydrated.app.domain.repository.HydrationRepository
import java.time.LocalDateTime
import javax.inject.Inject

class AddWaterIntakeUseCase @Inject constructor(
    private val repository: HydrationRepository
) {
    suspend operator fun invoke(amountMl: Int, timestamp: LocalDateTime = LocalDateTime.now()): Long {
        require(amountMl > 0) { "Water amount must be greater than zero" }
        val intake = WaterIntake(amountMl = amountMl, timestamp = timestamp)
        return repository.insertIntake(intake)
    }
}
