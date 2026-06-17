package com.example.taskerine_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskerine_v2.data.model.Report
import com.example.taskerine_v2.data.repository.TaskerineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ReportUiState {
    object Idle : ReportUiState()
    object Loading : ReportUiState()
    object Success : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}

class ReportViewModel(private val repository: TaskerineRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState: StateFlow<ReportUiState> = _uiState

    fun submitReport(
        reportedUsername: String,
        description: String,
        attachmentUris: List<String>,
        reporterId: String
    ) {
        if (reportedUsername.isBlank()) {
            _uiState.value = ReportUiState.Error("Please enter a username to report.")
            return
        }
        if (description.isBlank()) {
            _uiState.value = ReportUiState.Error("Please enter a description.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading
            try {
                val report = Report(
                    id = "r${System.currentTimeMillis()}",
                    reportedUsername = reportedUsername.trim(),
                    description = description.trim(),
                    attachmentUris = attachmentUris,
                    reporterId = reporterId
                )
                repository.submitReport(report)
                _uiState.value = ReportUiState.Success
            } catch (e: Exception) {
                _uiState.value = ReportUiState.Error("Failed to submit report. Try again.")
            }
        }
    }

    fun resetState() {
        _uiState.value = ReportUiState.Idle
    }
}

class ReportViewModelFactory(private val repository: TaskerineRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

