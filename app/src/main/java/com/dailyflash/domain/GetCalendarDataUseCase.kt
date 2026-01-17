package com.dailyflash.domain

import com.dailyflash.core.storage.VideoFile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth

/**
 * Use case for fetching calendar data with video indicators.
 * Maps repository data to domain entities for calendar display.
 */
class GetCalendarDataUseCase(
    private val videoRepository: IVideoRepository
) {
    /**
     * Get calendar data with video entities for a specific month.
     * @param yearMonth Target month
     * @return Flow of date-to-VideoEntity mapping
     */
    operator fun invoke(yearMonth: YearMonth): Flow<Map<LocalDate, VideoEntity?>> {
        return videoRepository.getVideosForMonth(yearMonth)
            .map { dateMap ->
                dateMap.mapValues { (_, file) -> file?.toEntity() }
            }
    }

    /**
     * Extension function to convert VideoFile to VideoEntity.
     */
    private fun VideoFile.toEntity() = VideoEntity(
        id = id,
        uri = uri,
        date = date,
        durationMs = durationMs,
        thumbnailUri = null // Generated lazily
    )
}
