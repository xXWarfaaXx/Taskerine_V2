package com.example.taskerine_v2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskerine_v2.data.model.Task
import com.example.taskerine_v2.data.model.User
import com.example.taskerine_v2.ui.components.LocationPickerMap
import com.example.taskerine_v2.viewmodel.TaskViewModel

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
    var useMapPicker by remember { mutableStateOf(false) }

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
                .padding(horizontal = 24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

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

            // Location section
            Text("Location", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

            // Toggle between text input and map picker
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !useMapPicker,
                    onClick = { useMapPicker = false },
                    label = { Text("Type Address") }
                )
                FilterChip(
                    selected = useMapPicker,
                    onClick = { useMapPicker = true },
                    label = { Text("Pick on Map") }
                )
            }

            if (useMapPicker) {
                LocationPickerMap(
                    onLocationPicked = { address, _ ->
                        location = address
                    }
                )
                if (location.isNotBlank()) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Selected Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("e.g. Brixton, London") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

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

            Spacer(modifier = Modifier.height(4.dp))

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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}