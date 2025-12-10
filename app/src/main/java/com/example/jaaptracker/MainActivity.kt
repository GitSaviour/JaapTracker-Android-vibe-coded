package com.example.jaaptracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jaaptracker.ui.theme.JaaptrackerTheme // Import your theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // This line applies your new dark theme to the entire app
            JaaptrackerTheme {
                val navController = rememberNavController()
                val viewModel: JaapViewModel = viewModel()

                NavHost(navController = navController, startDestination = "profiles") {
                    composable("profiles") {
                        ProfileScreen(viewModel = viewModel, onProfileClick = { profileId ->
                            navController.navigate("history/$profileId")
                        })
                    }
                    composable(
                        "history/{profileId}",
                        arguments = listOf(navArgument("profileId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val profileId = backStackEntry.arguments?.getLong("profileId") ?: 0L
                        HistoryScreen(viewModel = viewModel, profileId = profileId)
                    }
                }
            }
        }
    }
}