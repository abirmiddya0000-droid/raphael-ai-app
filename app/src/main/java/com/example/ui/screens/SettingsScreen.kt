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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.CharacterCatalog
import com.example.data.CharacterPreset
import com.example.data.entities.MemoryEntity
import com.example.permissions.CapabilityInfo
import com.example.permissions.CapabilityStatus
import com.example.ui.components.LuxLogo
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
fun SettingsScreen(
    capabilities: List<CapabilityInfo>,
    memories: List<MemoryEntity>,
    isMemoryEnabled: Boolean,
    ownerName: String,
    totalInputTokens: Long,
    totalOutputTokens: Long,
    activeTTSProvider: String,
    activeModel: String,
    selectedCharacter: CharacterPreset = CharacterCatalog.CHARACTERS[0],
    onSelectCharacter: (CharacterPreset) -> Unit = {},
    onOpenCharacterModal: () -> Unit = {},
    onRefreshCapabilities: () -> Unit,
    onRequestPermission: (CapabilityInfo) -> Unit,
    onToggleMemory: (Boolean) -> Unit,
    onAddMemory: (category: String, key: String, value: String) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onClearAllMemories: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Persona", "Capabilities", "Memory", "About")

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
                    text = "SETTINGS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxPureWhite,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "System Permissions, Memory & Integrity",
                    fontSize = 12.sp,
                    color = LuxTextMuted
                )
            }

            if (selectedTab == 1) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(LuxDarkSurface)
                        .border(1.dp, LuxBorderSubtle, CircleShape)
                        .clickable(onClick = onRefreshCapabilities)
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
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Minimal tab selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(LuxDarkSurface)
                .border(1.dp, LuxBorderSubtle, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, tabTitle ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) LuxElevatedSurface else LuxDarkSurface)
                        .border(
                            1.dp,
                            if (isSelected) LuxPureWhite.copy(alpha = 0.4f) else LuxDarkSurface,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedTab = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabTitle,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) LuxPureWhite else LuxTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> PersonaTabContent(
                selectedCharacter = selectedCharacter,
                onSelectCharacter = onSelectCharacter,
                onOpenCharacterModal = onOpenCharacterModal
            )
            1 -> CapabilitiesTabContent(
                capabilities = capabilities,
                onRequestPermission = onRequestPermission
            )
            2 -> MemoryTabContent(
                memories = memories,
                isMemoryEnabled = isMemoryEnabled,
                onToggleMemory = onToggleMemory,
                onAddMemory = onAddMemory,
                onDeleteMemory = onDeleteMemory,
                onClearAllMemories = onClearAllMemories
            )
            3 -> AboutTabContent(
                ownerName = ownerName,
                totalInputTokens = totalInputTokens,
                totalOutputTokens = totalOutputTokens,
                activeTTSProvider = activeTTSProvider,
                activeModel = activeModel
            )
        }
    }
}

@Composable
private fun PersonaTabContent(
    selectedCharacter: CharacterPreset,
    onSelectCharacter: (CharacterPreset) -> Unit,
    onOpenCharacterModal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Persona Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LuxDarkSurface)
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ACTIVE AI PERSONA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxTextMuted,
                    letterSpacing = 1.5.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
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

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = selectedCharacter.imageUrl,
                    contentDescription = selectedCharacter.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF00E5FF), CircleShape)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = selectedCharacter.name,
                        color = LuxPureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedCharacter.subtitle,
                        color = LuxSilver,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Quick Switcher Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CHOOSE BUTLER CHARACTER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LuxTextMuted,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Full Gallery",
                color = Color(0xFF00E5FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onOpenCharacterModal)
            )
        }

        // Character List
        CharacterCatalog.CHARACTERS.forEach { preset ->
            val isSelected = preset.id == selectedCharacter.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) LuxElevatedSurface else LuxDarkSurface)
                    .border(
                        1.dp,
                        if (isSelected) Color(0xFF00E5FF) else LuxBorderSubtle,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelectCharacter(preset) }
                    .padding(14.dp)
                    .testTag("persona_option_${preset.id}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    AsyncImage(
                        model = preset.imageUrl,
                        contentDescription = preset.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.dp, if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = preset.name,
                            color = if (isSelected) Color(0xFF00E5FF) else LuxPureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = preset.subtitle,
                            color = LuxTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SWITCH",
                            color = LuxPureWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CapabilitiesTabContent(
    capabilities: List<CapabilityInfo>,
    onRequestPermission: (CapabilityInfo) -> Unit
) {
    Column {
        Text(
            text = "PERMISSIONS & CAPABILITIES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = LuxTextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(capabilities, key = { it.id }) { cap ->
                val (statusLabel, statusColor) = when (cap.status) {
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
                        .clip(RoundedCornerShape(14.dp))
                        .background(LuxDarkSurface)
                        .border(1.dp, LuxBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cap.name,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuxPureWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = cap.description,
                                fontSize = 11.5.sp,
                                color = LuxSilver
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LuxElevatedSurface)
                                .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Req: ${cap.requiredPermission}",
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = LuxTextMuted
                            )
                            Text(
                                text = "Bound: ${cap.limitations}",
                                fontSize = 9.5.sp,
                                color = LuxTextMuted
                            )
                        }

                        if (cap.status != CapabilityStatus.GRANTED && cap.status != CapabilityStatus.NOT_SUPPORTED) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LuxElevatedSurface)
                                    .border(1.dp, LuxPureWhite.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { onRequestPermission(cap) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("grant_permission_${cap.id}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (cap.isSpecialAccess) "Configure" else "Grant",
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
        }
    }
}

@Composable
private fun MemoryTabContent(
    memories: List<MemoryEntity>,
    isMemoryEnabled: Boolean,
    onToggleMemory: (Boolean) -> Unit,
    onAddMemory: (category: String, key: String, value: String) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onClearAllMemories: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BUTLER MEMORY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxTextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "User-approved facts & preferences",
                    fontSize = 11.sp,
                    color = LuxSilver
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(LuxElevatedSurface)
                        .border(1.dp, LuxBorderSubtle, RoundedCornerShape(10.dp))
                        .clickable { showAddDialog = true }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("add_memory_button")
                ) {
                    Text("+ Remember", color = LuxPureWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                if (memories.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(LuxDarkSurface)
                            .border(1.dp, LuxBorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { showClearDialog = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("clear_all_memory_button")
                    ) {
                        Text("Clear All", color = LuxError, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(LuxDarkSurface)
                .border(1.dp, LuxBorderSubtle, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Memory Retention", color = LuxPureWhite, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                Text("Allow LUX to retain approved facts across sessions.", color = LuxTextMuted, fontSize = 10.5.sp)
            }
            Switch(
                checked = isMemoryEnabled,
                onCheckedChange = onToggleMemory,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = LuxObsidian,
                    checkedTrackColor = LuxPureWhite,
                    uncheckedThumbColor = LuxTextMuted,
                    uncheckedTrackColor = LuxElevatedSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (memories.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No stored facts. Instruct LUX to remember or tap '+ Remember'.",
                    color = LuxTextMuted,
                    fontSize = 12.5.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(memories, key = { it.id }) { memory ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LuxDarkSurface)
                            .border(1.dp, LuxBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(LuxElevatedSurface)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = memory.category,
                                        color = LuxSilver,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = memory.key,
                                    color = LuxPureWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = memory.value,
                                color = LuxSilver,
                                fontSize = 12.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Forget",
                            tint = LuxTextMuted,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onDeleteMemory(memory.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var keyText by remember { mutableStateOf("") }
        var valueText by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("FACT") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = LuxDarkSurface,
            title = {
                Text("Store Verified Fact", color = LuxPureWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("FACT", "PREFERENCE", "ROUTINE").forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedCategory == cat) LuxElevatedSurface else LuxDarkSurface)
                                    .border(1.dp, if (selectedCategory == cat) LuxPureWhite else LuxBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(cat, fontSize = 10.sp, color = if (selectedCategory == cat) LuxPureWhite else LuxTextMuted)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(LuxElevatedSurface)
                            .padding(10.dp)
                    ) {
                        if (keyText.isEmpty()) {
                            Text("Title / Topic", color = LuxTextMuted, fontSize = 12.sp)
                        }
                        BasicTextField(
                            value = keyText,
                            onValueChange = { keyText = it },
                            textStyle = TextStyle(color = LuxPureWhite, fontSize = 12.sp),
                            cursorBrush = SolidColor(LuxPureWhite),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(LuxElevatedSurface)
                            .padding(10.dp)
                    ) {
                        if (valueText.isEmpty()) {
                            Text("Fact details...", color = LuxTextMuted, fontSize = 12.sp)
                        }
                        BasicTextField(
                            value = valueText,
                            onValueChange = { valueText = it },
                            textStyle = TextStyle(color = LuxPureWhite, fontSize = 12.sp),
                            cursorBrush = SolidColor(LuxPureWhite),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (keyText.isNotBlank() && valueText.isNotBlank()) {
                            showAddDialog = false
                            onAddMemory(selectedCategory, keyText.trim(), valueText.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxPureWhite)
                ) {
                    Text("Remember", color = LuxObsidian)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = LuxSilver)
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = LuxDarkSurface,
            title = {
                Text("Clear All Memory?", color = LuxPureWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("All user preferences, routines, and verified owner facts will be deleted.", color = LuxSilver, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDialog = false
                        onClearAllMemories()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxError)
                ) {
                    Text("Clear All", color = LuxPureWhite)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = LuxSilver)
                }
            }
        )
    }
}

@Composable
private fun AboutTabContent(
    ownerName: String,
    totalInputTokens: Long,
    totalOutputTokens: Long,
    activeTTSProvider: String,
    activeModel: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            LuxLogo(size = 46.dp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Private AI Butler",
                color = LuxPureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Created for $ownerName",
                color = LuxTextMuted,
                fontSize = 12.sp
            )
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

            Spacer(modifier = Modifier.height(10.dp))

            AboutDiagnosticRow(label = "Primary Brain", value = activeModel)
            AboutDiagnosticRow(label = "TTS Synthesis", value = activeTTSProvider)
            AboutDiagnosticRow(label = "Input Tokens", value = totalInputTokens.toString())
            AboutDiagnosticRow(label = "Output Tokens", value = totalOutputTokens.toString())
            AboutDiagnosticRow(label = "Authorized Owner", value = ownerName)
            AboutDiagnosticRow(label = "Database Persistence", value = "Room (SQLite)")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp)
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
private fun AboutDiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = LuxTextSecondary)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = LuxPureWhite
        )
    }
}
