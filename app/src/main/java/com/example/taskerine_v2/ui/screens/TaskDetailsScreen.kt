package com.example.taskerine_v2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskerine_v2.data.model.Message
import com.example.taskerine_v2.data.model.Role
import com.example.taskerine_v2.data.model.TaskStatus
import com.example.taskerine_v2.data.model.User
import com.example.taskerine_v2.viewmodel.MessageViewModel
import com.example.taskerine_v2.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    currentUser: User?,
    taskViewModel: TaskViewModel,
    messageViewModel: MessageViewModel,
    onBack: () -> Unit,
    onReviews: (String) -> Unit
) {
    LaunchedEffect(taskId) {
        taskViewModel.selectTask(taskId)
        messageViewModel.loadMessages(taskId)
    }

    val task by taskViewModel.selectedTask.collectAsState()
    val messages by messageViewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Only tasker and requester of this task can chat
    val canChat = currentUser?.id == task?.requesterId ||
            currentUser?.id == task?.acceptedById

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
                    .fillMaxSize()
            ) {
                // Scrollable content + chat
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Task details
                    item {
                        Text(t.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Posted by ${t.requesterName}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(label = "Location", value = t.location)
                        DetailRow(label = "Reward", value = "£%.2f".format(t.reward))
                        DetailRow(
                            label = "Status",
                            value = t.status.name.lowercase().replaceFirstChar { it.uppercase() }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Description", fontWeight = FontWeight.SemiBold)
                        Text(t.description)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Action buttons
                    item {
                        val canAccept = currentUser?.role == Role.TASKER
                                && t.status == TaskStatus.OPEN
                                && t.requesterId != currentUser.id
                                && t.acceptedById != currentUser.id

                        if (canAccept) {
                            Button(
                                onClick = {
                                    currentUser.let { user ->
                                        taskViewModel.acceptTask(t.id, user.id)
                                        onBack()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Accept Task")
                            }
                        } else if (t.acceptedById == currentUser?.id && t.status == TaskStatus.ACCEPTED) {
                            OutlinedButton(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("You accepted this task")
                            }
                        }

                        val canComplete = currentUser?.id == t.requesterId
                                && t.status == TaskStatus.ACCEPTED

                        if (canComplete) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    taskViewModel.completeTask(t.id)
                                    onBack()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Mark as Completed")
                            }
                        }

                        if (t.status == TaskStatus.COMPLETED) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onReviews(t.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("⭐ Leave / View Reviews")
                            }
                        }
                    }

                    // Chat section header
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "💬 Task Chat",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (!canChat) {
                                Text(
                                    "(Only task participants can chat)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Messages
                    if (messages.isEmpty()) {
                        item {
                            Text(
                                "No messages yet. Start the conversation!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(messages) { message ->
                            MessageBubble(
                                message = message,
                                isCurrentUser = message.senderId == currentUser?.id
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                // Message input bar
                if (canChat) {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Type a message...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp)
                        )
                        FilledIconButton(
                            onClick = {
                                if (messageText.isNotBlank() && currentUser != null) {
                                    messageViewModel.sendMessage(
                                        taskId = t.id,
                                        senderId = currentUser.id,
                                        senderName = currentUser.username,
                                        content = messageText
                                    )
                                    messageText = ""
                                }
                            },
                            enabled = messageText.isNotBlank()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        } ?: Box(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("Task not found.")
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp
        )
    }
}

@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    val time = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        if (!isCurrentUser) {
            Text(
                message.senderName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    message.content,
                    fontSize = 14.sp,
                    color = if (isCurrentUser)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    time,
                    fontSize = 10.sp,
                    color = if (isCurrentUser)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}