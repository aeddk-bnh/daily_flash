package com.dailyflash.core.media

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dailyflash.core.storage.StorageManager
import com.dailyflash.domain.ExportOptions
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MediaProcessorInstrumentedTest {

    private lateinit var mediaProcessor: MediaProcessor
    private lateinit var context: android.content.Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mediaProcessor = MediaProcessor(context, StorageManager(context))
    }

    @After
    fun teardown() {
        mediaProcessor.cancel()
    }

    @Test
    fun stitch_emptyClips_failsFast() = runBlocking {
        val outputFile = File(context.cacheDir, "media_test_${System.currentTimeMillis()}.mp4")
        val result = mediaProcessor.stitchVideos(
            clips = emptyList(),
            outputUri = Uri.fromFile(outputFile),
            audioTrack = null,
            options = ExportOptions(),
            onProgress = {}
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun cancel_isSafe() {
        mediaProcessor.cancel()
        assertTrue(true)
    }
}
