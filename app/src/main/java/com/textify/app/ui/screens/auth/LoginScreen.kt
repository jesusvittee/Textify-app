package com.textify.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.textify.app.ui.theme.*

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido",
            color = FondoClaro,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Inicia sesión para continuar",
            color = AzulClaro,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo email
        Text(
            text = "Correo electrónico",
            color = AzulClaro,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulClaro,
                unfocusedBorderColor = AzulMedio,
                focusedTextColor = FondoClaro,
                unfocusedTextColor = FondoClaro
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Campo contraseña
        Text(
            text = "Contraseña",
            color = AzulClaro,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulClaro,
                unfocusedBorderColor = AzulMedio,
                focusedTextColor = FondoClaro,
                unfocusedTextColor = FondoClaro
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón ingresar
        Button(
            onClick = onNavigateToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Verde)
        ) {
            Text(
                text = "Ingresar",
                color = AzulOscuro,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text(
                text = "¿No tienes cuenta? Regístrate",
                color = AzulClaro,
                fontSize = 13.sp
            )
        }
    }
}