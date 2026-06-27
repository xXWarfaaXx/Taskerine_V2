package com.example.taskerine_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taskerine_v2.data.local.PreferencesManager
import com.example.taskerine_v2.data.repository.TaskerineRepository

class AuthViewModelFactory(
    private val repository: TaskerineRepository,
    private val preferencesManager: PreferencesManager   // ← NEW
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
