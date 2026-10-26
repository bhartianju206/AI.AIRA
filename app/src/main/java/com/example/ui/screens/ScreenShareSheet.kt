package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AiraAmber
import com.example.ui.theme.AiraCyan
import com.example.ui.theme.AiraGreen
import com.example.ui.theme.AiraPink
import com.example.ui.theme.AiraPurple

@Composable
fun ScreenShareSheet(
    isScreenShareActive: Boolean,
    isPaused: Boolean,
    latestBitmap: Bitmap?,
    onStartScreenShare: () -> Unit,
    onPauseScreenShare: () -> Unit,
    onResumeScreenShare: () -> Unit,
    onStopScreenShare: () -> Unit,
    onAnalyzeScreen: (String) -> Unit,
    onReadAllSentences: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceElevated = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = surfaceColor,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("screen_share_sheet"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AiraCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenShare,
                            contentDescription = "Screen Share",
                            tint = AiraCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "LIVE SCREEN SHARE",
                            style = MaterialTheme.typography.labelSmall,
                            color = AiraCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Aira AI Visual Vision",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Privacy Disclosure Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceElevated)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AiraGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Privacy Disclosure: Captured screen frames are sent directly to the Gemini API for visual analysis and question-answering. No images are permanently stored on any external server.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Active Status & Preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isScreenShareActive) {
                            if (isPaused) AiraAmber.copy(alpha = 0.12f) else AiraGreen.copy(alpha = 0.12f)
                        } else {
                            surfaceElevated
                        }
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isScreenShareActive) {
                                if (isPaused) AiraAmber.copy(alpha = 0.5f) else AiraGreen.copy(alpha = 0.5f)
                            } else borderColor
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SCREEN STREAM STATUS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isScreenShareActive) {
                                    if (isPaused) AiraAmber else AiraGreen
                                } else textSecondary,
                                fontSize = 10.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isScreenShareActive) {
                                            if (isPaused) AiraAmber.copy(alpha = 0.2f) else AiraGreen.copy(alpha = 0.2f)
                                        } else surfaceColor
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (!isScreenShareActive) "Inactive" else if (isPaused) "Paused" else "Streaming Live",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isScreenShareActive) {
                                        if (isPaused) AiraAmber else AiraGreen
                                    } else textSecondary
                                )
                            }
                        }

                        if (latestBitmap != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Image(
                                bitmap = latestBitmap.asImageBitmap(),
                                contentDescription = "Screen Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Controls: Start / Pause / Stop
                if (!isScreenShareActive) {
                    Button(
                        onClick = onStartScreenShare,
                        colors = ButtonDefaults.buttonColors(containerColor = AiraCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_screen_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenShare,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Start Live Screen Share",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { if (isPaused) onResumeScreenShare() else onPauseScreenShare() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPaused) AiraGreen else AiraAmber
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isPaused) "Resume" else "Pause",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onStopScreenShare,
                            colors = ButtonDefaults.buttonColors(containerColor = AiraPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Stop Share",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // AI Screen Queries (Quick Prompts)
                Text(
                    text = "ASK AIRA ABOUT YOUR SCREEN:",
                    style = MaterialTheme.typography.labelSmall,
                    color = AiraPurple,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ScreenQueryChip(
                        title = "What is on my screen?",
                        subtitle = "Aira describes apps, texts, and active content",
                        onClick = {
                            onAnalyzeScreen("What is currently on my screen? Please describe the apps, active contents, and what you see.")
                            onDismiss()
                        }
                    )
                    ScreenQueryChip(
                        title = "Read text & errors aloud",
                        subtitle = "Extracts all sentences and explains warnings",
                        onClick = {
                            onAnalyzeScreen("Please read all visible text and sentences on my screen, and explain any errors or messages.")
                            onDismiss()
                        }
                    )
                    ScreenQueryChip(
                        title = "What should I click next?",
                        subtitle = "Recommends next action or button to touch",
                        onClick = {
                            onAnalyzeScreen("Look at my screen and tell me what button or field I should click or interact with next.")
                            onDismiss()
                        }
                    )
                    ScreenQueryChip(
                        title = "Summarize this page",
                        subtitle = "Quick breakdown of articles, chat, or forms",
                        onClick = {
                            onAnalyzeScreen("Please summarize the main information and contents visible on this page.")
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AiraPurple, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun ScreenQueryChip(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                tint = AiraCyan,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
