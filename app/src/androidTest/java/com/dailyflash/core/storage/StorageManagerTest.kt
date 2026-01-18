package com.dailyflash.core.storage

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class StorageManagerTest {

    private lateinit var storageManager: StorageManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        storageManager = StorageManager(context)
    }

    @Test
    fun testSaveVideo() = runBlocking {
        val dummyData = ByteArray(1024) { 1 } // Non-empty
        val date = LocalDate.now()
        
        try {
            val uri = storageManager.saveVideo(dummyData, date)
            assertNotNull("URI should not be null", uri)
            
            // Verify file exists (indirectly via delete)
            val deleted = storageManager.deleteVideo(uri)
            assertTrue("Should be able to delete the saved video", deleted)
            
        } catch (e: Exception) {
            e.printStackTrace()
            fail("Failed to save video: ${e.message}")
        }
    }
}
