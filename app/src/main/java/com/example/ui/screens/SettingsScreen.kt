package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.PermissionItem
import com.example.ui.theme.AiraAmber
import com.example.ui.theme.AiraCyan
import com.example.ui.theme.AiraGreen
import com.example.ui.theme.AiraPink
import com.example.ui.theme.AiraPurple

@Composable
fun SettingsScreen(
    apiKey: String,
    onSaveApiKey: (String) -> Unit,
    onClearApiKey: () -> Unit,
    themeMode: String,
    onSelectThemeMode: (String) -> Unit,
    permissions: List<PermissionItem>,
    isInternetConnected: Boolean,
    isScreenShareActive: Boolean,
    onRequestMicrophone: () -> Unit,
    onRequestNotification: () -> Unit,
    onRequestScreenShare: () -> Unit,
    onStopScreenShare: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onClearChatHistory: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputKey by remember(apiKey) { mutableStateOf(apiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf(false) }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceElevated = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(borderColor)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("settings_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textPrimary
                            )
                        }
                        Column {
                            Text(
                                text = "Settings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "Aira AI v1.2 Configuration",
                                style = MaterialTheme.typography.labelSmall,
                                color = textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AiraPink.copy(alpha = 0.15f))
                            .border(1.dp, AiraPink.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "v1.2",
                            color = AiraPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. GEMINI API KEY SECTION
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_api_key_section"),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(borderColor)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = AiraCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Gemini API Key",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }

                        val isKeyConfigured = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isKeyConfigured) AiraGreen.copy(alpha = 0.18f)
                                    else AiraAmber.copy(alpha = 0.18f)
                                )
                                .border(
                                    1.dp,
                                    if (isKeyConfigured) AiraGreen.copy(alpha = 0.5f)
                                    else AiraAmber.copy(alpha = 0.5f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isKeyConfigured) "Key Configured ✓" else "No Key Set ⚠",
                                color = if (isKeyConfigured) AiraGreen else AiraAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter your Google Gemini API key to power Aira AI. Your key is stored securely on device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = {
                            inputKey = it
                            saveSuccessMessage = false
                        },
                        label = { Text("API Key (AIzaSy...)") },
                        placeholder = { Text("Paste your Gemini API key") },
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isKeyVisible) "Hide Key" else "Show Key",
                                    tint = textSecondary
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input_field"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = AiraCyan,
                            unfocusedBorderColor = borderColor
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onSaveApiKey(inputKey)
                            saveSuccessMessage = true
                        })
                    )

                    if (saveSuccessMessage) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "✓ API key saved successfully!",
                            color = AiraGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveApiKey(inputKey)
                                saveSuccessMessage = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AiraCyan),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_api_key_button")
                        ) {
                            Text(
                                text = "Save Key",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (apiKey.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    inputKey = ""
                                    onClearApiKey()
                                    saveSuccessMessage = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("clear_api_key_button")
                            ) {
                                Text(
                                    text = "Clear Key",
                                    color = AiraPink,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. THEME / APPEARANCE CONTROLS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_appearance_section"),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(borderColor)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = AiraPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Appearance & Theme",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Choose your preferred color theme. Saved automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionCard(
                            label = "Dark",
                            isSelected = themeMode == "DARK",
                            onClick = { onSelectThemeMode("DARK") },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionCard(
                            label = "Light",
                            isSelected = themeMode == "LIGHT",
                            onClick = { onSelectThemeMode("LIGHT") },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionCard(
                            label = "System",
                            isSelected = themeMode == "SYSTEM",
                            onClick = { onSelectThemeMode("SYSTEM") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. PERMISSIONS MANAGEMENT
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_permissions_section"),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(borderColor)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AiraGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Permissions & Services",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manage device features needed for voice, notifications, and live screen analysis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Internet status row
                    PermissionRowItem(
                        name = "Internet Connection",
                        description = "Mandatory for Gemini AI responses & vision",
                        isGranted = isInternetConnected,
                        actionLabel = if (isInternetConnected) "Active" else "Offline",
                        onAction = {}
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Microphone status row
                    val micPerm = permissions.find { it.id == "audio" }
                    PermissionRowItem(
                        name = "Microphone (Voice)",
                        description = "Enables hands-free voice speech recognition",
                        isGranted = micPerm?.isGranted == true,
                        actionLabel = if (micPerm?.isGranted == true) "Granted" else "Allow",
                        onAction = { if (micPerm?.isGranted != true) onRequestMicrophone() }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Screen Share status row
                    PermissionRowItem(
                        name = "Screen Capture (MediaProjection)",
                        description = "Allows Aira to see and inspect screen content",
                        isGranted = isScreenShareActive,
                        actionLabel = if (isScreenShareActive) "Stop" else "Start",
                        onAction = {
                            if (isScreenShareActive) onStopScreenShare() else onRequestScreenShare()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Notification status row
                    val notifPerm = permissions.find { it.id == "notifications" }
                    PermissionRowItem(
                        name = "Notifications",
                        description = "Foreground status for screen share and alerts",
                        isGranted = notifPerm?.isGranted == true,
                        actionLabel = if (notifPerm?.isGranted == true) "Granted" else "Allow",
                        onAction = { if (notifPerm?.isGranted != true) onRequestNotification() }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Accessibility service row
                    val accessPerm = permissions.find { it.id == "accessibility" }
                    PermissionRowItem(
                        name = "Accessibility Service",
                        description = "Touch buttons and read UI nodes on screen",
                        isGranted = accessPerm?.isGranted == true,
                        actionLabel = if (accessPerm?.isGranted == true) "Enabled" else "Open Settings",
                        onAction = onOpenAccessibilitySettings
                    )
                }
            }

            // 4. ADDITIONAL CONTROLS (Clear Chat, Version, Tips)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_extra_section"),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(borderColor)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AiraPink,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Actions & About",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Clear Chat History Button
                    OutlinedButton(
                        onClick = { showClearHistoryDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AiraPink),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_chat_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Chat History", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Usage tip card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(surfaceElevated)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "💡 Tip: Live Screen Share",
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start Screen Share anytime from the chat top bar. Aira will inspect your screen, read text, explain errors, or guide you through any app!",
                                color = textSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Version & Engine info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aira AI App Version",
                            color = textSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "v1.2 (Gemini 3.5 Flash)",
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Confirmation dialog for clearing chat history
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Chat History?") },
            text = { Text("This will reset all current conversation messages. Your API key and settings will be preserved.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearChatHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AiraPink)
                ) {
                    Text("Clear", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ThemeOptionCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) AiraPurple else MaterialTheme.colorScheme.outline
    val bgColor = if (isSelected) AiraPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AiraPurple,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = label,
                color = if (isSelected) AiraPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun PermissionRowItem(
    name: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) AiraGreen else AiraAmber)
                )
            }
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isGranted) AiraGreen.copy(alpha = 0.2f)
                    else AiraPink.copy(alpha = 0.2f)
                )
                .clickable(onClick = onAction)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = actionLabel,
                color = if (isGranted) AiraGreen else AiraPink,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}
