package com.example.taskerine_v2.ui.screens

<<<<<<< HEAD
=======


>>>>>>> 719f10f52cdd6910ef3937185d4ae8c9d17f6743
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
<<<<<<< HEAD
import com.example.taskerine_v2.data.model.Task
import com.example.taskerine_v2.data.model.User
import com.example.taskerine_v2.viewmodel.TaskViewModel

// ... rest of the file stays exactly the same
=======
import com.taskerine.data.model.Task
import com.taskerine.data.model.User
import com.taskerine.viewmodel.TaskViewModel
>>>>>>> 719f10f52cdd6910ef3937185d4ae8c9d17f6743

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostTaskScreen(
    currentUser: User?,
    taskViewModel: TaskViewModel,
    onTaskPosted: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var reward by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post a Task") },
                navigationIcon = {
                    IconButton(onClick = onTaskPosted) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = reward,
                onValueChange = { reward = it },
                label = { Text("Reward (£)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val rewardValue = reward.toDoubleOrNull()
                    when {
                        title.isBlank() || description.isBlank() || location.isBlank() ->
                            error = "Please fill in all fields"
                        rewardValue == null || rewardValue <= 0 ->
                            error = "Enter a valid reward amount"
                        currentUser == null ->
                            error = "You must be logged in"
                        else -> {
                            taskViewModel.postTask(
                                Task(
                                    title = title,
                                    description = description,
                                    location = location,
                                    reward = rewardValue,
                                    requesterId = currentUser.id,
                                    requesterName = currentUser.username
                                )
                            )
                            onTaskPosted()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Post Task")
            }
        }
    }
}