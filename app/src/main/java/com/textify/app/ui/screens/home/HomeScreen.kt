package com.textify.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class ConversacionPrevia(
    val nombre: String,
    val ultimoMensaje: String,
    val hora: String
)

@Composable
fun HomeScreen(navController: NavController) {

    val conversaciones = listOf(
        ConversacionPrevia("Ana García", "Hola, ¿cómo estás?", "10:30"),
        ConversacionPrevia("Carlos López", "Nos vemos mañana", "09:15"),
        ConversacionPrevia("María Torres", "Gracias por tu ayuda", "Ayer"),
    )

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoClaro)
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulOscuro)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Textify",
                    color = FondoClaro,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Lista de conversaciones
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(conversaciones) { conversacion ->
                    ConversacionItem(conversacion)
                    HorizontalDivider(color = FondoGris, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun ConversacionItem(conversacion: ConversacionPrevia) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(AzulMedio),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = conversacion.nombre.first().toString(),
                color = FondoClaro,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversacion.nombre,
                color = TextoPrimario,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = conversacion.ultimoMensaje,
                color = TextoMuted,
                fontSize = 12.sp
            )
        }

        Text(
            text = conversacion.hora,
            color = TextoMuted,
            fontSize = 11.sp
        )
    }
}