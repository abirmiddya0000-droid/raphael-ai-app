package com.example.ui

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.LuxDatabase
import com.example.data.entities.MessageEntity
import com.example.data.repository.ConversationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric test validating Room database operations for saving and retrieving
 * previous exchanges between the user and the AI butler.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChatHistoryRobolectricTest {

    private lateinit var database: LuxDatabase
    private lateinit var repository: ConversationRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LuxDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ConversationRepository(database.conversationDao(), database.messageDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testStoreAndRetrieveMultipleChatExchanges() = runBlocking {
        // 1. Create a session
        val conversationId = repository.createConversation("Cyberpunk Briefing")
        assertTrue(conversationId > 0)

        // 2. First exchange: User asks about mission status
        repository.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                role = "user",
                content = "What is the status of our security grid?"
            )
        )

        repository.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                role = "lux",
                content = "All perimeter defenses and sensor arrays are operating at 100% capacity.",
                modelUsed = "gemini-3.8-flash"
            )
        )

        // 3. Second exchange: User asks for follow-up action
        repository.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                role = "user",
                content = "Deploy autonomous scouts."
            )
        )

        repository.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                role = "lux",
                content = "Autonomous scouts deployed. Live telemetry streaming to your terminal.",
                modelUsed = "DEVICE_ACTION"
            )
        )

        // 4. Retrieve chronological exchange history from Room
        val historyList = repository.getMessagesList(conversationId)
        assertEquals(4, historyList.size)

        // Verify order of exchanges
        assertEquals("user", historyList[0].role)
        assertEquals("What is the status of our security grid?", historyList[0].content)

        assertEquals("lux", historyList[1].role)
        assertEquals("All perimeter defenses and sensor arrays are operating at 100% capacity.", historyList[1].content)
        assertEquals("gemini-3.8-flash", historyList[1].modelUsed)

        assertEquals("user", historyList[2].role)
        assertEquals("Deploy autonomous scouts.", historyList[2].content)

        assertEquals("lux", historyList[3].role)
        assertEquals("DEVICE_ACTION", historyList[3].modelUsed)

        // 5. Verify reactive Flow returns the complete message exchanges
        val flowMessages = repository.getMessagesForConversation(conversationId).first()
        assertEquals(4, flowMessages.size)

        // 6. Delete a message and verify exchange history updates
        repository.deleteMessage(historyList[2].id)
        val remainingMessages = repository.getMessagesList(conversationId)
        assertEquals(3, remainingMessages.size)

        // 7. Verify conversation preview was updated to reflect the latest exchange
        val conv = repository.getConversationById(conversationId)
        assertNotNull(conv)
        assertTrue(conv!!.lastMessage.contains("Autonomous scouts deployed"))
    }
}
