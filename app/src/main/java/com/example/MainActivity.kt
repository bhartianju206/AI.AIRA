package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.MemoryRepository
import com.example.data.db.AiraDatabase
import com.example.neural.AiraFriendBrain
import com.example.security.PermissionItem
import com.example.security.PermissionManager
import com.example.ui.screens.AuditLogScreen
import com.example.ui.screens.AutomationRoutinesSheet
import com.example.ui.screens.MainConsoleScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.PermissionCenterModal
import com.example.ui.theme.AiraCyan
import com.example.ui.theme.AiraPurple
import com.example.ui.theme.AiraTheme
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.AiraViewModel
import com.example.voice.VoiceCommandController
import com.example.voice.VoiceState

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: AiraViewModel
    private var voiceController: VoiceCommandController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AiraDatabase.getDatabase(this)
        val repository = MemoryRepository(database.airaDao())
        val friendBrain = AiraFriendBrain(repository)

        voiceController = VoiceCommandController(this) { command ->
            viewModel.submitCommand(command, this)
        }

        viewModel = AiraViewModel(
            repository = repository,
            friendBrain = friendBrain,
            voiceController = voiceController
        )

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            AiraTheme(isDarkTheme = isDarkTheme) {
                AiraApp(
                    activity = this,
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
fun AiraApp(
    activity: ComponentActivity,
    viewModel: AiraViewModel,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var showPermissionWindow by remember { mutableStateOf(false) }
    var permissionsList by remember { mutableStateOf(PermissionManager.getAllPermissions(context)) }

    // Recheck permissions whenever the app resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsList = PermissionManager.getAllPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // State collections
    val statusBanner by viewModel.statusBanner.collectAsState()
    val isAccessibilityEnabled by viewModel.isAccessibilityConnected.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()
    val screenSnapshot by viewModel.screenSnapshot.collectAsState()
    val commandInput by viewModel.commandInput.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val audioRms by viewModel.audioRms.collectAsState()

    val isMuted by viewModel.isMuted.collectAsState()
    val isOnlineMode by viewModel.isOnlineMode.collectAsState()
    val effectiveApiKey by viewModel.effectiveApiKey.collectAsState()

    val memories by viewModel.memories.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val routines by viewModel.routines.collectAsState()

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
    ) { _ ->
        permissionsList = PermissionManager.getAllPermissions(context)
    }

    // Predictive back handler
    BackHandler(enabled = selectedTab != 0 || showPermissionWindow) {
        if (showPermissionWindow) {
            showPermissionWindow = false
        } else {
            selectedTab = 0
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(68.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .testTag("aira_bottom_navigation"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val activeColor = if (isDarkTheme) AiraCyan else AiraPurple

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Memory else Icons.Outlined.Memory,
                            contentDescription = "Console",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Friend Aira", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = activeColor,
                        indicatorColor = activeColor,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_console")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.AutoMirrored.Filled.Chat else Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = "Chat",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Chat", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = activeColor,
                        indicatorColor = activeColor,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_chat")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.Bolt else Icons.Outlined.Bolt,
                            contentDescription = "Routines",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Tasks", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = activeColor,
                        indicatorColor = activeColor,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_routines")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.Shield else Icons.Outlined.Shield,
                            contentDescription = "Audit",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Privacy", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = activeColor,
                        indicatorColor = activeColor,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_audit")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> MainConsoleScreen(
                    statusBanner = statusBanner,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleTheme() },
                    isMuted = isMuted,
                    onToggleMute = { viewModel.toggleMute() },
                    isOnlineMode = isOnlineMode,
                    onToggleOnlineMode = { viewModel.toggleOnlineMode() },
                    effectiveApiKey = effectiveApiKey,
                    onSetCustomApiKey = { viewModel.setCustomApiKey(it) },
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    isExecuting = isExecuting,
                    screenSnapshot = screenSnapshot,
                    commandInput = commandInput,
                    chatMessages = chatMessages,
                    voiceState = voiceState,
                    audioRms = audioRms,
                    grantedPermissionsCount = permissionsList.count { it.isGranted },
                    totalPermissionsCount = permissionsList.size,
                    onOpenPermissionWindow = { showPermissionWindow = true },
                    onCommandChange = { viewModel.onCommandInputChange(it) },
                    onSubmitCommand = { prompt ->
                        viewModel.submitCommand(prompt, activity)
                    },
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
                    onAbortExecution = { viewModel.cancelExecution() },
                    onOpenAccessibilitySettings = { viewModel.triggerAccessibilitySettings(context) },
                    onOpenRoutines = { selectedTab = 2 },
                    onScanScreenNow = { viewModel.summarizeScreenNow(activity) },
                    onReadAllScreenSentences = { viewModel.readAllScreenSentences(activity) },
                    onTouchElement = { element -> viewModel.touchScreenElement(element, activity) },
                    onTouchCoordinates = { x, y -> viewModel.touchScreenCoordinates(x, y, activity) },
                    onNavigateToChat = { selectedTab = 1 }
                )

                1 -> ChatScreen(
                    chatMessages = chatMessages,
                    commandInput = commandInput,
                    onCommandChange = { viewModel.onCommandInputChange(it) },
                    onSubmitCommand = { prompt -> viewModel.submitCommand(prompt, activity) },
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
                    isOnlineMode = isOnlineMode,
                    isMuted = isMuted,
                    onToggleMute = { viewModel.toggleMute() },
                    isDarkTheme = isDarkTheme,
                    onOpenScreenShare = { selectedTab = 0 }
                )

                2 -> AutomationRoutinesSheet(
                    routines = routines,
                    onRunRoutine = { routine ->
                        viewModel.submitCommand(routine.triggerPhrase, activity)
                        selectedTab = 0
                    },
                    onToggleRoutine = { viewModel.toggleRoutine(it) }
                )

                3 -> AuditLogScreen(
                    auditLogs = auditLogs
                )
            }

            // Dedicated System Permission Gateway Window Modal
            if (showPermissionWindow) {
                PermissionCenterModal(
                    permissions = permissionsList,
                    onDismiss = { showPermissionWindow = false },
                    onRequestAudio = {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onRequestNotification = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onGrantAll = {
                        val ungranted = permissionsList.filter { !it.isGranted }
                        if (ungranted.any { it.id == "audio" }) {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else if (ungranted.any { it.id == "notifications" } && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else if (ungranted.any { it.id == "accessibility" }) {
                            PermissionManager.openAccessibilitySettings(context)
                        } else if (ungranted.any { it.id == "overlay" }) {
                            PermissionManager.openOverlaySettings(context)
                        } else {
                            PermissionManager.openAppSettings(context)
                        }
                    }
                )
            }
        }
    }
}
