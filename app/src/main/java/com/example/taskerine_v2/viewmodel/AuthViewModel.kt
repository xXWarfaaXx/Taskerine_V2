package com.example.taskerine_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskerine_v2.data.local.PreferencesManager
import com.example.taskerine_v2.data.model.Role
import com.example.taskerine_v2.data.model.User
import com.example.taskerine_v2.data.repository.TaskerineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: TaskerineRepository,
    private val preferencesManager: PreferencesManager   // ← NEW
) : ViewModel() {

    val currentUser: StateFlow<User?> = repository.currentUserFlow

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // --- Session restore state ---
    // True while we're checking DataStore for a remembered session on app launch.
    // NavGraph waits on this before deciding Welcome vs Home, so there's no flicker.
    private val _isCheckingSession = MutableStateFlow(true)
    val isCheckingSession: StateFlow<Boolean> = _isCheckingSession

    // --- Profile update state ---
    private val _profileUpdateError = MutableStateFlow<String?>(null)
    val profileUpdateError: StateFlow<String?> = _profileUpdateError

    private val _profileUpdateSuccess = MutableStateFlow(false)
    val profileUpdateSuccess: StateFlow<Boolean> = _profileUpdateSuccess

    init {
        restoreSessionIfRemembered()
    }

    private fun restoreSessionIfRemembered() {
        viewModelScope.launch {
            val rememberedId = preferencesManager.rememberedUserId.first()
            if (rememberedId != null) {
                repository.restoreSession(rememberedId)
            }
            _isCheckingSession.value = false
        }
    }

    /**
     * Logs in by username (replaces email-based login).
     * If rememberMe is true, the session is saved to DataStore and will
     * auto-restore next time the app launches. If false, any previously
     * remembered session is cleared.
     */
    fun loginByUsername(username: String, rememberMe: Boolean, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.loginByUsername(username)
            if (user != null) {
                _error.value = null
                if (rememberMe) {
                    preferencesManager.setRememberedUserId(user.id)
                } else {
                    preferencesManager.setRememberedUserId(null)
                }
                onResult(true)
            } else {
                _error.value = "No account found with that username"
                onResult(false)
            }
        }
    }

    fun register(username: String, email: String, role: Role, onResult: (Boolean) -> Unit) {
        if (username.isBlank() || email.isBlank()) {
            _error.value = "All fields are required"
            onResult(false)
            return
        }
        viewModelScope.launch {
            repository.register(username, email, role)
            _error.value = null
            onResult(true)
        }
    }

    fun logout() {
        repository.logout()
        viewModelScope.launch {
            preferencesManager.setRememberedUserId(null)
        }
    }

    /**
     * Updates the current user's username and/or email.
     * Validation and persistence both happen in the repository.
     */
    fun updateProfile(newUsername: String, newEmail: String) {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            val result = repository.updateUserProfile(userId, newUsername, newEmail)
            result.fold(
                onSuccess = {
                    _profileUpdateError.value = null
                    _profileUpdateSuccess.value = true
                },
                onFailure = { e ->
                    _profileUpdateError.value = e.message ?: "Failed to update profile"
                    _profileUpdateSuccess.value = false
                }
            )
        }
    }

    fun clearProfileUpdateState() {
        _profileUpdateError.value = null
        _profileUpdateSuccess.value = false
    }
}