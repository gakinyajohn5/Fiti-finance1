package com.fitifinance.comrade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitifinance.comrade.data.entity.Transaction
import com.fitifinance.comrade.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/** One bar on either chart: a short label plus the KES total for that bucket. */
data class SpendPoint(val label: String, val totalKes: Double, val isCurrent: Boolean = false)

data class SpendingChartState(
    val daily: List<SpendPoint> = emptyList(),
    val weekly: List<SpendPoint> = emptyList()
) {
    val dailyMax: Double get() = daily.maxOfOrNull { it.totalKes } ?: 0.0
    val weeklyMax: Double get() = weekly.maxOfOrNull { it.totalKes } ?: 0.0
}

class SpendingChartViewModel(repository: FinanceRepository) : ViewModel() {

    private val zone = ZoneId.systemDefault()

    val uiState: StateFlow<SpendingChartState> = repository.observeTransactions()
        .map { transactions -> SpendingChartState(daily = buildDaily(transactions), weekly = buildWeekly(transactions)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpendingChartState())

    /** Last 7 calendar days, oldest to newest, today included. */
    private fun buildDaily(transactions: List<Transaction>): List<SpendPoint> {
        val today = LocalDate.now(zone)
        val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
        val totalsByDay = transactions.groupBy {
            Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate()
        }.mapValues { (_, txns) -> txns.sumOf { it.amountKes } }

        return days.map { day ->
            SpendPoint(
                label = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1),
                totalKes = totalsByDay[day] ?: 0.0,
                isCurrent = day == today
            )
        }
    }

    /** Last 8 ISO weeks, oldest to newest, current week included. */
    private fun buildWeekly(transactions: List<Transaction>): List<SpendPoint> {
        val weekFields = WeekFields.of(Locale.getDefault())
        val today = LocalDate.now(zone)
        val currentWeekStart = today.minusDays((today.dayOfWeek.value - weekFields.firstDayOfWeek.value + 7L) % 7L)
        val weekStarts = (7 downTo 0).map { currentWeekStart.minusWeeks(it.toLong()) }

        val totalsByWeekStart = transactions.groupBy { txn ->
            val date = Instant.ofEpochMilli(txn.timestampMillis).atZone(zone).toLocalDate()
            date.minusDays((date.dayOfWeek.value - weekFields.firstDayOfWeek.value + 7L) % 7L)
        }.mapValues { (_, txns) -> txns.sumOf { it.amountKes } }

        return weekStarts.map { weekStart ->
            SpendPoint(
                label = "${weekStart.monthValue}/${weekStart.dayOfMonth}",
                totalKes = totalsByWeekStart[weekStart] ?: 0.0,
                isCurrent = weekStart == currentWeekStart
            )
        }
    }
}
