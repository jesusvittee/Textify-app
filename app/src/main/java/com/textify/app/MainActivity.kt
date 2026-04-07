package com.textify.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.textify.app.ui.Routes
import com.textify.app.ui.screens.splash.SplashScreen
import com.textify.app.ui.screens.auth.LoginScreen
import com.textify.app.ui.screens.home.HomeScreen
import com.textify.app.ui.screens.chat.ChatScreen
import com.textify.app.ui.screens.phrases.PhrasesScreen
import com.textify.app.ui.screens.profile.ProfileScreen
import com.textify.app.ui.theme.TextifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextifyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Routes.SPLASH
                    ) {
                        composable(Routes.REGISTER) {
                            RegisterScreen(onNavigateToLogin = {
                                navController.popBackStack()
                            })
                        }
                        composable(Routes.SPLASH) {
                            SplashScreen(onNavigateToLogin = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.SPLASH) { inclusive = true }
                                }
                            })
                        }
                        composable(Routes.LOGIN) {
                            LoginScreen(
                                onNavigateToHome = {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate(Routes.REGISTER)
                                }
                            )
                        }
                        composable(Routes.HOME) {
                            HomeScreen(navController = navController)
                        }
                        composable(Routes.CHAT) {
                            ChatScreen(navController = navController)
                        }
                        composable(Routes.PHRASES) {
                            PhrasesScreen(navController = navController)
                        }
                        composable(Routes.PROFILE) {
                            ProfileScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}