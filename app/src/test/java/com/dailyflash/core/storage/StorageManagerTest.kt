package com.dailyflash.core.storage

import android.content.Context
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class StorageManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var storageManager: StorageManager
    private lateinit var mockContext: Context
    private lateinit var filesDir: File

    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        filesDir = tempFolder.newFolder("files")
        whenever(mockContext.getExternalFilesDir(any())).thenReturn(filesDir)
        
        storageManager = StorageManager(mockContext)
    }

    @Test
    fun `saveVideo creates file in correct date path`() = kotlinx.coroutines.test.runTest {
        val date = LocalDate.of(2026, 1, 16)
        val data = "test data".toByteArray()
        
        val uri = storageManager.saveVideo(data, date)
        
        val expectedPath = File(filesDir, "DailyFlash/2026/01/16")
        assertTrue("Directory should exist", expectedPath.exists())
        assertEquals("URI should match file path", Uri.fromFile(expectedPath.listFiles()?.first()), uri)
    }

    @Test
    fun `getVideosByDate returns correct files`() = kotlinx.coroutines.test.runTest {
        val date = LocalDate.of(2026, 1, 16)
        setupVideoFile(date, "v1.mp4")
        setupVideoFile(date, "v2.mp4")
        
        val result = storageManager.getVideosByDate(date)
        
        assertEquals(2, result.size)
    }

    @Test
    fun `deleteVideo removes file and returns true`() = kotlinx.coroutines.test.runTest {
        val date = LocalDate.of(2026, 1, 16)
        val file = setupVideoFile(date, "delete_me.mp4")
        val uri = Uri.fromFile(file)
        
        val deleted = storageManager.deleteVideo(uri)
        
        assertTrue("Delete should return true", deleted)
        assertFalse("File should be gone", file.exists())
    }

    private fun setupVideoFile(date: LocalDate, name: String): File {
        val baseDir = File(filesDir, "DailyFlash")
        val datePath = DateOrganizer.getPathForDate(baseDir, date)
        datePath.mkdirs()
        val file = File(datePath, name)
        file.writeText("content")
        return file
    }
}
