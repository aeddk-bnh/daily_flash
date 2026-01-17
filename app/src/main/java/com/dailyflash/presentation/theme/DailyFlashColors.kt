package com.dailyflash.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * DailyFlash color palette.
 * Modern dark theme with vibrant accent colors.
 */
object AppColors {
    // Primary - Indigo gradient
    val Primary = Color(0xFF6366F1)
    val PrimaryVariant = Color(0xFF4F46E5)
    val PrimaryContainer = Color(0xFF312E81)
    val OnPrimary = Color.White
    val OnPrimaryContainer = Color(0xFFE0E7FF)
    
    // Secondary - Pink accent
    val Secondary = Color(0xFFF472B6)
    val SecondaryVariant = Color(0xFFEC4899)
    val SecondaryContainer = Color(0xFF831843)
    val OnSecondary = Color.White
    val OnSecondaryContainer = Color(0xFFFCE7F3)
    
    // Tertiary - Cyan for highlights
    val Tertiary = Color(0xFF22D3EE)
    val TertiaryContainer = Color(0xFF164E63)
    val OnTertiary = Color(0xFF0F172A)
    val OnTertiaryContainer = Color(0xFFCFFAFE)
    
    // Background - Slate dark mode
    val Background = Color(0xFF0F172A)      // Slate 900
    val Surface = Color(0xFF1E293B)         // Slate 800
    val SurfaceVariant = Color(0xFF334155)  // Slate 700
    val SurfaceHigh = Color(0xFF475569)     // Slate 600
    
    // Content
    val OnBackground = Color(0xFFF8FAFC)    // Slate 50
    val OnSurface = Color(0xFFE2E8F0)       // Slate 200
    val OnSurfaceVariant = Color(0xFF94A3B8) // Slate 400
    
    // Status colors
    val Error = Color(0xFFEF4444)
    val ErrorContainer = Color(0xFF7F1D1D)
    val OnError = Color.White
    val OnErrorContainer = Color(0xFFFEE2E2)
    
    val Success = Color(0xFF22C55E)
    val SuccessContainer = Color(0xFF14532D)
    val OnSuccess = Color.White
    val OnSuccessContainer = Color(0xFFDCFCE7)
    
    val Warning = Color(0xFFF59E0B)
    val WarningContainer = Color(0xFF78350F)
    
    // Recording button colors
    val RecordRed = Color(0xFFDC2626)
    val RecordRedLight = Color(0xFFF87171)
    val RecordRing = Color(0xFFFAFAFA)
    
    // Calendar specific
    val TodayIndicator = Color(0xFF6366F1)
    val SelectedDay = Color(0xFF4F46E5)
    val HasVideoIndicator = Color(0xFF22C55E)
    
    // Overlay
    val Scrim = Color(0x99000000)
    val GlassSurface = Color(0x661E293B)
    val GlassBorder = Color(0x33FFFFFF)
    val CardGradientStart = Color(0xFF1E293B)
    val CardGradientEnd = Color(0xFF0F172A)
}
