package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.accessibility.ScreenSnapshot
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.components.GestureControlPad
import com.example.ui.components.NeuralCoreOrb
import com.example.ui.components.ScreenReaderHUD
import com.example.ui.screens.ScreenShareDialog
import com.example.ui.theme.AiraAmber
import com.example.ui.theme.AiraCyan
import com.example.ui.theme.AiraGreen
import com.example.ui.theme.AiraPink
import com.example.ui.theme.AiraPurple
import com.example.voice.VoiceState

@Composable
fun MainConsoleScreen(
    statusBanner: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    isMuted: Boolean = false,
    onToggleMute: () -> Unit = {},
    isOnlineMode: Boolean,
    onToggleOnlineMode: () -> Unit,
    effectiveApiKey: String,
    onSetCustomApiKey: (String) -> Unit,
    isAccessibilityEnabled: Boolean,
    isExecuting: Boolean,
    screenSnapshot: ScreenSnapshot,
    commandInput: String,
    chatMessages: List<ChatMessage>,
    voiceState: VoiceState,
    audioRms: Float,
    grantedPermissionsCount: Int,
    totalPermissionsCount: Int,
    onOpenPermissionWindow: () -> Unit,
    onCommandChange: (String) -> Unit,
    onSubmitCommand: (String?) -> Unit,
    onVoiceToggle: () -> Unit,
    onAbortExecution: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenRoutines: () -> Unit,
    onScanScreenNow: () -> Unit,
    onReadAllScreenSentences: () -> Unit = {},
    onTouchElement: (String) -> Unit = {},
    onTouchCoordinates: (Float, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showScreenShareDialog by remember { mutableStateOf(false) }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceElevated = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Friend Header Card with Online Mode Pill & Theme Switcher
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_friend_banner"),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(AiraPurple.copy(alpha = 0.5f), AiraPink.copy(alpha = 0.5f))
                    )
                )
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(125.dp)) {
                    // Futuristic background art
                    Image(
                        painter = painterResource(id = R.drawable.aira_cyber_banner),
                        contentDescription = "AIRA Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = if (isDarkTheme) 0.32f else 0.16f
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, surfaceColor.copy(alpha = 0.92f))
                                )
                            )
                    )

                    // Header content
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(AiraPink.copy(alpha = 0.2f))
                                    .border(1.5.dp, AiraPink, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "A",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AiraPink,
                                    fontSize = 22.sp
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isOnlineMode) AiraGreen else AiraCyan)
                                    )
                                    Text(
                                        text = if (isOnlineMode) "AIRA ONLINE • GEMINI 3.5" else "AIRA OFFLINE • LOCAL CORE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isOnlineMode) AiraGreen else (if (isDarkTheme) AiraCyan else AiraPurple),
                                        letterSpacing = 1.2.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Aira (Your Best Friend)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Text(
                                    text = if (isOnlineMode) "Google API Key Active • Unlimited Tasks" else "100% On-Device Privacy (0 B Egress)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Right actions: Online Toggle + Theme Toggle + Permissions
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Screen Share & Auto-Touch Button
                                IconButton(
                                    onClick = { showScreenShareDialog = true },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(AiraCyan.copy(alpha = 0.2f))
                                        .border(1.dp, AiraCyan.copy(alpha = 0.6f), CircleShape)
                                        .testTag("screen_share_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ScreenShare,
                                        contentDescription = "Screen Share & Touch",
                                        tint = AiraCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Mute / Unmute Voice Button
                                IconButton(
                                    onClick = onToggleMute,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isMuted) AiraAmber.copy(alpha = 0.2f) else surfaceElevated)
                                        .border(1.dp, if (isMuted) AiraAmber else borderColor, CircleShape)
                                        .testTag("mute_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = if (isMuted) "Unmute Aira" else "Mute Aira",
                                        tint = if (isMuted) AiraAmber else AiraPink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Dark / Light Mode Toggle Button
                                IconButton(
                                    onClick = onToggleTheme,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(surfaceElevated)
                                        .border(1.dp, borderColor, CircleShape)
                                        .testTag("theme_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Theme",
                                        tint = if (isDarkTheme) AiraAmber else AiraPurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Key Settings Dialog Button
                                IconButton(
                                    onClick = { showApiKeyDialog = true },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(surfaceElevated)
                                        .border(1.dp, borderColor, CircleShape)
                                        .testTag("api_key_dialog_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = "Configure Google API Key",
                                        tint = if (effectiveApiKey.isNotBlank()) AiraGreen else AiraAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Permission Gateway Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (grantedPermissionsCount == totalPermissionsCount)
                                                AiraGreen.copy(alpha = 0.2f)
                                             else AiraAmber.copy(alpha = 0.2f)
                                        )
                                        .border(
                                            1.dp,
                                            if (grantedPermissionsCount == totalPermissionsCount)
                                                AiraGreen.copy(alpha = 0.6f)
                                             else AiraAmber.copy(alpha = 0.6f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable(onClick = onOpenPermissionWindow)
                                        .padding(horizontal = 7.dp, vertical = 5.dp)
                                        .testTag("open_permissions_button")
                                ) {
                                    Text(
                                        text = "$grantedPermissionsCount/$totalPermissionsCount",
                                        color = if (grantedPermissionsCount == totalPermissionsCount) AiraGreen else AiraAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Online Mode Pill Switcher
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isOnlineMode) AiraGreen.copy(alpha = 0.18f)
                                        else AiraPurple.copy(alpha = 0.18f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isOnlineMode) AiraGreen.copy(alpha = 0.5f)
                                        else AiraPurple.copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable(onClick = onToggleOnlineMode)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                    .testTag("online_mode_toggle")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isOnlineMode) Icons.Default.Cloud else Icons.Default.CloudOff,
                                        contentDescription = null,
                                        tint = if (isOnlineMode) AiraGreen else AiraPurple,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (isOnlineMode) "Online (Gemini)" else "Offline (Local)",
                                        color = if (isOnlineMode) AiraGreen else AiraPurple,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Neural Core Orb
            NeuralCoreOrb(
                isExecuting = isExecuting,
                isLocked = false,
                voiceState = voiceState,
                audioRms = audioRms,
                onClick = {
                    if (isExecuting) {
                        onAbortExecution()
                    } else {
                        onVoiceToggle()
                    }
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Spoken State Indicator
            Text(
                text = when {
                    voiceState == VoiceState.LISTENING -> "BOLO DOST, MAIN SUN RAHI HOON..."
                    voiceState == VoiceState.SPEAKING -> "AIRA BOL RAHI HAI..."
                    isExecuting -> "TASK COMPLETE KAR RAHI HOON..."
                    else -> "AIRA IS HERE • TAP KARKE BAAT KARO"
                },
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    voiceState == VoiceState.LISTENING -> AiraGreen
                    isExecuting -> AiraCyan
                    else -> textSecondary
                },
                letterSpacing = 1.1.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Live Friend Chat Log Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("friend_chat_card"),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(borderColor)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONVERSATION WITH AIRA",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDarkTheme) AiraPink else AiraPurple,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isOnlineMode) "Google Gemini 3.5 Online" else "Local Hinglish",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOnlineMode) AiraGreen else textSecondary,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chat messages list
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chatMessages.takeLast(4).forEach { msg ->
                            val isUser = msg.sender == MessageSender.USER
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.88f)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 14.dp,
                                                topEnd = 14.dp,
                                                bottomStart = if (isUser) 14.dp else 2.dp,
                                                bottomEnd = if (isUser) 2.dp else 14.dp
                                            )
                                        )
                                        .background(
                                            if (isUser) {
                                                if (isDarkTheme) AiraPurple.copy(alpha = 0.25f)
                                                else AiraPurple.copy(alpha = 0.12f)
                                            } else {
                                                surfaceElevated
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isUser) AiraPurple.copy(alpha = 0.4f) else borderColor,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = if (isUser) "You" else "Aira 💕",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUser) AiraCyan else AiraPink,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = msg.text,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textPrimary,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp
                                        )
                                        if (msg.actionSummary != null && msg.actionSummary != "NONE" && msg.actionSummary != "CHAT_ONLY") {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(AiraGreen.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "✓ Action Executed: ${msg.actionSummary}",
                                                    color = AiraGreen,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Natural Hinglish Quick Prompt Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickHinglishChip("🖥️ Screen Share & Touch") { showScreenShareDialog = true }
                QuickHinglishChip("🔊 Read Every Sentence") { onReadAllScreenSentences() }
                QuickHinglishChip("💬 Mom ko message karo") { onSubmitCommand("Mom ko message karo ki 10 minute me aa raha hoon") }
                QuickHinglishChip("👀 Screen padh ke batao") { onScanScreenNow() }
                QuickHinglishChip("⚙️ Settings kholo") { onSubmitCommand("open Settings") }
                QuickHinglishChip("🎵 YouTube chalao") { onSubmitCommand("YouTube chalao") }
                QuickHinglishChip("📸 Camera open karo") { onSubmitCommand("Camera kholo") }
                QuickHinglishChip("💡 Mujhe ek mast idea do") { onSubmitCommand("Aira mujhe koi mast creative idea do") }
                QuickHinglishChip("😊 Kaisi ho Aira?") { onSubmitCommand("Kaisi ho Aira?") }
                QuickHinglishChip("😂 Ek joke sunao") { onSubmitCommand("Aira koi mast joke sunao") }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Command / Chat Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(surfaceColor)
                    .border(
                        1.dp,
                        if (voiceState == VoiceState.LISTENING) AiraGreen
                        else if (isDarkTheme) AiraCyan.copy(alpha = 0.45f)
                        else AiraPurple.copy(alpha = 0.45f),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commandInput,
                    onValueChange = onCommandChange,
                    placeholder = {
                        Text(
                            if (isOnlineMode) "Google Gemini online active • Kuch bhi bolo..." else "Aira se baat karo ya koi kaam bolo...",
                            color = textSecondary,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("command_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    maxLines = 2
                )

                // Voice Mic Button
                IconButton(
                    onClick = onVoiceToggle,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (voiceState == VoiceState.LISTENING) AiraGreen.copy(alpha = 0.25f)
                            else surfaceElevated
                        )
                        .border(
                            1.dp,
                            if (voiceState == VoiceState.LISTENING) AiraGreen else borderColor,
                            CircleShape
                        )
                        .testTag("voice_command_button")
                ) {
                    Icon(
                        imageVector = if (voiceState == VoiceState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (voiceState == VoiceState.LISTENING) AiraGreen else (if (isDarkTheme) AiraCyan else AiraPurple),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Send Button
                IconButton(
                    onClick = { onSubmitCommand(null) },
                    enabled = commandInput.isNotBlank(),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (commandInput.isNotBlank()) {
                                if (isDarkTheme) AiraCyan else AiraPurple
                            } else surfaceElevated
                        )
                        .testTag("send_command_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send to Aira",
                        tint = if (commandInput.isNotBlank()) Color.White else textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Screen Reader HUD
            ScreenReaderHUD(
                snapshot = screenSnapshot,
                isAccessibilityEnabled = isAccessibilityEnabled,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                onScanScreenNow = onScanScreenNow
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Gesture Control Pad
            GestureControlPad(
                onCircleGesture = { onScanScreenNow() },
                onSwipeUp = { onOpenRoutines() },
                onSwipeDown = { onSubmitCommand("Aira mujhse baat karo") },
                onDoubleTap = { onAbortExecution() },
                onLongPress = { onVoiceToggle() }
            )

            Spacer(modifier = Modifier.height(110.dp))
        }

        // Google Gemini API Key Dialog
        if (showApiKeyDialog) {
            var inputKey by remember { mutableStateOf(effectiveApiKey) }

            AlertDialog(
                onDismissRequest = { showApiKeyDialog = false },
                containerColor = surfaceColor,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = AiraGreen)
                        Text("Google Gemini API Key", color = textPrimary)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Online Mode uses Google's latest Gemini 3.5 Flash model for reasoning, Q&A, and advanced autonomous tasks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )

                        if (effectiveApiKey.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AiraGreen.copy(alpha = 0.12f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "✓ Google API Key Detected & Ready",
                                    color = AiraGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedTextField(
                            value = inputKey,
                            onValueChange = { inputKey = it },
                            label = { Text("Google API Key") },
                            placeholder = { Text("AIzaSy...") },
                            modifier = Modifier.fillMaxWidth().testTag("api_key_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AiraCyan,
                                unfocusedBorderColor = borderColor,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
                            ),
                            maxLines = 2
                        )

                        Text(
                            text = "Keys can also be configured via Secrets panel in AI Studio (GEMINI_API_KEY).",
                            style = MaterialTheme.typography.labelSmall,
                            color = textSecondary,
                            fontSize = 10.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onSetCustomApiKey(inputKey.trim())
                            showApiKeyDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AiraCyan),
                        modifier = Modifier.testTag("save_api_key_button")
                    ) {
                        Text("Save & Connect", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showApiKeyDialog = false }) {
                        Text("Cancel", color = textSecondary)
                    }
                }
            )
        }

        // Live Screen Share & Auto-Touch Dialog
        if (showScreenShareDialog) {
            ScreenShareDialog(
                snapshot = screenSnapshot,
                isAccessibilityEnabled = isAccessibilityEnabled,
                onDismiss = { showScreenShareDialog = false },
                onRefreshScreen = onScanScreenNow,
                onReadAllSentences = onReadAllScreenSentences,
                onTouchElement = onTouchElement,
                onTouchCoordinates = onTouchCoordinates,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings
            )
        }
    }
}

@Composable
private fun QuickHinglishChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
