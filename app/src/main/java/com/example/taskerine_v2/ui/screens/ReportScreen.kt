package com.example.taskerine_v2.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.taskerine_v2.data.model.User
import com.example.taskerine_v2.viewmodel.ReportUiState
import com.example.taskerine_v2.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    currentUser: User?,
    reportViewModel: ReportViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by reportViewModel.uiState.collectAsState()

    var reportedUsername by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var attachedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var usernameError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // File picker — allows multiple files (images, PDFs, docs)
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        attachedUris = (attachedUris + uris).distinct()
    }

    // Handle UI state side-effects
    LaunchedEffect(uiState) {
        when (uiState) {
            is ReportUiState.Success -> {
                snackbarHostState.showSnackbar("Report submitted successfully.")
                reportViewModel.resetState()
                onBack()
            }
            is ReportUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as ReportUiState.Error).message)
                reportViewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Report a User") },
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Info banner
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Reports are reviewed by our team. Please provide as much detail as possible.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Username field
            Column {
                Text(
                    "Reported Username",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = reportedUsername,
                    onValueChange = {
                        reportedUsername = it
                        usernameError = false
                    },
                    placeholder = { Text("Enter username") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    isError = usernameError,
                    supportingText = {
                        if (usernameError) Text("Username is required", color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Description field
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Description",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        "${description.length}/1000",
                        fontSize = 12.sp,
                        color = if (description.length > 1000)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        if (it.length <= 1000) {
                            description = it
                            descriptionError = false
                        }
                    },
                    placeholder = { Text("Describe what happened in detail...") },
                    isError = descriptionError,
                    supportingText = {
                        if (descriptionError) Text("Description is required", color = MaterialTheme.colorScheme.error)
                    },
                    minLines = 5,
                    maxLines = 10,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Attach files section
            Column {
                Text(
                    "Attachments",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = {
                        filePicker.launch(
                            arrayOf(
                                "image/*",
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Attach Files (images, PDFs, docs)")
                }

                // Attached files list
                AnimatedVisibility(visible = attachedUris.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        attachedUris.forEachIndexed { index, uri ->
                            val fileName = uri.lastPathSegment
                                ?.substringAfterLast("/")
                                ?.substringAfterLast(":")
                                ?: "File ${index + 1}"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = fileName,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                IconButton(
                                    onClick = {
                                        attachedUris = attachedUris.toMutableList()
                                            .also { it.removeAt(index) }
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Submit button
            Button(
                onClick = {
                    usernameError = reportedUsername.isBlank()
                    descriptionError = description.isBlank()
                    if (!usernameError && !descriptionError) {
                        reportViewModel.submitReport(
                            reportedUsername = reportedUsername,
                            description = description,
                            attachmentUris = attachedUris.map { it.toString() },
                            reporterId = currentUser?.id ?: ""
                        )
                    }
                },
                enabled = uiState !is ReportUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState is ReportUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Submit Report", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}