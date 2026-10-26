package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.components.AiraMessageRenderer
import com.example.ui.components.ChatComposer
import com.example.ui.theme.AiraAmber
import com.example.ui.theme.AiraCyan
import com.example.ui.theme.AiraGreen
import com.example.ui.theme.AiraPink
import com.example.ui.theme.AiraPurple
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
    onStopGeneration: () -> Unit,
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
    onOpenDrawer: () -> Unit,
    onNewChat: () -> Unit,
    onRetryLastRequest: () -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val hasUserMessages = chatMessages.any { it.sender == MessageSender.USER }

    // Auto-scroll on new message
    LaunchedEffect(chatMessages.size, isGeneratingResponse) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("chat_screen")
    ) {
        // COMPACT CHATGPT-STYLE TOP APP BAR
        ChatGPTTopBar(
            onOpenDrawer = onOpenDrawer,
            onNewChat = onNewChat,
            onOpenScreenShare = onOpenScreenShare,
            isScreenShareActive = isScreenShareActive,
            isInternetConnected = isInternetConnected,
            isKeyConfigured = isKeyConfigured,
            isMuted = isMuted,
            onToggleMute = onToggleMute
        )

        // Screen Share active pill indicator
        AnimatedVisibility(visible = isScreenShareActive) {
            ScreenShareActiveBanner(onOpenScreenShare = onOpenScreenShare)
        }

        // Voice waveform indicator when listening
        AnimatedVisibility(visible = voiceState == VoiceState.LISTENING) {
            VoiceListeningBanner(audioRms = audioRms)
        }

        // MAIN CONTENT: Empty State OR Conversation Timeline
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (!hasUserMessages) {
                // ChatGPT "What can I help with?" Empty State
                ChatGPTEmptyState(
                    onSelectPrompt = { prompt -> onSubmitCommand(prompt) },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Message List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 12.dp)
                ) {
                    items(chatMessages, key = { it.id }) { message ->
                        if (message.sender == MessageSender.USER) {
                            UserMessageBubble(message = message)
                        } else {
                            AiraMessageBubble(
                                message = message,
                                onRetry = onRetryLastRequest
                            )
                        }
                    }

                    if (isGeneratingResponse) {
                        item {
                            TypingIndicatorBubble()
                        }
                    }
                }
            }
        }

        // CHATGPT-STYLE MESSAGE COMPOSER
        ChatComposer(
            commandInput = commandInput,
            onCommandChange = onCommandChange,
            onSubmitCommand = onSubmitCommand,
            onStopGeneration = onStopGeneration,
            isGeneratingResponse = isGeneratingResponse,
            voiceState = voiceState,
            onVoiceToggle = onVoiceToggle,
            onOpenScreenShare = onOpenScreenShare,
            onClearChat = onClearChat,
            isKeyConfigured = isKeyConfigured
        )
    }
}

@Composable
private fun ChatGPTTopBar(
    onOpenDrawer: () -> Unit,
    onNewChat: () -> Unit,
    onOpenScreenShare: () -> Unit,
    isScreenShareActive: Boolean,
    isInternetConnected: Boolean,
    isKeyConfigured: Boolean,
    isMuted: Boolean,
    onToggleMute: () -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Navigation Drawer Hamburger
        IconButton(
            onClick = onOpenDrawer,
            modifier = Modifier
                .size(40.dp)
                .testTag("chat_menu_button")
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Open navigation drawer",
                tint = onSurface,
                modifier = Modifier.size(22.dp)
            )
        }

        // Center: App title and subtle status dot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Aira AI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = onSurface,
                fontSize = 17.sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(AiraPink.copy(alpha = 0.18f))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "v1.2",
                    color = AiraPink,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(
                        if (!isInternetConnected) AiraAmber
                        else if (isKeyConfigured) AiraGreen
                        else AiraCyan
                    )
            )
        }

        // Right actions: Mute toggle, Screen Share, New Chat
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("chat_mute_toggle_button")
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = if (isMuted) "Unmute voice" else "Mute voice",
                    tint = if (isMuted) AiraAmber else onSurface,
                    modifier = Modifier.size(19.dp)
                )
            }

            IconButton(
                onClick = onOpenScreenShare,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("chat_screen_share_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ScreenShare,
                    contentDescription = "Screen Share",
                    tint = if (isScreenShareActive) AiraCyan else onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onNewChat,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("chat_new_chat_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New chat",
                    tint = onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatGPTEmptyState(
    onSelectPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val cardBorder = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Logo
        Image(
            painter = painterResource(id = R.drawable.ic_aira_logo),
            contentDescription = "Aira AI",
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .border(2.dp, AiraPink, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "What can I help with?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = onSurface,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Aira AI v1.2 with Gemini 3.5 & Live Screen Vision",
            style = MaterialTheme.typography.bodyMedium,
            color = onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 4 Suggestion Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PromptStarterCard(
                icon = "💡",
                title = "Explain quantum computing simply",
                cardBg = cardBg,
                cardBorder = cardBorder,
                onClick = { onSelectPrompt("Explain quantum computing in simple words with examples.") }
            )
            PromptStarterCard(
                icon = "🖥️",
                title = "Look at my screen & explain what's on it",
                cardBg = cardBg,
                cardBorder = cardBorder,
                onClick = { onSelectPrompt("Screen dekho aur batao kya likha hai?") }
            )
            PromptStarterCard(
                icon = "💻",
                title = "Write a clean Kotlin coroutine function",
                cardBg = cardBg,
                cardBorder = cardBorder,
                onClick = { onSelectPrompt("Write a clean Kotlin coroutine function with error handling.") }
            )
            PromptStarterCard(
                icon = "💬",
                title = "Aira mujhe koi funny joke sunao",
                cardBg = cardBg,
                cardBorder = cardBorder,
                onClick = { onSelectPrompt("Aira mujhe koi mast funny joke sunao!") }
            )
        }
    }
}

@Composable
private fun PromptStarterCard(
    icon: String,
    title: String,
    cardBg: Color,
    cardBorder: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(cardBorder)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = icon, fontSize = 18.sp)
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun UserMessageBubble(message: ChatMessage) {
    val bubbleColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val onSurface = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 4.dp
                    )
                )
                .background(bubbleColor)
                .border(1.dp, borderColor, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = onSurface,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun AiraMessageBubble(
    message: ChatMessage,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // AI Identity Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_aira_logo),
                contentDescription = "Aira AI",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(1.dp, AiraPink, CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(
                text = if (message.isError) "Aira (Error)" else "Aira AI",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (message.isError) AiraAmber else onSurface
            )
            Text(
                text = formattedTime,
                fontSize = 10.sp,
                color = onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        // Body content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AiraMessageRenderer(text = message.text)

                // Action Summary Badge
                if (message.actionSummary != null && message.actionSummary != "NONE" && message.actionSummary != "CHAT_ONLY") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AiraGreen.copy(alpha = 0.16f))
                            .border(1.dp, AiraGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "✓ Action Executed: ${message.actionSummary}",
                            color = AiraGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Retry Button if error
                if (message.isError && message.canRetry) {
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
                        Text("Retry Request", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Copy Action Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("Aira Response", message.text)
                            clipboard?.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Aira is thinking",
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

@Composable
private fun ScreenShareActiveBanner(onOpenScreenShare: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
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
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun VoiceListeningBanner(audioRms: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
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
                    text = "Aira listening... Speak now",
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
