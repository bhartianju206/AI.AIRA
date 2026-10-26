package com.example.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accessibility.AiraAccessibilityService
import com.example.accessibility.ScreenSnapshot
import com.example.data.AiraPreferences
import com.example.data.MemoryRepository
import com.example.data.model.AuditLog
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.neural.ActionKind
import com.example.neural.AiraExecutionAction
import com.example.neural.AiraFriendBrain
import com.example.neural.GeminiOnlineService
import com.example.neural.NetworkHelper
import com.example.neural.RealDeviceExecutor
import com.example.screenshare.ScreenCaptureService
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
    private val preferences: AiraPreferences,
    private val geminiService: GeminiOnlineService = GeminiOnlineService(),
    private val voiceController: VoiceCommandController? = null
) : ViewModel() {

    // Accessibility state
    val isAccessibilityConnected: StateFlow<Boolean> = AiraAccessibilityService.isConnected
    val screenSnapshot: StateFlow<ScreenSnapshot> = AiraAccessibilityService.latestScreenSnapshot

    // Screen Share MediaProjection state
    val isScreenShareActive: StateFlow<Boolean> = ScreenCaptureService.isCapturing
    val isScreenSharePaused: StateFlow<Boolean> = ScreenCaptureService.isPaused
    val latestScreenBitmap: StateFlow<Bitmap?> = ScreenCaptureService.latestBitmap

    // Theme Mode ("SYSTEM", "LIGHT", "DARK")
    private val _themeMode = MutableStateFlow(preferences.getThemeMode())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Internet Connectivity State & Modal
    private val _isInternetConnected = MutableStateFlow(true)
    val isInternetConnected: StateFlow<Boolean> = _isInternetConnected.asStateFlow()

    private val _showNoInternetDialog = MutableStateFlow(false)
    val showNoInternetDialog: StateFlow<Boolean> = _showNoInternetDialog.asStateFlow()

    // Gemini API Key State
    private val _savedApiKey = MutableStateFlow(preferences.getApiKey())
    val savedApiKey: StateFlow<String> = _savedApiKey.asStateFlow()

    private val _effectiveApiKey = MutableStateFlow("")
    val effectiveApiKey: StateFlow<String> = _effectiveApiKey.asStateFlow()

    // Chat and UI State
    private val _commandInput = MutableStateFlow("")
    val commandInput: StateFlow<String> = _commandInput.asStateFlow()

    private val _isGeneratingResponse = MutableStateFlow(false)
    val isGeneratingResponse: StateFlow<Boolean> = _isGeneratingResponse.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AIRA,
                text = "Hey dost! Main Aira hoon (v1.2), tumhari best friend. Maine Google Gemini AI connect kar liya hai! Ab tum mujhse duniya ka koi bhi sawaal pucho, coding karwao, ya screen share karke apni screen dikhao — main sab solve kar sakti hoon! 💕"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _statusBanner = MutableStateFlow("Aira AI v1.2 Ready • Gemini 3.5 Flash")
    val statusBanner: StateFlow<String> = _statusBanner.asStateFlow()

    private var lastPrompt: String? = null

    // Voice State
    val voiceState: StateFlow<VoiceState> = voiceController?.voiceState
        ?: MutableStateFlow(VoiceState.IDLE).asStateFlow()
    val audioRms: StateFlow<Float> = voiceController?.audioRms
        ?: MutableStateFlow(0f).asStateFlow()
    val isMuted: StateFlow<Boolean> = voiceController?.isMuted
        ?: MutableStateFlow(preferences.isVoiceMuted()).asStateFlow()

    val auditLogs: StateFlow<List<AuditLog>> = repository.recentAuditLogs
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        refreshEffectiveApiKey()
        if (preferences.isVoiceMuted()) {
            voiceController?.setMuted(true)
        }
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        preferences.setThemeMode(mode)
    }

    fun saveApiKey(key: String) {
        val trimmed = key.trim()
        preferences.setApiKey(trimmed)
        _savedApiKey.value = trimmed
        refreshEffectiveApiKey()
    }

    fun clearApiKey() {
        preferences.clearApiKey()
        _savedApiKey.value = ""
        refreshEffectiveApiKey()
    }

    private fun refreshEffectiveApiKey() {
        _effectiveApiKey.value = geminiService.getEffectiveApiKey(_savedApiKey.value)
    }

    fun toggleMute() {
        val muted = voiceController?.toggleMute() ?: false
        preferences.setVoiceMuted(muted)
        _statusBanner.value = if (muted) "Aira voice muted 🔇" else "Aira voice unmuted 🔊"
    }

    fun onCommandInputChange(text: String) {
        _commandInput.value = text
    }

    fun checkInternet(context: Context): Boolean {
        val available = NetworkHelper.isInternetAvailable(context)
        _isInternetConnected.value = available
        if (!available) {
            _showNoInternetDialog.value = true
        }
        return available
    }

    fun dismissNoInternetDialog() {
        _showNoInternetDialog.value = false
    }

    fun retryConnection(context: Context) {
        val online = checkInternet(context)
        if (online) {
            _showNoInternetDialog.value = false
            _statusBanner.value = "Connected to Internet!"
            if (!lastPrompt.isNullOrBlank()) {
                submitCommand(lastPrompt, context as? Activity)
            }
        }
    }

    fun submitCommand(prompt: String? = null, activity: Activity? = null) {
        val query = prompt ?: _commandInput.value.trim()
        if (query.isBlank() || _isGeneratingResponse.value) return

        val context = activity?.applicationContext ?: return

        // Check Internet Connectivity (Requirement 3: Mandatory Internet)
        if (!checkInternet(context)) {
            _showNoInternetDialog.value = true
            return
        }

        lastPrompt = query
        _commandInput.value = ""

        // Post User Message to chat timeline
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = query,
            hasScreenAttachment = isScreenShareActive.value
        )
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isGeneratingResponse.value = true
            _statusBanner.value = "Aira soch rahi hai..."

            val key = _effectiveApiKey.value
            if (key.isBlank()) {
                val errorMsg = ChatMessage(
                    sender = MessageSender.AIRA,
                    text = "Arre dost, pehle Settings me jakar apna Google Gemini API key enter aur save kar lo! Uske baad main tumhare har sawaal ka answer de sakungi. 😊",
                    isError = true,
                    canRetry = false
                )
                _chatMessages.value = _chatMessages.value + errorMsg
                _isGeneratingResponse.value = false
                _statusBanner.value = "Gemini API key missing in Settings"
                voiceController?.speak("Dost, pehle Settings me jaakar Gemini API key save kar lo!")
                return@launch
            }

            // Grab screen image if Screen Share is actively running
            val screenBase64 = if (isScreenShareActive.value) {
                ScreenCaptureService.latestBase64Jpeg.value
            } else {
                null
            }

            // Send past turns for follow-ups
            val recentChat = _chatMessages.value.takeLast(6).map {
                Pair(if (it.sender == MessageSender.USER) "USER" else "MODEL", it.text)
            }

            val result = geminiService.queryGemini(
                prompt = query,
                apiKey = key,
                screenBase64Jpeg = screenBase64,
                recentChatContext = recentChat
            )

            if (result.isSuccess) {
                // Execute device action if detected
                var deviceActionSuccess = false
                if (result.detectedAction != ActionKind.CHAT_ONLY && activity != null) {
                    val action = AiraExecutionAction(
                        kind = result.detectedAction,
                        target = result.actionTarget,
                        payload = result.actionPayload,
                        spokenHinglish = result.spokenHinglish,
                        spokenTaskEnglish = result.spokenEnglishTask ?: "",
                        textReplyHinglish = result.replyText
                    )
                    deviceActionSuccess = RealDeviceExecutor.executeAction(activity, action)
                }

                // Voice: Speak English if device task, Hinglish if chatting
                val speechToDeliver = if (result.detectedAction != ActionKind.CHAT_ONLY && !result.spokenEnglishTask.isNullOrBlank()) {
                    result.spokenEnglishTask
                } else {
                    result.spokenHinglish
                }
                voiceController?.speak(speechToDeliver)

                val airaMsg = ChatMessage(
                    sender = MessageSender.AIRA,
                    text = result.replyText,
                    actionSummary = if (result.detectedAction != ActionKind.CHAT_ONLY) result.detectedAction.name else null,
                    actionType = result.detectedAction.name,
                    hasScreenAttachment = screenBase64 != null
                )
                _chatMessages.value = _chatMessages.value + airaMsg
                _statusBanner.value = "Aira AI v1.2 • Answered"

                // Audit log
                repository.logAudit(
                    actionType = "GEMINI_${result.detectedAction.name}",
                    targetApp = result.actionTarget.ifBlank { "GEMINI_AI" },
                    summary = result.spokenHinglish,
                    status = if (deviceActionSuccess) "ACTION_SUCCESS" else "CHAT_SUCCESS"
                )
            } else {
                // Error handling (Requirement 2 & 8)
                val errorMsg = ChatMessage(
                    sender = MessageSender.AIRA,
                    text = result.errorMessage ?: "Kuch dikkat aa gayi dost, please try again!",
                    isError = true,
                    canRetry = true
                )
                _chatMessages.value = _chatMessages.value + errorMsg
                _statusBanner.value = "Error: ${result.errorMessage?.take(35)}..."
                voiceController?.speak(result.spokenHinglish)
            }

            _isGeneratingResponse.value = false
        }
    }

    fun retryLastRequest(activity: Activity?) {
        val prompt = lastPrompt
        if (!prompt.isNullOrBlank()) {
            submitCommand(prompt, activity)
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = listOf(
            ChatMessage(
                sender = MessageSender.AIRA,
                text = "Chat history clear ho gayi dost! Aira AI v1.2 ready hai, bolo kya help karun? ✨"
            )
        )
        _statusBanner.value = "Chat history cleared"
    }

    fun startVoiceCommand() {
        voiceController?.startListening()
    }

    fun stopVoiceCommand() {
        voiceController?.stopListening()
    }

    fun pauseScreenShare(context: Context) {
        ScreenCaptureService.pause(context)
    }

    fun resumeScreenShare(context: Context) {
        ScreenCaptureService.resume(context)
    }

    fun stopScreenShare(context: Context) {
        ScreenCaptureService.stop(context)
    }

    fun triggerAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun touchScreenElement(target: String, activity: Activity?) {
        val service = AiraAccessibilityService.instance.value
        if (service != null && AiraAccessibilityService.isConnected.value) {
            voiceController?.speak("Tapping $target on screen.")
            val success = service.findAndClick(target)

            val textReply = if (success) {
                "Screen par '$target' button touch kar diya dost! ✨"
            } else {
                "Screen par '$target' dhundhne ki koshish ki dost, par match nahi hua."
            }

            val msg = ChatMessage(
                sender = MessageSender.AIRA,
                text = textReply,
                actionSummary = "TOUCH_SCREEN: $target",
                actionType = "TOUCH_SCREEN"
            )
            _chatMessages.value = _chatMessages.value + msg
        } else {
            activity?.let { triggerAccessibilitySettings(it) }
            voiceController?.speak("Please enable accessibility permission to touch buttons.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceController?.destroy()
    }
}
