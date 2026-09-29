package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.entities.ConversationEntity
import com.example.data.entities.MessageEntity
import com.example.data.repository.ConversationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ConversationPersistenceTest {

    private lateinit var db: LuxDatabase
    private lateinit var repo: ConversationRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LuxDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = ConversationRepository(db.conversationDao(), db.messageDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testCreateConversationAndPersistMessages() = runBlocking {
        val convId = repo.createConversation("Strategic Briefing")
        assertTrue(convId > 0)

        val retrievedConv = repo.getConversationById(convId)
        assertNotNull(retrievedConv)
        assertEquals("Strategic Briefing", retrievedConv?.title)

        // Insert user message
        val userMsgId = repo.insertMessage(
            MessageEntity(
                conversationId = convId,
                role = "user",
                content = "Good morning LUX. Summarize schedule."
            )
        )
        assertTrue(userMsgId > 0)

        // Insert LUX response
        val luxMsgId = repo.insertMessage(
            MessageEntity(
                conversationId = convId,
                role = "lux",
                content = "Good morning. You have three meetings scheduled today.",
                modelUsed = "gemini-3.8-flash"
            )
        )
        assertTrue(luxMsgId > 0)

        // Verify messages list order and persistence
        val messages = repo.getMessagesList(convId)
        assertEquals(2, messages.size)
        assertEquals("user", messages[0].role)
        assertEquals("Good morning LUX. Summarize schedule.", messages[0].content)
        assertEquals("lux", messages[1].role)
        assertEquals("gemini-3.8-flash", messages[1].modelUsed)

        // Verify conversation preview updated automatically
        val updatedConv = repo.getConversationById(convId)
        assertNotNull(updatedConv)
        assertTrue(updatedConv!!.lastMessage.contains("Good morning"))

        // Verify Flow emits updated conversation list
        val allConversations = repo.allConversations.first()
        assertEquals(1, allConversations.size)
        assertEquals(convId, allConversations[0].id)
    }

    @Test
    fun testCascadingMessageDeletionWhenConversationDeleted() = runBlocking {
        val convId = repo.createConversation("Temporary Chat")
        repo.insertMessage(
            MessageEntity(
                conversationId = convId,
                role = "user",
                content = "Test message"
            )
        )

        assertEquals(1, repo.getMessagesList(convId).size)

        // Delete conversation
        repo.deleteConversation(convId)

        assertNull(repo.getConversationById(convId))
        assertEquals(0, repo.getMessagesList(convId).size)
    }

    @Test
    fun testGetLatestConversationForSessionRestoration() = runBlocking {
        val firstId = repo.createConversation("Older Briefing")
        // Delay slightly or ensure different timestamp
        val secondId = repo.createConversation("Newer Briefing")

        val latest = repo.getLatestConversation()
        assertNotNull(latest)
        assertEquals(secondId, latest?.id)
        assertEquals("Newer Briefing", latest?.title)
    }
}
