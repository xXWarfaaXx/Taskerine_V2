package com.example.taskerine_v2.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskerine_v2.data.model.Role
import com.example.taskerine_v2.data.model.Task
import com.example.taskerine_v2.data.model.User
import com.example.taskerine_v2.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUser: User?,
    taskViewModel: TaskViewModel,
    onTaskClick: (String) -> Unit,
    onPostTask: () -> Unit,
    onMyTasks: () -> Unit,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) { taskViewModel.loadOpenTasks() }
    val openTasks by taskViewModel.openTasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Taskerine") },
                actions = {
                    IconButton(onClick = onMyTasks) {
                        Icon(Icons.Default.List, contentDescription = "My Tasks")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentUser?.role == Role.REQUESTER) {
                FloatingActionButton(onClick = onPostTask) {
                    Icon(Icons.Default.Add, contentDescription = "Post Task")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Hello, ${currentUser?.username ?: "User"}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Role: ${currentUser?.role?.name?.lowercase()?.replaceFirstChar { it.uppercase() }}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Open Tasks", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (openTasks.isEmpty()) {
                Text("No tasks available right now.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(openTasks) { task ->
                        TaskCard(task = task, onClick = { onTaskClick(task.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(task: Task, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(task.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(task.location, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text("£%.2f".format(task.reward), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}