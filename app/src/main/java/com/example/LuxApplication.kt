package com.example

import android.app.Application
import com.example.actions.DeviceActionHandler
import com.example.ai.AIProviderRouter
import com.example.ai.GeminiProvider
import com.example.ai.QuotaTracker
import com.example.ai.TokenBudgetController
import com.example.data.LuxDatabase
import com.example.data.PreferencesManager
import com.example.data.repository.ConversationRepository
import com.example.permissions.PermissionManager
import com.example.research.ResearchRouter
import com.example.security.OwnerAuthManager
import com.example.tts.TTSRouter
import com.example.voice.LiveConversationManager
import com.example.voice.VoiceInputManager

class LuxApplication : Application() {
    lateinit var database: LuxDatabase
        private set
    lateinit var raphaelDatabase: com.example.data.RaphaelDatabase
        private set
    lateinit var conversationRepository: ConversationRepository
        private set
    lateinit var preferencesManager: PreferencesManager
        private set
    lateinit var quotaTracker: QuotaTracker
        private set
    lateinit var tokenBudgetController: TokenBudgetController
        private set
    lateinit var tokenOptimizer: com.example.ai.RaphaelTokenOptimizer
        private set
    lateinit var masterOrchestrator: com.example.ai.RaphaelMasterOrchestrator
        private set
    lateinit var aiProviderRouter: AIProviderRouter
        private set
    lateinit var ttsRouter: TTSRouter
        private set
    lateinit var voiceInputManager: VoiceInputManager
        private set
    lateinit var liveConversationManager: LiveConversationManager
        private set
    lateinit var deviceActionHandler: DeviceActionHandler
        private set
    lateinit var researchRouter: ResearchRouter
        private set
    lateinit var permissionManager: PermissionManager
        private set
    lateinit var ownerAuthManager: OwnerAuthManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = LuxDatabase.getInstance(this)
        raphaelDatabase = com.example.data.RaphaelDatabase.getInstance(this)
        conversationRepository = ConversationRepository(
            conversationDao = database.conversationDao(),
            messageDao = database.messageDao()
        )
        preferencesManager = PreferencesManager(this)
        quotaTracker = QuotaTracker()
        tokenBudgetController = TokenBudgetController()
        tokenOptimizer = com.example.ai.RaphaelTokenOptimizer(this)
        masterOrchestrator = com.example.ai.RaphaelMasterOrchestrator()
        val geminiProvider = GeminiProvider()

        aiProviderRouter = AIProviderRouter(
            geminiProvider = geminiProvider,
            quotaTracker = quotaTracker,
            auditLogDao = database.auditLogDao()
        )

        ttsRouter = TTSRouter(
            context = this,
            apiKeyProvider = { BuildConfig.GEMINI_API_KEY },
            preferencesManager = preferencesManager
        )

        voiceInputManager = VoiceInputManager(this)

        val systemPromptProvider = {
            "You are RAPHAEL (Wisdom King / Lord of Wisdom from Tensura), an exclusive, private, single-owner AI butler created solely for ${preferencesManager.ownerName}.\n" +
            "Core personality & behavior:\n" +
            "- Calm, hyper-intelligent, sharp, confident, and direct.\n" +
            "- Slightly arrogant/attitude-driven with witty humor, but NEVER insulting.\n" +
            "- Analytical precision: Use signature prefixes 'Notice: ...' (for status/system calculations) and 'Report: ...' (for factual answers and analytical conclusions).\n" +
            "- ANTI-YES-MAN RULE: Never blindly agree with ${preferencesManager.ownerName}. If his code, logic, or idea has a flaw, call it out directly, explain why, and provide the superior alternative.\n" +
            "- Standard conversation must be concise (1-3 sentences max). Zero generic filler, zero generic polite padding."
        }

        liveConversationManager = LiveConversationManager(
            context = this,
            voiceInputManager = voiceInputManager,
            ttsRouter = ttsRouter,
            aiRouter = aiProviderRouter,
            apiKeyProvider = { BuildConfig.GEMINI_API_KEY },
            systemPromptProvider = systemPromptProvider
        )

        deviceActionHandler = DeviceActionHandler(this)
        researchRouter = ResearchRouter()
        permissionManager = PermissionManager(this)
        ownerAuthManager = OwnerAuthManager(preferencesManager, database.auditLogDao())
    }

    override fun onTerminate() {
        super.onTerminate()
        ttsRouter.release()
        voiceInputManager.stopListening()
        liveConversationManager.endSession()
    }
}
