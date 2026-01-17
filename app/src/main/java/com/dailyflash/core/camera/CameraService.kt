package com.dailyflash.core.camera

import android.content.Context
import android.os.Build
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.dailyflash.core.storage.IStorageManager
import android.os.Handler
import android.os.Looper
import com.dailyflash.core.logging.FlowLogger
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.lang.Runnable
import java.time.LocalDate
import kotlin.coroutines.resume

/**
 * CameraX-based camera service implementation.
 * Handles camera lifecycle and fixed-duration recording.
 */
class CameraService(
    private val context: Context,
    private val storageManager: IStorageManager
) : ICameraService {

    companion object {
        private const val TAG = "CameraService"
    }

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewView: PreviewView? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var preview: Preview? = null
    private var activeRecording: Recording? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var camera: androidx.camera.core.Camera? = null
    private var currentCameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    /**
     * Bind camera to lifecycle owner.
     * Initializes CameraX, creates Preview and VideoCapture use cases,
     * and binds them to the provided lifecycle.
     *
     * @param owner Activity or Fragment lifecycle
     */
    override fun bindToLifecycle(owner: LifecycleOwner) {
        FlowLogger.flow("CameraBinding", "lifecycle=${owner.lifecycle.currentState}")
        lifecycleOwner = owner

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                // 1. Get ProcessCameraProvider instance
                cameraProvider = cameraProviderFuture.get()
                FlowLogger.resource("ALLOC", "ProcessCameraProvider", "provider=${cameraProvider.hashCode()}")

                // 2. Create Preview use case
                preview = Preview.Builder()
                    .build()
                    .also { previewUseCase ->
                        previewView?.let { view ->
                            previewUseCase.setSurfaceProvider(view.surfaceProvider)
                        }
                    }

                // 3. Create VideoCapture use case with Recorder
                // 3. Create VideoCapture use case with Recorder
                val qualitySelector = if (isEmulator()) {
                    QualitySelector.from(Quality.LOWEST)
                } else {
                    when (CameraConfig.VIDEO_QUALITY) {
                        "FHD" -> QualitySelector.from(Quality.FHD)
                        "UHD" -> QualitySelector.from(Quality.UHD)
                        else -> QualitySelector.from(Quality.HD)
                    }
                }

                val recorder = Recorder.Builder()
                    .setQualitySelector(qualitySelector)
                    .build()

                videoCapture = VideoCapture.withOutput(recorder)

                // 4. Use current camera selector
                val cameraSelector = currentCameraSelector

                // Unbind any existing use cases before rebinding
                cameraProvider?.unbindAll()

                // 5. Bind to lifecycle with both use cases
                camera = cameraProvider?.bindToLifecycle(
                    owner,
                    cameraSelector,
                    preview,
                    videoCapture
                )

                Log.d(TAG, "Camera bound to lifecycle successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind camera to lifecycle", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Get preview view for camera feed.
     * Creates a new PreviewView if not already created.
     *
     * @return PreviewView to embed in Compose
     */
    override fun getPreviewView(): PreviewView {
        if (previewView == null) {
            previewView = PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            // Connect preview to PreviewView if already initialized
            preview?.setSurfaceProvider(previewView?.surfaceProvider)
        }

        return previewView!!
    }

    /**
     * Record a video clip of specified duration.
     * Creates a temporary file, records for the specified duration,
     * then saves to storage via StorageManager.
     *
     * @param durationMs Recording duration in milliseconds (default 1000ms)
     * @return Result with URI of recorded video or error
     */
    override suspend fun recordClip(durationMs: Long): Result<Uri> {
        FlowLogger.flow("RecordingStart", "requestedDuration=${durationMs}ms")
        
        // 1. Check if videoCapture is ready
        val capture = videoCapture
        if (capture == null) {
            FlowLogger.error("RecordingStart", "VideoCapture not initialized")
            return Result.failure(
                IllegalStateException("VideoCapture not initialized. Call bindToLifecycle first.")
            )
        }

        // Check camera permission
        val permissionCheck = ContextCompat.checkSelfPermission(
            context, 
            android.Manifest.permission.CAMERA
        )
        if (permissionCheck != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            FlowLogger.error("RecordingStart", "Camera permission not granted")
            return Result.failure(
                SecurityException("Camera permission not granted")
            )
        }

        // Limit duration to max allowed
        val safeDuration = durationMs.coerceIn(
            CameraConfig.DEFAULT_CLIP_DURATION_MS,
            CameraConfig.MAX_RECORDING_DURATION_MS
        )

        return try {
            // 2. Create OutputOptions with temp file
            val tempFile = File.createTempFile(
                "dailyflash_temp_",
                ".mp4",
                context.cacheDir
            )
            FlowLogger.resource("ALLOC", "TempFile", "path=${tempFile.path}")

            val outputOptions = FileOutputOptions.Builder(tempFile).build()

            // 3. Start recording with prepareRecording()
            val recordingStartTime = System.currentTimeMillis()
            val recordingResult = suspendCancellableCoroutine<Result<File>> { continuation ->
                val stopHandler = Handler(Looper.getMainLooper())
                var hasCompleted = false
                
                activeRecording = capture.output
                    .prepareRecording(context, outputOptions)
                    .start(ContextCompat.getMainExecutor(context)) { event ->
                        when (event) {
                            is VideoRecordEvent.Finalize -> {
                                hasCompleted = true
                                if (event.hasError()) {
                                    Log.e(TAG, "Recording failed: ${event.error}")
                                    if (continuation.isActive) {
                                        continuation.resume(
                                            Result.failure(
                                                RuntimeException("Recording failed with error: ${event.error}")
                                            )
                                        )
                                    }
                                } else {
                                    Log.d(TAG, "Recording completed successfully")
                                    if (continuation.isActive) {
                                        continuation.resume(Result.success(tempFile))
                                    }
                                }
                            }
                            is VideoRecordEvent.Start -> {
                                Log.d(TAG, "Recording started")
                            }
                            is VideoRecordEvent.Status -> {
                                Log.v(TAG, "Recording status: ${event.recordingStats}")
                            }
                        }
                    }

                // 4. Use Handler to schedule stop after duration (instead of GlobalScope)
                val stopRunnable = Runnable {
                    if (!hasCompleted) {
                        // 5. Stop recording
                        activeRecording?.stop()
                        activeRecording = null
                    }
                }
                stopHandler.postDelayed(stopRunnable, safeDuration)

                continuation.invokeOnCancellation {
                    stopHandler.removeCallbacks(stopRunnable)
                    activeRecording?.stop()
                    activeRecording = null
                    tempFile.delete()
                }
            }

            // 6. Move temp file to storage via StorageManager
            recordingResult.fold(
                onSuccess = { file ->
                    FlowLogger.flow("RecordingComplete", "file=${file.length()} bytes")
                    
                    val videoBytes = file.readBytes()
                    file.delete() // Clean up temp file
                    FlowLogger.resource("RELEASE", "TempFile", "deleted=${file.path}")

                    // 7. Return Result.success(uri)
                    val savedUri = storageManager.saveVideo(videoBytes, LocalDate.now())
                    
                    val totalDuration = System.currentTimeMillis() - recordingStartTime
                    FlowLogger.timing("RecordClip", totalDuration, "uri=$savedUri, size=${videoBytes.size}")
                    FlowLogger.flow("RecordingSaved", "uri=$savedUri")
                    
                    Result.success(savedUri)
                },
                onFailure = { error ->
                    FlowLogger.error("Recording", error, "state=FAILED")
                    tempFile.delete()
                    FlowLogger.resource("RELEASE", "TempFile", "deleted after error")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            FlowLogger.error("Recording", e, "state=EXCEPTION")
            Log.e(TAG, "Error during recording", e)
            Result.failure(e)
        }
    }

    /**
     * Release camera resources.
     * Unbinds all use cases and cleans up resources.
     */
    override fun release() {
        // Stop any active recording
        activeRecording?.stop()
        activeRecording = null

        // Unbind camera use cases
        cameraProvider?.unbindAll()
        cameraProvider = null

        // Clear references
        preview = null
        videoCapture = null
        previewView = null
        lifecycleOwner = null

        Log.d(TAG, "Camera resources released")
    }

    override fun toggleTorch(enabled: Boolean) {
        val provider = cameraProvider ?: return
        // We need to access the camera object to control torch
        // Camera is bound in bindToLifecycle, but we don't store it.
        // Let's modify bindToLifecycle to store the camera object or re-bind.
        // Better: store the camera object.
        camera?.cameraControl?.enableTorch(enabled)
    }

    override fun switchCamera() {
        currentCameraSelector = if (currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        
        // Re-bind if we have a lifecycle owner
        lifecycleOwner?.let { owner ->
            bindToLifecycle(owner)
        }
    }

    /**
     * Check if camera is currently bound and ready.
     * @return true if camera is initialized and bound
     */
    fun isCameraReady(): Boolean {
        return cameraProvider != null && videoCapture != null
    }

    /**
     * Check if currently recording.
     * @return true if a recording is in progress
     */
    fun isRecording(): Boolean {
        return activeRecording != null
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK")
    }
}
