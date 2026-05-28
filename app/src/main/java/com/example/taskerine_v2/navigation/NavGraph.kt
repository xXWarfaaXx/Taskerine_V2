package com.example.taskerine_v2.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.taskerine_v2.data.model.Role
import com.example.taskerine_v2.ui.screens.*
import com.example.taskerine_v2.viewmodel.AuthViewModel
import com.example.taskerine_v2.viewmodel.CoinViewModel
import com.example.taskerine_v2.viewmodel.ReviewViewModel
import com.example.taskerine_v2.viewmodel.TaskViewModel

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Auth : Screen("auth/{role}") {
        fun createRoute(role: Role) = "auth/${role.name}"
    }
    object Home : Screen("home")
    object PostTask : Screen("post_task")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object MyTasks : Screen("my_tasks")
    object CoinStore : Screen("coin_store")
    object Reviews : Screen("reviews/{taskId}") {
        fun createRoute(taskId: String) = "reviews/$taskId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    coinViewModel: CoinViewModel,
    reviewViewModel: ReviewViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) Screen.Home.route else Screen.Welcome.route
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
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onPostTask = { navController.navigate(Screen.PostTask.route) },
                onMyTasks = { navController.navigate(Screen.MyTasks.route) },
                onCoinStore = { navController.navigate(Screen.CoinStore.route) },
                onLogout = {
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
                onBack = { navController.popBackStack() },
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
    }
}