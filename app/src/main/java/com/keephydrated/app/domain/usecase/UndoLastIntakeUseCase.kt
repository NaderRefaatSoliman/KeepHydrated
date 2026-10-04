package com.keephydrated.app.domain.usecase

import com.keephydrated.app.domain.repository.HydrationRepository
import java.time.LocalDate
import javax.inject.Inject

class UndoLastIntakeUseCase @Inject constructor(
    private val repository: HydrationRepository
) {
    suspend operator fun invoke(date: LocalDate = LocalDate.now()): Boolean {
        return repository.deleteLatestIntakeForDate(date)
    }
}
