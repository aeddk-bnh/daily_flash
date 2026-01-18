package com.dailyflash.domain.settings

import java.time.LocalDate

/**
 * Use case to update recording streak after a successful capture.
 */
class UpdateStreakUseCase(
    private val repository: ISettingsRepository
) {
    suspend operator fun invoke() {
        repository.updateStreak(LocalDate.now())
    }
}
