package com.systemleveling.app.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.systemleveling.feature.home.advancement.ClassAdvancementScreen
import com.systemleveling.feature.home.advancement.ClassAdvancementViewModel
import com.systemleveling.feature.home.npc.NpcChatScreen
import com.systemleveling.feature.home.npc.NpcChatViewModel
import com.systemleveling.feature.home.ui.HomeScreen
import com.systemleveling.feature.home.ui.HomeViewModel
import com.systemleveling.feature.onboarding.ui.OnboardingScreen
import com.systemleveling.feature.onboarding.ui.OnboardingViewModel
import com.systemleveling.feature.onboarding.ui.SplashScreen
import com.systemleveling.feature.quests.ui.QuestListScreen
import com.systemleveling.feature.quests.ui.QuestViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String,
    postSplashDestination: String = "home"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen(
                onComplete = {
                    navController.navigate(postSplashDestination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding") {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = viewModel,
                onFinish = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToQuests = { navController.navigate("quests") },
                onNavigateToSkills = { navController.navigate("skills") },
                onNavigateToInventory = { navController.navigate("inventory") },
                onNavigateToTitles = { navController.navigate("titles") },
                onNavigateToFinance = { navController.navigate("finance") },
                onNavigateToLibrary = { navController.navigate("library") },
                onNavigateToJournal = { navController.navigate("journal") },
                onNavigateToCalendar = { navController.navigate("calendar") },
                onNavigateToNpc = { navController.navigate("npc") },
                onNavigateToAdvancement = { navController.navigate("advancement") }
            )
        }

        composable("advancement") {
            val viewModel: ClassAdvancementViewModel = hiltViewModel()
            ClassAdvancementScreen(
                viewModel = viewModel,
                onComplete = {
                    navController.navigate("home") {
                        popUpTo("advancement") { inclusive = true }
                    }
                }
            )
        }

        composable("npc") {
            val viewModel: NpcChatViewModel = hiltViewModel()
            NpcChatScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("quests") {
            val viewModel: QuestViewModel = hiltViewModel()
            QuestListScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("skills") {
            val viewModel: com.systemleveling.feature.skills.ui.SkillTreeViewModel = hiltViewModel()
            com.systemleveling.feature.skills.ui.SkillTreeScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("inventory") {
            val viewModel: com.systemleveling.feature.inventory.ui.InventoryViewModel = hiltViewModel()
            com.systemleveling.feature.inventory.ui.InventoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("titles") {
            val viewModel: com.systemleveling.feature.titles.ui.TitleViewModel = hiltViewModel()
            com.systemleveling.feature.titles.ui.TitleScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("finance") {
            val viewModel: com.systemleveling.feature.finance.ui.FinanceViewModel = hiltViewModel()
            com.systemleveling.feature.finance.ui.FinanceScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("library") {
            val viewModel: com.systemleveling.feature.library.ui.LibraryViewModel = hiltViewModel()
            com.systemleveling.feature.library.ui.LibraryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("journal") {
            val viewModel: com.systemleveling.feature.journal.ui.JournalViewModel = hiltViewModel()
            com.systemleveling.feature.journal.ui.JournalScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("calendar") {
            val viewModel: com.systemleveling.feature.calendar.ui.CalendarViewModel = hiltViewModel()
            com.systemleveling.feature.calendar.ui.CalendarScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
