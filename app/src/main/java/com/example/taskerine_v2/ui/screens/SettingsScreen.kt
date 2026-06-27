package com.example.taskerine_v2.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskerine_v2.data.local.PreferencesManager
import com.example.taskerine_v2.data.model.User
import com.example.taskerine_v2.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: User?,
    authViewModel: AuthViewModel,
    preferencesManager: PreferencesManager,
    onBack: () -> Unit,
    onSwitchRole: () -> Unit,      // ← NEW: navigates to Welcome WITHOUT logging out
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailUpdatesEnabled by remember { mutableStateOf(false) }

    // Dark mode is now backed by DataStore so it persists across restarts
    // and is read by MainActivity to apply the theme app-wide.
    val darkModeEnabled by preferencesManager.isDarkModeEnabled.collectAsState(initial = false)

    // --- Edit profile dialog state ---
    var editField by remember { mutableStateOf<EditableField?>(null) } // null = no dialog open
    var editValue by remember { mutableStateOf("") }

    val profileError by authViewModel.profileUpdateError.collectAsState()
    val profileSuccess by authViewModel.profileUpdateSuccess.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // React to profile update results
    LaunchedEffect(profileSuccess, profileError) {
        if (profileSuccess) {
            snackbarHostState.showSnackbar("Profile updated successfully.")
            editField = null
            authViewModel.clearProfileUpdateState()
        }
        // profileError is shown inline inside the dialog itself, not as a snackbar,
        // so the user can correct it without losing their place.
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Log Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { Text("This will permanently delete your account and all your data. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteAccount()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Edit Username/Email dialog ---
    editField?.let { field ->
        AlertDialog(
            onDismissRequest = {
                editField = null
                authViewModel.clearProfileUpdateState()
            },
            title = { Text("Edit ${field.label}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editValue,
                        onValueChange = { editValue = it },
                        label = { Text(field.label) },
                        singleLine = true,
                        isError = profileError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (profileError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            profileError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when (field) {
                        EditableField.USERNAME -> authViewModel.updateProfile(
                            newUsername = editValue,
                            newEmail = currentUser?.email ?: ""
                        )
                        EditableField.EMAIL -> authViewModel.updateProfile(
                            newUsername = currentUser?.username ?: "",
                            newEmail = editValue
                        )
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    editField = null
                    authViewModel.clearProfileUpdateState()
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile card
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    currentUser?.username?.first()?.uppercase() ?: "?",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Column {
                            Text(
                                currentUser?.username ?: "Unknown",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                currentUser?.email ?: "",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Role: ${currentUser?.role?.name?.lowercase()?.replaceFirstChar { it.uppercase() }}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Account section
            item {
                SettingsSectionHeader(title = "Account")
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Person,
                        title = "Username",
                        subtitle = currentUser?.username ?: "-",
                        onClick = {
                            editValue = currentUser?.username ?: ""
                            editField = EditableField.USERNAME
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        icon = Icons.Default.Email,
                        title = "Email",
                        subtitle = currentUser?.email ?: "-",
                        onClick = {
                            editValue = currentUser?.email ?: ""
                            editField = EditableField.EMAIL
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        icon = Icons.Default.Star,
                        title = "Coins",
                        subtitle = "🪙 ${currentUser?.coins ?: 0}"
                        // No onClick — coins aren't user-editable
                    )
                }
            }

            // Notifications section
            item {
                SettingsSectionHeader(title = "Notifications")
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Default.Notifications,
                        title = "Push Notifications",
                        subtitle = "Get notified about task updates",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsToggleRow(
                        icon = Icons.Default.Email,
                        title = "Email Updates",
                        subtitle = "Receive updates via email",
                        checked = emailUpdatesEnabled,
                        onCheckedChange = { emailUpdatesEnabled = it }
                    )
                }
            }

            // Appearance section
            item {
                SettingsSectionHeader(title = "Appearance")
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Default.Settings,
                        title = "Dark Mode",
                        subtitle = "Switch to dark theme",
                        checked = darkModeEnabled,
                        onCheckedChange = { enabled ->
                            coroutineScope.launch {
                                preferencesManager.setDarkModeEnabled(enabled)
                            }
                        }
                    )
                }
            }

            // About section
            item {
                SettingsSectionHeader(title = "About")
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "App Version",
                        subtitle = "1.0.0"
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        icon = Icons.Default.Lock,
                        title = "Privacy Policy",
                        subtitle = "View our privacy policy"
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "Terms of Service",
                        subtitle = "View terms and conditions"
                    )
                }
            }

            // Danger zone
            item {
                SettingsSectionHeader(title = "Account Actions")
                SettingsCard {
                    // Switch Role — goes back to Welcome to pick a different
                    // role WITHOUT logging out. Session and Remember Me are
                    // both left untouched.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSwitchRole() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Switch Role",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Stay signed in and pick a different role",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Logout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Log Out",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "Sign out of your account",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { showLogoutDialog = true }) {
                            Text("Log Out", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Delete account
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Delete Account",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "Permanently delete your account",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { showDeleteDialog = true }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// --- Local enum for which field is being edited ---
private enum class EditableField(val label: String) {
    USERNAME("Username"),
    EMAIL("Email")
}

// --- Reusable composables ---

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        title,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null   // ← optional now; rows without it stay non-interactive
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onClick != null) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}