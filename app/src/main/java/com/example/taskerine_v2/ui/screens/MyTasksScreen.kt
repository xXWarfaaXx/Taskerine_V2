package com.example.taskerine_v2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskerine_v2.data.model.User
import com.example.taskerine_v2.viewmodel.TaskViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTasksScreen(
    currentUser: User?,
    taskViewModel: TaskViewModel,
    onTaskClick: (String) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { taskViewModel.loadMyTasks(it) }
    }
    val myTasks by taskViewModel.myTasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tasks") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            if (myTasks.isEmpty()) {
                Text(
                    "No tasks yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(myTasks) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            onClick = { onTaskClick(task.id) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(task.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(task.status.name, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("£%.2f".format(task.reward), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}