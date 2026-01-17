package com.dailyflash.domain

data class ExportOptions(
    val includeDateOverlay: Boolean = false,
    val fadeAudio: Boolean = false,
    val dateText: String? = null // e.g. "Jan 2024"
)
