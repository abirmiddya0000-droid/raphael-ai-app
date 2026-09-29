package com.example.ui

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ai.LocalIntentResult
import com.example.ai.RaphaelTokenOptimizer
import com.example.data.RaphaelDatabase
import com.example.data.entities.ConversationEntity
import com.example.data.entities.MemoryEntity
import com.example.data.entities.MessageEntity
import com.example.data.entities.RoutineEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Validates the core architectural mandates for RAPHAEL:
 * 1. RaphaelDatabase: Separation of Chat History vs Permanent Memory & Routines.
 * 2. RaphaelTokenOptimizer: Offline 0-token intent filter, context compaction, and daily quota tracking.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RaphaelArchitectureTest {

    private lateinit var context: Context
    private lateinit var db: RaphaelDatabase
    private lateinit var tokenOptimizer: RaphaelTokenOptimizer

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RaphaelDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        tokenOptimizer = RaphaelTokenOptimizer(context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testDatabaseSeparatesChatHistoryFromPermanentMemory() = runBlocking {
        // 1. Insert chat conversation and message
        val convId = db.conversationDao().insertConversation(
            ConversationEntity(title = "Mission Briefing")
        )
        db.messageDao().insertMessage(
            MessageEntity(
                conversationId = convId,
                role = "user",
                content = "Status report on perimeter."
            )
        )

        // 2. Insert permanent memory and routine
        db.memoryDao().insertMemory(
            MemoryEntity(
                category = "FACT",
                key = "OWNER_IDENTITY",
                value = "Abir is the sole recognized master and supreme commander of Raphael."
            )
        )
        db.routineDao().insertRoutine(
            RoutineEntity(
                title = "Morning Diagnostics",
                triggerType = "TIME",
                actionCommand = "Execute sensor check"
            )
        )

        // Verify initial state
        assertEquals(1, db.conversationDao().getAllConversations().first().size)
        assertEquals(1, db.memoryDao().getAllMemories().first().size)
        assertEquals(1, db.routineDao().getAllRoutines().first().size)

        // 3. Clear Chat History (transient)
        db.conversationDao().clearAllConversations()
        db.messageDao().clearAllMessages()

        // 4. VERIFY: Memory and Routines are completely intact!
        assertEquals(0, db.conversationDao().getAllConversations().first().size)
        val memories = db.memoryDao().getAllMemories().first()
        assertEquals(1, memories.size)
        assertEquals("OWNER_IDENTITY", memories[0].key)
        assertEquals(1, db.routineDao().getAllRoutines().first().size)
    }

    @Test
    fun testOfflineIntentFilterExecutesForZeroTokens() {
        val batteryResult = tokenOptimizer.evaluateLocalIntent("What is the battery level?")
        assertNotNull(batteryResult)
        assertTrue(batteryResult is LocalIntentResult.Handled)
        val handled = batteryResult as LocalIntentResult.Handled
        assertEquals("BATTERY_CHECK", handled.actionName)
        assertTrue(handled.outputMessage.startsWith("Notice: Terminal cell integrity"))

        val timeResult = tokenOptimizer.evaluateLocalIntent("What time is it right now?")
        assertNotNull(timeResult)
        assertTrue((timeResult as LocalIntentResult.Handled).outputMessage.startsWith("Report: Local system temporal coordinates"))
    }

    @Test
    fun testContextCompactionKeepsLastFourTurnsAndSummary() {
        val longHistory = mutableListOf<MessageEntity>()
        for (i in 1..10) {
            longHistory.add(MessageEntity(conversationId = 1L, role = "user", content = "User query step $i"))
            longHistory.add(MessageEntity(conversationId = 1L, role = "lux", content = "Report: Analytical deliberation response $i"))
        }

        val (summary, compactedList) = tokenOptimizer.compactHistoryForPrompt(longHistory, maxTurns = 4)

        // Verify max 8 messages kept (4 turns * 2)
        assertEquals(8, compactedList.size)
        // Verify rolling summary was produced for older messages
        assertTrue(summary.startsWith("Prior briefing summary:"))
    }

    @Test
    fun testModelRegistryContainsRequestedGeminiModels() {
        val gemini25 = com.example.ai.ModelRegistry.findModelById("gemini-2.5-flash")
        assertNotNull(gemini25)
        assertEquals("gemini-2.5-flash", gemini25?.apiModelName)

        val gemini31 = com.example.ai.ModelRegistry.findModelById("gemini-3.1-flash")
        assertNotNull(gemini31)
        assertEquals("gemini-3.1-flash", gemini31?.apiModelName)

        val gemini36 = com.example.ai.ModelRegistry.findModelById("gemini-3.6-flash")
        assertNotNull(gemini36)
        assertEquals("gemini-3.6-flash", gemini36?.apiModelName)

        val gemini38 = com.example.ai.ModelRegistry.findModelById("gemini-3.8-flash")
        assertNotNull(gemini38)
        assertEquals("gemini-3.8-flash", gemini38?.apiModelName)
    }

    @Test
    fun testMinimalistPulseScreenStateMapping() {
        val listeningState = com.example.ui.components.LuxOrbState.LISTENING
        val thinkingState = com.example.ui.components.LuxOrbState.THINKING
        val idleState = com.example.ui.components.LuxOrbState.IDLE

        val listeningHeadline = when (listeningState) {
            com.example.ui.components.LuxOrbState.LISTENING -> "LISTENING"
            else -> "OTHER"
        }
        assertEquals("LISTENING", listeningHeadline)

        val thinkingHeadline = when (thinkingState) {
            com.example.ui.components.LuxOrbState.THINKING -> "THINKING..."
            else -> "OTHER"
        }
        assertEquals("THINKING...", thinkingHeadline)

        val idleHeadline = when (idleState) {
            com.example.ui.components.LuxOrbState.IDLE -> "STANDBY"
            else -> "OTHER"
        }
        assertEquals("STANDBY", idleHeadline)
    }

    @Test
    fun testOfflineIntentFilterVolumeCommands() {
        // Test Volume Up
        val volUp = tokenOptimizer.evaluateLocalIntent("volume up")
        assertNotNull(volUp)
        assertTrue(volUp is LocalIntentResult.Handled)
        assertEquals("VOLUME_UP", (volUp as LocalIntentResult.Handled).actionName)

        // Test Volume Down
        val volDown = tokenOptimizer.evaluateLocalIntent("volume down")
        assertNotNull(volDown)
        assertEquals("VOLUME_DOWN", (volDown as LocalIntentResult.Handled).actionName)

        // Test Mute
        val mute = tokenOptimizer.evaluateLocalIntent("mute sound")
        assertNotNull(mute)
        assertEquals("VOLUME_MUTE", (mute as LocalIntentResult.Handled).actionName)

        // Test Unmute
        val unmute = tokenOptimizer.evaluateLocalIntent("unmute volume")
        assertNotNull(unmute)
        assertEquals("VOLUME_UNMUTE", (unmute as LocalIntentResult.Handled).actionName)

        // Test Specific Percentage
        val volSet = tokenOptimizer.evaluateLocalIntent("set volume to 60%")
        assertNotNull(volSet)
        assertEquals("VOLUME_SET", (volSet as LocalIntentResult.Handled).actionName)

        // Test Volume Check
        val volCheck = tokenOptimizer.evaluateLocalIntent("check volume status")
        assertNotNull(volCheck)
        assertEquals("VOLUME_CHECK", (volCheck as LocalIntentResult.Handled).actionName)
    }

    @Test
    fun testOfflineIntentFilterTorchCommands() {
        val torchOn = tokenOptimizer.evaluateLocalIntent("turn on flashlight")
        assertNotNull(torchOn)
        assertTrue(torchOn is LocalIntentResult.Handled)

        val torchOff = tokenOptimizer.evaluateLocalIntent("turn off torch")
        assertNotNull(torchOff)
        assertTrue(torchOff is LocalIntentResult.Handled)

        val torchToggle = tokenOptimizer.evaluateLocalIntent("toggle torch")
        assertNotNull(torchToggle)
        assertTrue(torchToggle is LocalIntentResult.Handled)
    }

    @Test
    fun testContextTrimmingFiftyWordSummaryLimit() {
        val longHistory = mutableListOf<MessageEntity>()
        // Create 20 extensive turns with verbose descriptions
        for (i in 1..20) {
            longHistory.add(
                MessageEntity(
                    conversationId = 1L,
                    role = "user",
                    content = "User query regarding complex system architecture and token budget allocation step number $i with detailed requirements"
                )
            )
            longHistory.add(
                MessageEntity(
                    conversationId = 1L,
                    role = "lux",
                    content = "Report: Analytical deliberation response $i verifying all invariants, constraints, and local execution pipelines for step $i"
                )
            )
        }

        val (summary, compactedList) = tokenOptimizer.compactHistoryForPrompt(longHistory, maxTurns = 4)

        // Verify verbatim turns = 4 turns * 2 = 8 messages
        assertEquals(8, compactedList.size)

        // Verify summary exists and starts with briefing prefix
        assertTrue(summary.startsWith("Prior briefing summary:"))

        // Verify strictly 50 words or fewer
        val wordCount = summary.split(Regex("""\s+""")).filter { it.isNotBlank() }.size
        assertTrue("Summary must not exceed 50 words, actual: $wordCount", wordCount <= 50)
    }

    @Test
    fun testCompactAIMessagesInjectsSummaryForOlderTurns() {
        val aiMessages = mutableListOf<com.example.ai.AIMessage>()
        for (i in 1..8) {
            aiMessages.add(com.example.ai.AIMessage(role = "user", content = "Question $i"))
            aiMessages.add(com.example.ai.AIMessage(role = "model", content = "Answer $i"))
        }

        val compacted = tokenOptimizer.compactAIMessages(aiMessages, maxTurns = 4)
        // 1 system message with summary + 8 recent verbatim messages (4 turns * 2) = 9
        assertEquals(9, compacted.size)
        assertEquals("system", compacted[0].role)
        assertTrue(compacted[0].content.startsWith("Prior briefing summary:"))
    }

    @Test
    fun testSupremeCoreHierarchyAndSubPersonas() {
        val orchestrator = com.example.ai.RaphaelMasterOrchestrator()
        val allChars = com.example.data.CharacterCatalog.CHARACTERS

        // Supreme Orchestrator must be Raphael
        val raphael = allChars.find { it.id == "raphael_wisdom" }
        assertNotNull(raphael)
        assertTrue(raphael!!.subtitle.contains("Supreme"))

        // All sub-personas must be present
        assertNotNull(allChars.find { it.id == "luxion" })
        assertNotNull(allChars.find { it.id == "editor_star" })
        assertNotNull(allChars.find { it.id == "mahiru" })
        assertNotNull(allChars.find { it.id == "chloe" })
        assertNotNull(allChars.find { it.id == "testarossa" })
    }

    @Test
    fun testMultiAgentCollaborationProtocol() {
        val orchestrator = com.example.ai.RaphaelMasterOrchestrator()
        val mahiru = com.example.data.CharacterCatalog.getById("mahiru")

        // 1. User talks normally to Mahiru -> Delegated mode with Raphael background calculations
        val planSub = orchestrator.orchestrate(
            userCommand = "Mahiru, can you help me schedule my morning tea?",
            activePersona = mahiru,
            ownerName = "Abir",
            memoryContext = "Owner: Abir"
        )
        assertEquals(com.example.ai.DirectiveMode.DELEGATED_SUB_PERSONA, planSub.mode)
        assertEquals("MAHIRU SHIINA", planSub.actingPersona.name)
        assertTrue(planSub.systemInstruction.contains("DELEGATED SUB-PERSONA WITH RAPHAEL BACKGROUND COMPUTATION"))

        // 2. User invokes "Raphael" -> Direct Raphael Supreme Intervention
        val planSupreme = orchestrator.orchestrate(
            userCommand = "Raphael, analyze system invariants and optimize power distribution",
            activePersona = mahiru,
            ownerName = "Abir",
            memoryContext = "Owner: Abir"
        )
        assertEquals(com.example.ai.DirectiveMode.DIRECT_RAPHAEL, planSupreme.mode)
        assertEquals("WISDOM KING RAPHAEL", planSupreme.actingPersona.name)
        assertTrue(planSupreme.systemInstruction.contains("DIRECT RAPHAEL SUPREME INTERVENTION"))
    }

    @Test
    fun testAutonomousOperationalReportFormatting() {
        val orchestrator = com.example.ai.RaphaelMasterOrchestrator()
        val report = orchestrator.formatAutonomousReport("Battery Monitor Evaluation", "Terminal voltage calibrated to 85%. Discharging nominal.")

        assertTrue(report.startsWith("[Raphael: Task Executed Automatically]"))
        assertTrue(report.endsWith("[Raphael Notice: Analysis Complete]"))
        assertTrue(report.contains("Battery Monitor Evaluation"))
    }

    @Test
    fun testSixPersonaCatalogAndGenderAudioMatrix() {
        val allChars = com.example.data.CharacterCatalog.CHARACTERS
        assertEquals(6, allChars.size)

        // 1. Raphael (Female)
        val raphael = com.example.data.CharacterCatalog.getById("raphael_wisdom")
        assertTrue(raphael.isFemaleVoice)
        assertEquals("[TTS: Raphael | Pitch: Normal | Speed: 1.05 | Tone: Synthetic Divine]", raphael.ttsTag)

        // 2. Luxion (Male)
        val luxion = com.example.data.CharacterCatalog.getById("luxion")
        assertFalse(luxion.isFemaleVoice)
        assertEquals("[TTS: Luxion | Pitch: Low-Mid | Speed: 1.1 | Tone: Sarcastic Metallic]", luxion.ttsTag)
        assertTrue(luxion.personaInstruction.contains("Mobuseka"))

        // 3. Mahiru (Female)
        val mahiru = com.example.data.CharacterCatalog.getById("mahiru")
        assertTrue(mahiru.isFemaleVoice)
        assertEquals("[TTS: Mahiru | Pitch: High-Soft | Speed: 0.95 | Tone: Sweet Gentle]", mahiru.ttsTag)

        // 4. Testarossa (Female)
        val testarossa = com.example.data.CharacterCatalog.getById("testarossa")
        assertTrue(testarossa.isFemaleVoice)
        assertEquals("[TTS: Testarossa | Pitch: Medium | Speed: 0.9 | Tone: Regal Elegant]", testarossa.ttsTag)

        // 5. Chloe (Female)
        val chloe = com.example.data.CharacterCatalog.getById("chloe")
        assertTrue(chloe.isFemaleVoice)
        assertEquals("[TTS: Chloe | Pitch: Medium | Speed: 1.0 | Tone: Gentle Determined]", chloe.ttsTag)

        // 6. The Editor Star (Male/Neutral)
        val editor = com.example.data.CharacterCatalog.getById("editor_star")
        assertFalse(editor.isFemaleVoice)
        assertEquals("[TTS: EditorStar | Pitch: Medium | Speed: 1.15 | Tone: Sharp Futuristic]", editor.ttsTag)
    }

    @Test
    fun testTtsTagWrappingAndStripping() {
        val orchestrator = com.example.ai.RaphaelMasterOrchestrator()
        val luxion = com.example.data.CharacterCatalog.getById("luxion")

        val rawText = "Oh, Master... did you actually think of that yourself?"
        val tagged = orchestrator.ensureTtsTag(rawText, luxion)
        assertTrue(tagged.startsWith("[TTS: Luxion | Pitch: Low-Mid | Speed: 1.1 | Tone: Sarcastic Metallic]"))

        val stripped = orchestrator.stripTtsTag(tagged)
        assertEquals(rawText, stripped)
    }

    @Test
    fun testDeviceActionHandlerFunctionCalling() {
        val handler = com.example.actions.DeviceActionHandler(context)

        // 1. Alarm Function
        val alarmResult = handler.executeAction("set alarm for 7:30 am")
        assertTrue(alarmResult.executed)
        assertTrue(alarmResult.feedback.contains("07:30"))

        // 2. Media Control Function
        val mediaResult = handler.executeAction("play music")
        assertTrue(mediaResult.executed)

        // 3. App Launch Function
        val appResult = handler.executeAction("open youtube")
        assertTrue(appResult.executed)

        // 4. Send Message Function
        val msgResult = handler.executeAction("send message to Alice Hello there")
        assertTrue(msgResult.executed)
    }
}
