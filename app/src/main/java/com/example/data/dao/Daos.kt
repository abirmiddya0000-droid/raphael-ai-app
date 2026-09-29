package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entities.AuditLogEntity
import com.example.data.entities.ConversationEntity
import com.example.data.entities.MemoryEntity
import com.example.data.entities.MessageEntity
import com.example.data.entities.RoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestConversation(): ConversationEntity?

    @Query("SELECT COUNT(*) FROM conversations")
    suspend fun getConversationCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE conversations SET lastMessage = :lastMessage, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePreview(id: Long, lastMessage: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: Long)

    @Query("DELETE FROM conversations")
    suspend fun clearAllConversations()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesList(conversationId: Long): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(conversationId: Long, limit: Int = 20): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessage(conversationId: Long): MessageEntity?

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): MessageEntity?

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    suspend fun getMessageCount(conversationId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>): List<Long>

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: Long)

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY createdAt DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE category = :category ORDER BY createdAt DESC")
    suspend fun getMemoriesByCategory(category: String): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Query("DELETE FROM memories")
    suspend fun clearAllMemories()
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity): Long
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutine(id: Long)
}
