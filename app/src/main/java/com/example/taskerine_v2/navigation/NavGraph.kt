package com.taskerine.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.taskerine.ui.screens.*
import com.taskerine.viewmodel.AuthViewModel
import com.taskerine.viewmodel.TaskViewModel

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object PostTask : Screen("post_task")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object MyTasks : Screen("my_tasks")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) Screen.Home.route else Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
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
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Auth.route) {
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
                onBack = { navController.popBackStack() }
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
    }
}