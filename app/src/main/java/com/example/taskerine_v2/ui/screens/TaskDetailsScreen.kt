package com.example.taskerine_v2.ui.screens

import com.example.taskerine_v2.ui.components.TaskLocationMap
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
import androidx.compose.ui.graphics.Color
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

// A system message is one sent by "system" — displayed differently in chat
private val SYSTEM_SENDER_ID = "system"

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
    val completionError by taskViewModel.completionError.collectAsState()
    val completionSuccess by taskViewModel.completionSuccess.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var completionRequested by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Check if a completion request message already exists in chat
    // so we don't let the Tasker spam "Mark as Complete"
    val completionAlreadyRequested = remember(messages) {
        messages.any { it.senderId == SYSTEM_SENDER_ID && it.content.contains("marked this task as complete") }
    }

    // React to payment/completion result
    LaunchedEffect(completionSuccess, completionError) {
        if (completionSuccess) {
            snackbarHostState.showSnackbar("✅ Payment released! Task marked as complete.")
            taskViewModel.clearCompletionState()
        }
        if (completionError != null) {
            snackbarHostState.showSnackbar(completionError ?: "")
            taskViewModel.clearCompletionState()
        }
    }

    val canChat = currentUser?.id == task?.requesterId ||
            currentUser?.id == task?.acceptedById

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        Spacer(modifier = Modifier.height(8.dp))
                        TaskLocationMap(location = t.location)
                        Spacer(modifier = Modifier.height(8.dp))
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
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Accept task (Tasker only, open tasks)
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
                            } else if (t.acceptedById == currentUser?.id
                                && t.status == TaskStatus.ACCEPTED
                            ) {
                                OutlinedButton(
                                    onClick = {},
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("You accepted this task")
                                }
                            }

                            // ── Mark as Complete (Tasker who accepted) ───────
                            val isAcceptedTasker = currentUser?.id == t.acceptedById
                                    && t.status == TaskStatus.ACCEPTED

                            if (isAcceptedTasker && !completionAlreadyRequested) {
                                Button(
                                    onClick = {
                                        taskViewModel.requestCompletion(
                                            taskId = t.id,
                                            taskerName = currentUser?.username ?: "Tasker"
                                        ) {}
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("✅ Mark as Complete")
                                }
                            }

                            if (isAcceptedTasker && completionAlreadyRequested) {
                                OutlinedButton(
                                    onClick = {},
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Completion request sent — awaiting Requester confirmation")
                                }
                            }

                            // ── Confirm & Release Payment (Requester) ────────
                            val isRequesterPendingConfirm = currentUser?.id == t.requesterId
                                    && t.status == TaskStatus.ACCEPTED
                                    && completionAlreadyRequested

                            if (isRequesterPendingConfirm) {
                                Button(
                                    onClick = {
                                        taskViewModel.completeTaskWithPayment(
                                            taskId = t.id,
                                            taskerId = t.acceptedById ?: "",
                                            rewardAmount = t.reward.toInt()
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    )
                                ) {
                                    Text("💰 Confirm & Release Payment (${t.reward.toInt()} coins)")
                                }
                            }

                            // Reviews (completed tasks)
                            if (t.status == TaskStatus.COMPLETED) {
                                Button(
                                    onClick = { onReviews(t.id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("⭐ Leave / View Reviews")
                                }
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
                            if (message.senderId == SYSTEM_SENDER_ID) {
                                SystemMessageBubble(message = message)
                            } else {
                                MessageBubble(
                                    message = message,
                                    isCurrentUser = message.senderId == currentUser?.id
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                // Message input bar
                if (canChat && t.status != TaskStatus.COMPLETED) {
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

// System messages rendered differently — centred, amber-tinted, no bubble tail
@Composable
fun SystemMessageBubble(message: Message) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.content,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
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
        Text(text = value, fontSize = 14.sp)
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