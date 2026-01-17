package com.dailyflash.core.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

class DateOrganizerTest {

    @Test
    fun `getPathForDate returns correct directory structure`() {
        val baseDir = File("/test/base")
        val date = LocalDate.of(2026, 1, 16)
        
        val result = DateOrganizer.getPathForDate(baseDir, date)
        
        // Use File.separator to be OS agnostic in verification, 
        // effectively checking if it ends with "base/2026/01/16"
        assertTrue(result.absolutePath.endsWith("2026${File.separator}01${File.separator}16"))
    }

    @Test
    fun `generateFilename returns correct format`() {
        val date = LocalDate.of(2026, 1, 16)
        val filename = DateOrganizer.generateFilename(date)
        
        assertTrue(filename.startsWith("dailyflash_"))
        assertTrue(filename.endsWith(".mp4"))
    }

    @Test
    fun `parseDateFromPath returns correct date for valid path`() {
        val path = "/storage/emulated/0/Movies/DailyFlash/2026/01/16/video.mp4"
        val expected = LocalDate.of(2026, 1, 16)
        
        val result = DateOrganizer.parseDateFromPath(path)
        
        assertEquals(expected, result)
    }

    @Test
    fun `parseDateFromPath returns null for invalid path`() {
        val path = "/storage/emulated/0/Movies/DailyFlash/invalid/path/video.mp4"
        
        val result = DateOrganizer.parseDateFromPath(path)
        
        assertEquals(null, result)
    }
}
