package com.fitifinance.comrade.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * The "hero" card used for balances/budgets — an eyebrow label, a large
 * headline figure, an optional thin progress bar, and an optional status
 * pill. Used on Dashboard, Meal Planner, and Night-Out for visual consistency.
 */
@Composable
fun HeroStatCard(
    label: String,
    valueText: String,
    modifier: Modifier = Modifier,
    progressFraction: Float? = null,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    statusText: String? = null,
    statusColor: Color = MaterialTheme.colorScheme.primary,
    trailing: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        label.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        valueText,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                trailing?.invoke()
            }

            if (progressFraction != null) {
                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { progressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }

            if (statusText != null) {
                Spacer(Modifier.height(10.dp))
                StatusPill(text = statusText, color = statusColor)
            }
        }
    }
}

/** Small rounded status badge, e.g. "On track" or "85% used". */
@Composable
fun StatusPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.extraLarge)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(6.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/** Amount text that colors itself green for positive/credit and coral for negative/debit. */
@Composable
fun AmountText(amountKes: Double, isCredit: Boolean, modifier: Modifier = Modifier) {
    val color = if (isCredit) SuccessGreen else WarningRed
    val sign = if (isCredit) "+" else "-"
    Text(
        "$sign KSh ${kotlin.math.abs(amountKes).toInt()}",
        style = MaterialTheme.typography.titleMedium,
        color = color,
        modifier = modifier
    )
}
