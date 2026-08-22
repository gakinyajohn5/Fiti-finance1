package com.fitifinance.comrade.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Scaffold
import com.fitifinance.comrade.ui.dashboard.DashboardScreen
import com.fitifinance.comrade.ui.insights.SpendingChartScreen
import com.fitifinance.comrade.ui.meals.MealPlannerScreen
import com.fitifinance.comrade.ui.meals.WeeklyMealPlanScreen
import com.fitifinance.comrade.ui.navigation.FitiBottomNavBar
import com.fitifinance.comrade.ui.nightout.NightOutScreen
import com.fitifinance.comrade.ui.onboarding.OnboardingWizard
import com.fitifinance.comrade.ui.savings.SavingsJarsScreen
import com.fitifinance.comrade.ui.security.SecuritySettingsScreen
import com.fitifinance.comrade.ui.transactions.TransactionsScreen

/** Top-level destinations, reached via the outer NavHost (full-screen pushes). */
object Routes {
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
    const val WEEKLY_MEAL_PLAN = "weekly_meal_plan"
    const val TRANSACTIONS = "transactions"
    const val SPENDING_CHART = "spending_chart"
    const val SECURITY_SETTINGS = "security_settings"
}

/** The four tabs living behind the bottom nav bar, on their own inner NavHost. */
object MainTabs {
    const val DASHBOARD = "tab_dashboard"
    const val MEALS = "tab_meals"
    const val NIGHT_OUT = "tab_night_out"
    const val SAVINGS = "tab_savings"
}

@Composable
fun FitiNavGraph(startDestination: String) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.ONBOARDING) {
            OnboardingWizard(onComplete = {
                navController.navigate(Routes.MAIN) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }

        composable(Routes.MAIN) {
            MainTabScaffold(
                onOpenWeeklyPlan = { navController.navigate(Routes.WEEKLY_MEAL_PLAN) },
                onOpenTransactions = { navController.navigate(Routes.TRANSACTIONS) },
                onOpenSpendingChart = { navController.navigate(Routes.SPENDING_CHART) },
                onOpenSecuritySettings = { navController.navigate(Routes.SECURITY_SETTINGS) }
            )
        }

        composable(Routes.WEEKLY_MEAL_PLAN) {
            WeeklyMealPlanScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.TRANSACTIONS) {
            TransactionsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SPENDING_CHART) {
            SpendingChartScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SECURITY_SETTINGS) {
            SecuritySettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

/**
 * Hosts the bottom nav bar plus its own inner NavHost for the four tabs.
 * Anything a tab needs to reach outside the tab set (Weekly Meal Plan, full
 * Transactions history) bubbles up via the callbacks passed in, so it opens
 * as a full-screen push on the OUTER NavHost rather than living inside a tab.
 */
@Composable
private fun MainTabScaffold(
    onOpenWeeklyPlan: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenSpendingChart: () -> Unit,
    onOpenSecuritySettings: () -> Unit
) {
    val tabNavController = rememberNavController()

    Scaffold(
        bottomBar = { FitiBottomNavBar(tabNavController) }
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = MainTabs.DASHBOARD,
            modifier = Modifier.padding(padding)
        ) {
            composable(MainTabs.DASHBOARD) {
                DashboardScreen(
                    onOpenMeals = { tabNavController.navigate(MainTabs.MEALS) },
                    onOpenNightOut = { tabNavController.navigate(MainTabs.NIGHT_OUT) },
                    onOpenSavings = { tabNavController.navigate(MainTabs.SAVINGS) },
                    onOpenTransactions = onOpenTransactions,
                    onOpenSpendingChart = onOpenSpendingChart,
                    onOpenSecuritySettings = onOpenSecuritySettings
                )
            }

            composable(MainTabs.MEALS) {
                MealPlannerScreen(
                    onBack = { tabNavController.navigate(MainTabs.DASHBOARD) },
                    onOpenWeeklyPlan = onOpenWeeklyPlan
                )
            }

            composable(MainTabs.NIGHT_OUT) {
                NightOutScreen(onBack = { tabNavController.navigate(MainTabs.DASHBOARD) })
            }

            composable(MainTabs.SAVINGS) {
                SavingsJarsScreen(onBack = { tabNavController.navigate(MainTabs.DASHBOARD) })
            }
        }
    }
}
