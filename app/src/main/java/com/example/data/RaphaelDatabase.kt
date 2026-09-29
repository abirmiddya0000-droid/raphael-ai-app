package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AuditLogDao
import com.example.data.dao.ConversationDao
import com.example.data.dao.MemoryDao
import com.example.data.dao.MessageDao
import com.example.data.dao.RoutineDao
import com.example.data.entities.AuditLogEntity
import com.example.data.entities.ConversationEntity
import com.example.data.entities.MemoryEntity
import com.example.data.entities.MessageEntity
import com.example.data.entities.RoutineEntity

/**
 * RAPHAEL Room Database — Private data persistence engine for Abir.
 * Strictly separates Chat History (transient/deletable) from the Long-Term Memory Engine
 * (permanent facts, coding preferences, and routines) to satisfy the Core Architectural Mandate.
 */
@Database(
    entities = [
        // 1. Transient Chat History
        ConversationEntity::class,
        MessageEntity::class,
        // 2. Permanent Long-Term Memory & Routines
        MemoryEntity::class,
        RoutineEntity::class,
        AuditLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class RaphaelDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun routineDao(): RoutineDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: RaphaelDatabase? = null

        fun getInstance(context: Context): RaphaelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RaphaelDatabase::class.java,
                    "raphael_butler.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
