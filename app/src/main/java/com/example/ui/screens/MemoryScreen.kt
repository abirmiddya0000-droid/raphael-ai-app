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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.entities.MemoryEntity
import com.example.ui.theme.LuxBorderSubtle
import com.example.ui.theme.LuxDarkSurface
import com.example.ui.theme.LuxElevatedSurface
import com.example.ui.theme.LuxError
import com.example.ui.theme.LuxObsidian
import com.example.ui.theme.LuxPlatinum
import com.example.ui.theme.LuxPureWhite
import com.example.ui.theme.LuxSilver
import com.example.ui.theme.LuxTextMuted
import com.example.ui.theme.LuxTextSecondary

@Composable
fun MemoryScreen(
    memories: List<MemoryEntity>,
    isMemoryEnabled: Boolean,
    onToggleMemory: (Boolean) -> Unit,
    onAddMemory: (category: String, key: String, value: String) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onClearAllMemories: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

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
                    text = "BUTLER MEMORY",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxPureWhite,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Explicit Owner Facts & Preferences",
                    fontSize = 12.sp,
                    color = LuxTextMuted
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Add memory button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(LuxElevatedSurface)
                        .border(1.dp, LuxBorderSubtle, RoundedCornerShape(12.dp))
                        .clickable { showAddDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("add_memory_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Remember",
                            tint = LuxPureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remember", color = LuxPureWhite, fontSize = 12.sp)
                    }
                }

                if (memories.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(LuxDarkSurface)
                            .border(1.dp, LuxBorderSubtle, RoundedCornerShape(12.dp))
                            .clickable { showClearDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("clear_all_memory_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear All",
                            tint = LuxError,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Memory Retention Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(LuxDarkSurface)
                .border(1.dp, LuxBorderSubtle, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Memory Retention",
                    color = LuxPureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Allows LUX to retain verified facts across sessions.",
                    color = LuxTextMuted,
                    fontSize = 11.sp
                )
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

        Spacer(modifier = Modifier.height(16.dp))

        if (memories.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No stored facts. Instruct LUX to remember, or tap '+ Remember'.",
                    color = LuxTextMuted,
                    fontSize = 12.5.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(memories, key = { it.id }) { memory ->
                    MemoryItemCard(
                        memory = memory,
                        onForget = { onDeleteMemory(memory.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddMemoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { cat, k, v ->
                showAddDialog = false
                onAddMemory(cat, k, v)
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
private fun MemoryItemCard(
    memory: MemoryEntity,
    onForget: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LuxDarkSurface)
            .border(1.dp, LuxBorderSubtle, RoundedCornerShape(12.dp))
            .padding(14.dp),
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
                .clickable(onClick = onForget)
        )
    }
}

@Composable
private fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, key: String, value: String) -> Unit
) {
    var keyText by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("FACT") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxDarkSurface,
        title = {
            Text("Store Verified Fact", color = LuxPureWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Category Selector
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

                // Key
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(LuxElevatedSurface)
                        .padding(10.dp)
                ) {
                    if (keyText.isEmpty()) {
                        Text("Title / Topic (e.g. Current City)", color = LuxTextMuted, fontSize = 12.sp)
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

                // Value
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(LuxElevatedSurface)
                        .padding(10.dp)
                ) {
                    if (valueText.isEmpty()) {
                        Text("Fact details (e.g. Abir works in AI and mobile systems)", color = LuxTextMuted, fontSize = 12.sp)
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
                        onConfirm(selectedCategory, keyText.trim(), valueText.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuxPureWhite)
            ) {
                Text("Remember", color = LuxObsidian)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = LuxSilver)
            }
        }
    )
}
