package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AuditLogDao
import com.example.data.dao.ConversationDao
import com.example.data.dao.MemoryDao
import com.example.data.dao.MessageDao
import com.example.data.entities.AuditLogEntity
import com.example.data.entities.ConversationEntity
import com.example.data.entities.MemoryEntity
import com.example.data.entities.MessageEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        AuditLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class LuxDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: LuxDatabase? = null

        fun getInstance(context: Context): LuxDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LuxDatabase::class.java,
                    "lux_butler.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
