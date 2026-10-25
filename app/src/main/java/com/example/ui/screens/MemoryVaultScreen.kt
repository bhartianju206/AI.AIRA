package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CryptoVault
import com.example.data.model.AiraMemory
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

@Composable
fun MemoryVaultScreen(
    memories: List<AiraMemory>,
    biometricPolicy: String,
    onSetBiometricPolicy: (String) -> Unit,
    onSaveMemory: (key: String, rawValue: String, summary: String, category: String) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onPurgeVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showPurgeConfirmDialog by remember { mutableStateOf(false) }
    val revealedValues = remember { mutableStateMapOf<Long, Boolean>() }

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Vault Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AiraPurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Neural Memory Vault",
                                    tint = AiraPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "ENCRYPTED NEURAL VAULT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AiraPurple,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = "Personalized Interaction Memory",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "AIRA remembers your preferred messaging apps, contact aliases, communication tone, and automation preferences. All entries are encrypted with AES-256 GCM in hardware AndroidKeyStore with zero network transmission.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = AiraGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "100% Local Device Privacy • No Biometric Lockouts",
                                style = MaterialTheme.typography.labelSmall,
                                color = AiraGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STORED PREFERENCES (${memories.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = AiraCyan,
                        letterSpacing = 1.2.sp
                    )
                    TextButton(
                        onClick = { showPurgeConfirmDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = AiraRed),
                        modifier = Modifier.testTag("purge_vault_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Purge All", fontSize = 12.sp)
                    }
                }
            }

            items(memories, key = { it.id }) { memory ->
                val isRevealed = revealedValues[memory.id] == true
                MemoryItemCard(
                    memory = memory,
                    isRevealed = isRevealed,
                    onToggleReveal = {
                        revealedValues[memory.id] = !isRevealed
                    },
                    onDelete = { onDeleteMemory(memory.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Add Memory FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = AiraCyan,
            contentColor = DarkBackground,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_memory_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Memory")
        }
    }

    // Add Memory Dialog
    if (showAddDialog) {
        var keyInput by remember { mutableStateOf("") }
        var valueInput by remember { mutableStateOf("") }
        var summaryInput by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("PREFERENCE") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Teach AIRA a New Preference", color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "This preference will be encrypted in local storage for personalized autonomous tasks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Key (e.g. sister_contact, default_tone)") },
                        modifier = Modifier.fillMaxWidth().testTag("memory_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AiraCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = valueInput,
                        onValueChange = { valueInput = it },
                        label = { Text("Raw Value (encrypted)") },
                        modifier = Modifier.fillMaxWidth().testTag("memory_value_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AiraCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = summaryInput,
                        onValueChange = { summaryInput = it },
                        label = { Text("Summary Description") },
                        modifier = Modifier.fillMaxWidth().testTag("memory_summary_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AiraCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (keyInput.isNotBlank() && valueInput.isNotBlank()) {
                            onSaveMemory(
                                keyInput.trim(),
                                valueInput.trim(),
                                summaryInput.ifBlank { valueInput },
                                selectedCategory
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AiraCyan),
                    modifier = Modifier.testTag("save_memory_button")
                ) {
                    Text("Encrypt & Save", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Purge Confirmation Dialog
    if (showPurgeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPurgeConfirmDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Purge Entire Memory Vault?", color = AiraRed, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will irreversibly delete all learned preferences, contact aliases, and behavior rules from device storage. This action cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPurgeVault()
                        showPurgeConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AiraRed),
                    modifier = Modifier.testTag("confirm_purge_button")
                ) {
                    Text("Purge Vault", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun MemoryItemCard(
    memory: AiraMemory,
    isRevealed: Boolean,
    onToggleReveal: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AiraPurple.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = memory.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = AiraPurple,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = memory.key,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleReveal,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Decrypted View",
                            tint = AiraCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Memory",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = memory.plainSummary,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            if (isRevealed) {
                Spacer(modifier = Modifier.height(8.dp))
                val decrypted = CryptoVault.decrypt(memory.encryptedValue)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Decrypted Value: $decrypted",
                        style = MaterialTheme.typography.labelSmall,
                        color = AiraGreen,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "Encrypted with AES-256-GCM (Hardware Vault)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PolicyChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) AiraCyan.copy(alpha = 0.2f) else DarkSurfaceElevated)
            .border(
                1.dp,
                if (isSelected) AiraCyan else DarkBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) AiraCyan else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp
        )
    }
}
