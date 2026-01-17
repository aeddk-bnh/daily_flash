package com.dailyflash.data

import android.net.Uri
import com.dailyflash.core.storage.IStorageManager
import com.dailyflash.core.storage.VideoFile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.io.File
import java.time.LocalDate
import java.time.YearMonth

class VideoRepositoryImplTest {

    private lateinit var repository: VideoRepositoryImpl
    private lateinit var mockStorageManager: IStorageManager

    @Before
    fun setup() {
        mockStorageManager = mock(IStorageManager::class.java)
        repository = VideoRepositoryImpl(mockStorageManager, null)
    }

    @Test
    fun `getVideosInRange returns cached videos if available`() = runTest {
        val date = LocalDate.of(2026, 1, 16)
        val video = VideoFile("id1", mock(Uri::class.java), date, 1000, 1024, 0)
        
        // Seed cache via save or manual reflection (here we simulate by mocking storage first return)
        whenever(mockStorageManager.getVideosByRange(any(), any())).thenReturn(listOf(video))
        
        // First call populates cache
        repository.getVideosInRange(date, date)
        
        // Second call should not hit storage
        val result = repository.getVideosInRange(date, date)
        
        assertEquals(1, result.size)
        assertEquals(video, result[0])
        
        // Verify storage called only once
        verify(mockStorageManager, times(1)).getVideosByRange(any(), any())
    }

    @Test
    fun `deleteVideo removes from storage and cache`() = runTest {
        val date = LocalDate.of(2026, 1, 16)
        val uri = mock(Uri::class.java)
        whenever(uri.path).thenReturn("/path/to/video.mp4")
        val video = VideoFile("id1", uri, date, 1000, 1024, 0)
        
        // Seed cache
        whenever(mockStorageManager.getVideosByRange(any(), any()))
            .thenReturn(listOf(video)) // First call returns video
            .thenReturn(emptyList())   // Second call (after delete) returns empty
            
        repository.getVideosInRange(date, date)
        
        whenever(mockStorageManager.deleteVideo(any())).thenReturn(true)
        
        val result = repository.deleteVideo("id1")
        
        assertTrue(result.isSuccess)
        verify(mockStorageManager).deleteVideo(uri)
        
        // Verify removed from cache
        val cached = repository.getVideosInRange(date, date)
        assertTrue(cached.isEmpty())
    }
    
    @Test
    fun `getVideosForMonth filters correctly`() = runTest {
        val month = YearMonth.of(2026, 1)
        val date1 = LocalDate.of(2026, 1, 15)
        val video1 = VideoFile("id1", mock(Uri::class.java), date1, 1000, 1024, 0)
        
        // Mock storage to return list
        whenever(mockStorageManager.getVideosByRange(any(), any())).thenReturn(listOf(video1))
        
        val flow = repository.getVideosForMonth(month)
        val map = flow.first()
        
        assertEquals(video1, map[date1])
        assertTrue(map.containsKey(LocalDate.of(2026, 1, 1))) // Should contain keys for whole month?
        // The implementation populates keys for the loop
    }
}
