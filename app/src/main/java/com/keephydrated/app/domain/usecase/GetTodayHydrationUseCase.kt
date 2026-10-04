package com.keephydrated.app.domain.usecase

import com.keephydrated.app.domain.model.DailyHydrationSummary
import com.keephydrated.app.domain.repository.HydrationRepository
import com.keephydrated.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class GetTodayHydrationUseCase @Inject constructor(
    private val hydrationRepository: HydrationRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(date: LocalDate = LocalDate.now()): Flow<DailyHydrationSummary> {
        return combine(
            hydrationRepository.getIntakesForDate(date),
            settingsRepository.getUserSettings()
        ) { intakes, settings ->
            val total = intakes.sumOf { it.amountMl }
            DailyHydrationSummary(
                date = date,
                totalIntakeMl = total,
                goalMl = settings.dailyGoalMl,
                intakes = intakes
            )
        }
    }
}
