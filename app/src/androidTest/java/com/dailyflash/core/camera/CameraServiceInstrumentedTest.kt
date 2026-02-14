package com.dailyflash.core.camera

import android.Manifest
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.dailyflash.core.storage.StorageManager
import com.dailyflash.presentation.MainActivity
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraServiceInstrumentedTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var cameraService: CameraService

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        cameraService = CameraService(context, StorageManager(context))
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun teardown() {
        cameraService.release()
        scenario.close()
    }

    @Test
    fun previewView_canBeCreated_afterBind() {
        scenario.onActivity { activity ->
            cameraService.bindToLifecycle(activity)
            assertNotNull(cameraService.getPreviewView())
        }
    }

    @Test
    fun release_stopsRecordingState() {
        cameraService.release()
        assertFalse(cameraService.isRecording())
    }
}
