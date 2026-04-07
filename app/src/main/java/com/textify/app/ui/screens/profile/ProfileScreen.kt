package com.textify.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.textify.app.ui.components.BottomNavBar
import com.textify.app.ui.theme.*

@Composable
fun ProfileScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoClaro)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulOscuro)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Perfil",
                    color = FondoClaro,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AzulMedio),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "U",
                    color = FondoClaro,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Usuario",
                color = TextoPrimario,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "usuario@correo.com",
                color = TextoMuted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Opciones
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OpcionPerfil(texto = "Editar perfil")
                OpcionPerfil(texto = "Configuración de voz")
                OpcionPerfil(texto = "Modo oscuro")
                OpcionPerfil(texto = "Cerrar sesión", esDestructivo = true)
            }
        }
    }
}

@Composable
fun OpcionPerfil(texto: String, esDestructivo: Boolean = false) {
    OutlinedButton(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (esDestructivo) MaterialTheme.colorScheme.error
            else TextoPrimario
        )
    ) {
        Text(
            text = texto,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}