package com.dailyflash.core.storage

import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.net.Uri
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class StorageManagerTest {

    private lateinit var storageManager: StorageManager
    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: ContentResolver

    @Before
    fun setup() {
        mockContext = mock()
        mockContentResolver = mock()
        whenever(mockContext.contentResolver).thenReturn(mockContentResolver)
        whenever(mockContext.cacheDir).thenReturn(File("build/tmp/test-cache").apply { mkdirs() })
        storageManager = StorageManager(mockContext)
    }

    @Test
    fun `saveVideo writes bytes via content resolver and returns inserted uri`() = kotlinx.coroutines.test.runTest {
        val date = LocalDate.of(2026, 1, 16)
        val data = "test data".toByteArray()
        val insertedUri = Uri.parse("content://media/external/video/media/123")
        val outputStream = ByteArrayOutputStream()

        whenever(mockContentResolver.insert(any(), any())).thenReturn(insertedUri)
        whenever(mockContentResolver.openOutputStream(eq(insertedUri))).thenReturn(outputStream)

        val uri = storageManager.saveVideo(data, date)

        assertEquals(insertedUri, uri)
        assertArrayEquals(data, outputStream.toByteArray())
    }

    @Test
    fun `getVideosByDate filters results by requested date`() = kotlinx.coroutines.test.runTest {
        val date = LocalDate.of(2026, 1, 16)
        val cursor = MatrixCursor(
            arrayOf(
                android.provider.MediaStore.Video.Media._ID,
                android.provider.MediaStore.Video.Media.DISPLAY_NAME,
                android.provider.MediaStore.Video.Media.DATE_TAKEN,
                android.provider.MediaStore.Video.Media.DURATION,
                android.provider.MediaStore.Video.Media.SIZE,
                android.provider.MediaStore.Video.Media.DATA
            )
        )
        cursor.addRow(arrayOf(1L, "dailyflash_20260116_1000.mp4", 0L, 1000L, 1024L, "/tmp/a.mp4"))
        cursor.addRow(arrayOf(2L, "dailyflash_20260117_1000.mp4", 0L, 1000L, 2048L, "/tmp/b.mp4"))

        whenever(mockContentResolver.query(any(), any(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(cursor)

        val result = storageManager.getVideosByDate(date)

        assertEquals(1, result.size)
        assertEquals(date, result.first().date)
    }

    @Test
    fun `deleteVideo returns true when resolver deletes a row`() = kotlinx.coroutines.test.runTest {
        val uri = Uri.parse("content://media/external/video/media/321")
        whenever(mockContentResolver.delete(eq(uri), anyOrNull(), anyOrNull())).thenReturn(1)

        val deleted = storageManager.deleteVideo(uri)

        assertTrue(deleted)
    }
}
