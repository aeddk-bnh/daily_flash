package com.dailyflash.presentation.components

import com.dailyflash.presentation.theme.AppColors
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Animated record button component.
 * Shows recording state with pulsing animation.
 *
 * @param isRecording Whether recording is in progress
 * @param showSuccess Whether to show success state
 * @param enabled Whether button is clickable
 * @param onClick Callback when button is clicked
 */
@Composable
fun RecordButton(
    isRecording: Boolean,
    showSuccess: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    
    // Pulse animation for recording state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    // Inner circle color animation
    val innerColor by animateColorAsState(
        targetValue = when {
            showSuccess -> AppColors.Success
            isRecording -> AppColors.RecordRed
            else -> AppColors.RecordRed
        },
        animationSpec = tween(300),
        label = "innerColor"
    )
    
    // Ring color animation
    val ringColor by animateColorAsState(
        targetValue = when {
            showSuccess -> AppColors.Success
            isRecording -> AppColors.RecordRedLight
            else -> AppColors.RecordRing
        },
        animationSpec = tween(300),
        label = "ringColor"
    )
    
    // Apply scale only when recording
    val currentScale = if (isRecording) pulseScale else 1f
    
    Box(
        modifier = modifier
            .size(80.dp)
            .scale(currentScale)
            .semantics {
                role = Role.Button
                contentDescription = when {
                    isRecording -> "Recording in progress"
                    showSuccess -> "Recording successful"
                    else -> "Tap to record"
                }
            }
            .clip(CircleShape)
            .background(Color.Transparent)
            .border(4.dp, ringColor, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true, radius = 40.dp),
                enabled = enabled && !isRecording,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner recording circle
        Box(
            modifier = Modifier
                .size(if (isRecording) 24.dp else 56.dp)
                .clip(if (isRecording) {
                    androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                } else {
                    CircleShape
                })
                .background(innerColor)
        )
    }
}
