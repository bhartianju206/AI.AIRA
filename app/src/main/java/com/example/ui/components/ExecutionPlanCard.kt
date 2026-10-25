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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neural.AutonomousExecutionPlan
import com.example.neural.StepStatus
import com.example.ui.theme.AiraCyan
import com.example.ui.theme.AiraGreen
import com.example.ui.theme.AiraPurple
import com.example.ui.theme.AiraRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ExecutionPlanCard(
    plan: AutonomousExecutionPlan,
    isExecuting: Boolean,
    currentStepIndex: Int,
    onAbort: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedSteps = plan.steps.count { it.status == StepStatus.COMPLETED }
    val progress = if (plan.steps.isNotEmpty()) completedSteps.toFloat() / plan.steps.size.toFloat() else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, AiraCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("execution_plan_card")
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
                        .background(AiraCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Neural Plan",
                        tint = AiraCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "AUTONOMOUS REASONING PLAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = AiraCyan,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = plan.intentType.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            if (isExecuting) {
                IconButton(
                    onClick = onAbort,
                    modifier = Modifier
                        .size(36.dp)
                        .background(AiraRed.copy(alpha = 0.15f), CircleShape)
                        .testTag("abort_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Abort Plan",
                        tint = AiraRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Telemetry Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryBadge(
                icon = Icons.Default.Shield,
                text = "0 B Egress",
                color = AiraGreen
            )
            TelemetryBadge(
                icon = Icons.Default.Memory,
                text = "${plan.inferenceTimeMs}ms local",
                color = AiraPurple
            )
            if (plan.requiresBiometric) {
                TelemetryBadge(
                    icon = Icons.Default.Lock,
                    text = "Biometric Gated",
                    color = AiraCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = plan.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = AiraCyan,
            trackColor = DarkSurfaceElevated,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Steps sequence
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            plan.steps.forEachIndexed { index, step ->
                StepRow(step = step, isCurrent = index == currentStepIndex)
            }
        }
    }
}

@Composable
private fun StepRow(
    step: com.example.neural.ExecutionStep,
    isCurrent: Boolean
) {
    val (statusColor, statusIcon) = when (step.status) {
        StepStatus.COMPLETED -> Pair(AiraGreen, Icons.Default.CheckCircle)
        StepStatus.FAILED -> Pair(AiraRed, Icons.Default.Error)
        StepStatus.EXECUTING -> Pair(AiraCyan, null)
        else -> Pair(TextMuted, null)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isCurrent) DarkSurfaceElevated else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.15f))
                .border(1.dp, statusColor.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (step.status == StepStatus.EXECUTING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = AiraCyan
                )
            } else if (statusIcon != null) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = step.status.name,
                    tint = statusColor,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    text = "${step.stepNumber}",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = if (isCurrent) TextPrimary else TextSecondary
            )
            Text(
                text = step.resultMessage ?: step.description,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun TelemetryBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp
        )
    }
}
