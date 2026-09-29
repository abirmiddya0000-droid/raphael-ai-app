package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.permissions.CapabilityInfo
import com.example.permissions.CapabilityStatus
import com.example.ui.theme.LuxBorderGlow
import com.example.ui.theme.LuxBorderSubtle
import com.example.ui.theme.LuxDarkSurface
import com.example.ui.theme.LuxElevatedSurface
import com.example.ui.theme.LuxError
import com.example.ui.theme.LuxObsidian
import com.example.ui.theme.LuxPlatinum
import com.example.ui.theme.LuxPureWhite
import com.example.ui.theme.LuxSilver
import com.example.ui.theme.LuxSuccess
import com.example.ui.theme.LuxTextMuted
import com.example.ui.theme.LuxTextSecondary
import com.example.ui.theme.LuxWarning

@Composable
fun CapabilitiesScreen(
    capabilities: List<CapabilityInfo>,
    onRefresh: () -> Unit,
    onRequestPermission: (CapabilityInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LuxObsidian)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TRIGGERS & ORCHESTRATOR",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxPureWhite,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Wisdom King Raphael • Supreme Calculation Core",
                    fontSize = 12.sp,
                    color = Color(0xFF00E5FF)
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(LuxDarkSurface)
                    .border(1.dp, LuxBorderSubtle, CircleShape)
                    .clickable(onClick = onRefresh)
                    .padding(8.dp)
                    .testTag("refresh_capabilities_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = LuxPureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Supreme Core Architecture Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LuxDarkSurface)
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Supreme Core",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RAPHAEL OMNISCIENT ORCHESTRATOR",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuxPureWhite,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ONLINE",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Continuous background listening & auto-execution active. Sub-personas (The Editor Star, Mahiru Shiina, Chloe Aubert, Testarossa) operate under Raphael's real-time oversight.",
                color = LuxSilver,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Autonomous Action",
                    tint = LuxSuccess,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Autonomous Actions: Enabled (No redundant confirmations)",
                    color = LuxSuccess,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "HARDWARE & SYSTEM CAPABILITIES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = LuxTextMuted,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(capabilities, key = { it.id }) { cap ->
                CapabilityCard(
                    capability = cap,
                    onAction = { onRequestPermission(cap) }
                )
            }
        }
    }
}

@Composable
private fun CapabilityCard(
    capability: CapabilityInfo,
    onAction: () -> Unit
) {
    val (statusLabel, statusColor) = when (capability.status) {
        CapabilityStatus.GRANTED -> Pair("GRANTED", LuxSuccess)
        CapabilityStatus.AVAILABLE -> Pair("AVAILABLE", LuxPureWhite)
        CapabilityStatus.NOT_GRANTED -> Pair("NOT GRANTED", LuxTextMuted)
        CapabilityStatus.NOT_SUPPORTED -> Pair("NOT SUPPORTED", LuxError)
        CapabilityStatus.REQUIRES_SPECIAL_ACCESS -> Pair("SPECIAL ACCESS", LuxWarning)
        CapabilityStatus.ERROR -> Pair("ERROR", LuxError)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LuxDarkSurface)
            .border(1.dp, LuxBorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = capability.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxPureWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = capability.description,
                    fontSize = 11.5.sp,
                    color = LuxSilver
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(LuxElevatedSurface)
                    .border(1.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Technical details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Required Permission: ${capability.requiredPermission}",
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = LuxTextMuted
                )
                Text(
                    text = "Bound: ${capability.limitations}",
                    fontSize = 9.5.sp,
                    color = LuxTextMuted
                )
            }

            if (capability.status != CapabilityStatus.GRANTED && capability.status != CapabilityStatus.NOT_SUPPORTED) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LuxElevatedSurface)
                        .border(1.dp, LuxPureWhite.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onAction)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("grant_permission_${capability.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (capability.isSpecialAccess) "Configure" else "Grant",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = LuxPureWhite
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Launch,
                            contentDescription = null,
                            tint = LuxPureWhite,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
