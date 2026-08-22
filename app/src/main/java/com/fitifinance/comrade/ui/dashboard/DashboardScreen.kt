package com.fitifinance.comrade.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.data.entity.Transaction
import com.fitifinance.comrade.data.entity.TransactionCategory
import com.fitifinance.comrade.engine.ThemeMode
import com.fitifinance.comrade.viewmodel.DailySpend
import com.fitifinance.comrade.viewmodel.DashboardState
import com.fitifinance.comrade.viewmodel.DashboardViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    onOpenMeals: () -> Unit,
    onOpenNightOut: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenSpendingChart: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val vm: DashboardViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState()
    val theme by vm.themeMode.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp)
    ) {
        item {
            DashboardHeader(state, theme, vm::previewThemeMode, onOpenSettings)
            Spacer(Modifier.height(18.dp))
            BudgetHeroCard(state)
            Spacer(Modifier.height(18.dp))
            QuickActionsRow(
                partyEnabled = state.profile.partyDashboardEnabled,
                onOpenMeals = onOpenMeals,
                onOpenNightOut = onOpenNightOut,
                onOpenSavings = onOpenSavings,
                onOpenTransactions = onOpenTransactions
            )
            Spacer(Modifier.height(18.dp))
            WeeklySpendCard(state.last7DaysSpend, onOpenSpendingChart)
            Spacer(Modifier.height(18.dp))
            Text("Recent transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }

        if (state.recentTransactions.isEmpty()) {
            item {
                Text(
                    "No transactions yet — they'll show up here as soon as M-PESA SMS come in or you log one manually.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            items(state.recentTransactions) { tx ->
                TransactionRow(tx)
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun DashboardHeader(
    state: DashboardState,
    theme: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    onOpenSettings: () -> Unit
) {
    var showModeSwitcher by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Habari, Comrade \uD83D\uDC4B", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "${theme.name.lowercase().replaceFirstChar { it.uppercase() }} mode",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { showModeSwitcher = !showModeSwitcher }) {
                    Icon(Icons.Filled.Person, contentDescription = "Switch mode (demo)", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (showModeSwitcher) {
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                ThemeMode.CAMPUS to "Campus",
                ThemeMode.BAR to "Bar",
                ThemeMode.KIBANDA to "Kibanda"
            ).forEach { (mode, label) ->
                FilterChip(selected = theme == mode, onClick = { onSelectMode(mode) }, label = { Text(label) })
            }
        }
    }
}

/**
 * Stands in for a wallet-balance card, but shows something the app can
 * actually back with real data: budget remaining this month, against the
 * month's total spend. A fabricated "account balance" would be misleading
 * since this app tracks M-PESA spend, not a live account balance API.
 */
@Composable
private fun BudgetHeroCard(state: DashboardState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "Budget remaining this month",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "KSh ${state.monthBudgetRemainingKes.toInt()}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Transparent)
                        .padding(0.dp)
                ) {
                    Text(
                        "Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .background(Color.Transparent)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "KSh ${state.monthSpendKes.toInt()} spent of KSh ${state.profile.monthlyBudgetKes.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
                Text(
                    "KSh ${state.totalSavedKes.toInt()} saved",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    partyEnabled: Boolean,
    onOpenMeals: () -> Unit,
    onOpenNightOut: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenTransactions: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionItem(Icons.Filled.Restaurant, "Meals", onOpenMeals)
        if (partyEnabled) {
            QuickActionItem(Icons.Filled.LocalBar, "Night Out", onOpenNightOut)
        }
        QuickActionItem(Icons.Filled.Savings, "Savings", onOpenSavings)
        QuickActionItem(Icons.Filled.Receipt, "History", onOpenTransactions)
    }
}

@Composable
private fun QuickActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onClick) {
                Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun WeeklySpendCard(days: List<DailySpend>, onOpenSpendingChart: () -> Unit) {
    Card(
        onClick = onOpenSpendingChart,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "This week",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "See full spending insights",
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(14.dp))
            if (days.isEmpty()) {
                Text(
                    "No spend logged yet this week.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            } else {
                val maxAmount = (days.maxOfOrNull { it.amountKes } ?: 0.0).coerceAtLeast(1.0)
                Row(
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    days.forEach { day ->
                        val heightFraction = (day.amountKes / maxAmount).toFloat().coerceIn(0.06f, 1f)
                        val isPeak = day.amountKes == maxAmount
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(heightFraction)
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(
                                    if (isPeak) MaterialTheme.colorScheme.tertiary
                                    else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f)
                                )
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    days.forEach { day ->
                        Text(
                            day.dayLabel,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

private fun categoryIcon(category: TransactionCategory): ImageVector = when (category) {
    TransactionCategory.FOOD -> Icons.Filled.Restaurant
    TransactionCategory.GROCERIES -> Icons.Filled.ShoppingCart
    TransactionCategory.UTILITIES -> Icons.Filled.Bolt
    TransactionCategory.RENT -> Icons.Filled.Home
    TransactionCategory.ENTERTAINMENT -> Icons.Filled.Movie
    TransactionCategory.NIGHT_OUT -> Icons.Filled.LocalBar
    TransactionCategory.FAMILY_BLACK_TAX -> Icons.Filled.FamilyRestroom
    TransactionCategory.SHARED_HOUSE_EXPENSES -> Icons.Filled.Apartment
    TransactionCategory.LOAN_DEBT_REPAYMENT -> Icons.Filled.CreditCard
    TransactionCategory.CHAMA_SOCIAL -> Icons.Filled.Groups
    TransactionCategory.UNCATEGORIZED -> Icons.Filled.Receipt
}

@Composable
private fun TransactionRow(transaction: Transaction) {
    val timeLabel = remember(transaction.timestampMillis) {
        val instant = Instant.ofEpochMilli(transaction.timestampMillis)
        val zoned = instant.atZone(ZoneId.systemDefault())
        val today = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
        val prefix = if (zoned.toLocalDate() == today) "Today" else if (zoned.toLocalDate() == today.minusDays(1)) "Yesterday" else zoned.toLocalDate().toString()
        "$prefix, ${zoned.format(DateTimeFormatter.ofPattern("h:mm a"))}"
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    categoryIcon(transaction.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    transaction.counterparty.ifBlank { transaction.category.name.lowercase().replace('_', ' ') },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        Text(
            "-KSh ${transaction.amountKes.toInt()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}
