package com.dailyflash.core.media

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dailyflash.core.storage.IStorageManager
import com.dailyflash.core.storage.VideoFile
import com.dailyflash.domain.ExportOptions
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class MediaProcessorIntegrationTest {

    private lateinit var processor: MediaProcessor
    private lateinit var context: Context

    private class FakeStorageManager : IStorageManager {
        override suspend fun saveVideo(data: ByteArray, date: LocalDate): Uri = Uri.EMPTY
        override suspend fun getVideosByDate(date: LocalDate): List<VideoFile> = emptyList()
        override suspend fun getVideosByRange(start: LocalDate, end: LocalDate): List<VideoFile> = emptyList()
        override suspend fun deleteVideo(uri: Uri): Boolean = true
        override suspend fun exportVideoToGallery(videoFile: File): Uri = Uri.EMPTY
        override suspend fun getAllVideos(): List<VideoFile> = emptyList()
        override fun createTempFile(prefix: String, suffix: String): File =
            File.createTempFile(prefix, suffix)
        override suspend fun deleteVideosOlderThan(date: LocalDate): Int = 0
        override fun getStorageLocationDescription(): String = "test"
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        processor = MediaProcessor(context, FakeStorageManager())
    }

    @Test
    fun stitch_emptyClips_returnsFailure() = runBlocking {
        val output = Uri.fromFile(File(context.cacheDir, "out_${System.currentTimeMillis()}.mp4"))
        val result = processor.stitchVideos(
            clips = emptyList(),
            outputUri = output,
            audioTrack = null,
            options = ExportOptions(),
            onProgress = {}
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun cancel_doesNotCrash() {
        processor.cancel()
        assertTrue(true)
    }
}
