package com.example.chatbothealth

import LoginScreen
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatbothealth.ui.theme.AppTheme

data class Message(val content: String, val isUser: Boolean, val userName: String, val userImageId: Int)

interface OpenAIOptions {
    var maxToken: Int
}

data class ChatOpenAIOptions(
    val userMessage: String = "",
    val systemMessage: String = "",
    val assistantMessage: String = "",
    var temperature: Double = 0.4,
    val top_p: Double = 1.0,
    override var maxToken: Int
) : OpenAIOptions

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "profile") {
                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }
                    composable("chat") { ChatScreen(navController) }
                    composable("goals") { GoalsScreen(navController) }
                    composable("profile") { ProfileScreen(navController) }
                    composable("settings") { SettingsScreen(navController) }

                }
            }
        }
    }
}

// Define your LoginScreen, RegisterScreen, and ChatScreen composable functions here.
