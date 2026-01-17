package com.dailyflash.domain

import android.net.Uri
import com.dailyflash.core.storage.VideoFile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class CaptureVideoUseCaseTest {

    private val mockRepository: IVideoRepository = mock()
    private val useCase = CaptureVideoUseCase(mockRepository)

    @Test
    fun `invoke saves video and maps to entity`() = runTest {
        val uri = mock<Uri>()
        val date = LocalDate.now()
        val videoFile = VideoFile("id1", uri, date, 1000, 1024, 0)
        
        whenever(mockRepository.saveVideo(any(), any())).thenReturn(Result.success(videoFile))

        val result = useCase(uri)

        assertTrue(result.isSuccess)
        val entity = result.getOrThrow()
        assertEquals("id1", entity.id)
        assertEquals(uri, entity.uri)
        assertEquals(date, entity.date)
        verify(mockRepository).saveVideo(uri, date)
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val uri = mock<Uri>()
        val error = RuntimeException("Save failed")
        
        whenever(mockRepository.saveVideo(any(), any())).thenReturn(Result.failure(error))

        val result = useCase(uri)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
