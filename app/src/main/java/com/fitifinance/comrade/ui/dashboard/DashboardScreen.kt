package com.fitifinance.comrade.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.data.entity.Transaction
import com.fitifinance.comrade.engine.HealthScore
import com.fitifinance.comrade.ui.theme.GlassBackground
import com.fitifinance.comrade.ui.theme.GlassCard
import com.fitifinance.comrade.ui.theme.glassColors
import com.fitifinance.comrade.viewmodel.DashboardViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel
import com.fitifinance.comrade.viewmodel.toExpenseGroup
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Presentation-only for now: the app does not yet model multiple accounts, so
 * switching here changes the label but not the figures.
 */
private val DisplayAccounts = listOf("All accounts", "M-PESA", "Cash")

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
    var selectedAccount by rememberSaveable { mutableStateOf(DisplayAccounts.first()) }

    val score = HealthScore.from(
        monthBudgetKes = state.profile.monthlyBudgetKes,
        monthSpendKes = state.monthSpendKes,
        totalSavedKes = state.totalSavedKes,
        pendingPromptCount = state.pendingPromptCount
    )
    val weekLabels = state.last7DaysSpend.map { it.dayLabel }
    val weekAmounts = state.last7DaysSpend.map { it.amountKes }

    GlassBackground(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                BentoHeader(
                    firstName = state.profile.name.trim().substringBefore(' '),
                    accounts = DisplayAccounts,
                    selectedAccount = selectedAccount,
                    onSelectAccount = { selectedAccount = it },
                    onOpenProfile = onOpenSettings
                )
            }
            item {
                BalanceCard(state = state, accountDetail = "M-PESA primary account")
            }
            item {
                QuickActions(
                    nightOutEnabled = state.profile.partyDashboardEnabled,
                    onOpenMeals = onOpenMeals,
                    onOpenNightOut = onOpenNightOut,
                    onOpenSavings = onOpenSavings,
                    onOpenTransactions = onOpenTransactions
                )
            }
            item { ScoreCard(score) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Income is not tracked yet: the SMS parser only records money going out.
                    MetricBarCard(
                        title = "Income",
                        totalText = null,
                        caption = "Last 7 days",
                        values = emptyList(),
                        labels = emptyList(),
                        emptyText = "No income recorded yet",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBarCard(
                        title = "Expenses",
                        totalText = formatKsh(state.weekSpendKes),
                        caption = "Last 7 days",
                        values = weekAmounts,
                        labels = weekLabels,
                        emptyText = "No spending this week",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenSpendingChart
                    )
                }
            }
            item { MajorExpensesCard(state.monthCategorySpend) }
            item { RecentActivityCard(state.recentTransactions, onOpenTransactions) }
        }
    }
}

@Composable
private fun QuickActions(
    nightOutEnabled: Boolean,
    onOpenMeals: () -> Unit,
    onOpenNightOut: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenTransactions: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QuickAction(Icons.Filled.Restaurant, "Meals", onOpenMeals, Modifier.weight(1f))
        if (nightOutEnabled) {
            QuickAction(Icons.Filled.LocalBar, "Night out", onOpenNightOut, Modifier.weight(1f))
        }
        QuickAction(Icons.Filled.Savings, "Savings", onOpenSavings, Modifier.weight(1f))
        QuickAction(Icons.Filled.Receipt, "History", onOpenTransactions, Modifier.weight(1f))
    }
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier) {
    val g = glassColors()
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        onClick = onClick
    ) {
        Icon(icon, contentDescription = null, tint = g.accentEnd, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun RecentActivityCard(transactions: List<Transaction>, onOpenAll: () -> Unit) {
    val g = glassColors()
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent activity", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "View all",
                style = MaterialTheme.typography.labelLarge,
                color = g.accentEnd,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onOpenAll)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        if (transactions.isEmpty()) {
            Text(
                "Transactions appear here when M-PESA messages arrive or you log one manually.",
                style = MaterialTheme.typography.bodyMedium,
                color = g.textMuted
            )
        } else {
            transactions.forEach { TransactionRow(it) }
        }
    }
}

@Composable
private fun TransactionRow(transaction: Transaction) {
    val g = glassColors()
    val timeLabel = remember(transaction.timestampMillis) {
        val zoned = Instant.ofEpochMilli(transaction.timestampMillis).atZone(ZoneId.systemDefault())
        val today = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
        val day = when (zoned.toLocalDate()) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> zoned.toLocalDate().toString()
        }
        "$day, ${zoned.format(DateTimeFormatter.ofPattern("h:mm a"))}"
    }
    val group = remember(transaction.category) { transaction.category.toExpenseGroup() }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(g.accentStart.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(expenseGroupIcon(group), contentDescription = null, tint = g.accentEnd, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                transaction.counterparty.ifBlank { group.label },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(timeLabel, style = MaterialTheme.typography.labelMedium, color = g.textMuted)
        }
        Text(
            "-${formatKsh(transaction.amountKes)}",
            style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error
        )
    }
}
