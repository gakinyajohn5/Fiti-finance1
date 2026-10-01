package com.fitifinance.comrade.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.engine.HealthScore
import com.fitifinance.comrade.ui.theme.GlassCard
import com.fitifinance.comrade.ui.theme.NeonProgressBar
import com.fitifinance.comrade.ui.theme.glassColors
import com.fitifinance.comrade.viewmodel.CategorySpend
import com.fitifinance.comrade.viewmodel.DashboardState
import com.fitifinance.comrade.viewmodel.ExpenseGroup
import java.util.Locale
import kotlin.math.roundToLong

internal fun formatKsh(amountKes: Double): String =
    "KSh " + String.format(Locale.US, "%,d", amountKes.roundToLong())

// ---------------------------------------------------------------------------
// 1. Header: greeting + account switcher + profile avatar
// ---------------------------------------------------------------------------

@Composable
internal fun BentoHeader(
    firstName: String,
    accounts: List<String>,
    selectedAccount: String,
    onSelectAccount: (String) -> Unit,
    onOpenProfile: () -> Unit
) {
    val g = glassColors()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = if (firstName.isBlank()) "Welcome back!" else "Welcome back, $firstName!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))
            AccountSwitcher(accounts, selectedAccount, onSelectAccount)
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(g.accent)
                .clickable(role = Role.Button, onClickLabel = "Open profile", onClick = onOpenProfile),
            contentAlignment = Alignment.Center
        ) {
            val initial = firstName.firstOrNull()?.uppercase()
            if (initial == null) {
                Icon(Icons.Filled.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(
                    initial,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun AccountSwitcher(
    accounts: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val g = glassColors()
    var expanded by remember { mutableStateOf(false) }
    val pill = RoundedCornerShape(50)
    Box {
        Row(
            modifier = Modifier
                .clip(pill)
                .background(g.fillTop)
                .border(1.dp, g.borderStart, pill)
                .clickable(role = Role.DropdownList, onClickLabel = "Switch account") { expanded = true }
                .padding(start = 12.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.AccountBalanceWallet,
                contentDescription = null,
                tint = g.accentEnd,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(selected, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = g.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Text(
                            account,
                            color = if (account == selected) g.accentEnd else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (account == selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSelect(account)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 2. Main balance card
// ---------------------------------------------------------------------------

/**
 * Hero card. The app tracks spend against a budget rather than reading a live
 * M-PESA/bank balance, so the headline is "available this month" (budget minus
 * spend), which it can back with real data.
 */
@Composable
internal fun BalanceCard(
    state: DashboardState,
    accountDetail: String,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(28.dp)
    val budget = state.profile.monthlyBudgetKes
    val fraction = if (budget > 0) (state.monthSpendKes / budget).toFloat().coerceIn(0f, 1f) else 0f
    val onHero = cs.onPrimary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(cs.primary, lerp(cs.primary, Color.Black, 0.55f))))
            .drawBehind {
                val c = Offset(size.width, 0f)
                val r = size.width * 0.75f
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color.White.copy(alpha = 0.20f), Color.Transparent), center = c, radius = r),
                    radius = r,
                    center = c
                )
            }
            .border(1.dp, Color.White.copy(alpha = 0.18f), shape)
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Available this month",
                    style = MaterialTheme.typography.labelLarge,
                    color = onHero.copy(alpha = 0.78f)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    accountDetail,
                    style = MaterialTheme.typography.labelMedium,
                    color = onHero.copy(alpha = 0.6f)
                )
            }
            CardBadge()
        }
        Spacer(Modifier.height(14.dp))
        Text(
            formatKsh(state.monthBudgetRemainingKes),
            style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"),
            color = onHero
        )
        Spacer(Modifier.height(18.dp))
        BudgetTrack(fraction)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "${formatKsh(state.monthSpendKes)} of ${formatKsh(budget)} spent",
                style = MaterialTheme.typography.bodyMedium,
                color = onHero.copy(alpha = 0.78f)
            )
            Text(
                "${formatKsh(state.totalSavedKes)} saved",
                style = MaterialTheme.typography.bodyMedium,
                color = onHero.copy(alpha = 0.78f)
            )
        }
    }
}

@Composable
private fun BudgetTrack(fraction: Float) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val animated by animateFloatAsState(if (started) fraction else 0f, tween(700), label = "budgetTrack")
    Box(
        Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.22f))
    ) {
        if (animated > 0f) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

/** Two interlocking circles: a generic card-network mark that stays on-palette. */
@Composable
private fun CardBadge() {
    val g = glassColors()
    Box(Modifier.size(width = 40.dp, height = 26.dp).semantics { contentDescription = "Card" }) {
        Box(
            Modifier
                .size(26.dp)
                .background(Color.White.copy(alpha = 0.92f), CircleShape)
        )
        Box(
            Modifier
                .padding(start = 14.dp)
                .size(26.dp)
                .background(g.accentEnd.copy(alpha = 0.92f), CircleShape)
        )
    }
}

// ---------------------------------------------------------------------------
// 3. Financial health (score) card
// ---------------------------------------------------------------------------

@Composable
internal fun ScoreCard(score: HealthScore?, modifier: Modifier = Modifier) {
    val g = glassColors()
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Financial health", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (score != null) {
                Text(
                    score.rating,
                    style = MaterialTheme.typography.labelLarge,
                    color = g.accentEnd,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(g.accentEnd.copy(alpha = 0.14f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ScoreGauge(score)
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (score == null) {
                    Text(
                        "Log a few transactions or add to a savings jar and your score will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = g.textMuted
                    )
                } else {
                    score.factors.forEach { factor ->
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(factor.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    "${(factor.fraction * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = g.textMuted
                                )
                            }
                            Spacer(Modifier.height(5.dp))
                            NeonProgressBar(factor.fraction, height = 6.dp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Based on your budgeting and savings in this app. Not a credit bureau score.",
            style = MaterialTheme.typography.labelMedium,
            color = g.textMuted
        )
    }
}

@Composable
private fun ScoreGauge(score: HealthScore?) {
    val g = glassColors()
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started) (score?.progress ?: 0f) else 0f,
        animationSpec = tween(900),
        label = "scoreGauge"
    )
    Box(Modifier.size(124.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(124.dp)) {
            val stroke = 12.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = g.track,
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            if (progress > 0f) {
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(g.accentStart, g.accentEnd),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, 0f)
                    ),
                    startAngle = 150f,
                    sweepAngle = 240f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                score?.score?.toString() ?: "N/A",
                style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "of ${HealthScore.MAX}",
                style = MaterialTheme.typography.labelMedium,
                color = g.textMuted
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 4. Income / Expenses metric cards with mini bar charts
// ---------------------------------------------------------------------------

@Composable
internal fun MetricBarCard(
    title: String,
    totalText: String?,
    caption: String,
    values: List<Double>,
    labels: List<String>,
    emptyText: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val g = glassColors()
    val hasData = values.any { it > 0.0 }
    GlassCard(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        onClick = onClick
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = g.textMuted)
        Spacer(Modifier.height(4.dp))
        Text(
            totalText ?: "KSh 0",
            style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(caption, style = MaterialTheme.typography.labelMedium, color = g.textMuted)
        Spacer(Modifier.height(14.dp))
        if (hasData) {
            MiniBars(values)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                labels.forEach { label ->
                    Text(
                        label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = g.textMuted
                    )
                }
            }
        } else {
            Box(Modifier.fillMaxWidth().height(82.dp), contentAlignment = Alignment.Center) {
                Text(
                    emptyText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = g.textMuted
                )
            }
        }
    }
}

@Composable
private fun MiniBars(values: List<Double>) {
    val g = glassColors()
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val grow by animateFloatAsState(if (started) 1f else 0f, tween(800), label = "miniBars")
    val max = (values.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEach { value ->
            val h = ((value / max).toFloat().coerceIn(0.06f, 1f) * grow).coerceIn(0.01f, 1f)
            val isPeak = value > 0.0 && value == max
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(h)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(
                        if (isPeak) g.accentVertical
                        else Brush.verticalGradient(listOf(g.accentStart.copy(alpha = 0.5f), g.accentStart.copy(alpha = 0.15f)))
                    )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 5. Major expenses with horizontal progress bars
// ---------------------------------------------------------------------------

internal fun expenseGroupIcon(group: ExpenseGroup): ImageVector = when (group) {
    ExpenseGroup.HOUSING -> Icons.Filled.Apartment
    ExpenseGroup.UTILITIES -> Icons.Filled.Bolt
    ExpenseGroup.FOOD -> Icons.Filled.Restaurant
    ExpenseGroup.ENTERTAINMENT -> Icons.Filled.Movie
    ExpenseGroup.FAMILY -> Icons.Filled.FamilyRestroom
    ExpenseGroup.DEBT -> Icons.Filled.CreditCard
    ExpenseGroup.GROUP -> Icons.Filled.Groups
    ExpenseGroup.OTHER -> Icons.Filled.Receipt
}

@Composable
internal fun MajorExpensesCard(
    categories: List<CategorySpend>,
    modifier: Modifier = Modifier,
    maxRows: Int = 4
) {
    val g = glassColors()
    val total = categories.sumOf { it.amountKes }
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Text("Major expenses", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("This month, by category", style = MaterialTheme.typography.labelMedium, color = g.textMuted)
        Spacer(Modifier.height(14.dp))
        if (categories.isEmpty() || total <= 0.0) {
            Text(
                "No spending recorded this month yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = g.textMuted
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                categories.take(maxRows).forEach { item ->
                    val share = (item.amountKes / total).toFloat()
                    Column {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(g.accentStart.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    expenseGroupIcon(item.group),
                                    contentDescription = null,
                                    tint = g.accentEnd,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                item.group.label,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                formatKsh(item.amountKes),
                                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) { NeonProgressBar(share) }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "${(share * 100).toInt()}%",
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                                color = g.textMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
