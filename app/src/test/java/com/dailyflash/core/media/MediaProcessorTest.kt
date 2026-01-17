package com.dailyflash.core.media

import android.content.Context
import android.net.Uri
import androidx.media3.transformer.Transformer
import androidx.test.core.app.ApplicationProvider
import com.dailyflash.core.storage.IStorageManager
import com.dailyflash.domain.ExportOptions
import com.dailyflash.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class MediaProcessorTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context

    @Mock
    private lateinit var storageManager: IStorageManager
    
    private lateinit var mediaProcessor: MediaProcessor

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
        mediaProcessor = MediaProcessor(context, storageManager)
    }

    @Test
    fun `stitchVideos returns failure if clips are empty`() = runTest {
        val outputUri = mock(Uri::class.java)
        val result = mediaProcessor.stitchVideos(
            clips = emptyList(),
            outputUri = outputUri,
            audioTrack = null, // Assuming audioTrack can be null for this test case
            options = ExportOptions()
        ) {
            // progress
        }
        assertTrue(result.isFailure)
    }
}
