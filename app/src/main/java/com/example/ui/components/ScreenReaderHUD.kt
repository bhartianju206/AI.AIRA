package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accessibility.ScreenSnapshot
import com.example.ui.theme.AiraAmber
import com.example.ui.theme.AiraCyan
import com.example.ui.theme.AiraGreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ScreenReaderHUD(
    snapshot: ScreenSnapshot,
    isAccessibilityEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onScanScreenNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("screen_reader_hud")
    ) {
        // Header
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
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AiraGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Screen Reader",
                        tint = AiraGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "AUTONOMOUS SCREEN READER",
                        style = MaterialTheme.typography.labelSmall,
                        color = AiraGreen,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = snapshot.packageName.ifBlank { "Active Window Inspector" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Status chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isAccessibilityEnabled) AiraGreen.copy(alpha = 0.15f)
                        else AiraAmber.copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (isAccessibilityEnabled) AiraGreen.copy(alpha = 0.4f)
                        else AiraAmber.copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isAccessibilityEnabled) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isAccessibilityEnabled) AiraGreen else AiraAmber,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (isAccessibilityEnabled) "Service Online" else "Needs Permission",
                        color = if (isAccessibilityEnabled) AiraGreen else AiraAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Summary text
        Text(
            text = snapshot.summaryText,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            lineHeight = 18.sp
        )

        // If service is disabled, show direct call to action
        if (!isAccessibilityEnabled) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AiraAmber.copy(alpha = 0.1f))
                    .border(1.dp, AiraAmber.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Accessibility for AIRA AI",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = AiraAmber
                    )
                    Text(
                        text = "Required to read active screen, type, and execute autonomous taps.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                Button(
                    onClick = onOpenAccessibilitySettings,
                    colors = ButtonDefaults.buttonColors(containerColor = AiraAmber),
                    modifier = Modifier.testTag("enable_accessibility_button")
                ) {
                    Text("Grant", color = DarkSurfaceCard, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.size(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open Settings",
                        tint = DarkSurfaceCard,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Live Detected Nodes Preview
        if (snapshot.readableTexts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "DETECTED ON-SCREEN TOKENS (${snapshot.nodeCount} nodes):",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                snapshot.readableTexts.take(3).forEach { token ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = token.take(24),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action button to scan & summarize
        OutlinedButton(
            onClick = onScanScreenNow,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("scan_screen_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AiraGreen),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(AiraGreen.copy(alpha = 0.5f)))
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Analyze Screen & Read Aloud", fontWeight = FontWeight.SemiBold)
        }
    }
}
