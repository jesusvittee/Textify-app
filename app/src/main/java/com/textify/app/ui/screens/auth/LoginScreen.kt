package com.textify.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textify.app.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    // Valores predeterminados para desarrollo
    var email by remember { mutableStateOf("nicolasvitejesus@gmail.com") }
    var password by remember { mutableStateOf("Awe12lo34") }
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))

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
            singleLine = true,
            enabled = authState !is AuthState.Loading
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
            singleLine = true,
            enabled = authState !is AuthState.Loading
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).error,
                color = Rojo,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Botón Entrar
        Button(
            onClick = { viewModel.login(email, password, onNavigateToHome) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Verde),
            enabled = authState !is AuthState.Loading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AzulOscuro)
            } else {
                Text(
                    text = "Entrar",
                    color = AzulOscuro,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToRegister,
            enabled = authState !is AuthState.Loading
        ) {
            Text(
                text = "¿No tienes cuenta? Regístrate",
                color = AzulClaro,
                fontSize = 13.sp
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}
