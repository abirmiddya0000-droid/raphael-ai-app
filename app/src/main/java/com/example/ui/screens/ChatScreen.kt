package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.MessageEntity
import com.example.ui.components.LuxLogo
import com.example.ui.theme.LuxBorderSubtle
import com.example.ui.theme.LuxDarkSurface
import com.example.ui.theme.LuxElevatedSurface
import com.example.ui.theme.LuxError
import com.example.ui.theme.LuxObsidian
import com.example.ui.theme.LuxPlatinum
import com.example.ui.theme.LuxPureWhite
import com.example.ui.theme.LuxSilver
import com.example.ui.theme.LuxTextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    messages: List<MessageEntity>,
    isLoading: Boolean,
    currentModelBadge: String,
    conversationTitle: String = "Briefing",
    onSendMessage: (String) -> Unit,
    onNewConversation: () -> Unit,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onSpeakMessage: (String) -> Unit,
    onCopyMessage: (String) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var showNewChatConfirmDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    fun handleNewChatClick() {
        if (inputText.isNotBlank()) {
            showNewChatConfirmDialog = true
        } else {
            onNewConversation()
        }
    }

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LuxObsidian)
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(LuxDarkSurface)
                        .border(1.dp, LuxBorderSubtle, CircleShape)
                        .clickable(onClick = onBack)
                        .padding(6.dp)
                        .testTag("chat_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = LuxPureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))
                LuxLogo(size = 22.dp)
                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = conversationTitle,
                        color = LuxPureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = currentModelBadge,
                        fontSize = 9.5.sp,
                        color = LuxSilver,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // History Button (Opens Room saved chat history)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(LuxDarkSurface)
                        .border(1.dp, LuxBorderSubtle, RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenHistory)
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .testTag("chat_history_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Chat History",
                            tint = LuxPureWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "History",
                            color = LuxPureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // New Chat Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(LuxElevatedSurface)
                        .border(1.dp, LuxBorderSubtle, RoundedCornerShape(12.dp))
                        .clickable(onClick = ::handleNewChatClick)
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .testTag("new_chat_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "+ New Chat",
                            tint = LuxPureWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New",
                            color = LuxPureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 90.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "L U X",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuxPureWhite,
                            letterSpacing = 4.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Awaiting your command, Abir.",
                            fontSize = 13.sp,
                            color = LuxTextMuted
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    onSpeak = { onSpeakMessage(message.content) },
                    onCopy = { onCopyMessage(message.content) },
                    onDelete = { onDeleteMessage(message.id) },
                    onRetry = onRetry
                )
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = LuxPureWhite,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "LUX is deliberating...",
                            color = LuxTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Bottom Input Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LuxDarkSurface)
                .border(1.dp, LuxBorderSubtle)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = "State your instruction...",
                            color = LuxTextMuted,
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(
                            color = LuxPureWhite,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(LuxPureWhite),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank() && !isLoading) {
                                    val q = inputText.trim()
                                    inputText = ""
                                    onSendMessage(q)
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_input_field")
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank() && !isLoading) LuxPureWhite else LuxElevatedSurface)
                        .clickable(enabled = inputText.isNotBlank() && !isLoading) {
                            val q = inputText.trim()
                            inputText = ""
                            onSendMessage(q)
                        }
                        .testTag("chat_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() && !isLoading) LuxObsidian else LuxTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (showNewChatConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showNewChatConfirmDialog = false },
                containerColor = LuxDarkSurface,
                title = {
                    Text(
                        text = "Start New Chat?",
                        color = LuxPureWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "You have an unsent draft in the text input field. The current conversation is already saved to Chat History. Starting a new chat will reset your input.",
                        color = LuxSilver,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showNewChatConfirmDialog = false
                            inputText = ""
                            onNewConversation()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxPureWhite)
                    ) {
                        Text("Start New Chat", color = LuxObsidian)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showNewChatConfirmDialog = false }
                    ) {
                        Text("Cancel", color = LuxSilver)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: MessageEntity,
    onSpeak: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit
) {
    val isUser = message.role == "user"
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) LuxElevatedSurface else LuxDarkSurface)
                .border(
                    width = 1.dp,
                    color = if (message.isError) LuxError else LuxBorderSubtle,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .combinedClickable(
                    onClick = {},
                    onLongClick = { showMenu = true }
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "LUX",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuxPlatinum,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Read aloud",
                                tint = LuxTextMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable(onClick = onSpeak)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Text selection container enabled for user selection
                SelectionContainer {
                    Text(
                        text = message.content,
                        color = if (message.isError) LuxError else LuxPureWhite,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    )
                }

                if (message.isError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(onClick = onRetry)
                            .padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = LuxPureWhite,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Retry",
                            fontSize = 11.sp,
                            color = LuxPureWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.modelUsed.isNotBlank()) {
                        Text(
                            text = message.modelUsed,
                            fontSize = 9.sp,
                            color = LuxTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = formattedTime,
                        fontSize = 9.sp,
                        color = LuxTextMuted
                    )
                }
            }

            // Long Press / Message Actions Dropdown Menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(LuxDarkSurface)
            ) {
                DropdownMenuItem(
                    text = { Text("Copy", color = LuxPureWhite, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = LuxPureWhite, modifier = Modifier.size(16.dp))
                    },
                    onClick = {
                        showMenu = false
                        onCopy()
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Read aloud", color = LuxPureWhite, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = LuxPureWhite, modifier = Modifier.size(16.dp))
                    },
                    onClick = {
                        showMenu = false
                        onSpeak()
                    }
                )
                if (!isUser) {
                    DropdownMenuItem(
                        text = { Text("Regenerate", color = LuxPureWhite, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = LuxPureWhite, modifier = Modifier.size(16.dp))
                        },
                        onClick = {
                            showMenu = false
                            onRetry()
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Delete message", color = LuxError, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = LuxError, modifier = Modifier.size(16.dp))
                    },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}
