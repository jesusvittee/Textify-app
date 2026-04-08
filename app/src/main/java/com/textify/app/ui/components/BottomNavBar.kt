package com.textify.app.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import com.textify.app.ui.Routes
import com.textify.app.ui.theme.*

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = BurbujaOyente,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = { navController.navigate(Routes.HOME) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AzulOscuro,
                selectedTextColor = AzulOscuro,
                indicatorColor = FondoGris,
                unselectedIconColor = TextoMuted,
                unselectedTextColor = TextoMuted
            )
        )
        NavigationBarItem(
            selected = currentRoute == Routes.CHAT,
            onClick = { navController.navigate(Routes.CHAT) },
            icon = { Icon(Icons.Filled.Chat, contentDescription = "Chat") },
            label = { Text("Chat") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AzulOscuro,
                selectedTextColor = AzulOscuro,
                indicatorColor = FondoGris,
                unselectedIconColor = TextoMuted,
                unselectedTextColor = TextoMuted
            )
        )
        NavigationBarItem(
            selected = currentRoute == Routes.PHRASES,
            onClick = { navController.navigate(Routes.PHRASES) },
            icon = { Icon(Icons.Filled.Star, contentDescription = "Frases") },
            label = { Text("Frases") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AzulOscuro,
                selectedTextColor = AzulOscuro,
                indicatorColor = FondoGris,
                unselectedIconColor = TextoMuted,
                unselectedTextColor = TextoMuted
            )
        )
        NavigationBarItem(
            selected = currentRoute == Routes.PROFILE,
            onClick = { navController.navigate(Routes.PROFILE) },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AzulOscuro,
                selectedTextColor = AzulOscuro,
                indicatorColor = FondoGris,
                unselectedIconColor = TextoMuted,
                unselectedTextColor = TextoMuted
            )
        )
    }
}
