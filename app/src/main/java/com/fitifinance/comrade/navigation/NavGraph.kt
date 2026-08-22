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
import com.fitifinance.comrade.ui.settings.AboutScreen
import com.fitifinance.comrade.ui.settings.AppearanceSettingsScreen
import com.fitifinance.comrade.ui.settings.BudgetSettingsScreen
import com.fitifinance.comrade.ui.settings.DataPrivacyScreen
import com.fitifinance.comrade.ui.settings.NotificationSettingsScreen
import com.fitifinance.comrade.ui.settings.ProfileSettingsScreen
import com.fitifinance.comrade.ui.settings.SettingsHubScreen
import com.fitifinance.comrade.ui.transactions.TransactionsScreen

/** Top-level destinations, reached via the outer NavHost (full-screen pushes). */
object Routes {
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
    const val WEEKLY_MEAL_PLAN = "weekly_meal_plan"
    const val TRANSACTIONS = "transactions"
    const val SPENDING_CHART = "spending_chart"
    const val SETTINGS_HUB = "settings_hub"
    const val SECURITY_SETTINGS = "security_settings"
    const val BUDGET_SETTINGS = "budget_settings"
    const val NOTIFICATION_SETTINGS = "notification_settings"
    const val DATA_PRIVACY_SETTINGS = "data_privacy_settings"
    const val APPEARANCE_SETTINGS = "appearance_settings"
    const val PROFILE_SETTINGS = "profile_settings"
    const val ABOUT = "about"
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
                onOpenSettings = { navController.navigate(Routes.SETTINGS_HUB) }
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

        composable(Routes.SETTINGS_HUB) {
            SettingsHubScreen(
                onBack = { navController.popBackStack() },
                onOpenAppLock = { navController.navigate(Routes.SECURITY_SETTINGS) },
                onOpenBudgets = { navController.navigate(Routes.BUDGET_SETTINGS) },
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATION_SETTINGS) },
                onOpenDataPrivacy = { navController.navigate(Routes.DATA_PRIVACY_SETTINGS) },
                onOpenAppearance = { navController.navigate(Routes.APPEARANCE_SETTINGS) },
                onOpenProfile = { navController.navigate(Routes.PROFILE_SETTINGS) },
                onOpenAbout = { navController.navigate(Routes.ABOUT) }
            )
        }

        composable(Routes.SECURITY_SETTINGS) {
            SecuritySettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.BUDGET_SETTINGS) {
            BudgetSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.NOTIFICATION_SETTINGS) {
            NotificationSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.DATA_PRIVACY_SETTINGS) {
            DataPrivacyScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.APPEARANCE_SETTINGS) {
            AppearanceSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PROFILE_SETTINGS) {
            ProfileSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
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
    onOpenSettings: () -> Unit
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
                    onOpenSettings = onOpenSettings
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
