package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LuxBorderSubtle
import com.example.ui.theme.LuxDarkSurface
import com.example.ui.theme.LuxElevatedSurface
import com.example.ui.theme.LuxNeonCrimson
import com.example.ui.theme.LuxNeonDarkRed
import com.example.ui.theme.LuxNeonDockBg
import com.example.ui.theme.LuxNeonNavActive
import com.example.ui.theme.LuxNeonNavMuted
import com.example.ui.theme.LuxNeonRed
import com.example.ui.theme.LuxObsidian
import com.example.ui.theme.LuxPureWhite
import com.example.ui.theme.LuxTextMuted

enum class LuxScreen {
    HOME,
    CHAT,
    TRIGGERS,
    HISTORY,
    SETTINGS
}

/**
 * Cyberpunk Bottom Dock matching the requested HTML/CSS layout.
 * Features a glassmorphic floating pill with Home, Chat, Center Mic Action Button,
 * Triggers, and Settings.
 */
@Composable
fun LuxCyberBottomDock(
    currentScreen: LuxScreen,
    isListening: Boolean,
    onNavigate: (LuxScreen) -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dock_mic_glow")
    val micPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating Dock Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(LuxNeonDockBg)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Home
            CyberDockItem(
                icon = Icons.Default.Home,
                label = "Home",
                selected = currentScreen == LuxScreen.HOME,
                testTag = "dock_home",
                onClick = { onNavigate(LuxScreen.HOME) }
            )

            // Chat
            CyberDockItem(
                icon = Icons.AutoMirrored.Filled.Chat,
                label = "Chat",
                selected = currentScreen == LuxScreen.CHAT || currentScreen == LuxScreen.HISTORY,
                testTag = "dock_chat",
                onClick = { onNavigate(LuxScreen.CHAT) }
            )

            // Placeholder space for the elevated center mic button
            Spacer(modifier = Modifier.size(56.dp))

            // Triggers
            CyberDockItem(
                icon = Icons.Default.Bolt,
                label = "Triggers",
                selected = currentScreen == LuxScreen.TRIGGERS,
                testTag = "dock_triggers",
                onClick = { onNavigate(LuxScreen.TRIGGERS) }
            )

            // Settings
            CyberDockItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                selected = currentScreen == LuxScreen.SETTINGS,
                testTag = "dock_settings",
                onClick = { onNavigate(LuxScreen.SETTINGS) }
            )
        }

        // Center Elevated Mic Action Button (-20dp offset)
        Box(
            modifier = Modifier
                .offset(y = (-20).dp)
                .size(62.dp)
                .scale(if (isListening) micPulse else 1f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(LuxNeonRed, LuxNeonDarkRed)
                    )
                )
                .border(3.dp, Color(0xFF1A1A24), CircleShape)
                .clickable(onClick = onMicClick)
                .testTag("center_dock_mic_btn"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                contentDescription = "Voice Input",
                tint = LuxPureWhite,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun CyberDockItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val activeColor = LuxNeonNavActive
    val mutedColor = LuxNeonNavMuted

    Column(
        modifier = Modifier
            .size(58.dp, 56.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) activeColor else mutedColor,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) activeColor else mutedColor
        )
    }
}

@Composable
fun LuxTopBar(
    currentScreen: LuxScreen,
    onNavigate: (LuxScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(LuxObsidian)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onNavigate(LuxScreen.HOME) }
                .padding(vertical = 4.dp)
        ) {
            LuxLogo(size = 28.dp)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TopNavPill(
                label = "Home",
                selected = currentScreen == LuxScreen.HOME,
                testTag = "top_nav_home",
                onClick = { onNavigate(LuxScreen.HOME) }
            )
            TopNavPill(
                label = "Chat",
                selected = currentScreen == LuxScreen.CHAT,
                testTag = "top_nav_chat",
                onClick = { onNavigate(LuxScreen.CHAT) }
            )
            TopNavPill(
                label = "Triggers",
                selected = currentScreen == LuxScreen.TRIGGERS,
                testTag = "top_nav_triggers",
                onClick = { onNavigate(LuxScreen.TRIGGERS) }
            )
            TopNavPill(
                label = "Settings",
                selected = currentScreen == LuxScreen.SETTINGS,
                testTag = "top_nav_settings",
                onClick = { onNavigate(LuxScreen.SETTINGS) }
            )
        }
    }
}

@Composable
private fun TopNavPill(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) LuxElevatedSurface else LuxObsidian)
            .border(
                width = 1.dp,
                color = if (selected) LuxNeonRed.copy(alpha = 0.6f) else LuxBorderSubtle,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) LuxPureWhite else LuxTextMuted
        )
    }
}

@Composable
fun LuxBottomBar(
    currentScreen: LuxScreen,
    onNavigate: (LuxScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    LuxCyberBottomDock(
        currentScreen = currentScreen,
        isListening = false,
        onNavigate = onNavigate,
        onMicClick = { onNavigate(LuxScreen.HOME) },
        modifier = modifier
    )
}
