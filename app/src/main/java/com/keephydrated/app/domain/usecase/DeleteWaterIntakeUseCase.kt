package com.keephydrated.app.domain.usecase

import com.keephydrated.app.domain.repository.HydrationRepository
import javax.inject.Inject

class DeleteWaterIntakeUseCase @Inject constructor(
    private val repository: HydrationRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteIntake(id)
    }
}
