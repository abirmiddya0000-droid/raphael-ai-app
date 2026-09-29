package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.entities.ConversationEntity

/**
 * Screen displaying the stored chat history from the Room database in a clean,
 * scrollable list format, styled to match the app's premium cyberpunk aesthetic.
 * Delegates to ChatHistoryScreen for modularity and full API compatibility.
 */
@Composable
fun HistoryScreen(
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
    ChatHistoryScreen(
        conversations = conversations,
        activeConversationId = activeConversationId,
        onSelectConversation = onSelectConversation,
        onNewConversation = onNewConversation,
        onDeleteConversation = onDeleteConversation,
        onClearAllConversations = onClearAllConversations,
        onVerifyOwnerPin = onVerifyOwnerPin,
        onBack = onBack,
        modifier = modifier
    )
}
