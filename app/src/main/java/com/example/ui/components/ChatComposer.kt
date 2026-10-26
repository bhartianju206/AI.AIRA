package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AiraAmber
import com.example.ui.theme.AiraCyan
import com.example.ui.theme.AiraGreen
import com.example.ui.theme.AiraPink
import com.example.voice.VoiceState

@Composable
fun ChatComposer(
    commandInput: String,
    onCommandChange: (String) -> Unit,
    onSubmitCommand: (String?) -> Unit,
    onStopGeneration: () -> Unit,
    isGeneratingResponse: Boolean,
    voiceState: VoiceState,
    onVoiceToggle: () -> Unit,
    onOpenScreenShare: () -> Unit,
    onClearChat: () -> Unit,
    isKeyConfigured: Boolean,
    modifier: Modifier = Modifier
) {
    var showAttachmentMenu by remember { mutableStateOf(false) }

    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val composerBg = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    val selectionColors = remember(primaryColor) {
        TextSelectionColors(
            handleColor = primaryColor,
            backgroundColor = primaryColor.copy(alpha = 0.35f)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("chat_composer_container")
    ) {
        // Rounded ChatGPT-style input bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(composerBg)
                .border(
                    width = 1.dp,
                    color = if (voiceState == VoiceState.LISTENING) AiraGreen
                    else if (isGeneratingResponse) AiraCyan.copy(alpha = 0.6f)
                    else outlineColor,
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attachment / Plus (+) Button
                Box {
                    IconButton(
                        onClick = { showAttachmentMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                            .testTag("chat_attachment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attachment options",
                            tint = onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Attachment Menu
                    DropdownMenu(
                        expanded = showAttachmentMenu,
                        onDismissRequest = { showAttachmentMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Live Screen Share", fontWeight = FontWeight.Medium) },
                            onClick = {
                                showAttachmentMenu = false
                                onOpenScreenShare()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ask about my screen", fontWeight = FontWeight.Medium) },
                            onClick = {
                                showAttachmentMenu = false
                                onSubmitCommand("Screen dekh kar batao kya likha hai?")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Chat", color = AiraAmber, fontWeight = FontWeight.Medium) },
                            onClick = {
                                showAttachmentMenu = false
                                onClearChat()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Text Input Field (Fixed visibility bug: explicit textStyle, onSurface color, explicit cursor)
                CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                    OutlinedTextField(
                        value = commandInput,
                        onValueChange = onCommandChange,
                        placeholder = {
                            Text(
                                text = if (!isKeyConfigured) "Settings me Gemini key dalein..."
                                else "Ask Aira anything...",
                                color = onSurfaceVariant.copy(alpha = 0.65f),
                                fontSize = 14.sp
                            )
                        },
                        textStyle = TextStyle(
                            color = onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 21.sp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = onSurface,
                            unfocusedTextColor = onSurface,
                            disabledTextColor = onSurfaceVariant,
                            cursorColor = primaryColor,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        maxLines = 5,
                        enabled = !isGeneratingResponse
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Action Button: Stop Generation OR Voice Mic OR Send Button
                if (isGeneratingResponse) {
                    // STOP GENERATION BUTTON (ChatGPT style filled rounded square)
                    IconButton(
                        onClick = onStopGeneration,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AiraPink)
                            .testTag("chat_stop_generation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop generation",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else if (commandInput.isNotBlank()) {
                    // SEND BUTTON
                    IconButton(
                        onClick = { onSubmitCommand(null) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                } else {
                    // MICROPHONE BUTTON
                    IconButton(
                        onClick = onVoiceToggle,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (voiceState == VoiceState.LISTENING) AiraGreen.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            )
                            .testTag("chat_voice_mic_button")
                    ) {
                        Icon(
                            imageVector = if (voiceState == VoiceState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Voice input",
                            tint = if (voiceState == VoiceState.LISTENING) AiraGreen else onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
