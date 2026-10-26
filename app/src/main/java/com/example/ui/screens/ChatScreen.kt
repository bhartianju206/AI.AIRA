package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.material.icons.filled.ScreenShare
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
    voiceState: VoiceState,
    audioRms: Float,
    onVoiceToggle: () -> Unit,
    isOnlineMode: Boolean,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    isDarkTheme: Boolean,
    onOpenScreenShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    // Auto-scroll to bottom on new message
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Chat Header with Logo & Status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("chat_header_card"),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(18.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(borderColor)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_aira_logo),
                        contentDescription = "Aira Avatar",
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
                                text = "Aira",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnlineMode) AiraGreen else AiraCyan)
                            )
                        }
                        Text(
                            text = if (isOnlineMode) "Online • Hindi Accent • Hinglish" else "Local Core • Hinglish",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOnlineMode) AiraGreen else textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onOpenScreenShare,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AiraCyan.copy(alpha = 0.2f))
                            .border(1.dp, AiraCyan.copy(alpha = 0.6f), CircleShape)
                            .testTag("chat_screen_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenShare,
                            contentDescription = "Screen Share",
                            tint = AiraCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) AiraAmber.copy(alpha = 0.2f) else surfaceVariant)
                            .border(1.dp, if (isMuted) AiraAmber else borderColor, CircleShape)
                            .testTag("chat_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = if (isMuted) AiraAmber else AiraPink,
                            modifier = Modifier.size(16.dp)
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
                            text = "Aira sun rahi hai... (Speak now)",
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

        // Chat Timeline
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { message ->
                ChatBubble(message = message, isDarkTheme = isDarkTheme)
            }
        }

        // Hinglish Quick Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ChatChip("Kaisi ho Aira?") { onSubmitCommand("Kaisi ho Aira?") }
            ChatChip("Kya kar rahi ho?") { onSubmitCommand("Kya kar rahi ho?") }
            ChatChip("Ek romantic joke sunao") { onSubmitCommand("Aira mujhe koi funny joke sunao") }
            ChatChip("Mujhe thoda motivate karo") { onSubmitCommand("Aira mujhe motivate karo") }
            ChatChip("YouTube chalao") { onSubmitCommand("YouTube chalao") }
            ChatChip("Camera kholo") { onSubmitCommand("Camera open karo") }
            ChatChip("Screen padh ke batao") { onSubmitCommand("Screen padh ke batao") }
        }

        // Chat Input Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(surfaceColor)
                .border(
                    1.dp,
                    if (voiceState == VoiceState.LISTENING) AiraGreen
                    else if (isDarkTheme) AiraCyan.copy(alpha = 0.45f)
                    else AiraPurple.copy(alpha = 0.45f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commandInput,
                onValueChange = onCommandChange,
                placeholder = {
                    Text(
                        if (isOnlineMode) "Aira se kuch bhi pucho ya kaam bolo..." else "Hinglish me baat karo ya command do...",
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
                maxLines = 3
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (commandInput.isNotBlank()) {
                            if (isDarkTheme) AiraCyan else AiraPurple
                        } else surfaceVariant
                    )
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (commandInput.isNotBlank()) Color.White else textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    isDarkTheme: Boolean
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
                .fillMaxWidth(0.82f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) {
                        if (isDarkTheme) AiraPurple.copy(alpha = 0.35f) else AiraPurple.copy(alpha = 0.15f)
                    } else {
                        surfaceVariant
                    }
                )
                .border(
                    1.dp,
                    if (isUser) AiraPurple.copy(alpha = 0.5f) else borderColor,
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
                        text = if (isUser) "You" else "Aira 💕",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) AiraCyan else AiraPink,
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
                            text = "✓ Completed: ${message.actionSummary}",
                            color = AiraGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
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
