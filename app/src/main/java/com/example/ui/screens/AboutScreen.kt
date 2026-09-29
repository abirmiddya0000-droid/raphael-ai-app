package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LuxLogo
import com.example.ui.theme.LuxBorderSubtle
import com.example.ui.theme.LuxDarkSurface
import com.example.ui.theme.LuxElevatedSurface
import com.example.ui.theme.LuxObsidian
import com.example.ui.theme.LuxPlatinum
import com.example.ui.theme.LuxPureWhite
import com.example.ui.theme.LuxSilver
import com.example.ui.theme.LuxSuccess
import com.example.ui.theme.LuxTextMuted
import com.example.ui.theme.LuxTextSecondary

@Composable
fun AboutScreen(
    ownerName: String,
    totalInputTokens: Long,
    totalOutputTokens: Long,
    activeTTSProvider: String,
    activeModel: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LuxObsidian)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            LuxLogo(size = 56.dp)

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Private AI Butler",
                color = LuxPureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Created by $ownerName",
                color = LuxTextMuted,
                fontSize = 13.sp
            )
        }

        // Diagnostics Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LuxDarkSurface)
                .border(1.dp, LuxBorderSubtle, RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SYSTEM INTEGRITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxTextMuted,
                    letterSpacing = 1.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(LuxSuccess)
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                    Text(
                        text = "SECURE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuxSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            DiagnosticRow(label = "Primary Brain", value = activeModel)
            DiagnosticRow(label = "TTS Synthesis", value = activeTTSProvider)
            DiagnosticRow(label = "Input Tokens", value = totalInputTokens.toString())
            DiagnosticRow(label = "Output Tokens", value = totalOutputTokens.toString())
            DiagnosticRow(label = "Authorized Owner", value = ownerName)
            DiagnosticRow(label = "Architecture", value = "Android Native (Standalone)")
        }

        // Footer Statement
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "LUX Operating Environment",
                color = LuxTextMuted,
                fontSize = 10.sp
            )
            Text(
                text = "Private • Confidential • Uncompromised",
                color = LuxSilver.copy(alpha = 0.5f),
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = LuxTextSecondary
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = LuxPureWhite
        )
    }
}
