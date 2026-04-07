package com.textify.app.ui.screens.phrases

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.textify.app.ui.components.BottomNavBar
import com.textify.app.ui.theme.*

@Composable
fun PhrasesScreen(navController: NavController) {
    val frases = remember {
        mutableStateListOf(
            "Hola, buenos días. ¿En qué puedo ayudarte?",
            "Necesito un momento para escribir mi respuesta.",
            "No escucho bien, ¿puedes repetirlo más despacio?",
            "Por favor escribe lo que me quieres decir.",
            "Gracias, entendí perfectamente."
        )
    }
    var nuevaFrase by remember { mutableStateOf("") }

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
                    .padding(16.dp)
            ) {
                Text(
                    text = "Frases rápidas",
                    color = FondoClaro,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Agregar frase
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nuevaFrase,
                    onValueChange = { nuevaFrase = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nueva frase...", fontSize = 13.sp) },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (nuevaFrase.isNotBlank()) {
                            frases.add(nuevaFrase)
                            nuevaFrase = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Verde),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Agregar", color = AzulOscuro, fontSize = 13.sp)
                }
            }

            // Lista de frases
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(frases) { frase ->
                    FraseItem(frase)
                    HorizontalDivider(color = FondoGris)
                }
            }
        }
    }
}

@Composable
fun FraseItem(frase: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = frase,
            color = TextoPrimario,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            lineHeight = 20.sp
        )
        IconButton(onClick = { }) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Reproducir",
                tint = AzulOscuro
            )
        }
    }
}