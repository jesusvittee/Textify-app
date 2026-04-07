package com.textify.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.textify.app.ui.theme.*

data class Mensaje(
    val texto: String,
    val esMio: Boolean
)

@Composable
fun ChatScreen(navController: NavController) {
    var textoInput by remember { mutableStateOf("") }
    val mensajes = remember {
        mutableStateListOf(
            Mensaje("Hola, buenos días. ¿En qué puedo ayudarte?", false),
            Mensaje("Necesito un momento para escribir mi respuesta.", true),
        )
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulOscuro)
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = FondoClaro
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Chat activo",
                        color = FondoClaro,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BurbujaOyente)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textoInput,
                    onValueChange = { textoInput = it },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    placeholder = { Text("Escribe un mensaje...", fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulClaro,
                        unfocusedBorderColor = FondoGris
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Botón micrófono
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Verde),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = "Micrófono",
                            tint = AzulOscuro
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                // Botón enviar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AzulMedio),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        if (textoInput.isNotBlank()) {
                            mensajes.add(Mensaje(textoInput, true))
                            textoInput = ""
                        }
                    }) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "Enviar",
                            tint = FondoClaro
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoGris)
                .padding(paddingValues)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mensajes) { mensaje ->
                BurbujaMensaje(mensaje)
            }
        }
    }
}

@Composable
fun BurbujaMensaje(mensaje: Mensaje) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mensaje.esMio)
            Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomEnd = if (mensaje.esMio) 2.dp else 12.dp,
                        bottomStart = if (mensaje.esMio) 12.dp else 2.dp
                    )
                )
                .background(if (mensaje.esMio) BurbujaSordo else BurbujaOyente)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 260.dp)
        ) {
            Text(
                text = mensaje.texto,
                color = if (mensaje.esMio) AzulOscuro else TextoPrimario,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}