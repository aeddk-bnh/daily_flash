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
import com.dailyflash.core.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class CameraIntegrationTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun bindPreviewAndRelease_smoke() = runBlocking(Dispatchers.Main) {
        val storageManager = StorageManager(context)
        val cameraService = CameraService(context, storageManager)
        val lifecycleOwner = TestLifecycleOwner()

        lifecycleOwner.handle(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handle(Lifecycle.Event.ON_START)
        lifecycleOwner.handle(Lifecycle.Event.ON_RESUME)

        cameraService.bindToLifecycle(lifecycleOwner)
        delay(300)

        assertNotNull(cameraService.getPreviewView())

        cameraService.release()
        assertFalse(cameraService.isRecording())
    }

    private class TestLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle
            get() = registry

        fun handle(event: Lifecycle.Event) {
            registry.handleLifecycleEvent(event)
        }
    }
}
