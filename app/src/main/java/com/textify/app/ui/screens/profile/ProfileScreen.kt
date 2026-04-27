package com.textify.app.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.textify.app.ui.Routes
import com.textify.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Estados para diálogos de confirmación de sincronización
    var showPushConfirmDialog by remember { mutableStateOf(false) }
    var showPullConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = contentPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.statusBarsPadding()) }
            
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, AzulClaro.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AzulOscuro),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.name.take(2).uppercase(),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = uiState.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = uiState.email,
                                fontSize = 14.sp,
                                color = AzulMedio,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // --- SINCRONIZACIÓN REMOTA ---
            item {
                SectionCard("SINCRONIZACIÓN REMOTA") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Estado de la nube",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    uiState.syncStatus,
                                    fontSize = 12.sp,
                                    color = if(uiState.syncStatus.contains("Error")) Rojo else AzulMedio
                                )
                            }
                            
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = AzulMedio)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = AzulMedio
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { showPushConfirmDialog = true },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isSyncing,
                                colors = ButtonDefaults.buttonColors(containerColor = Verde),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Subir", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showPullConfirmDialog = true },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isSyncing,
                                colors = ButtonDefaults.buttonColors(containerColor = AzulMedio),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Bajar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // MODALIDAD OFFLINE
            item {
                SectionCard("MODO OFFLINE") {
                    Text(
                        "Usa la app sin conexión descargando el paquete de voz en español.",
                        fontSize = 13.sp,
                        color = TextoMuted,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    if (uiState.isOfflinePackageInstalled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Verde, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Paquete instalado", color = Verde, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.downloadOfflinePackage() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isDownloading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AzulMedio,
                                disabledContainerColor = FondoGris
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isDownloading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(Modifier.width(12.dp))
                                Text(uiState.downloadProgress, color = Color.White)
                            } else {
                                Icon(Icons.Default.Download, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Descargar paquete (40MB)")
                            }
                        }
                    }
                }
            }

            // Apariencia
            item {
                SectionCard("APARIENCIA") {
                    SettingsSwitchRow(
                        label = "Modo oscuro",
                        checked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Tamaño de fuente", 
                        color = MaterialTheme.colorScheme.onSurface, 
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("A", fontSize = 12.sp, color = TextoMuted, fontWeight = FontWeight.Bold)
                        Slider(
                            value = uiState.fontScale,
                            onValueChange = { viewModel.setFontScale(it) },
                            valueRange = 0.8f..1.5f,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = AzulMedio,
                                activeTrackColor = AzulMedio,
                                inactiveTrackColor = FondoGris
                            )
                        )
                        Text("A", fontSize = 22.sp, color = TextoMuted, fontWeight = FontWeight.Bold)
                    }
                    
                    Text(
                        text = when {
                            uiState.fontScale < 0.95f -> "Pequeño"
                            uiState.fontScale < 1.1f -> "Normal"
                            else -> "Grande"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = AzulMedio
                    )
                }
            }

            // Voz
            item {
                SectionCard("VOZ") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if(uiState.isDarkMode) SuperficieOscura2 else Color(0xFFF0F2F5), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        GenderButton(
                            text = "Femenino",
                            icon = Icons.Default.Female,
                            selected = uiState.voiceGender == VoiceGender.FEMALE,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setVoiceGender(VoiceGender.FEMALE) }
                        )
                        GenderButton(
                            text = "Masculino",
                            icon = Icons.Default.Male,
                            selected = uiState.voiceGender == VoiceGender.MALE,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setVoiceGender(VoiceGender.MALE) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val voicesToShow = if (uiState.voiceGender == VoiceGender.FEMALE) uiState.femaleVoices else uiState.maleVoices
                    voicesToShow.forEach { voice ->
                        VoiceItem(
                            voice = voice,
                            selected = uiState.selectedVoiceId == voice.id,
                            onClick = { 
                                viewModel.setSelectedVoice(voice.id)
                                viewModel.playVoicePreview(voice)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Action Buttons
            item {
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, AzulClaro.copy(alpha = 0.5f))
                ) {
                    Text("Cerrar sesión", color = if(uiState.isDarkMode) Color.White else AzulOscuro, fontWeight = FontWeight.Bold)
                }
            }

            item {
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Eliminar cuenta", color = Rojo, fontSize = 14.sp)
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // --- DIÁLOGOS DE CONFIRMACIÓN ---

    if (showPushConfirmDialog) {
        CustomAlertDialog(
            title = "¿Subir a la nube?",
            message = "Esta acción reemplazará los datos de la nube con los de tu dispositivo. Los datos antiguos en la nube se borrarán.",
            icon = Icons.Default.CloudUpload,
            confirmText = "Subir ahora",
            confirmColor = Verde,
            onConfirm = {
                showPushConfirmDialog = false
                viewModel.performSync(pushLocal = true)
            },
            onDismiss = { showPushConfirmDialog = false }
        )
    }

    if (showPullConfirmDialog) {
        CustomAlertDialog(
            title = "¿Bajar de la nube?",
            message = "Se descargarán los datos de la nube y se sobrescribirá tu información local actual. No podrás deshacer este cambio.",
            icon = Icons.Default.CloudDownload,
            confirmText = "Bajar ahora",
            confirmColor = AzulMedio,
            onConfirm = {
                showPullConfirmDialog = false
                viewModel.performSync(pushLocal = false)
            },
            onDismiss = { showPullConfirmDialog = false }
        )
    }

    if (showLogoutDialog) {
        CustomAlertDialog(
            title = "¿Cerrar sesión?",
            message = "Tu historial local se conservará en este dispositivo.",
            icon = Icons.AutoMirrored.Filled.Logout,
            confirmText = "Sí, cerrar sesión",
            confirmColor = Verde,
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showDeleteDialog) {
        CustomAlertDialog(
            title = "¿Eliminar cuenta?",
            message = "Esta acción es permanente y eliminará todos tus datos tanto locales como en la nube.",
            icon = Icons.Default.DeleteForever,
            confirmText = "Eliminar permanentemente",
            confirmColor = Rojo,
            onConfirm = { showDeleteDialog = false },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun CustomAlertDialog(
    title: String,
    message: String,
    icon: ImageVector,
    confirmText: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = confirmColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    color = TextoMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Cancelar", color = TextoMuted)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(confirmText, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = AzulMedio,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        SectionTitle(title)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, AzulClaro.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Verde,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = FondoGris
            )
        )
    }
}

@Composable
fun GenderButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (selected) (if(MaterialTheme.colorScheme.primary == AzulOscuro) Color.White else AzulMedio) else Color.Transparent,
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (selected) (if(MaterialTheme.colorScheme.primary == AzulOscuro) AzulMedio else Color.White) else TextoMuted)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (selected) (if(MaterialTheme.colorScheme.primary == AzulOscuro) AzulOscuro else Color.White) else TextoMuted)
        }
    }
}

@Composable
fun VoiceItem(voice: VoiceOption, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Verde else AzulClaro.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AzulOscuro),
                contentAlignment = Alignment.Center
            ) {
                Text(voice.initial, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(voice.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(voice.description, fontSize = 12.sp, color = TextoMuted)
            }
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = AzulMedio,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.padding(4.dp))
            }
        }
    }
}
