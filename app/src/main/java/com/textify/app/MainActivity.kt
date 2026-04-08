package com.textify.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.textify.app.ui.Routes
import com.textify.app.ui.screens.splash.SplashScreen
import com.textify.app.ui.screens.auth.LoginScreen
import com.textify.app.ui.screens.auth.RegisterScreen
import com.textify.app.ui.screens.home.HomeScreen
import com.textify.app.ui.screens.chat.ChatScreen
import com.textify.app.ui.screens.phrases.PhrasesScreen
import com.textify.app.ui.screens.profile.ProfileScreen
import com.textify.app.ui.theme.TextifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Habilitar diseño de borde a borde
        enableEdgeToEdge()
        
        try {
            setContent {
                TextifyTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        TextifyNavigation()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al iniciar Compose: ${e.message}")
        }
    }
}

@Composable
fun TextifyNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(route = Routes.SPLASH) {
            SplashScreen(onNavigateToLogin = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
        
        composable(route = Routes.LOGIN) {
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
        
        composable(route = Routes.REGISTER) {
            RegisterScreen(onNavigateToLogin = {
                navController.popBackStack()
            })
        }
        
        composable(route = Routes.HOME) {
            HomeScreen(navController = navController)
        }
        
        composable(route = Routes.CHAT) {
            ChatScreen(navController = navController)
        }
        
        composable(route = Routes.PHRASES) {
            PhrasesScreen(navController = navController)
        }
        
        composable(route = Routes.PROFILE) {
            ProfileScreen(navController = navController)
        }
    }
}
