package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

class VoiceCommandController(
    private val context: Context,
    private val onCommandRecognized: (String) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _lastTranscribedText = MutableStateFlow("")
    val lastTranscribedText: StateFlow<String> = _lastTranscribedText.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@VoiceCommandController)
            }
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            initRecognizer()
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra("android.speech.extra.PREFER_OFFLINE", true)
        }

        try {
            _voiceState.value = VoiceState.LISTENING
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _voiceState.value = VoiceState.IDLE
        } catch (_: Exception) {}
    }

    fun speak(text: String) {
        if (isTtsReady && tts != null) {
            _voiceState.value = VoiceState.SPEAKING
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AIRA_TTS_${System.currentTimeMillis()}")
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        if (_voiceState.value == VoiceState.SPEAKING) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isTtsReady = true
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _voiceState.value = VoiceState.LISTENING
    }

    override fun onBeginningOfSpeech() {
        _voiceState.value = VoiceState.LISTENING
    }

    override fun onRmsChanged(rmsdB: Float) {
        _audioRms.value = maxOf(0f, rmsdB)
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _voiceState.value = VoiceState.PROCESSING
    }

    override fun onError(error: Int) {
        _voiceState.value = VoiceState.IDLE
        _audioRms.value = 0f
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull() ?: ""
        if (text.isNotBlank()) {
            _lastTranscribedText.value = text
            onCommandRecognized(text)
        }
        _voiceState.value = VoiceState.IDLE
        _audioRms.value = 0f
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
        if (!partial.isNullOrBlank()) {
            _lastTranscribedText.value = partial
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
    }
}
