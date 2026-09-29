package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LuxPlatinum
import com.example.ui.theme.LuxPureWhite
import com.example.ui.theme.LuxSilver
import com.example.ui.theme.LuxTextMuted

@Composable
fun LuxLogo(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    showSubtitle: Boolean = false,
    subtitle: String = "PRIVATE AI BUTLER"
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Geometric Emblem
            Canvas(modifier = Modifier.size(size)) {
                val w = this.size.width
                val h = this.size.height
                val cx = w / 2f
                val cy = h / 2f

                // Outer diamond shield
                val diamondPath = Path().apply {
                    moveTo(cx, cy - h * 0.44f) // Top apex
                    lineTo(cx + w * 0.40f, cy) // Right
                    lineTo(cx, cy + h * 0.44f) // Bottom
                    lineTo(cx - w * 0.40f, cy) // Left
                    close()
                }
                drawPath(
                    path = diamondPath,
                    color = LuxPureWhite,
                    style = Stroke(width = 1.8f, cap = StrokeCap.Round)
                )

                // Inner faceted geometric line
                drawLine(
                    color = LuxSilver.copy(alpha = 0.5f),
                    start = Offset(cx - w * 0.40f, cy),
                    end = Offset(cx + w * 0.40f, cy),
                    strokeWidth = 1f
                )
                drawLine(
                    color = LuxSilver.copy(alpha = 0.5f),
                    start = Offset(cx, cy - h * 0.44f),
                    end = Offset(cx, cy + h * 0.44f),
                    strokeWidth = 1f
                )

                // Bold central 'L' monogram
                val lPath = Path().apply {
                    moveTo(cx - w * 0.12f, cy - h * 0.18f)
                    lineTo(cx - w * 0.04f, cy - h * 0.18f)
                    lineTo(cx - w * 0.04f, cy + h * 0.12f)
                    lineTo(cx + w * 0.16f, cy + h * 0.12f)
                    lineTo(cx + w * 0.16f, cy + h * 0.18f)
                    lineTo(cx - w * 0.12f, cy + h * 0.18f)
                    close()
                }
                drawPath(path = lPath, color = LuxPureWhite)

                // Small apex brilliance star
                drawCircle(
                    color = LuxPlatinum,
                    radius = w * 0.045f,
                    center = Offset(cx, cy - h * 0.44f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Typography
            Text(
                text = "L U X",
                color = LuxPureWhite,
                fontSize = (size.value * 0.55f).sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 6.sp
            )
        }

        if (showSubtitle) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = LuxTextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.5.sp
            )
        }
    }
}
