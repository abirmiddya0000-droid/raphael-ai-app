package com.example.data.repository

import com.example.data.dao.ConversationDao
import com.example.data.dao.MessageDao
import com.example.data.entities.ConversationEntity
import com.example.data.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstracting conversation and message persistence from the UI/ViewModel.
 * Adheres to modern Android Clean Architecture and Room Persistence guidelines.
 */
class ConversationRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) {
    val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()

    suspend fun getConversationById(id: Long): ConversationEntity? {
        return conversationDao.getConversationById(id)
    }

    suspend fun getLatestConversation(): ConversationEntity? {
        return conversationDao.getLatestConversation()
    }

    suspend fun createConversation(
        title: String = "Primary Briefing",
        initialPreview: String = ""
    ): Long {
        val now = System.currentTimeMillis()
        val conv = ConversationEntity(
            title = title,
            lastMessage = initialPreview,
            createdAt = now,
            updatedAt = now
        )
        return conversationDao.insertConversation(conv)
    }

    suspend fun updateConversationTitle(id: Long, title: String) {
        conversationDao.updateTitle(id, title, System.currentTimeMillis())
    }

    suspend fun updateConversationPreview(id: Long, lastMessage: String) {
        conversationDao.updatePreview(id, lastMessage, System.currentTimeMillis())
    }

    suspend fun deleteConversation(id: Long) {
        // Cascade delete ensures messages are removed, and we explicitly clear for robustness
        messageDao.deleteMessagesForConversation(id)
        conversationDao.deleteConversation(id)
    }

    suspend fun clearAllConversations() {
        messageDao.clearAllMessages()
        conversationDao.clearAllConversations()
    }

    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    suspend fun getMessagesList(conversationId: Long): List<MessageEntity> {
        return messageDao.getMessagesList(conversationId)
    }

    suspend fun getRecentMessages(conversationId: Long, limit: Int = 20): List<MessageEntity> {
        return messageDao.getRecentMessages(conversationId, limit)
    }

    suspend fun getMessageById(id: Long): MessageEntity? {
        return messageDao.getMessageById(id)
    }

    suspend fun insertMessage(message: MessageEntity): Long {
        val messageId = messageDao.insertMessage(message)
        val preview = if (message.role == "user") {
            "You: ${message.content.take(80)}"
        } else {
            "LUX: ${message.content.take(80)}"
        }
        conversationDao.updatePreview(message.conversationId, preview, message.timestamp)
        return messageId
    }

    suspend fun deleteMessage(id: Long) {
        val msg = messageDao.getMessageById(id)
        messageDao.deleteMessageById(id)
        if (msg != null) {
            val latest = messageDao.getLatestMessage(msg.conversationId)
            val newPreview = if (latest != null) {
                if (latest.role == "user") "You: ${latest.content.take(80)}" else "LUX: ${latest.content.take(80)}"
            } else {
                ""
            }
            conversationDao.updatePreview(msg.conversationId, newPreview, System.currentTimeMillis())
        }
    }
}
