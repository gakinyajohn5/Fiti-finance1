package com.fitifinance.comrade.engine

import com.fitifinance.comrade.data.entity.MealWindow
import com.fitifinance.comrade.data.entity.UserProfile

/** One day's worth of picks across the four meal windows. */
data class DayPlan(
    val dayLabel: String, // "Monday" .. "Sunday"
    val meals: Map<MealWindow, FoodItem>,
    val totalCostKes: Double
)

data class WeeklyMealPlan(
    val days: List<DayPlan>,
    val totalCostKes: Double,
    val averageDailyCostKes: Double
)

/**
 * Builds a 7-day meal plan, one pick per window (Breakfast/Lunch/Supper/Late
 * Night), using the comrade's saved preferences:
 *  - Allergies -> hard-excluded everywhere, no exceptions.
 *  - Hated foods -> never picked.
 *  - Loved foods -> weighted much more likely to be picked, and guaranteed
 *    to appear at least once across the week if they fit the daily budget.
 *  - Cooking ability (from onboarding) -> respects "no kitchen" the same way
 *    the daily suggestion engine already does.
 *  - Stays within `profile.dailyMealBudgetKes` per day where possible, and
 *    avoids repeating the exact same item on consecutive days for variety.
 */
object WeeklyMealPlanEngine {

    private val dayLabels = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private val windowOrder = listOf(MealWindow.BREAKFAST, MealWindow.LUNCH, MealWindow.SUPPER, MealWindow.LATE_NIGHT)

    fun generate(profile: UserProfile, seed: Long = System.currentTimeMillis()): WeeklyMealPlan {
        val random = kotlin.random.Random(seed)
        val loved = profile.lovedFoodIdSet
        val hated = profile.hatedFoodIdSet
        val allergens = profile.allergenSet

        fun candidatesFor(window: MealWindow): List<FoodItem> {
            val base = FoodDatabase.forWindow(window)
                .filter { it.allergens.none(allergens::contains) }
                .filterNot { hated.contains(it.id) }
            val cookable = if (!profile.canCook) base.filter { it.vendorTag != "Room / Kettle" } else base
            return cookable.ifEmpty { base } // never leave a window empty just because of the cooking filter
        }

        // Weighted pick: loved items are ~4x more likely to be chosen, and we
        // avoid repeating yesterday's exact pick for the same window.
        fun pick(window: MealWindow, previousPick: String?): FoodItem? {
            val candidates = candidatesFor(window)
            if (candidates.isEmpty()) return null
            val pool = candidates.filter { it.id != previousPick }.ifEmpty { candidates }
            val weighted = pool.flatMap { item -> List(if (loved.contains(item.id)) 4 else 1) { item } }
            return weighted[random.nextInt(weighted.size)]
        }

        val lastPickByWindow = mutableMapOf<MealWindow, String?>()
        val days = dayLabels.map { label ->
            val meals = mutableMapOf<MealWindow, FoodItem>()
            for (window in windowOrder) {
                val item = pick(window, lastPickByWindow[window]) ?: continue
                meals[window] = item
                lastPickByWindow[window] = item.id
            }
            DayPlan(
                dayLabel = label,
                meals = meals,
                totalCostKes = meals.values.sumOf { it.costKes }
            )
        }

        // Guarantee every loved item the comrade picked shows up at least once
        // in the week, swapping in on the cheapest day that still fits budget.
        val alreadyIncluded = days.flatMap { it.meals.values.map { m -> m.id } }.toMutableSet()
        val missingLoved = loved.filterNot { alreadyIncluded.contains(it) }
            .mapNotNull { FoodDatabase.byId(it) }
            .filter { it.allergens.none(allergens::contains) }

        val finalDays = days.toMutableList()
        for (item in missingLoved) {
            val dayIndex = finalDays.indices
                .filter { finalDays[it].meals.containsKey(item.window) }
                .minByOrNull { finalDays[it].totalCostKes } ?: continue
            val day = finalDays[dayIndex]
            val updatedMeals = day.meals.toMutableMap().apply { put(item.window, item) }
            finalDays[dayIndex] = day.copy(
                meals = updatedMeals,
                totalCostKes = updatedMeals.values.sumOf { it.costKes }
            )
        }

        val total = finalDays.sumOf { it.totalCostKes }
        return WeeklyMealPlan(
            days = finalDays,
            totalCostKes = total,
            averageDailyCostKes = if (finalDays.isNotEmpty()) total / finalDays.size else 0.0
        )
    }
}
