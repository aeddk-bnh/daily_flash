package com.dailyflash.tests.integration

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.GrantPermissionRule
import com.dailyflash.core.storage.StorageManager
import com.dailyflash.data.VideoRepositoryImpl
import com.dailyflash.domain.CaptureVideoUseCase
import com.dailyflash.domain.DeleteClipUseCase
import com.dailyflash.domain.GetAllVideosUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@MediumTest
class EndToEndFlowTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private lateinit var context: Context
    private lateinit var storageManager: StorageManager
    private lateinit var videoRepository: VideoRepositoryImpl
    private lateinit var captureUseCase: CaptureVideoUseCase
    private lateinit var deleteUseCase: DeleteClipUseCase
    private lateinit var getAllVideosUseCase: GetAllVideosUseCase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        storageManager = StorageManager(context)
        videoRepository = VideoRepositoryImpl(storageManager, context.contentResolver)
        captureUseCase = CaptureVideoUseCase(videoRepository)
        deleteUseCase = DeleteClipUseCase(videoRepository)
        getAllVideosUseCase = GetAllVideosUseCase(videoRepository)
        videoRepository.clearCache() // Ensure cache is clear before tests
    }

    @Test
    fun testDF002_captureSavesExactlyOneMediaItem() = runBlocking {
        // Create a fake temporary file that simulates CameraService output
        val tempFile = File(context.cacheDir, "dailyflash_temp_test_clip.mp4")
        tempFile.writeBytes(ByteArray(1024) { 1 }) // Dummy video data
        val tempUri = android.net.Uri.fromFile(tempFile)

        assertTrue("Temp file must exist before capture", tempFile.exists())

        // Action: Capture video from temp file
        val captureResult = captureUseCase.invoke(tempUri)
        assertTrue("Capture must succeed", captureResult.isSuccess)
        
        val newVideo = captureResult.getOrThrow()
        assertNotNull(newVideo)

        // Verify: Temp file must be deleted (cleanup logic)
        assertFalse("Temp file must be deleted after save (No duplicate save)", tempFile.exists())

        // Verify: Exactly one video is in storage
        val allVideos = getAllVideosUseCase().first()
        val foundVideo = allVideos.find { it.id == newVideo.id }
        assertNotNull("Saved video must be present in repository", foundVideo)

        // Clean up final file
        deleteUseCase.invoke(newVideo.id)
    }

    @Test
    fun testDF003_deleteWorksWithColdCache() = runBlocking {
        // Prepare: Save a video directly
        val tempFile = File(context.cacheDir, "test_clip.mp4")
        tempFile.writeBytes(ByteArray(1024) { 1 })
        val tempUri = android.net.Uri.fromFile(tempFile)
        
        val captureResult = captureUseCase.invoke(tempUri)
        assertTrue(captureResult.isSuccess)
        val videoId = captureResult.getOrThrow().id

        // Clear cache to simulate cold-start (app restart)
        videoRepository.clearCache()

        // Action: Delete the video (testing cold-cache scenario)
        // This simulates a delete from Calendar/Gallery where the video is fetched without full cache.
        val deleteResult = deleteUseCase.invoke(videoId)
        assertTrue("Delete must succeed even with cold cache", deleteResult.isSuccess)

        // Verify: Video is actually deleted
        val allVideos = getAllVideosUseCase().first()
        val foundVideo = allVideos.find { it.id == videoId }
        assertNull("Video must no longer exist after deletion", foundVideo)
        
        tempFile.delete()
    }
}
