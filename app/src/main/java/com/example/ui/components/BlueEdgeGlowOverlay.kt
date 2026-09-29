package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * Screen-edge electric blue aura animation that pulses when Raphael is actively
 * speaking or listening, satisfying Requirement 4.
 */
@Composable
fun BlueEdgeGlowOverlay(
    isActive: Boolean, // True when Raphael is LISTENING, SPEAKING, or PROCESSING
    soundLevel: Float = 0f,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "ElectricBlueEdgeTransition")

    val pulseIntensity by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "edge_pulse_intensity"
    )

    val electricCyan = Color(0xFF00E5FF)
    val azureBlue = Color(0xFF0088FF)

    val dynamicAlpha = (pulseIntensity + (soundLevel.coerceIn(0f, 10f) / 10f) * 0.25f).coerceIn(0f, 1f)

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val borderGlowThickness = 48.dp.toPx()

            // 1. Top Edge Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        electricCyan.copy(alpha = 0.55f * dynamicAlpha),
                        azureBlue.copy(alpha = 0.25f * dynamicAlpha),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = borderGlowThickness
                ),
                topLeft = Offset(0f, 0f),
                size = Size(width, borderGlowThickness)
            )

            // 2. Bottom Edge Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        azureBlue.copy(alpha = 0.25f * dynamicAlpha),
                        electricCyan.copy(alpha = 0.55f * dynamicAlpha)
                    ),
                    startY = height - borderGlowThickness,
                    endY = height
                ),
                topLeft = Offset(0f, height - borderGlowThickness),
                size = Size(width, borderGlowThickness)
            )

            // 3. Left Edge Gradient
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        electricCyan.copy(alpha = 0.55f * dynamicAlpha),
                        azureBlue.copy(alpha = 0.25f * dynamicAlpha),
                        Color.Transparent
                    ),
                    startX = 0f,
                    endX = borderGlowThickness
                ),
                topLeft = Offset(0f, 0f),
                size = Size(borderGlowThickness, height)
            )

            // 4. Right Edge Gradient
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        azureBlue.copy(alpha = 0.25f * dynamicAlpha),
                        electricCyan.copy(alpha = 0.55f * dynamicAlpha)
                    ),
                    startX = width - borderGlowThickness,
                    endX = width
                ),
                topLeft = Offset(width - borderGlowThickness, 0f),
                size = Size(borderGlowThickness, height)
            )

            // 5. Crisp Corner Matrix Brackets
            val cornerLength = 32.dp.toPx()
            val strokeW = 3.dp.toPx()
            val bracketColor = electricCyan.copy(alpha = 0.85f * dynamicAlpha)

            // Top-Left
            drawLine(bracketColor, Offset(0f, 0f), Offset(cornerLength, 0f), strokeW, StrokeCap.Square)
            drawLine(bracketColor, Offset(0f, 0f), Offset(0f, cornerLength), strokeW, StrokeCap.Square)

            // Top-Right
            drawLine(bracketColor, Offset(width, 0f), Offset(width - cornerLength, 0f), strokeW, StrokeCap.Square)
            drawLine(bracketColor, Offset(width, 0f), Offset(width, cornerLength), strokeW, StrokeCap.Square)

            // Bottom-Left
            drawLine(bracketColor, Offset(0f, height), Offset(cornerLength, height), strokeW, StrokeCap.Square)
            drawLine(bracketColor, Offset(0f, height), Offset(0f, height - cornerLength), strokeW, StrokeCap.Square)

            // Bottom-Right
            drawLine(bracketColor, Offset(width, height), Offset(width - cornerLength, height), strokeW, StrokeCap.Square)
            drawLine(bracketColor, Offset(width, height), Offset(width, height - cornerLength), strokeW, StrokeCap.Square)
        }
    }
}
