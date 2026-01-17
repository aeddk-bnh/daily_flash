package com.dailyflash.core.camera

import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

/**
 * Interface for camera operations.
 * Handles CameraX lifecycle and 1-second video recording.
 */

interface ICameraService {
    /**
     * Bind camera to lifecycle owner.
     * @param owner Activity or Fragment lifecycle
     */
    fun bindToLifecycle(owner: LifecycleOwner)
    
    /**
     * Get preview view for camera feed.
     * @return PreviewView to embed in Compose
     */
    fun getPreviewView(): PreviewView
    
    /**
     * Record a video clip of specified duration.
     * @param durationMs Recording duration in milliseconds (default 1000ms)
     * @return Result with URI of recorded video or error
     */
    suspend fun recordClip(durationMs: Long = 1000): Result<Uri>
    
    /**
     * Release camera resources.
     */
    fun release()

    /**
     * Toggle camera torch/flash.
     * @param enabled True to turn on, false to turn off
     */
    fun toggleTorch(enabled: Boolean)
}
