package com.example.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a conversation session in LUX.
 * Stores metadata and last message preview for the Chat History screen.
 */
@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["updatedAt"])
    ]
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val lastMessage: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Entity representing an individual chat message in a conversation.
 * Tied to ConversationEntity with cascading deletion and indices for fast retrieval.
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["timestamp"])
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val role: String, // "user", "lux", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String = "",
    val tokenCount: Int = 0,
    val isError: Boolean = false
)

/**
 * Entity representing approved long-term memory for the owner.
 */
@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "PREFERENCE", "FACT", "ROUTINE", "CONTEXT"
    val key: String,
    val value: String,
    val createdAt: Long = System.currentTimeMillis(),
    val source: String = "USER_DIRECT"
)

/**
 * Entity logging security-sensitive events, model fallbacks, and owner actions.
 */
@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val authorizedByOwner: Boolean = true
)

/**
 * Entity representing automated routines and active triggers for Abir.
 */
@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val triggerType: String, // "TIME", "BATTERY", "HOTWORD", "MANUAL"
    val actionCommand: String,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
