package com.example.taskerine_v2.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskerine_v2.data.model.Message
import com.example.taskerine_v2.data.model.Task
import com.example.taskerine_v2.data.model.TaskStatus
import com.example.taskerine_v2.data.model.User
import com.example.taskerine_v2.viewmodel.MessageViewModel
import com.example.taskerine_v2.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesInboxScreen(
    currentUser: User?,
    taskViewModel: TaskViewModel,
    messageViewModel: MessageViewModel,
    onTaskClick: (String) -> Unit
) {
    val allTasks by taskViewModel.allTasks.collectAsState()

    // All tasks this user is involved in — as requester or accepted tasker
    val myConversations = remember(allTasks, currentUser) {
        allTasks.filter { task ->
            task.requesterId == currentUser?.id ||
                    task.acceptedById == currentUser?.id
        }.sortedByDescending { it.status.ordinal }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Messages") })
        }
    ) { padding ->
        if (myConversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        "No conversations yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Accept or post a task to start chatting",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(myConversations, key = { it.id }) { task ->
                    ConversationRow(
                        task = task,
                        currentUser = currentUser,
                        messageViewModel = messageViewModel,
                        onClick = { onTaskClick(task.id) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
fun ConversationRow(
    task: Task,
    currentUser: User?,
    messageViewModel: MessageViewModel,
    onClick: () -> Unit
) {
    // Collect the live message flow for this specific task
    // collectAsState(initial = emptyList()) handles the plain Flow correctly
    val messages by messageViewModel.getMessagesForTask(task.id)
        .collectAsState(initial = emptyList<Message>())

    val lastMessage: Message? = messages.lastOrNull()

    val statusColor = when (task.status) {
        TaskStatus.OPEN      -> MaterialTheme.colorScheme.primary
        TaskStatus.ACCEPTED  -> MaterialTheme.colorScheme.tertiary
        TaskStatus.COMPLETED -> MaterialTheme.colorScheme.onSurfaceVariant
        else                 -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val statusLabel = task.status.name.lowercase()
        .replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar — first letter of task title
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    task.title.first().uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    task.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (lastMessage != null) {
                    Text(
                        formatMessageTime(lastMessage.timestamp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Last message preview
                val previewText = when {
                    lastMessage == null -> "No messages yet"
                    lastMessage.senderId == "system" -> "🔔 ${lastMessage.content}"
                    lastMessage.senderId == currentUser?.id -> "You: ${lastMessage.content}"
                    else -> "${lastMessage.senderName}: ${lastMessage.content}"
                }
                Text(
                    text = previewText,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Status badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        statusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000L      -> "Just now"
        diff < 3_600_000L   -> "${diff / 60_000}m ago"
        diff < 86_400_000L  -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        else                -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}