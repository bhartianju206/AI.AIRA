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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.neural.AutonomousExecutionPlan
import com.example.ui.components.CyberGridBackground
import com.example.ui.components.ExecutionPlanCard
import com.example.ui.components.GestureControlPad
import com.example.ui.components.NeuralCoreOrb
import com.example.ui.components.ScreenReaderHUD
import com.example.ui.theme.AiraAmber
import com.example.ui.theme.AiraCyan
import com.example.ui.theme.AiraGreen
import com.example.ui.theme.AiraPurple
import com.example.ui.theme.AiraRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.voice.VoiceState

@Composable
fun MainConsoleScreen(
    statusBanner: String,
    isAccessibilityEnabled: Boolean,
    isVaultLocked: Boolean,
    isExecuting: Boolean,
    currentStepIndex: Int,
    activePlan: AutonomousExecutionPlan?,
    screenSnapshot: ScreenSnapshot,
    commandInput: String,
    voiceState: VoiceState,
    audioRms: Float,
    grantedPermissionsCount: Int,
    totalPermissionsCount: Int,
    onOpenPermissionWindow: () -> Unit,
    onCommandChange: (String) -> Unit,
    onSubmitCommand: (String?) -> Unit,
    onVoiceToggle: () -> Unit,
    onAbortExecution: () -> Unit,
    onLockVault: () -> Unit,
    onUnlockVault: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenRoutines: () -> Unit,
    onScanScreenNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    CyberGridBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Hero Cyber Header Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_cyber_banner"),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(AiraCyan.copy(alpha = 0.6f), AiraPurple.copy(alpha = 0.6f))
                    )
                )
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(115.dp)) {
                    // Futuristic background art
                    Image(
                        painter = painterResource(id = R.drawable.aira_cyber_banner),
                        contentDescription = "AIRA Neural Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.38f
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, DarkSurfaceCard.copy(alpha = 0.95f))
                                )
                            )
                    )

                    // Content on top
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isVaultLocked) AiraRed else AiraGreen)
                                )
                                Text(
                                    text = "AIRA • OFFLINE NEURAL CORE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AiraCyan,
                                    letterSpacing = 1.4.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Autonomous Device Agent",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Hardware AES-256 GCM • 0.0 B Egress",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        // Right actions: Permissions Badge + Vault Lock Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Permission Gateway Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
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
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable(onClick = onOpenPermissionWindow)
                                    .padding(horizontal = 9.dp, vertical = 6.dp)
                                    .testTag("open_permissions_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Permissions",
                                        tint = if (grantedPermissionsCount == totalPermissionsCount) AiraGreen else AiraAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "$grantedPermissionsCount/$totalPermissionsCount",
                                        color = if (grantedPermissionsCount == totalPermissionsCount) AiraGreen else AiraAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Panic Lockdown Button
                            IconButton(
                                onClick = { if (isVaultLocked) onUnlockVault() else onLockVault() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isVaultLocked) AiraRed.copy(alpha = 0.25f)
                                        else DarkSurfaceElevated
                                    )
                                    .border(
                                        1.dp,
                                        if (isVaultLocked) AiraRed else DarkBorder,
                                        CircleShape
                                    )
                                    .testTag("vault_lock_button")
                            ) {
                                Icon(
                                    imageVector = if (isVaultLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = if (isVaultLocked) "Unlock Vault" else "Lock Vault",
                                    tint = if (isVaultLocked) AiraRed else AiraCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-time Telemetry Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TelemetryPill("EGRESS", "0.0 B", AiraGreen)
                TelemetryPill("ENGINE", "100% Local", AiraCyan)
                TelemetryPill("VAULT", "AES-256", AiraPurple)
                TelemetryPill("LATENCY", "8.2 ms", TextPrimary)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Glowing Interactive Neural Core Orb
            NeuralCoreOrb(
                isExecuting = isExecuting,
                isLocked = isVaultLocked,
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

            Spacer(modifier = Modifier.height(8.dp))

            // Holographic Status Subtitle
            Text(
                text = when {
                    isVaultLocked -> "VAULT LOCKED • BIOMETRIC VERIFICATION REQUIRED"
                    voiceState == VoiceState.LISTENING -> "AURAL RECOGNITION ACTIVE • SPEAK NOW..."
                    voiceState == VoiceState.SPEAKING -> "AIRA SYNTHESIZING VOICE RESPONSE..."
                    isExecuting -> "AUTONOMOUS REASONING & EXECUTION IN PROGRESS"
                    else -> "TAP OR HOLD TO ENGAGE OFFLINE VOICE COMMAND"
                },
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    isVaultLocked -> AiraRed
                    voiceState == VoiceState.LISTENING -> AiraGreen
                    isExecuting -> AiraCyan
                    else -> TextMuted
                },
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Futuristic Command Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceCard)
                    .border(
                        1.dp,
                        if (voiceState == VoiceState.LISTENING) AiraGreen
                        else AiraCyan.copy(alpha = 0.45f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commandInput,
                    onValueChange = onCommandChange,
                    placeholder = {
                        Text(
                            "Type or speak: text Mom, read screen, launch apps...",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("command_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
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
                            else DarkSurfaceElevated
                        )
                        .border(
                            1.dp,
                            if (voiceState == VoiceState.LISTENING) AiraGreen else DarkBorder,
                            CircleShape
                        )
                        .testTag("voice_command_button")
                ) {
                    Icon(
                        imageVector = if (voiceState == VoiceState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (voiceState == VoiceState.LISTENING) AiraGreen else AiraCyan,
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
                        .background(if (commandInput.isNotBlank()) AiraCyan else DarkSurfaceElevated)
                        .testTag("send_command_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Execute Command",
                        tint = if (commandInput.isNotBlank()) DarkBackground else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Prompt Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickCommandChip("💬 Text Mom: On my way") { onSubmitCommand("Text Mom that I'm on my way") }
                QuickCommandChip("🔍 Read & Summarize Screen") { onSubmitCommand("read screen and summarize") }
                QuickCommandChip("⚙️ Open Settings") { onSubmitCommand("open Settings") }
                QuickCommandChip("📸 Capture Screenshot") { onSubmitCommand("take screenshot") }
                QuickCommandChip("🏠 Return Home") { onSubmitCommand("go home") }
                QuickCommandChip("🔒 Panic Lock Vault") { onSubmitCommand("panic lock vault") }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Active Execution Plan Card
            if (activePlan != null) {
                ExecutionPlanCard(
                    plan = activePlan,
                    isExecuting = isExecuting,
                    currentStepIndex = currentStepIndex,
                    onAbort = onAbortExecution
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

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
                onSwipeDown = { onLockVault() },
                onDoubleTap = { onAbortExecution() },
                onLongPress = { onVoiceToggle() }
            )

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun TelemetryPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 9.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun QuickCommandChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
