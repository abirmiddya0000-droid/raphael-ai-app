package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LuxNeonCrimson
import com.example.ui.theme.LuxNeonPinkGlow
import com.example.ui.theme.LuxNeonRed

/**
 * Cyberpunk AI Voice Orb with animated pulse rings, diamond glyph core,
 * and bouncing equalizer audio dots. Matches the requested HTML/CSS specification.
 */
@Composable
fun CyberVisualizerOrb(
    isActive: Boolean,
    audioAmplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_orb_pulse")

    // Pulse Ring 1 Animation
    val ring1Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_1"
    )

    // Pulse Ring 2 Animation (700ms offset)
    val ring2Progress by infiniteTransition.animateFloat(
        initialValue = 0.32f,
        targetValue = 1.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_2"
    )

    // Breathing core animation
    val coreGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_glow"
    )

    // Modulation from incoming voice audio decibels
    val normalizedAudio = (audioAmplitude.coerceIn(0f, 16f) / 16f)

    Column(
        modifier = modifier.testTag("cyber_visualizer_orb"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Orb Center Container
        Box(
            modifier = Modifier
                .size(160.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 80.dp),
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Pulse Ring 1
            val progress1 = ring1Progress % 1f
            val scale1 = 0.65f + progress1 * 0.95f
            val alpha1 = (1f - progress1) * (if (isActive) 0.95f else 0.45f)
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale1)
                    .border(2.dp, LuxNeonCrimson.copy(alpha = alpha1), CircleShape)
            )

            // Pulse Ring 2
            val progress2 = ring2Progress % 1f
            val scale2 = 0.65f + progress2 * 0.95f
            val alpha2 = (1f - progress2) * (if (isActive) 0.85f else 0.35f)
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale2)
                    .border(2.dp, LuxNeonRed.copy(alpha = alpha2), CircleShape)
            )

            // Soft radial glow aura
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(coreGlow + normalizedAudio * 0.25f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                LuxNeonRed.copy(alpha = if (isActive) 0.45f else 0.20f),
                                LuxNeonCrimson.copy(alpha = if (isActive) 0.20f else 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Center Diamond Glyph (Upward light pink triangle + Downward crimson triangle)
            DiamondGlyph(
                scale = if (isActive) (1.05f + normalizedAudio * 0.2f) else 1.0f
            )
        }

        Spacer(modifier = Modifier.height(34.dp))

        // Equalizer Bar Dots (8 dots with bounce animation)
        EqualizerDotsBar(
            isActive = isActive,
            audioBoost = normalizedAudio
        )
    }
}

/**
 * Center Diamond Glyph drawn with two opposing triangles.
 */
@Composable
private fun DiamondGlyph(
    scale: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(54.dp, 64.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f

            val halfBase = 24.dp.toPx()
            val triHeight = 28.dp.toPx()

            // Diamond Top: Upward triangle in soft pink-white (#ffcad2)
            val topPath = Path().apply {
                moveTo(centerX, centerY - 2.dp.toPx() - triHeight)
                lineTo(centerX + halfBase, centerY - 2.dp.toPx())
                lineTo(centerX - halfBase, centerY - 2.dp.toPx())
                close()
            }
            drawPath(
                path = topPath,
                color = LuxNeonPinkGlow,
                style = Fill
            )

            // Diamond Bottom: Downward triangle in intense crimson (#ff0033)
            val bottomPath = Path().apply {
                moveTo(centerX - halfBase, centerY + 2.dp.toPx())
                lineTo(centerX + halfBase, centerY + 2.dp.toPx())
                lineTo(centerX, centerY + 2.dp.toPx() + triHeight)
                close()
            }
            drawPath(
                path = bottomPath,
                color = LuxNeonRed,
                style = Fill
            )
        }
    }
}

/**
 * 8 Equalizer dots with vertical bounce animation and audio decibels modulation.
 */
@Composable
private fun EqualizerDotsBar(
    isActive: Boolean,
    audioBoost: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_bounce")

    // Staggered bounce animations for 4 phases
    val bounce0 by infiniteTransition.animateFloat(
        initialValue = 5f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_0"
    )

    val bounce1 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_1"
    )

    val bounce2 by infiniteTransition.animateFloat(
        initialValue = 7f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_2"
    )

    val bounce3 by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_3"
    )

    Row(
        modifier = modifier.testTag("equalizer_dots"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dotHeights = listOf(
            bounce0, bounce1, bounce2, bounce3,
            bounce1, bounce3, bounce0, bounce2
        )

        dotHeights.forEach { baseHeight ->
            val finalHeight = if (isActive) {
                (baseHeight + audioBoost * 16f).coerceIn(4f, 26f).dp
            } else {
                5.dp
            }

            val dotAlpha = if (isActive) 0.95f else 0.45f

            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(finalHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(LuxNeonCrimson.copy(alpha = dotAlpha))
            )
        }
    }
}
