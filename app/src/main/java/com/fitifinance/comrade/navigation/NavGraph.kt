package com.fitifinance.comrade.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
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

/**
 * Destinations on the inner NavHost. The bottom bar shows Home, Analytics, Transfers
 * and Profile; Meals, Night Out and Savings are reached from the dashboard quick actions.
 */
object MainTabs {
    const val DASHBOARD = "tab_dashboard"
    const val ANALYTICS = "tab_analytics"
    const val TRANSFERS = "tab_transfers"
    const val PROFILE = "tab_profile"
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
                onNavigateOuter = { route -> navController.navigate(route) }
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
 * Hosts the bottom nav bar plus its own inner NavHost for the tabs. Anything that
 * must open full-screen (Weekly Meal Plan, individual settings pages) bubbles up via
 * the callbacks so it lands on the OUTER NavHost rather than inside a tab.
 */
@Composable
private fun MainTabScaffold(
    onOpenWeeklyPlan: () -> Unit,
    onNavigateOuter: (String) -> Unit
) {
    val tabNavController = rememberNavController()

    // Same behaviour as tapping a bottom-bar tab: one copy per tab, state preserved.
    val goToTab: (String) -> Unit = { route ->
        tabNavController.navigate(route) {
            popUpTo(tabNavController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

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
                    onOpenMeals = { goToTab(MainTabs.MEALS) },
                    onOpenNightOut = { goToTab(MainTabs.NIGHT_OUT) },
                    onOpenSavings = { goToTab(MainTabs.SAVINGS) },
                    onOpenTransactions = { goToTab(MainTabs.TRANSFERS) },
                    onOpenSpendingChart = { goToTab(MainTabs.ANALYTICS) },
                    onOpenSettings = { goToTab(MainTabs.PROFILE) }
                )
            }

            composable(MainTabs.ANALYTICS) {
                SpendingChartScreen(onBack = { goToTab(MainTabs.DASHBOARD) })
            }

            composable(MainTabs.TRANSFERS) {
                TransactionsScreen(onBack = { goToTab(MainTabs.DASHBOARD) })
            }

            composable(MainTabs.PROFILE) {
                SettingsHubScreen(
                    onBack = { goToTab(MainTabs.DASHBOARD) },
                    onOpenAppLock = { onNavigateOuter(Routes.SECURITY_SETTINGS) },
                    onOpenBudgets = { onNavigateOuter(Routes.BUDGET_SETTINGS) },
                    onOpenNotifications = { onNavigateOuter(Routes.NOTIFICATION_SETTINGS) },
                    onOpenDataPrivacy = { onNavigateOuter(Routes.DATA_PRIVACY_SETTINGS) },
                    onOpenAppearance = { onNavigateOuter(Routes.APPEARANCE_SETTINGS) },
                    onOpenProfile = { onNavigateOuter(Routes.PROFILE_SETTINGS) },
                    onOpenAbout = { onNavigateOuter(Routes.ABOUT) }
                )
            }

            composable(MainTabs.MEALS) {
                MealPlannerScreen(
                    onBack = { goToTab(MainTabs.DASHBOARD) },
                    onOpenWeeklyPlan = onOpenWeeklyPlan
                )
            }

            composable(MainTabs.NIGHT_OUT) {
                NightOutScreen(onBack = { goToTab(MainTabs.DASHBOARD) })
            }

            composable(MainTabs.SAVINGS) {
                SavingsJarsScreen(onBack = { goToTab(MainTabs.DASHBOARD) })
            }
        }
    }
}
