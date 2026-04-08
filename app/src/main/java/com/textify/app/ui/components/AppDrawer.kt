package com.textify.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.textify.app.ui.theme.*

@Composable
fun AppDrawer(
    userName: String = "Usuario",
    userEmail: String = "usuario@correo.com",
    onNavigateToProfile: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPhrases: () -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(FondoClaro)
    ) {
        // Header del drawer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulOscuro)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(AzulMedio),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.first().toString(),
                    color = FondoClaro,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = userName,
                color = FondoClaro,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = userEmail,
                color = AzulClaro,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Opciones del drawer
        DrawerItem(texto = "Inicio", onClick = { onNavigateToHome(); onClose() })
        DrawerItem(texto = "Frases rápidas", onClick = { onNavigateToPhrases(); onClose() })
        DrawerItem(texto = "Mi perfil", onClick = { onNavigateToProfile(); onClose() })

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = FondoGris
        )

        DrawerItem(
            texto = "Cerrar sesión",
            onClick = onLogout,
            esDestructivo = true
        )
    }
}

@Composable
fun DrawerItem(
    texto: String,
    onClick: () -> Unit,
    esDestructivo: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = texto,
            color = if (esDestructivo) TextoMuted else TextoPrimario,
            fontSize = 14.sp
        )
    }
}