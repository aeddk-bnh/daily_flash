package com.dailyflash.core.logging

import android.util.Log

/**
 * Centralized logging utility for execution flow tracking.
 * 
 * Implements workflow-compliant logging categories:
 * - FLOW: State transitions and execution path
 * - ERROR: Errors with state context
 * - RESOURCE: Resource allocation/release
 * - TIMING: Performance metrics
 * 
 * Usage:
 * ```
 * FlowLogger.flow("CameraBinding", "lifecycle=RESUMED")
 * FlowLogger.error("Recording", exception, "state=ACTIVE")
 * FlowLogger.resource("ALLOC", "Camera", "provider=$providerId")
 * FlowLogger.timing("Recording", 1234L)
 * ```
 */
object FlowLogger {
    
    private const val TAG_PREFIX = "DailyFlash"
    
    // Enable/disable logging (can be controlled by BuildConfig)
    var isEnabled = true
    
    /**
     * Log state transition or execution flow.
     * 
     * @param state Current state name
     * @param data Additional context data
     */
    fun flow(state: String, data: String = "") {
        if (!isEnabled) return
        
        val message = if (data.isNotEmpty()) {
            "FLOW: STATE=$state | $data"
        } else {
            "FLOW: STATE=$state"
        }
        
        Log.d("$TAG_PREFIX-FLOW", message)
    }
    
    /**
     * Log error with state context.
     * 
     * @param state Current state when error occurred
     * @param error The exception/error
     * @param context Additional context about the error
     */
    fun error(state: String, error: Throwable, context: String = "") {
        if (!isEnabled) return
        
        val message = buildString {
            append("ERROR: STATE=$state")
            if (context.isNotEmpty()) {
                append(" | $context")
            }
            append(" | ${error.javaClass.simpleName}: ${error.message}")
        }
        
        Log.e("$TAG_PREFIX-ERROR", message, error)
    }
    
    /**
     * Log error message with state context (without exception).
     */
    fun error(state: String, message: String) {
        if (!isEnabled) return
        Log.e("$TAG_PREFIX-ERROR", "ERROR: STATE=$state | $message")
    }
    
    /**
     * Log resource allocation or release.
     * 
     * @param action ALLOC or RELEASE
     * @param resource Resource name
     * @param details Additional resource details
     */
    fun resource(action: String, resource: String, details: String = "") {
        if (!isEnabled) return
        
        val message = if (details.isNotEmpty()) {
            "RESOURCE: $action $resource | $details"
        } else {
            "RESOURCE: $action $resource"
        }
        
        Log.d("$TAG_PREFIX-RESOURCE", message)
    }
    
    /**
     * Log operation timing/performance.
     * 
     * @param operation Operation name
     * @param durationMs Duration in milliseconds
     * @param details Additional performance context
     */
    fun timing(operation: String, durationMs: Long, details: String = "") {
        if (!isEnabled) return
        
        val message = if (details.isNotEmpty()) {
            "TIMING: $operation completed in ${durationMs}ms | $details"
        } else {
            "TIMING: $operation completed in ${durationMs}ms"
        }
        
        Log.i("$TAG_PREFIX-TIMING", message)
    }
    
    /**
     * Helper for timing operations with automatic duration calculation.
     */
    inline fun <T> measureTiming(operation: String, details: String = "", block: () -> T): T {
        val startTime = System.currentTimeMillis()
        return try {
            block()
        } finally {
            val duration = System.currentTimeMillis() - startTime
            timing(operation, duration, details)
        }
    }
}
