package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.AiraPreferences
import com.example.data.MemoryRepository
import com.example.data.db.AiraDatabase
import com.example.neural.AiraFriendBrain
import com.example.screenshare.ScreenCaptureService
import com.example.security.PermissionManager
import com.example.ui.components.AiraDrawerContent
import com.example.ui.components.NoInternetDialog
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ScreenShareSheet
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AiraTheme
import com.example.ui.viewmodel.AiraViewModel
import com.example.voice.VoiceCommandController
import com.example.voice.VoiceState
import kotlinx.coroutines.launch

enum class AppDestination {
    CHAT,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: AiraViewModel
    private var voiceController: VoiceCommandController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = AiraPreferences(this)
        val database = AiraDatabase.getDatabase(this)
        val repository = MemoryRepository(database.airaDao())
        val friendBrain = AiraFriendBrain(repository)

        voiceController = VoiceCommandController(this) { command ->
            viewModel.submitCommand(command, this)
        }

        viewModel = AiraViewModel(
            repository = repository,
            friendBrain = friendBrain,
            preferences = preferences,
            voiceController = voiceController
        )

        // Initial internet connectivity check
        viewModel.checkInternet(this)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            AiraTheme(isDarkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AiraMainApp(
                        activity = this,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AiraMainApp(
    activity: ComponentActivity,
    viewModel: AiraViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentScreen by remember { mutableStateOf(AppDestination.CHAT) }
    var showScreenShareSheet by remember { mutableStateOf(false) }
    var permissionsList by remember { mutableStateOf(PermissionManager.getAllPermissions(context)) }

    // Recheck permissions and internet on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsList = PermissionManager.getAllPermissions(context)
                viewModel.checkInternet(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // State collections
    val chatMessages by viewModel.chatMessages.collectAsState()
    val commandInput by viewModel.commandInput.collectAsState()
    val isGeneratingResponse by viewModel.isGeneratingResponse.collectAsState()
    val isInternetConnected by viewModel.isInternetConnected.collectAsState()
    val showNoInternetDialog by viewModel.showNoInternetDialog.collectAsState()
    val savedApiKey by viewModel.savedApiKey.collectAsState()
    val effectiveApiKey by viewModel.effectiveApiKey.collectAsState()
    val isScreenShareActive by viewModel.isScreenShareActive.collectAsState()
    val isScreenSharePaused by viewModel.isScreenSharePaused.collectAsState()
    val latestScreenBitmap by viewModel.latestScreenBitmap.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val audioRms by viewModel.audioRms.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()

    // Permission launcher for Voice Audio Recording
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionsList = PermissionManager.getAllPermissions(context)
        if (isGranted) {
            viewModel.startVoiceCommand()
        }
    }

    // Permission launcher for Notifications
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        permissionsList = PermissionManager.getAllPermissions(context)
    }

    // MediaProjection screen capture launcher
    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            ScreenCaptureService.start(context, result.resultCode, result.data!!)
            Toast.makeText(context, "Aira Screen Share Started", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Screen share permission was denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Back handler
    BackHandler(enabled = drawerState.isOpen || currentScreen != AppDestination.CHAT || showScreenShareSheet) {
        if (showScreenShareSheet) {
            showScreenShareSheet = false
        } else if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            currentScreen = AppDestination.CHAT
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            AppDestination.CHAT -> {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AiraDrawerContent(
                            sessions = sessions,
                            currentSessionId = currentSessionId,
                            onSelectSession = { id ->
                                viewModel.selectSession(id)
                                scope.launch { drawerState.close() }
                            },
                            onNewChat = {
                                viewModel.createNewChat()
                                scope.launch { drawerState.close() }
                            },
                            onDeleteSession = { id ->
                                viewModel.deleteSession(id)
                            },
                            onOpenSettings = {
                                currentScreen = AppDestination.SETTINGS
                                scope.launch { drawerState.close() }
                            },
                            onOpenScreenShare = {
                                showScreenShareSheet = true
                                scope.launch { drawerState.close() }
                            },
                            isScreenShareActive = isScreenShareActive
                        )
                    }
                ) {
                    ChatScreen(
                        chatMessages = chatMessages,
                        commandInput = commandInput,
                        onCommandChange = { viewModel.onCommandInputChange(it) },
                        onSubmitCommand = { prompt ->
                            viewModel.submitCommand(prompt, activity)
                        },
                        onStopGeneration = { viewModel.stopGeneration() },
                        isGeneratingResponse = isGeneratingResponse,
                        isInternetConnected = isInternetConnected,
                        isKeyConfigured = effectiveApiKey.isNotBlank(),
                        isScreenShareActive = isScreenShareActive,
                        voiceState = voiceState,
                        audioRms = audioRms,
                        onVoiceToggle = {
                            if (voiceState == VoiceState.LISTENING) {
                                viewModel.stopVoiceCommand()
                            } else {
                                val permissionCheck = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                )
                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    viewModel.startVoiceCommand()
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        isMuted = isMuted,
                        onToggleMute = { viewModel.toggleMute() },
                        onOpenScreenShare = { showScreenShareSheet = true },
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNewChat = { viewModel.createNewChat() },
                        onRetryLastRequest = { viewModel.retryLastRequest(activity) },
                        onClearChat = { viewModel.clearChatHistory() }
                    )
                }
            }

            AppDestination.SETTINGS -> {
                SettingsScreen(
                    apiKey = savedApiKey,
                    onSaveApiKey = { viewModel.saveApiKey(it) },
                    onClearApiKey = { viewModel.clearApiKey() },
                    themeMode = themeMode,
                    onSelectThemeMode = { viewModel.setThemeMode(it) },
                    permissions = permissionsList,
                    isInternetConnected = isInternetConnected,
                    isScreenShareActive = isScreenShareActive,
                    onRequestMicrophone = {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onRequestNotification = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onRequestScreenShare = {
                        val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
                        projectionManager?.let {
                            screenCaptureLauncher.launch(it.createScreenCaptureIntent())
                        }
                    },
                    onStopScreenShare = {
                        viewModel.stopScreenShare(context)
                    },
                    onOpenAccessibilitySettings = {
                        viewModel.triggerAccessibilitySettings(context)
                    },
                    onOpenAppSettings = {
                        PermissionManager.openAppSettings(context)
                    },
                    onClearChatHistory = {
                        viewModel.clearChatHistory()
                    },
                    onNavigateBack = {
                        currentScreen = AppDestination.CHAT
                    }
                )
            }
        }

        // Live Screen Share Modal Sheet
        if (showScreenShareSheet) {
            ScreenShareSheet(
                isScreenShareActive = isScreenShareActive,
                isPaused = isScreenSharePaused,
                latestBitmap = latestScreenBitmap,
                onStartScreenShare = {
                    val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
                    projectionManager?.let {
                        screenCaptureLauncher.launch(it.createScreenCaptureIntent())
                    }
                },
                onPauseScreenShare = { viewModel.pauseScreenShare(context) },
                onResumeScreenShare = { viewModel.resumeScreenShare(context) },
                onStopScreenShare = { viewModel.stopScreenShare(context) },
                onAnalyzeScreen = { query ->
                    viewModel.submitCommand(query, activity)
                },
                onReadAllSentences = {
                    viewModel.submitCommand("Screen par jitne sentences likhe hain sab read karke explain karo", activity)
                },
                onDismiss = { showScreenShareSheet = false }
            )
        }

        // Mandatory No Internet Modal Dialog (Requirement 3)
        if (showNoInternetDialog) {
            NoInternetDialog(
                onRetry = { viewModel.retryConnection(context) },
                onDismiss = { viewModel.dismissNoInternetDialog() }
            )
        }
    }
}
