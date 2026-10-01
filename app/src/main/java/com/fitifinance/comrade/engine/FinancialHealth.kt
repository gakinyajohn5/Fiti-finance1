package com.fitifinance.comrade.engine

import java.time.LocalDate
import kotlin.math.roundToInt

data class HealthFactor(val label: String, val fraction: Float)

data class HealthScore(
    val score: Int,
    val rating: String,
    val factors: List<HealthFactor>
) {
    /** 0f..1f position of [score] within [MIN]..[MAX], for gauges. */
    val progress: Float get() = (score - MIN).toFloat() / (MAX - MIN)

    companion object {
        const val MIN = 300
        const val MAX = 850

        /**
         * A 300-850 financial health score built only from data this app owns:
         * how closely the month's spending tracks the budget (50%), the size of the
         * savings buffer (30%), and how many transactions still need categorizing (20%).
         *
         * This is NOT a credit-bureau score. Returns null until there is something
         * to score, so a brand-new account never shows a flattering default.
         */
        fun from(
            monthBudgetKes: Double,
            monthSpendKes: Double,
            totalSavedKes: Double,
            pendingPromptCount: Int,
            today: LocalDate = LocalDate.now()
        ): HealthScore? {
            if (monthSpendKes <= 0.0 && totalSavedKes <= 0.0) return null

            val budget = monthBudgetKes.coerceAtLeast(1.0)
            val elapsed = (today.dayOfMonth.toDouble() / today.lengthOfMonth()).coerceAtLeast(0.1)

            // Pace: 1.0 means spending exactly in line with the budget for this point in the month.
            val pace = monthSpendKes / (budget * elapsed)
            val budgetFactor = (2.0 - pace).coerceIn(0.0, 1.0)
            val savingsFactor = (totalSavedKes / (budget * 0.2)).coerceIn(0.0, 1.0)
            val recordsFactor = (1.0 - pendingPromptCount / 10.0).coerceIn(0.0, 1.0)

            val weighted = budgetFactor * 0.5 + savingsFactor * 0.3 + recordsFactor * 0.2
            val score = (MIN + weighted * (MAX - MIN)).roundToInt()

            val rating = when {
                score >= 740 -> "Excellent"
                score >= 670 -> "Good"
                score >= 580 -> "Fair"
                else -> "Needs attention"
            }
            return HealthScore(
                score = score,
                rating = rating,
                factors = listOf(
                    HealthFactor("Budget pace", budgetFactor.toFloat()),
                    HealthFactor("Savings buffer", savingsFactor.toFloat()),
                    HealthFactor("Records up to date", recordsFactor.toFloat())
                )
            )
        }
    }
}
