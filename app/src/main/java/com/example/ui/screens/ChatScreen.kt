package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.theme.AiraAmber
import com.example.ui.theme.AiraCyan
import com.example.ui.theme.AiraGreen
import com.example.ui.theme.AiraPink
import com.example.ui.theme.AiraPurple
import com.example.ui.theme.TextMuted
import com.example.voice.VoiceState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    chatMessages: List<ChatMessage>,
    commandInput: String,
    onCommandChange: (String) -> Unit,
    onSubmitCommand: (String?) -> Unit,
    isGeneratingResponse: Boolean,
    isInternetConnected: Boolean,
    isKeyConfigured: Boolean,
    isScreenShareActive: Boolean,
    voiceState: VoiceState,
    audioRms: Float,
    onVoiceToggle: () -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onOpenScreenShare: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetryLastRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    // Auto-scroll on new message or when generating starts
    LaunchedEffect(chatMessages.size, isGeneratingResponse) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 14.dp)
            .testTag("chat_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // CLEAN TOP HEADER (Logo, "Aira AI v1.2", Status, Screen Share & Settings)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("chat_top_header"),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(borderColor)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_aira_logo),
                        contentDescription = "Aira AI Logo",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, AiraPink, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Aira AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AiraPink.copy(alpha = 0.18f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "v1.2",
                                    color = AiraPink,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        // Status indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (!isInternetConnected) AiraAmber
                                        else if (isKeyConfigured) AiraGreen
                                        else AiraCyan
                                    )
                            )
                            Text(
                                text = if (!isInternetConnected) "No Internet"
                                else if (isKeyConfigured) "Gemini 3.5 Ready"
                                else "Key Required in Settings",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (!isInternetConnected) AiraAmber
                                else if (isKeyConfigured) AiraGreen
                                else AiraCyan,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Right: Action Buttons (Mute, Screen Share, Settings)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Mute Voice Button
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) AiraAmber.copy(alpha = 0.2f) else surfaceVariant)
                            .border(1.dp, if (isMuted) AiraAmber else borderColor, CircleShape)
                            .testTag("chat_mute_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Unmute Aira" else "Mute Aira",
                            tint = if (isMuted) AiraAmber else AiraPink,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Screen Share Button
                    IconButton(
                        onClick = onOpenScreenShare,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isScreenShareActive) AiraCyan.copy(alpha = 0.3f)
                                else surfaceVariant
                            )
                            .border(
                                1.dp,
                                if (isScreenShareActive) AiraCyan else borderColor,
                                CircleShape
                            )
                            .testTag("chat_screen_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenShare,
                            contentDescription = "Screen Share",
                            tint = if (isScreenShareActive) AiraCyan else textPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Settings Button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(surfaceVariant)
                            .border(1.dp, borderColor, CircleShape)
                            .testTag("chat_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = textPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }

        // Voice Listening Waveform Banner if active
        if (voiceState == VoiceState.LISTENING) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AiraGreen.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(AiraGreen.copy(alpha = 0.5f))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aira sun rahi hai... (Speak in Hindi or English)",
                            color = AiraGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(audioRms * 10).toInt()} dB",
                            color = AiraGreen,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (audioRms / 10f).coerceIn(0.1f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = AiraGreen,
                        trackColor = AiraGreen.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // Screen Share active pill
        if (isScreenShareActive) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AiraCyan.copy(alpha = 0.12f))
                    .border(1.dp, AiraCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .clickable(onClick = onOpenScreenShare)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenShare,
                        contentDescription = null,
                        tint = AiraCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Live Screen Share Active: Aira can see your screen",
                        color = AiraCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "Controls →",
                    color = AiraCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // MAIN CHAT TIMELINE
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { message ->
                ChatBubbleItem(
                    message = message,
                    onRetry = onRetryLastRequest
                )
            }

            // Typing / Thinking indicator when generating
            if (isGeneratingResponse) {
                item {
                    TypingIndicatorBubble()
                }
            }
        }

        // Quick Hinglish Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ChatChip("Kaisi ho Aira?") { onSubmitCommand("Kaisi ho Aira?") }
            ChatChip("Screen dekho kya likha hai") { onSubmitCommand("Screen dekh kar batao kya likha hai?") }
            ChatChip("Ek funny joke sunao") { onSubmitCommand("Aira mujhe koi funny joke sunao") }
            ChatChip("Explain photosynthesis") { onSubmitCommand("Can you explain photosynthesis simply?") }
            ChatChip("Write a Python script") { onSubmitCommand("Write a Python script for web scraping") }
            ChatChip("Open YouTube") { onSubmitCommand("Open YouTube") }
        }

        // CHAT INPUT BOX
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(surfaceColor)
                .border(
                    1.dp,
                    if (voiceState == VoiceState.LISTENING) AiraGreen
                    else borderColor,
                    RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commandInput,
                onValueChange = onCommandChange,
                placeholder = {
                    Text(
                        text = if (!isKeyConfigured) "Pehle Settings me Gemini API key daalo..."
                        else "Aira se kuch bhi pucho ya kaam bolo...",
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary
                ),
                maxLines = 3,
                enabled = !isGeneratingResponse
            )

            // Voice Mic Button
            IconButton(
                onClick = onVoiceToggle,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (voiceState == VoiceState.LISTENING) AiraGreen.copy(alpha = 0.25f)
                        else surfaceVariant
                    )
                    .border(
                        1.dp,
                        if (voiceState == VoiceState.LISTENING) AiraGreen else borderColor,
                        CircleShape
                    )
                    .testTag("chat_voice_mic_button")
            ) {
                Icon(
                    imageVector = if (voiceState == VoiceState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = if (voiceState == VoiceState.LISTENING) AiraGreen else AiraPink,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Send Button (Disabled while generating to prevent duplicate requests)
            IconButton(
                onClick = { onSubmitCommand(null) },
                enabled = commandInput.isNotBlank() && !isGeneratingResponse,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (commandInput.isNotBlank() && !isGeneratingResponse) AiraPink
                        else surfaceVariant
                    )
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (commandInput.isNotBlank() && !isGeneratingResponse) Color.White else textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    onRetry: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Image(
                painter = painterResource(id = R.drawable.ic_aira_logo),
                contentDescription = "Aira",
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(1.dp, AiraPink, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (message.isError) {
                        AiraAmber.copy(alpha = 0.15f)
                    } else if (isUser) {
                        AiraPink.copy(alpha = 0.20f)
                    } else {
                        surfaceVariant
                    }
                )
                .border(
                    1.dp,
                    if (message.isError) AiraAmber.copy(alpha = 0.6f)
                    else if (isUser) AiraPink.copy(alpha = 0.45f)
                    else borderColor,
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (message.isError) "Error ⚠" else if (isUser) "You" else "Aira AI 💕",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (message.isError) AiraAmber else if (isUser) AiraPink else AiraCyan,
                        fontSize = 11.sp
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                if (message.actionSummary != null && message.actionSummary != "NONE" && message.actionSummary != "CHAT_ONLY") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AiraGreen.copy(alpha = 0.18f))
                            .border(1.dp, AiraGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "✓ Action: ${message.actionSummary}",
                            color = AiraGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (message.isError && message.canRetry) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onRetry,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AiraAmber),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry Request", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingIndicatorBubble() {
    val transition = rememberInfiniteTransition(label = "typing_dots")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_aira_logo),
            contentDescription = "Aira",
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .border(1.dp, AiraPink, CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Aira soch rahi hai",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AiraPink.copy(alpha = alpha))
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AiraCyan.copy(alpha = alpha))
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AiraPurple.copy(alpha = alpha))
                )
            }
        }
    }
}

@Composable
private fun ChatChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
