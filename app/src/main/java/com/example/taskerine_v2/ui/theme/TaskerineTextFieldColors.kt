package com.example.taskerine_v2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable

/**
 * Standard OutlinedTextField colors for Taskerine.
 * Forces typed text to render at full onSurface opacity/contrast,
 * since some Material3 versions render default input text too faint.
 *
 * Usage: OutlinedTextField(..., colors = TaskerineTextFieldColors())
 */
@Composable
fun TaskerineTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary
)
