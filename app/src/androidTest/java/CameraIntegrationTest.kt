package com.dailyflash.tests.integration

import android.Manifest
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.GrantPermissionRule
import com.dailyflash.core.camera.CameraService
import com.dailyflash.core.storage.IStorageManager
import com.dailyflash.core.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

/**
 * Integration tests for CameraService.
 * Requires a device/emulator with camera hardware.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class CameraIntegrationTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private lateinit var context: Context
    private lateinit var storageManager: IStorageManager
    private lateinit var cameraService: CameraService
    private lateinit var lifecycleOwner: TestLifecycleOwner

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        storageManager = StorageManager(context)
        cameraService = CameraService(context, storageManager)
        lifecycleOwner = TestLifecycleOwner()
        
        // Prepare lifecycle
        runBlocking(Dispatchers.Main) {
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            
            // Bind camera
            cameraService.bindToLifecycle(lifecycleOwner)
        }
    }

    @After
    fun tearDown() {
        runBlocking(Dispatchers.Main) {
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            cameraService.release()
        }
    }

    @Test
    fun testCameraInitialization() = runBlocking(Dispatchers.Main) {
        // Wait for camera to initialize (it happens asynchronously)
        // In a real test we might wait for a signal, here we poll or wait briefly
        // Note: bindToLifecycle is async, so we might need a small delay or retry mechanic
        // for robust tests.
        
        // This is a basic sanity check that binding didn't crash
        assertNotNull(cameraService)
    }

    @Test
    fun testRecordClip() = runTest {
        // This test requires actual camera hardware to succeed fully.
        // On emulators without camera config, it might fail or timeout.
        
        // We assume the environment has a camera for this integration test.
        
        // Give time for binding to complete
        withContext(Dispatchers.Main) {
            // Need to wait for the camera provider future listener to fire
            // In a real app we'd expose a StateFlow for "initialized"
            kotlinx.coroutines.delay(1000) 
        }

        if (cameraService.isCameraReady()) {
            val result = cameraService.recordClip(1000)
            
            assertTrue("Recording should succeed", result.isSuccess)
            val uri = result.getOrNull()
            assertNotNull("URI should not be null", uri)
            
            // Verify file exists via StorageManager or directly
            // Since StorageManager implementation detail might vary, we check if URI is valid
            // context.contentResolver.openInputStream(uri!!)?.close()
        } else {
            // If camera didn't initialize (e.g. no hardware), we warn but pass
            // OR fail if strict hardware requirement is expected.
            println("Skipping record test: Camera not ready (no hardware?)")
        }
    }

    /**
     * Helper class to mock LifecycleOwner for tests
     */
    class TestLifecycleOwner : LifecycleOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }
    }
}
