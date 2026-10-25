package com.example.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accessibility.AiraAccessibilityService
import com.example.accessibility.ScreenSnapshot
import com.example.data.MemoryRepository
import com.example.data.model.AiraMemory
import com.example.data.model.AuditLog
import com.example.data.model.AutomationRoutine
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.neural.ActionKind
import com.example.neural.AiraFriendBrain
import com.example.neural.GeminiOnlineService
import com.example.neural.RealDeviceExecutor
import com.example.voice.VoiceCommandController
import com.example.voice.VoiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AiraViewModel(
    private val repository: MemoryRepository,
    private val friendBrain: AiraFriendBrain,
    private val geminiOnlineService: GeminiOnlineService = GeminiOnlineService(),
    private val voiceController: VoiceCommandController? = null
) : ViewModel() {

    // Accessibility state
    val isAccessibilityConnected: StateFlow<Boolean> = AiraAccessibilityService.isConnected
    val screenSnapshot: StateFlow<ScreenSnapshot> = AiraAccessibilityService.latestScreenSnapshot

    // Theme Mode Toggle (Dark Mode / Light Mode)
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Online Mode / Google API Key State
    private val _isOnlineMode = MutableStateFlow(true)
    val isOnlineMode: StateFlow<Boolean> = _isOnlineMode.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _effectiveApiKey = MutableStateFlow("")
    val effectiveApiKey: StateFlow<String> = _effectiveApiKey.asStateFlow()

    // Command & Chat State
    private val _commandInput = MutableStateFlow("")
    val commandInput: StateFlow<String> = _commandInput.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AIRA,
                text = "Hey dost! Main Aira hoon, tumhari best friend. Maine Online Mode bhi activate kar diya hai with Google Gemini! Ab tum mujhse duniya ka koi bhi sawaal pucho ya phone ka koi bhi task bolo, main sab kar sakti hoon! 💕"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private val _statusBanner = MutableStateFlow("Aira Online Mode Active • Google Gemini Ready")
    val statusBanner: StateFlow<String> = _statusBanner.asStateFlow()

    // Voice State
    val voiceState: StateFlow<VoiceState> = voiceController?.voiceState
        ?: MutableStateFlow(VoiceState.IDLE).asStateFlow()
    val audioRms: StateFlow<Float> = voiceController?.audioRms
        ?: MutableStateFlow(0f).asStateFlow()

    // Database streams
    val memories: StateFlow<List<AiraMemory>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = repository.recentAuditLogs
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val routines: StateFlow<List<AutomationRoutine>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        refreshEffectiveApiKey()
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun toggleOnlineMode() {
        _isOnlineMode.value = !_isOnlineMode.value
        _statusBanner.value = if (_isOnlineMode.value) {
            "Aira Online Mode Active • Google Gemini Ready"
        } else {
            "Aira Offline Mode Active • 100% Local Privacy (0 B Egress)"
        }
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
        refreshEffectiveApiKey()
    }

    private fun refreshEffectiveApiKey() {
        _effectiveApiKey.value = geminiOnlineService.getEffectiveApiKey(_customApiKey.value)
    }

    fun onCommandInputChange(text: String) {
        _commandInput.value = text
    }

    fun submitCommand(prompt: String? = null, activity: Activity? = null) {
        val query = prompt ?: _commandInput.value.trim()
        if (query.isBlank()) return

        _commandInput.value = ""

        // Add user message to conversation
        val userMsg = ChatMessage(sender = MessageSender.USER, text = query)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isExecuting.value = true
            _statusBanner.value = "Aira soch rahi hai..."

            val context = activity?.applicationContext ?: return@launch
            var action: com.example.neural.AiraExecutionAction? = null

            // If Online Mode is enabled, try Google Gemini first
            if (_isOnlineMode.value) {
                val apiKey = geminiOnlineService.getEffectiveApiKey(_customApiKey.value)
                if (apiKey.isNotBlank()) {
                    val recentChat = _chatMessages.value.takeLast(4).map {
                        Pair(if (it.sender == MessageSender.USER) "USER" else "MODEL", it.text)
                    }
                    action = geminiOnlineService.queryGeminiOnline(query, apiKey, recentChat)
                }
            }

            // Fallback to offline friend brain if online returned null or offline mode
            if (action == null) {
                action = friendBrain.processFriendInteraction(query, context)
            }

            // Speak in natural Hinglish
            voiceController?.speak(action.spokenHinglish)

            // ACTUALLY execute the real Android task on device
            var success = false
            if (activity != null) {
                success = RealDeviceExecutor.executeAction(activity, action)
            }

            val replyText = if (action.kind != ActionKind.CHAT_ONLY && success) {
                "${action.textReplyHinglish} ✨"
            } else {
                action.textReplyHinglish
            }

            val airaMsg = ChatMessage(
                sender = MessageSender.AIRA,
                text = replyText,
                actionSummary = if (action.kind != ActionKind.CHAT_ONLY) action.kind.name else null,
                actionType = action.kind.name
            )
            _chatMessages.value = _chatMessages.value + airaMsg

            _statusBanner.value = "Aira: ${action.spokenHinglish.take(45)}..."
            _isExecuting.value = false

            // Audit log
            repository.logAudit(
                actionType = if (_isOnlineMode.value) "ONLINE_${action.kind.name}" else "LOCAL_${action.kind.name}",
                targetApp = action.target.ifBlank { "AIRA_FRIEND" },
                summary = action.textReplyHinglish,
                status = if (success) "EXECUTED" else "CHAT"
            )
        }
    }

    fun cancelExecution() {
        voiceController?.stopSpeaking()
        _isExecuting.value = false
        _statusBanner.value = "Ruk gayi dost! Batao aage kya karna hai?"
    }

    fun startVoiceCommand() {
        voiceController?.startListening()
    }

    fun stopVoiceCommand() {
        voiceController?.stopListening()
    }

    fun triggerAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun saveMemory(key: String, rawValue: String, summary: String, category: String) {
        viewModelScope.launch {
            repository.savePreference(key, rawValue, summary, category)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun purgeVault() {
        viewModelScope.launch {
            repository.purgeAllMemories()
            _statusBanner.value = "Saari memories clear ho gayi dost!"
        }
    }

    fun toggleRoutine(routine: AutomationRoutine) {
        viewModelScope.launch {
            repository.setRoutineEnabled(routine.id, !routine.isEnabled)
        }
    }

    fun summarizeScreenNow(activity: Activity?) {
        submitCommand("Screen padh ke batao kya likha hai", activity)
    }

    override fun onCleared() {
        super.onCleared()
        voiceController?.destroy()
    }
}
