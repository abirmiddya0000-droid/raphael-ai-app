package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.ConversationEntity
import com.example.ui.components.LuxLogo
import com.example.ui.theme.LuxBorderGlow
import com.example.ui.theme.LuxBorderSubtle
import com.example.ui.theme.LuxDarkSurface
import com.example.ui.theme.LuxElevatedSurface
import com.example.ui.theme.LuxError
import com.example.ui.theme.LuxNeonCrimson
import com.example.ui.theme.LuxNeonDarkRed
import com.example.ui.theme.LuxNeonLime
import com.example.ui.theme.LuxNeonPinkGlow
import com.example.ui.theme.LuxNeonRed
import com.example.ui.theme.LuxObsidian
import com.example.ui.theme.LuxPlatinum
import com.example.ui.theme.LuxPureWhite
import com.example.ui.theme.LuxSilver
import com.example.ui.theme.LuxTextDark
import com.example.ui.theme.LuxTextMuted
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Premium scrollable list screen displaying stored chat history from the Room database.
 * Formatted with luxury cyberpunk obsidian surfaces, search filtering, dynamic timeline groups,
 * and high-contrast exchange previews.
 */
@Composable
fun ChatHistoryScreen(
    conversations: List<ConversationEntity>,
    activeConversationId: Long?,
    onSelectConversation: (Long) -> Unit,
    onNewConversation: () -> Unit,
    onDeleteConversation: (Long) -> Unit,
    onClearAllConversations: () -> Unit,
    onVerifyOwnerPin: (String) -> Boolean = { true },
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "ACTIVE", "RECENT"
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var conversationToDelete by remember { mutableStateOf<ConversationEntity?>(null) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    BackHandler {
        onBack()
    }

    // Filter conversations based on query and filter chips
    val filteredConversations by remember(conversations, searchQuery, selectedFilter, activeConversationId) {
        derivedStateOf {
            conversations.filter { conv ->
                val matchesQuery = if (searchQuery.isBlank()) {
                    true
                } else {
                    conv.title.contains(searchQuery, ignoreCase = true) ||
                            conv.lastMessage.contains(searchQuery, ignoreCase = true)
                }

                val matchesFilter = when (selectedFilter) {
                    "ACTIVE" -> conv.id == activeConversationId
                    "RECENT" -> {
                        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                        conv.updatedAt >= oneDayAgo
                    }
                    else -> true
                }

                matchesQuery && matchesFilter
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LuxObsidian)
            .statusBarsPadding()
    ) {
        // --- 1. PREMIUM TOP APP BAR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            LuxElevatedSurface,
                            LuxObsidian
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            LuxBorderGlow.copy(alpha = 0.6f),
                            LuxNeonCrimson.copy(alpha = 0.35f),
                            LuxBorderSubtle
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                )
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button & Title Section
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LuxDarkSurface)
                                .border(1.dp, LuxBorderSubtle, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = false, radius = 20.dp),
                                    onClick = onBack
                                )
                                .padding(8.dp)
                                .testTag("history_back_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = LuxPureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CHAT ARCHIVE",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LuxPureWhite,
                                    letterSpacing = 2.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Active local database sync indicator
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(LuxNeonLime)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Local Room Persistence  •  ${conversations.size} Sessions",
                                fontSize = 11.5.sp,
                                color = LuxTextMuted
                            )
                        }
                    }

                    // Top Action: + New Chat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(LuxNeonRed, LuxNeonDarkRed)
                                )
                            )
                            .border(1.dp, LuxNeonPinkGlow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onNewConversation
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("history_new_chat_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Session",
                                tint = LuxPureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "New",
                                color = LuxPureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- 2. SEARCH & FILTER ROW ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LuxDarkSurface)
                            .border(1.dp, LuxBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = LuxTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search stored exchanges...",
                                        color = LuxTextDark,
                                        fontSize = 12.5.sp
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    textStyle = TextStyle(
                                        color = LuxPureWhite,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    cursorBrush = SolidColor(LuxNeonRed),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("history_search_input")
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = LuxSilver,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { searchQuery = "" }
                                )
                            }
                        }
                    }

                    // Clear All Button (Only if conversations exist)
                    if (conversations.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(LuxDarkSurface)
                                .border(1.dp, LuxError.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = {
                                        pinInput = ""
                                        pinError = false
                                        showClearConfirmDialog = true
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 9.dp)
                                .testTag("history_clear_all_button")
                        ) {
                            Text(
                                text = "Clear All",
                                color = LuxError,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Pill Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HistoryFilterChip(
                        label = "All Exchanges",
                        count = conversations.size,
                        isSelected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" }
                    )
                    HistoryFilterChip(
                        label = "Active Briefing",
                        count = if (activeConversationId != null && conversations.any { it.id == activeConversationId }) 1 else 0,
                        isSelected = selectedFilter == "ACTIVE",
                        onClick = { selectedFilter = "ACTIVE" }
                    )
                    HistoryFilterChip(
                        label = "Last 24h",
                        isSelected = selectedFilter == "RECENT",
                        onClick = { selectedFilter = "RECENT" }
                    )
                }
            }
        }

        // --- 3. SCROLLABLE LIST OF STORED EXCHANGES ---
        if (filteredConversations.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(LuxDarkSurface)
                            .border(1.dp, LuxBorderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = LuxNeonCrimson.copy(alpha = 0.7f),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (searchQuery.isNotEmpty()) "NO MATCHING EXCHANGES" else "NO ARCHIVED BRIEFINGS",
                        color = LuxPureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            "No stored chat logs match \"$searchQuery\"."
                        } else {
                            "Every dialogue with your AI butler is stored locally in Room. Start a new session to begin recording."
                        },
                        color = LuxTextMuted,
                        fontSize = 12.5.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(LuxNeonRed, LuxNeonDarkRed)
                                )
                            )
                            .border(1.dp, LuxNeonPinkGlow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable(onClick = onNewConversation)
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "+ Initiate New Briefing",
                            color = LuxPureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredConversations, key = { it.id }) { conv ->
                    val isSelected = conv.id == activeConversationId
                    ConversationHistoryCard(
                        conversation = conv,
                        isActive = isSelected,
                        onCardClick = { onSelectConversation(conv.id) },
                        onDeleteClick = { conversationToDelete = conv }
                    )
                }
            }
        }
    }

    // --- 4. INDIVIDUAL CONVERSATION DELETE DIALOG ---
    conversationToDelete?.let { conv ->
        AlertDialog(
            onDismissRequest = { conversationToDelete = null },
            containerColor = LuxDarkSurface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text(
                    text = "Delete Chat Session?",
                    color = LuxPureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Permanently remove \"${conv.title}\" and all associated message records from the Room database?",
                    color = LuxSilver,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = conv.id
                        conversationToDelete = null
                        onDeleteConversation(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxError),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", color = LuxPureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { conversationToDelete = null },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LuxSilver),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuxBorderSubtle),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- 5. CLEAR ALL CONVERSATIONS DIALOG WITH SECURITY PIN ---
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = LuxDarkSurface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Owner authorization",
                        tint = LuxError,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Purge All Chat Archives",
                        color = LuxPureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "This will irreversibly purge ALL ${conversations.size} conversation threads and message logs from the local Room database.",
                        color = LuxSilver,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Enter Owner PIN (Default: 0000) to confirm:",
                        color = LuxTextMuted,
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            pinInput = it
                            pinError = false
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                        ),
                        singleLine = true,
                        isError = pinError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxNeonRed,
                            unfocusedBorderColor = LuxBorderSubtle,
                            focusedTextColor = LuxPureWhite,
                            unfocusedTextColor = LuxPureWhite,
                            cursorColor = LuxNeonRed
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("owner_pin_input")
                    )
                    if (pinError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Invalid Owner PIN. Clearance denied.",
                            color = LuxError,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (onVerifyOwnerPin(pinInput)) {
                            showClearConfirmDialog = false
                            onClearAllConversations()
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxError),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_clear_all_button")
                ) {
                    Text("Purge Database", color = LuxPureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearConfirmDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LuxSilver),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuxBorderSubtle),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Filter chip component for quick sorting and filtering of chat history.
 */
@Composable
private fun HistoryFilterChip(
    label: String,
    count: Int? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) LuxNeonCrimson.copy(alpha = 0.2f) else LuxDarkSurface,
        animationSpec = tween(150),
        label = "chip_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) LuxNeonCrimson else LuxBorderSubtle,
        animationSpec = tween(150),
        label = "chip_border"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = if (isSelected) LuxPureWhite else LuxSilver,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (count != null && count > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) LuxNeonRed else LuxElevatedSurface)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 9.5.sp,
                        color = LuxPureWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * High-fidelity card representing a single persisted conversation session in Room.
 */
@Composable
private fun ConversationHistoryCard(
    conversation: ConversationEntity,
    isActive: Boolean,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
    val relativeTime = remember(conversation.updatedAt) {
        formatRelativeTime(conversation.updatedAt)
    }
    val fullDate = remember(conversation.updatedAt) {
        dateFormat.format(Date(conversation.updatedAt))
    }

    val cardBorderBrush = if (isActive) {
        Brush.horizontalGradient(
            colors = listOf(
                LuxNeonRed,
                LuxNeonCrimson.copy(alpha = 0.6f),
                LuxBorderGlow
            )
        )
    } else {
        SolidColor(LuxBorderSubtle)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isActive) {
                    Brush.verticalGradient(
                        colors = listOf(
                            LuxElevatedSurface,
                            LuxDarkSurface
                        )
                    )
                } else {
                    SolidColor(LuxDarkSurface)
                }
            )
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                brush = cardBorderBrush,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onCardClick
            )
            .padding(14.dp)
            .testTag("conversation_card_${conversation.id}")
    ) {
        Column {
            // Top Row: Title + Relative Timestamp + Active Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Left session glyph
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isActive) LuxNeonRed.copy(alpha = 0.25f) else LuxElevatedSurface)
                            .border(
                                1.dp,
                                if (isActive) LuxNeonRed else LuxBorderSubtle,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        LuxLogo(size = 14.dp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = conversation.title.ifBlank { "Briefing #${conversation.id}" },
                            color = LuxPureWhite,
                            fontSize = 14.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$relativeTime ($fullDate)",
                            color = LuxTextMuted,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Status or Active Badge
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LuxNeonRed.copy(alpha = 0.15f))
                            .border(1.dp, LuxNeonRed, RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            color = LuxNeonPinkGlow,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Excerpt Preview of Last Message
            val previewText = conversation.lastMessage.ifBlank { "No messages exchanged yet." }
            val isUserRole = previewText.startsWith("You:")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(LuxElevatedSurface.copy(alpha = 0.8f))
                    .border(1.dp, LuxBorderSubtle.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    // Role Pill Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isUserRole) LuxBorderGlow else LuxNeonDarkRed)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isUserRole) "USER" else "AI BUTLER",
                            color = LuxPureWhite,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = previewText.removePrefix("You: ").removePrefix("LUX: "),
                        color = LuxSilver,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onCardClick)
                ) {
                    Text(
                        text = "Open conversation",
                        color = if (isActive) LuxNeonPinkGlow else LuxTextMuted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open",
                        tint = if (isActive) LuxNeonPinkGlow else LuxTextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                }

                // Delete Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false, radius = 16.dp),
                            onClick = onDeleteClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete conversation",
                        tint = LuxTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Calculates a friendly human-readable relative timestamp for conversation cards.
 */
private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000L -> "Just now"
        diff < 3600_000L -> "${diff / 60_000L}m ago"
        diff < 86400_000L -> "${diff / 3600_000L}h ago"
        diff < 172800_000L -> "Yesterday"
        else -> {
            val days = diff / 86400_000L
            if (days < 7) "${days}d ago" else SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
