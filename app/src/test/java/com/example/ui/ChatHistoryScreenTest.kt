package com.example.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.data.entities.ConversationEntity
import com.example.ui.screens.ChatHistoryScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChatHistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testChatHistoryRendersConversationsAndHandlesSelection() {
        val sampleConversations = listOf(
            ConversationEntity(
                id = 101L,
                title = "Cyber Security Briefing",
                lastMessage = "You: Summarize network defenses",
                createdAt = System.currentTimeMillis() - 5000,
                updatedAt = System.currentTimeMillis() - 1000
            ),
            ConversationEntity(
                id = 102L,
                title = "Anime Butler Calibration",
                lastMessage = "LUX: Voice parameters updated",
                createdAt = System.currentTimeMillis() - 10000,
                updatedAt = System.currentTimeMillis() - 3000
            )
        )

        var selectedConversationId = 0L
        var newChatClicked = false
        var backClicked = false

        composeTestRule.setContent {
            ChatHistoryScreen(
                conversations = sampleConversations,
                activeConversationId = 101L,
                onSelectConversation = { selectedConversationId = it },
                onNewConversation = { newChatClicked = true },
                onDeleteConversation = {},
                onClearAllConversations = {},
                onBack = { backClicked = true }
            )
        }

        // Verify title & badges
        composeTestRule.onNodeWithText("CHAT ARCHIVE").assertIsDisplayed()
        composeTestRule.onNodeWithText("ACTIVE").assertIsDisplayed()

        // Verify conversations displayed
        composeTestRule.onNodeWithText("Cyber Security Briefing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Anime Butler Calibration").assertIsDisplayed()

        // Test card click selection
        composeTestRule.onNodeWithTag("conversation_card_102").performClick()
        assertEquals(102L, selectedConversationId)

        // Test new chat button
        composeTestRule.onNodeWithTag("history_new_chat_button").performClick()
        assertTrue(newChatClicked)

        // Test back button
        composeTestRule.onNodeWithTag("history_back_button").performClick()
        assertTrue(backClicked)
    }

    @Test
    fun testChatHistorySearchFiltersItems() {
        val sampleConversations = listOf(
            ConversationEntity(
                id = 201L,
                title = "Quantum Grid Operation",
                lastMessage = "LUX: Quantum link stabilized"
            ),
            ConversationEntity(
                id = 202L,
                title = "Personal Butler Routine",
                lastMessage = "You: Set reminder for 7 PM"
            )
        )

        composeTestRule.setContent {
            ChatHistoryScreen(
                conversations = sampleConversations,
                activeConversationId = null,
                onSelectConversation = {},
                onNewConversation = {},
                onDeleteConversation = {},
                onClearAllConversations = {}
            )
        }

        // Both visible initially
        composeTestRule.onNodeWithText("Quantum Grid Operation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Personal Butler Routine").assertIsDisplayed()

        // Type query to filter
        composeTestRule.onNodeWithTag("history_search_input").performTextInput("Quantum")
        composeTestRule.onNodeWithText("Quantum Grid Operation").assertIsDisplayed()
    }
}
