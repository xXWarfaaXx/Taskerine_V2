package com.example.taskerine_v2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.taskerine_v2.data.local.PreferencesManager
import com.example.taskerine_v2.navigation.NavGraph
import com.example.taskerine_v2.ui.theme.TaskerineTheme
import com.example.taskerine_v2.viewmodel.AuthViewModel
import com.example.taskerine_v2.viewmodel.AuthViewModelFactory
import com.example.taskerine_v2.viewmodel.CoinViewModel
import com.example.taskerine_v2.viewmodel.CoinViewModelFactory
import com.example.taskerine_v2.viewmodel.MessageViewModel
import com.example.taskerine_v2.viewmodel.MessageViewModelFactory
import com.example.taskerine_v2.viewmodel.ReportViewModel
import com.example.taskerine_v2.viewmodel.ReportViewModelFactory
import com.example.taskerine_v2.viewmodel.ReviewViewModel
import com.example.taskerine_v2.viewmodel.ReviewViewModelFactory
import com.example.taskerine_v2.viewmodel.TaskViewModel
import com.example.taskerine_v2.viewmodel.TaskViewModelFactory

class MainActivity : ComponentActivity() {

    private val repository by lazy { (application as TaskerineApp).repository }
    private val preferencesManager by lazy { PreferencesManager(applicationContext) }  // ← NEW

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(repository, preferencesManager)
    }
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(repository)
    }
    private val coinViewModel: CoinViewModel by viewModels {
        CoinViewModelFactory(repository)
    }
    private val reviewViewModel: ReviewViewModel by viewModels {
        ReviewViewModelFactory(repository)
    }
    private val messageViewModel: MessageViewModel by viewModels {
        MessageViewModelFactory(repository)
    }
    private val reportViewModel: ReportViewModel by viewModels {
        ReportViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Reads the saved preference reactively — updates instantly when
            // SettingsScreen writes a new value, no restart needed.
            val isDarkMode by preferencesManager.isDarkModeEnabled.collectAsState(initial = false)

            TaskerineTheme(darkTheme = isDarkMode) {
                Surface {
                    val navController = rememberNavController()
                    NavGraph(
                        navController    = navController,
                        authViewModel    = authViewModel,
                        taskViewModel    = taskViewModel,
                        coinViewModel    = coinViewModel,
                        reviewViewModel  = reviewViewModel,
                        messageViewModel = messageViewModel,
                        reportViewModel  = reportViewModel,
                        preferencesManager = preferencesManager   // ← NEW
                    )
                }
            }
        }
    }
}