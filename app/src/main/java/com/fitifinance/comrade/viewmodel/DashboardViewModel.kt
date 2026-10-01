package com.fitifinance.comrade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitifinance.comrade.data.entity.Transaction
import com.fitifinance.comrade.data.entity.TransactionCategory
import com.fitifinance.comrade.data.entity.UserProfile
import com.fitifinance.comrade.engine.LocationContextEngine
import com.fitifinance.comrade.engine.ThemeMode
import com.fitifinance.comrade.repository.FinanceRepository
import com.fitifinance.comrade.settings.AppPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class DailySpend(val dayLabel: String, val amountKes: Double)

/** User-facing spending groups. Several stored categories can roll up into one group. */
enum class ExpenseGroup(val label: String) {
    HOUSING("Housing"),
    UTILITIES("Utilities"),
    FOOD("Food"),
    ENTERTAINMENT("Entertainment"),
    FAMILY("Family support"),
    DEBT("Loans and debt"),
    GROUP("Group contributions"),
    OTHER("Other")
}

data class CategorySpend(val group: ExpenseGroup, val amountKes: Double)

fun TransactionCategory.toExpenseGroup(): ExpenseGroup = when (this) {
    TransactionCategory.RENT, TransactionCategory.SHARED_HOUSE_EXPENSES -> ExpenseGroup.HOUSING
    TransactionCategory.UTILITIES -> ExpenseGroup.UTILITIES
    TransactionCategory.FOOD, TransactionCategory.GROCERIES -> ExpenseGroup.FOOD
    TransactionCategory.ENTERTAINMENT, TransactionCategory.NIGHT_OUT -> ExpenseGroup.ENTERTAINMENT
    TransactionCategory.FAMILY_BLACK_TAX -> ExpenseGroup.FAMILY
    TransactionCategory.LOAN_DEBT_REPAYMENT -> ExpenseGroup.DEBT
    TransactionCategory.CHAMA_SOCIAL -> ExpenseGroup.GROUP
    TransactionCategory.UNCATEGORIZED -> ExpenseGroup.OTHER
}

data class DashboardState(
    val profile: UserProfile = UserProfile(),
    val todayMealSpendKes: Double = 0.0,
    val monthSpendKes: Double = 0.0,
    val pendingPromptCount: Int = 0,
    val activeJarCount: Int = 0,
    val totalSavedKes: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val last7DaysSpend: List<DailySpend> = emptyList(),
    /** This month's spend grouped for the Major Expenses card, largest first. */
    val monthCategorySpend: List<CategorySpend> = emptyList()
) {
    val weekSpendKes: Double get() = last7DaysSpend.sumOf { it.amountKes }

    /** Budget remaining this month — the honest stand-in for a "wallet balance",
     * since this app tracks spend against a budget rather than a live M-PESA
     * balance API. */
    val monthBudgetRemainingKes: Double get() = (profile.monthlyBudgetKes - monthSpendKes).coerceAtLeast(0.0)
}

class DashboardViewModel(
    private val repository: FinanceRepository,
    private val locationEngine: LocationContextEngine,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = locationEngine.currentMode

    init {
        // Honors a manual "Appearance" override from Settings instead of location detection.
        appPreferences.state
            .onEach { settings ->
                if (!settings.themeAutoMode) {
                    locationEngine.forceMode(settings.manualThemeMode)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun startOfDayMillis(): Long =
        LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun startOfMonthMillis(): Long =
        LocalDate.now(ZoneId.systemDefault()).withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun startOfDaysAgoMillis(daysAgo: Long): Long =
        LocalDate.now(ZoneId.systemDefault()).minusDays(daysAgo)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun now(): Long = System.currentTimeMillis()

    private fun buildLast7DaysSpend(transactions: List<Transaction>): List<DailySpend> {
        val zone = ZoneId.systemDefault()
        return (6 downTo 0).map { daysAgo ->
            val day = LocalDate.now(zone).minusDays(daysAgo.toLong())
            val dayStart = day.atStartOfDay(zone).toInstant().toEpochMilli()
            val dayEnd = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val total = transactions.filter { it.timestampMillis in dayStart until dayEnd }.sumOf { it.amountKes }
            DailySpend(day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1), total)
        }
    }

    val uiState: StateFlow<DashboardState> = combine(
        repository.observeProfile(),
        repository.observeMealSpendBetween(startOfDayMillis(), now()),
        repository.observeTransactions(),
        repository.observePendingPrompts(),
        repository.observeActiveJars()
    ) { profile, mealSpend, transactions, pending, jars ->
        val monthStart = startOfMonthMillis()
        val monthTransactions = transactions.filter { it.timestampMillis >= monthStart }
        val monthSpend = monthTransactions.sumOf { it.amountKes }
        val categorySpend = monthTransactions
            .groupBy { it.category.toExpenseGroup() }
            .map { (group, items) -> CategorySpend(group, items.sumOf { it.amountKes }) }
            .sortedByDescending { it.amountKes }
        val recentWindowStart = startOfDaysAgoMillis(6)
        val recentWindowTx = transactions.filter { it.timestampMillis >= recentWindowStart }
        DashboardState(
            profile = profile ?: UserProfile(),
            todayMealSpendKes = mealSpend,
            monthSpendKes = monthSpend,
            pendingPromptCount = pending.size,
            activeJarCount = jars.size,
            totalSavedKes = jars.sumOf { it.currentAmountKes },
            recentTransactions = transactions.sortedByDescending { it.timestampMillis }.take(5),
            last7DaysSpend = buildLast7DaysSpend(recentWindowTx),
            monthCategorySpend = categorySpend
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    /** Manual mode override for devices/emulators without live location (demo-friendly). */
    fun previewThemeMode(mode: ThemeMode) {
        // Directly reflects into the shared engine's flow for a consistent demo experience.
        locationEngine.forceMode(mode)
    }
}
