package com.example.taskerine_v2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.taskerine_v2.navigation.NavGraph
import com.example.taskerine_v2.ui.theme.TaskerineTheme
import com.example.taskerine_v2.viewmodel.AuthViewModel
import com.example.taskerine_v2.viewmodel.CoinViewModel
import com.example.taskerine_v2.viewmodel.ReviewViewModel
import com.example.taskerine_v2.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val taskViewModel: TaskViewModel by viewModels()
    private val coinViewModel: CoinViewModel by viewModels()
    private val reviewViewModel: ReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskerineTheme {
                Surface {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        taskViewModel = taskViewModel,
                        coinViewModel = coinViewModel,
                        reviewViewModel = reviewViewModel
                    )
                }
            }
        }
    }
}