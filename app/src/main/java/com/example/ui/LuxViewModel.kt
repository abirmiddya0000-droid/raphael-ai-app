package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.LuxApplication
import com.example.ai.AIMessage
import com.example.ai.AIRouter
import com.example.ai.AIRequest
import com.example.ai.AIResponseResult
import com.example.data.CharacterCatalog
import com.example.data.CharacterPreset
import com.example.data.entities.ConversationEntity
import com.example.data.entities.MemoryEntity
import com.example.data.entities.MessageEntity
import com.example.overlay.LuxFloatingService
import com.example.permissions.CapabilityInfo
import com.example.permissions.CapabilityStatus
import com.example.ui.components.LuxOrbState
import com.example.ui.components.LuxScreen
import com.example.voice.LiveSessionState
import com.example.voice.VoiceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LuxViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as LuxApplication
    private val db = app.database
    private val convRepo = app.conversationRepository
    private val prefs = app.preferencesManager
    private val aiRouter: AIRouter = app.aiProviderRouter
    private val tokenController = app.tokenBudgetController
    private val ttsRouter = app.ttsRouter
    private val voiceInputManager = app.voiceInputManager
    val liveConversationManager = app.liveConversationManager
    private val deviceActions = app.deviceActionHandler
    private val researchRouter = app.researchRouter
    val permissionManager = app.permissionManager
    val ownerAuthManager = app.ownerAuthManager

    // Navigation & Screen State
    private val _currentScreen = MutableStateFlow(LuxScreen.HOME)
    val currentScreen: StateFlow<LuxScreen> = _currentScreen.asStateFlow()

    // Orb State & Status text
    private val _orbState = MutableStateFlow(LuxOrbState.IDLE)
    val orbState: StateFlow<LuxOrbState> = _orbState.asStateFlow()

    private val _statusText = MutableStateFlow("LUX Ready")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    // Active Conversation
    private val _activeConversationId = MutableStateFlow<Long?>(null)
    val activeConversationId: StateFlow<Long?> = _activeConversationId.asStateFlow()

    // Conversations Flow (Room backed)
    val conversations = convRepo.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Messages Flow
    private val _activeMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val activeMessages: StateFlow<List<MessageEntity>> = _activeMessages.asStateFlow()

    private var messagesCollectJob: Job? = null

    // Memories Flow
    val memories = db.memoryDao().getAllMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Capabilities List
    private val _capabilities = MutableStateFlow<List<CapabilityInfo>>(emptyList())
    val capabilities: StateFlow<List<CapabilityInfo>> = _capabilities.asStateFlow()

    // Live Headphone Session
    val liveSessionState: StateFlow<LiveSessionState> = liveConversationManager.sessionState

    // General Loading State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Quota Tracker Gauge (Mana Recycling)
    val quotaRemainingPercentage: StateFlow<Float> = app.tokenOptimizer.quotaRemainingPercentage

    // Real-time audio amplitude for responsive orb visualizer
    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    // Character Persona Profile State
    private val _selectedCharacter = MutableStateFlow(CharacterCatalog.getById(prefs.selectedCharacterId))
    val selectedCharacter: StateFlow<CharacterPreset> = _selectedCharacter.asStateFlow()

    private val _isCharacterModalOpen = MutableStateFlow(false)
    val isCharacterModalOpen: StateFlow<Boolean> = _isCharacterModalOpen.asStateFlow()

    fun openCharacterModal() {
        _isCharacterModalOpen.value = true
    }

    fun closeCharacterModal() {
        _isCharacterModalOpen.value = false
    }

    fun selectCharacter(character: CharacterPreset) {
        prefs.selectedCharacterId = character.id
        _selectedCharacter.value = character
        ttsRouter.configureVoiceForPersona(character)
        _statusText.value = "Persona: ${character.name}"
    }

    init {
        ttsRouter.configureVoiceForPersona(_selectedCharacter.value)
        refreshCapabilities()
        initializeDefaultConversation()
    }

    fun navigateTo(screen: LuxScreen) {
        _currentScreen.value = screen
    }

    fun refreshCapabilities() {
        _capabilities.value = permissionManager.checkAllCapabilities()
    }

    private fun initializeDefaultConversation() {
        viewModelScope.launch(Dispatchers.IO) {
            val lastSavedId = prefs.lastActiveConversationId
            val existing = if (lastSavedId > 0) {
                convRepo.getConversationById(lastSavedId)
            } else {
                null
            } ?: convRepo.getLatestConversation()

            val id = if (existing == null) {
                val newId = convRepo.createConversation("Primary Briefing")
                prefs.lastActiveConversationId = newId
                newId
            } else {
                prefs.lastActiveConversationId = existing.id
                existing.id
            }
            _activeConversationId.value = id
            loadMessagesForConversation(id)
        }
    }

    fun selectConversation(id: Long) {
        _activeConversationId.value = id
        prefs.lastActiveConversationId = id
        loadMessagesForConversation(id)
        _currentScreen.value = LuxScreen.CHAT
    }

    fun startNewConversation() {
        viewModelScope.launch(Dispatchers.IO) {
            val title = "New Briefing"
            val newId = convRepo.createConversation(title)
            _activeConversationId.value = newId
            prefs.lastActiveConversationId = newId
            loadMessagesForConversation(newId)
            withContext(Dispatchers.Main) {
                _currentScreen.value = LuxScreen.CHAT
            }
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            convRepo.deleteConversation(id)
            ownerAuthManager.logSensitiveAction("DELETE_CONVERSATION", "Deleted conversation ID $id", true)
            if (_activeConversationId.value == id) {
                val remaining = convRepo.getLatestConversation()
                if (remaining != null) {
                    _activeConversationId.value = remaining.id
                    prefs.lastActiveConversationId = remaining.id
                    loadMessagesForConversation(remaining.id)
                } else {
                    initializeDefaultConversation()
                }
            }
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            convRepo.clearAllConversations()
            prefs.lastActiveConversationId = -1L
            ownerAuthManager.logSensitiveAction("CLEAR_ALL_CHATS", "Purged all conversation history", true)
            initializeDefaultConversation()
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            convRepo.deleteMessage(id)
        }
    }

    fun copyMessageToClipboard(content: String) {
        val clipboard = app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("LUX Message", content)
        clipboard.setPrimaryClip(clip)
    }

    fun retryLastMessage() {
        val currentList = _activeMessages.value
        val lastUserMessage = currentList.findLast { it.role == "user" }
        if (lastUserMessage != null) {
            processInstruction(lastUserMessage.content, speakResponse = false)
        }
    }

    private fun loadMessagesForConversation(conversationId: Long) {
        messagesCollectJob?.cancel()
        messagesCollectJob = viewModelScope.launch(Dispatchers.IO) {
            convRepo.getMessagesForConversation(conversationId).collect { msgList ->
                _activeMessages.value = msgList
            }
        }
    }

    // Voice Interaction: Tap-to-Talk Microphone (Single Turn)
    fun startMicrophoneInput() {
        if (_orbState.value == LuxOrbState.LISTENING) {
            voiceInputManager.stopListening()
            _soundLevel.value = 0f
            _orbState.value = LuxOrbState.IDLE
            _statusText.value = "LUX Ready"
            return
        }

        viewModelScope.launch(Dispatchers.Main) {
            _orbState.value = LuxOrbState.LISTENING
            _statusText.value = "Listening to instruction..."
            _soundLevel.value = 0f

            voiceInputManager.startListening(
                onStateChanged = { state ->
                    when (state) {
                        is VoiceState.Ready -> {
                            _orbState.value = LuxOrbState.LISTENING
                            _statusText.value = "Listening to instruction..."
                        }
                        is VoiceState.Listening -> {
                            _orbState.value = LuxOrbState.LISTENING
                            _statusText.value = if (state.isStreaming) "Listening..." else "Listening to instruction..."
                        }
                        is VoiceState.PartialResult -> {
                            _orbState.value = LuxOrbState.LISTENING
                            _statusText.value = "\"${state.text}\""
                            _soundLevel.value = state.rmsdB
                        }
                        is VoiceState.Processing -> {
                            _orbState.value = LuxOrbState.PROCESSING
                            _statusText.value = "Analyzing speech..."
                        }
                        is VoiceState.Success -> {
                            _soundLevel.value = 0f
                            _statusText.value = "Processing instruction..."
                            processInstruction(state.recognizedText, speakResponse = true)
                        }
                        is VoiceState.Error -> {
                            _soundLevel.value = 0f
                            _orbState.value = LuxOrbState.ERROR
                            _statusText.value = state.message
                        }
                        is VoiceState.Idle -> {
                            _soundLevel.value = 0f
                            if (_orbState.value == LuxOrbState.LISTENING) {
                                _orbState.value = LuxOrbState.IDLE
                                _statusText.value = "LUX Ready"
                            }
                        }
                    }
                },
                onRmsChanged = { rms ->
                    _soundLevel.value = rms
                }
            )
        }
    }

    // Voice Interaction: Live Headphone Mode (Two-way continuous)
    fun toggleHeadphoneSession() {
        if (liveConversationManager.isSessionRunning()) {
            liveConversationManager.endSession()
            _orbState.value = LuxOrbState.IDLE
            _statusText.value = "LUX Ready"
        } else {
            _orbState.value = LuxOrbState.LISTENING
            _statusText.value = "Live Headphone Active"
            liveConversationManager.startSession(viewModelScope)
        }
    }

    fun endLiveSession() {
        liveConversationManager.endSession()
        _orbState.value = LuxOrbState.IDLE
        _statusText.value = "LUX Ready"
    }

    fun bargeInLiveSession() {
        liveConversationManager.bargeIn(viewModelScope)
    }

    // Core Instruction Execution
    fun processInstruction(rawInput: String, speakResponse: Boolean = false) {
        val input = rawInput.trim()
        if (input.isEmpty()) return

        val conversationId = _activeConversationId.value ?: 1L

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _orbState.value = LuxOrbState.THINKING
            _statusText.value = "Deliberating..."

            // 1. Record User Message
            convRepo.insertMessage(
                MessageEntity(
                    conversationId = conversationId,
                    role = "user",
                    content = input,
                    timestamp = System.currentTimeMillis()
                )
            )

            // Update conversation title from first user query if still generic
            val existingConv = convRepo.getConversationById(conversationId)
            if (existingConv != null && (existingConv.title.startsWith("Primary Briefing") || existingConv.title.startsWith("Briefing #") || existingConv.title.startsWith("New Briefing"))) {
                val derivedTitle = if (input.length > 28) input.take(28) + "..." else input
                convRepo.updateConversationTitle(conversationId, derivedTitle)
            }

            // 1.5 Local Offline Intent Filter (0 Tokens)
            val offlineResult = app.tokenOptimizer.evaluateLocalIntent(input)
            if (offlineResult is com.example.ai.LocalIntentResult.Handled) {
                convRepo.insertMessage(
                    MessageEntity(
                        conversationId = conversationId,
                        role = "lux",
                        content = offlineResult.outputMessage,
                        modelUsed = "LOCAL_0_TOKEN (${offlineResult.actionName})",
                        isError = !offlineResult.success
                    )
                )

                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _orbState.value = if (offlineResult.success) LuxOrbState.IDLE else LuxOrbState.ERROR
                    _statusText.value = "Action Executed (0 Tokens)"
                    if (speakResponse) {
                        speakText(offlineResult.outputMessage)
                    }
                }
                return@launch
            }

            // 2. Check for Direct Device Actions (e.g. Alarms, Media, Apps, Settings, Messaging)
            val lower = input.lowercase()
            val isDeviceAction = lower.startsWith("open ") || lower.startsWith("launch ") ||
                    lower.contains("alarm") || lower.contains("wake me up") ||
                    lower.contains("play music") || lower.contains("pause music") || lower.contains("next song") || lower.contains("stop music") ||
                    lower.startsWith("send message") || lower.startsWith("text ") || lower.startsWith("whatsapp ") ||
                    lower.contains("turn on ") || lower.contains("turn off ")
            if (isDeviceAction) {
                val actionResult = deviceActions.executeAction(input)
                val replyContent = actionResult.feedback

                convRepo.insertMessage(
                    MessageEntity(
                        conversationId = conversationId,
                        role = "lux",
                        content = replyContent,
                        modelUsed = "DEVICE_ACTION",
                        isError = !actionResult.executed
                    )
                )

                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _orbState.value = if (actionResult.executed) LuxOrbState.IDLE else LuxOrbState.ERROR
                    _statusText.value = if (actionResult.executed) "Action Executed" else "Action Restricted"
                    if (speakResponse) {
                        speakText(replyContent)
                    }
                }
                return@launch
            }

            // 3. Check for Web Research (e.g. "search for ...", "lookup ...")
            if (lower.startsWith("search ") || lower.startsWith("web research ") || lower.startsWith("lookup ") || lower.startsWith("check url ")) {
                val query = input.replace(Regex("^(search for|search|lookup|web research|check url)\\s*", RegexOption.IGNORE_CASE), "")
                val research = researchRouter.performResearch(query)
                val reply = when (research) {
                    is com.example.research.ResearchResult.Success -> "${research.summary}\n(Source: ${research.sourceUrl})"
                    is com.example.research.ResearchResult.Error -> research.message
                }

                convRepo.insertMessage(
                    MessageEntity(
                        conversationId = conversationId,
                        role = "lux",
                        content = reply,
                        modelUsed = "WEB_RESEARCH",
                        isError = research is com.example.research.ResearchResult.Error
                    )
                )

                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _orbState.value = LuxOrbState.IDLE
                    _statusText.value = "Research Complete"
                    if (speakResponse) {
                        speakText(reply)
                    }
                }
                return@launch
            }

            // 4. Send to Main AI Brain (Reasoning Models with Sequential Fallback)
            val history = convRepo.getMessagesList(conversationId)
            val aiMessages = history.map { AIMessage(role = it.role, content = it.content) }

            val activeMemories = if (prefs.isMemoryEnabled) {
                db.memoryDao().getMemoriesByCategory("FACT") + db.memoryDao().getMemoriesByCategory("PREFERENCE")
            } else {
                emptyList()
            }

            val trimmedMessages = app.tokenOptimizer.compactAIMessages(aiMessages, maxTurns = 4)
            val optimizedMessages = tokenController.prepareOptimizedMessages(trimmedMessages, activeMemories)
            val memoryContext = tokenController.formatMemoryContext(activeMemories, prefs.ownerName)

            val currentPersona = _selectedCharacter.value
            val orchestrationPlan = app.masterOrchestrator.orchestrate(
                userCommand = input,
                activePersona = currentPersona,
                ownerName = prefs.ownerName,
                memoryContext = memoryContext
            )

            val request = AIRequest(
                messages = optimizedMessages,
                systemInstruction = orchestrationPlan.systemInstruction,
                temperature = 0.6f,
                maxTokens = 1024
            )

            val apiKey = BuildConfig.GEMINI_API_KEY
            val responseResult = aiRouter.routeRequest(request, apiKey)

            when (responseResult) {
                is AIResponseResult.Success -> {
                    prefs.recordTokenUsage(responseResult.inputTokens, responseResult.outputTokens)
                    app.tokenOptimizer.recordCloudRequest(responseResult.outputTokens)

                    val taggedContent = app.masterOrchestrator.ensureTtsTag(responseResult.text, orchestrationPlan.actingPersona)
                    convRepo.insertMessage(
                        MessageEntity(
                            conversationId = conversationId,
                            role = "lux",
                            content = taggedContent,
                            modelUsed = responseResult.modelUsed + if (responseResult.fallbackOccurred) " (Fallback)" else "",
                            tokenCount = responseResult.outputTokens,
                            isError = false
                        )
                    )

                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                        _orbState.value = LuxOrbState.IDLE
                        _statusText.value = if (responseResult.fallbackOccurred) "Responded (Fallback)" else "LUX Ready"

                        if (speakResponse) {
                            speakText(taggedContent)
                        }
                    }
                }

                is AIResponseResult.Error -> {
                    convRepo.insertMessage(
                        MessageEntity(
                            conversationId = conversationId,
                            role = "lux",
                            content = responseResult.message,
                            modelUsed = "ERROR",
                            isError = true
                        )
                    )

                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                        _orbState.value = LuxOrbState.ERROR
                        _statusText.value = if (responseResult.isQuotaError) "Quota Unavailable" else "AI Provider Error"
                    }
                }
            }
        }
    }

    fun speakText(text: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _orbState.value = LuxOrbState.SPEAKING
            ttsRouter.configureVoiceForPersona(_selectedCharacter.value)
            ttsRouter.speak(
                text = text,
                onStart = {
                    _orbState.value = LuxOrbState.SPEAKING
                },
                onDone = {
                    _orbState.value = LuxOrbState.IDLE
                },
                onError = {
                    _orbState.value = LuxOrbState.ERROR
                }
            )
        }
    }

    // Memory operations
    fun addMemory(category: String, key: String, value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.memoryDao().insertMemory(
                MemoryEntity(
                    category = category,
                    key = key,
                    value = value,
                    source = "OWNER_EXPLICIT"
                )
            )
            ownerAuthManager.logSensitiveAction("ADD_MEMORY", "Stored memory '$key'", true)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            db.memoryDao().deleteMemory(id)
            ownerAuthManager.logSensitiveAction("FORGET_MEMORY", "Purged memory ID $id", true)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch(Dispatchers.IO) {
            db.memoryDao().clearAllMemories()
            ownerAuthManager.logSensitiveAction("CLEAR_ALL_MEMORIES", "All owner memories purged", true)
        }
    }

    fun toggleMemoryRetention(enabled: Boolean) {
        prefs.isMemoryEnabled = enabled
    }

    // Overlay Floating Orb Toggle
    fun toggleFloatingOrb(context: android.content.Context, enable: Boolean) {
        if (enable) {
            if (android.provider.Settings.canDrawOverlays(context)) {
                prefs.isOverlayEnabled = true
                LuxFloatingService.start(context)
            } else {
                permissionManager.openSpecialAccessSettings("CAP_OVERLAY")
            }
        } else {
            prefs.isOverlayEnabled = false
            LuxFloatingService.stop(context)
        }
    }

    fun verifyOwnerPin(pin: String): Boolean = ownerAuthManager.verifyPin(pin)

    val totalInputTokens: Long get() = prefs.totalInputTokens
    val totalOutputTokens: Long get() = prefs.totalOutputTokens
    val ownerName: String get() = prefs.ownerName
    val activeTTSProvider: String get() = ttsRouter.activeProviderName
    val activeModel: String get() = prefs.primaryModel
}
