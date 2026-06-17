package com.example.taskerine_v2.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.taskerine_v2.data.model.Role
import com.example.taskerine_v2.ui.screens.*
import com.example.taskerine_v2.viewmodel.AuthViewModel
import com.example.taskerine_v2.viewmodel.CoinViewModel
import com.example.taskerine_v2.viewmodel.MessageViewModel
import com.example.taskerine_v2.viewmodel.ReportViewModel
import com.example.taskerine_v2.viewmodel.ReviewViewModel
import com.example.taskerine_v2.viewmodel.TaskViewModel

private val AmberPrimary = Color(0xFFFFA726)
private val NavBackground = Color(0xFFFFFBF2)

sealed class Screen(val route: String) {
    object Welcome    : Screen("welcome")
    object Auth       : Screen("auth/{role}") {
        fun createRoute(role: Role) = "auth/${role.name}"
    }
    object Home       : Screen("home")
    object PostTask   : Screen("post_task")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object MyTasks    : Screen("my_tasks")
    object CoinStore  : Screen("coin_store")
    object Reviews    : Screen("reviews/{taskId}") {
        fun createRoute(taskId: String) = "reviews/$taskId"
    }
    object Settings   : Screen("settings")
    object Report     : Screen("report")          // ← NEW
}

data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

private val bottomNavItems = listOf(
    BottomNavItem("Home",     Icons.Filled.Home,         Screen.Home.route),
    BottomNavItem("My Tasks", Icons.Filled.List,         Screen.MyTasks.route),
    BottomNavItem("Coins",    Icons.Filled.ShoppingCart, Screen.CoinStore.route),
    BottomNavItem("Settings", Icons.Filled.Settings,     Screen.Settings.route),
)

private val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.MyTasks.route,
    Screen.CoinStore.route,
    Screen.Settings.route,
)

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    coinViewModel: CoinViewModel,
    reviewViewModel: ReviewViewModel,
    messageViewModel: MessageViewModel,
    reportViewModel: ReportViewModel          // ← NEW
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        containerColor = NavBackground,
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = NavBackground,
                    tonalElevation = androidx.compose.ui.unit.Dp(4f)
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = AmberPrimary,
                                selectedTextColor   = AmberPrimary,
                                indicatorColor      = AmberPrimary.copy(alpha = 0.15f),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (currentUser != null) Screen.Home.route else Screen.Welcome.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onRoleSelected = { role ->
                        navController.navigate(Screen.Auth.createRoute(role))
                    }
                )
            }

            composable(
                route = Screen.Auth.route,
                arguments = listOf(navArgument("role") { type = NavType.StringType })
            ) { backStackEntry ->
                val role = Role.valueOf(
                    backStackEntry.arguments?.getString("role") ?: Role.TASKER.name
                )
                AuthScreen(
                    authViewModel = authViewModel,
                    preselectedRole = role,
                    onAuthSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    currentUser = currentUser,
                    taskViewModel = taskViewModel,
                    coinViewModel = coinViewModel,
                    onTaskClick = { taskId ->
                        navController.navigate(Screen.TaskDetail.createRoute(taskId))
                    },
                    onPostTask  = { navController.navigate(Screen.PostTask.route) },
                    onMyTasks   = { navController.navigate(Screen.MyTasks.route) },
                    onCoinStore = { navController.navigate(Screen.CoinStore.route) },
                    onSettings  = { navController.navigate(Screen.Settings.route) },
                    onReport    = { navController.navigate(Screen.Report.route) },  // ← NEW
                    onLogout    = {
                        authViewModel.logout()
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.PostTask.route) {
                PostTaskScreen(
                    currentUser = currentUser,
                    taskViewModel = taskViewModel,
                    onTaskPosted = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.TaskDetail.route,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
                TaskDetailScreen(
                    taskId = taskId,
                    currentUser = currentUser,
                    taskViewModel = taskViewModel,
                    messageViewModel = messageViewModel,
                    onBack    = { navController.popBackStack() },
                    onReviews = { id -> navController.navigate(Screen.Reviews.createRoute(id)) }
                )
            }

            composable(Screen.MyTasks.route) {
                MyTasksScreen(
                    currentUser = currentUser,
                    taskViewModel = taskViewModel,
                    onTaskClick = { taskId ->
                        navController.navigate(Screen.TaskDetail.createRoute(taskId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CoinStore.route) {
                CoinStoreScreen(
                    currentUser = currentUser,
                    coinViewModel = coinViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Reviews.route,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
                ReviewScreen(
                    taskId = taskId,
                    currentUser = currentUser,
                    reviewViewModel = reviewViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    currentUser = currentUser,
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onDeleteAccount = {
                        authViewModel.logout()
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Report screen ────────────────────────────────────────────────
            composable(Screen.Report.route) {
                ReportScreen(
                    currentUser = currentUser,
                    reportViewModel = reportViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}