package com.dailyflash.core.camera

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.dailyflash.MainActivity
import com.dailyflash.core.storage.IStorageManager
import com.dailyflash.core.storage.StorageManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

/**
 * Instrumented tests for CameraService.
 * 
 * These tests run on actual Android devices/emulators and validate:
 * - TC-CAP-001: Successful 1-second video capture
 * - TC-CAP-002: Camera permission handling
 * - TC-CAP-005: Rapid consecutive recording
 * - TC-CAP-006: Recording during interruption
 * - TC-CAP-007: Camera switching
 * 
 * Prerequisites:
 * - Run on device/emulator with camera hardware
 * - Permissions granted via GrantPermissionRule
 * - Sufficient storage available
 */
@RunWith(AndroidJUnit4::class)
class CameraServiceInstrumentedTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private lateinit var context: Context
    private lateinit var storageManager: IStorageManager
    private lateinit var cameraService: CameraService
    private lateinit var activityScenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        storageManager = StorageManager(context)
        cameraService = CameraService(context, storageManager)
        
        // Launch activity for lifecycle
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        cameraService.release()
        activityScenario.close()
        
        // Cleanup test videos
        cleanupTestVideos()
    }

    // ==================== TC-CAP-001: Successful 1-Second Recording ====================

    @Test
    fun testSuccessful1SecondRecording() = runBlocking {
        // PRECONDITION: Camera permission granted (via GrantPermissionRule)
        // PRECONDITION: Activity lifecycle active

        var recordedUri: Uri? = null

        activityScenario.onActivity { activity ->
            runBlocking {
                // Bind camera to activity lifecycle
                cameraService.bindToLifecycle(activity)
                
                // Wait for camera initialization
                Thread.sleep(1000)

                // ACTION: Record 1-second clip
                val startTime = System.currentTimeMillis()
                val result = cameraService.recordClip(durationMs = 1000)
                val duration = System.currentTimeMillis() - startTime

                // ASSERTION: Recording succeeded
                assertTrue("Recording should succeed", result.isSuccess)
                
                recordedUri = result.getOrNull()
                assertNotNull("URI should be returned", recordedUri)

                // ASSERTION: Duration approximately 1 second (±500ms tolerance)
                assertTrue(
                    "Duration should be ~1000ms, was ${duration}ms",
                    duration in 800..1500
                )
            }
        }

        // ASSERTION: Video file exists and is valid
        recordedUri?.let { uri ->
            val file = File(uri.path!!)
            assertTrue("Video file should exist", file.exists())
            assertTrue("Video file should have content", file.length() > 0)
            
            // ASSERTION: File saved in correct date folder
            val today = LocalDate.now()
            val expectedPath = "${today.year}/${String.format("%02d", today.monthValue)}/${String.format("%02d", today.dayOfMonth)}"
            assertTrue(
                "File should be in today's folder",
                uri.path!!.contains(expectedPath)
            )
        }
    }

    // ==================== TC-CAP-002: Permission Handling ====================

    @Test
    fun testCameraInitializationWithPermission() {
        activityScenario.onActivity { activity ->
            // PRECONDITION: Permission granted

            // ACTION: Bind camera
            cameraService.bindToLifecycle(activity)
            
            // Wait for initialization
            Thread.sleep(500)

            // ASSERTION: Camera ready
            assertTrue("Camera should be ready with permission", cameraService.isCameraReady())
        }
    }

    // ==================== TC-CAP-005: Rapid Consecutive Recording ====================

    @Test
    fun testRapidConsecutiveRecordings() = runBlocking {
        val recordedUris = mutableListOf<Uri>()

        activityScenario.onActivity { activity ->
            runBlocking {
                cameraService.bindToLifecycle(activity)
                Thread.sleep(1000) // Wait for camera ready

                // ACTION: Record 5 clips consecutively
                val startTime = System.currentTimeMillis()
                
                repeat(5) { index ->
                    val result = cameraService.recordClip(1000)
                    
                    // ASSERTION: Each recording succeeds
                    assertTrue("Recording $index should succeed", result.isSuccess)
                    
                    result.getOrNull()?.let { recordedUris.add(it) }
                    
                    // Small delay between recordings (realistic)
                    if (index < 4) Thread.sleep(200)
                }

                val totalTime = System.currentTimeMillis() - startTime

                // ASSERTION: All 5 recordings completed
                assertEquals("Should have 5 recordings", 5, recordedUris.size)

                // ASSERTION: All URIs are unique
                assertEquals(
                    "All URIs should be unique",
                    5,
                    recordedUris.distinct().size
                )

                // ASSERTION: Camera still responsive
                assertTrue("Camera should still be ready", cameraService.isCameraReady())
                assertFalse("Should not be recording after completion", cameraService.isRecording())

                // ASSERTION: Total time reasonable
                assertTrue(
                    "Total time should be reasonable (< 15s), was ${totalTime}ms",
                    totalTime < 15000
                )
            }
        }

        // ASSERTION: All files exist
        recordedUris.forEach { uri ->
            val file = File(uri.path!!)
            assertTrue("File ${uri.path} should exist", file.exists())
            assertTrue("File should have content", file.length() > 0)
        }
    }

    // ==================== TC-CAP-006: Recording Interruption ====================

    @Test
    fun testRecordingInterruptionViaLifecycle() = runBlocking {
        activityScenario.onActivity { activity ->
            runBlocking {
                cameraService.bindToLifecycle(activity)
                Thread.sleep(1000)

                // Start a longer recording
                val recordingJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    cameraService.recordClip(5000) // 5-second recording
                }

                // Let recording start
                Thread.sleep(500)

                // ASSERTION: Recording active
                assertTrue("Should be recording", cameraService.isRecording())

                // SIMULATE INTERRUPTION: Move activity to background
                activityScenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)

                // Cancel recording
                recordingJob.cancel()

                // Wait a bit
                Thread.sleep(200)

                // ASSERTION: Recording stopped
                assertFalse("Should not be recording after interruption", cameraService.isRecording())
            }
        }
    }

    // ==================== TC-CAP-007: Camera Validation ====================

    @Test
    fun testCameraPreviewViewCreation() {
        activityScenario.onActivity { activity ->
            cameraService.bindToLifecycle(activity)

            // ACTION: Get preview view
            val previewView = cameraService.getPreviewView()

            // ASSERTION: Preview view created
            assertNotNull("Preview view should be created", previewView)
            
            // ASSERTION: Preview view has surface provider
            assertNotNull("Preview view should have surface provider", previewView.surfaceProvider)
        }
    }

    // ==================== Helper Methods ====================

    private fun cleanupTestVideos() {
        try {
            val moviesDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES)
            val baseDir = File(moviesDir, "DailyFlash")
            if (baseDir.exists()) {
                // Keep directory structure, just delete test files from today
                val today = LocalDate.now()
                val todayDir = File(
                    baseDir,
                    "${today.year}/${String.format("%02d", today.monthValue)}/${String.format("%02d", today.dayOfMonth)}"
                )
                todayDir.listFiles()?.forEach { it.delete() }
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    // ==================== Performance Validation ====================

    @Test
    fun testRecordingPerformance() = runBlocking {
        activityScenario.onActivity { activity ->
            runBlocking {
                cameraService.bindToLifecycle(activity)
                Thread.sleep(1000)

                val times = mutableListOf<Long>()

                // Record 3 clips and measure time
                repeat(3) {
                    val start = System.currentTimeMillis()
                    val result = cameraService.recordClip(1000)
                    val elapsed = System.currentTimeMillis() - start
                    
                    assertTrue("Recording should succeed", result.isSuccess)
                    times.add(elapsed)
                    
                    Thread.sleep(100)
                }

                // ASSERTION: Average recording time acceptable
                val avgTime = times.average()
                assertTrue(
                    "Average recording time should be reasonable (< 2s), was ${avgTime}ms",
                    avgTime < 2000
                )
            }
        }
    }

    // ==================== Resource Cleanup Validation ====================

    @Test
    fun testProperResourceCleanup() {
        activityScenario.onActivity activity@{ activity ->
            // Bind camera
            cameraService.bindToLifecycle(activity)
            Thread.sleep(500)
            
            assertTrue("Camera should be ready", cameraService.isCameraReady())

            // Release camera
            cameraService.release()

            // ASSERTION: Cleanup successful
            assertFalse("Camera should not be ready after release", cameraService.isCameraReady())
            assertFalse("Should not be recording after release", cameraService.isRecording())

            // ASSERTION: Can re-bind after release
            cameraService.bindToLifecycle(activity)
            Thread.sleep(500)
            
            // Should work again
            assertNotNull("Should be able to get preview view after re-bind", cameraService.getPreviewView())
        }
    }
}
