package com.example.taskerine_v2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskerine_v2.data.model.Role
import com.example.taskerine_v2.data.model.TaskStatus
import com.example.taskerine_v2.data.model.User
import com.example.taskerine_v2.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    currentUser: User?,
    taskViewModel: TaskViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(taskId) { taskViewModel.selectTask(taskId) }
    val task by taskViewModel.selectedTask.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        task?.let { t ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(t.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Posted by ${t.requesterName}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                HorizontalDivider()

                DetailRow(label = "Location", value = t.location)
                DetailRow(label = "Reward", value = "£%.2f".format(t.reward))
                DetailRow(label = "Status", value = t.status.name)

                HorizontalDivider()

                Text("Description", fontWeight = FontWeight.SemiBold)
                Text(t.description)

                Spacer(modifier = Modifier.weight(1f))

                val canAccept = currentUser?.role == Role.TASKER
                        && t.status == TaskStatus.OPEN
                        && t.requesterId != currentUser?.id
                        && t.acceptedById != currentUser?.id

                if (canAccept) {
                    Button(
                        onClick = {
                            currentUser?.let { user ->
                                taskViewModel.acceptTask(t.id, user.id)
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Accept Task")
                    }
                } else if (t.acceptedById == currentUser?.id) {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("You accepted this task")
                    }
                }
            }
        } ?: Box(modifier = Modifier.padding(padding).padding(24.dp)) {
            Text("Task not found.")
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}