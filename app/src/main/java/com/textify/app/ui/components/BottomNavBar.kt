package com.textify.app.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import com.textify.app.ui.Routes
import com.textify.app.ui.theme.*

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == Routes.CHAT,
            onClick = { 
                navController.navigate(Routes.CHAT) {
                    popUpTo(Routes.CHAT) { inclusive = true }
                }
            },
            icon = { Icon(Icons.Filled.Chat, contentDescription = "Conversar") },
            label = { Text("Conversar") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                unselectedIconColor = TextoMuted,
                unselectedTextColor = TextoMuted
            )
        )
        NavigationBarItem(
            selected = currentRoute == Routes.PHRASES,
            onClick = { 
                navController.navigate(Routes.PHRASES) {
                    popUpTo(Routes.PHRASES) { inclusive = true }
                }
            },
            icon = { Icon(Icons.Filled.Star, contentDescription = "Frases") },
            label = { Text("Frases") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                unselectedIconColor = TextoMuted,
                unselectedTextColor = TextoMuted
            )
        )
        NavigationBarItem(
            selected = currentRoute == Routes.PROFILE,
            onClick = { 
                navController.navigate(Routes.PROFILE) {
                    popUpTo(Routes.PROFILE) { inclusive = true }
                }
            },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                unselectedIconColor = TextoMuted,
                unselectedTextColor = TextoMuted
            )
        )
    }
}
