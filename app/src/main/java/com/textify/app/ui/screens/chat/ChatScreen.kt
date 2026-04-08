package com.textify.app.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.textify.app.data.local.entity.ConversationEntity
import com.textify.app.domain.model.Message
import com.textify.app.ui.components.MessageBubble
import com.textify.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var textoInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleListening()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                drawerShape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
            ) {
                ChatDrawerContent(
                    conversations = uiState.conversations,
                    currentConversationId = uiState.currentConversation?.id,
                    onSelectConversation = {
                        viewModel.selectConversation(it)
                        scope.launch { drawerState.close() }
                    },
                    onNewConversation = {
                        viewModel.createNewConversation("Textify")
                        scope.launch { drawerState.close() }
                    },
                    onDelete = { viewModel.deleteConversation(it) },
                    onRename = { id, name -> viewModel.renameConversation(id, name) },
                    onTogglePin = { viewModel.togglePinConversation(it) }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = FondoClaro,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Textify",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
                        }
                    },
                    actions = {
                        ConnectivityChip(isOnline = uiState.isOnline)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AzulOscuro
                    )
                )
            },
            bottomBar = {
                Box(modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding())) {
                    ChatBottomBar(
                        isRecording = uiState.isRecording,
                        textoInput = textoInput,
                        onTextoChange = { textoInput = it },
                        onSend = {
                            if (textoInput.isNotBlank()) {
                                viewModel.sendMessage(textoInput)
                                textoInput = ""
                            }
                        },
                        onMicClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                viewModel.toggleListening()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        recordingDuration = uiState.recordingDuration
                    )
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.messages) { mensaje ->
                    MessageBubble(message = mensaje)
                }
            }
        }
    }
}

@Composable
fun ConnectivityChip(isOnline: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isOnline) VerdeClaro else Color(0xFFE9EEF2),
        border = BorderStroke(1.dp, if (isOnline) Verde else FondoGris),
        modifier = Modifier.padding(end = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) Verde else TextoMuted)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOnline) "Online" else "Offline",
                color = if (isOnline) Verde else TextoMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ChatBottomBar(
    isRecording: Boolean,
    textoInput: String,
    onTextoChange: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    recordingDuration: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRecording) {
                IconButton(onClick = onMicClick) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = TextoMuted)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(Rojo.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Rojo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Grabando...", color = Rojo, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(text = recordingDuration, color = TextoMuted, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                FloatingActionButton(
                    onClick = onMicClick,
                    containerColor = Verde,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Terminar")
                }
            } else {
                OutlinedTextField(
                    value = textoInput,
                    onValueChange = onTextoChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje...", fontSize = 14.sp) },
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FondoGris,
                        unfocusedBorderColor = FondoGris,
                        focusedContainerColor = Color(0xFFF0F2F5).copy(alpha = 0.5f),
                        unfocusedContainerColor = Color(0xFFF0F2F5).copy(alpha = 0.5f)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() })
                )
                Spacer(modifier = Modifier.width(12.dp))
                FloatingActionButton(
                    onClick = { if (textoInput.isNotBlank()) onSend() else onMicClick() },
                    containerColor = Verde,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        if (textoInput.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                        contentDescription = "Acción"
                    )
                }
            }
        }
    }
}

@Composable
fun ChatDrawerContent(
    conversations: List<ConversationEntity>,
    currentConversationId: String?,
    onSelectConversation: (ConversationEntity) -> Unit,
    onNewConversation: () -> Unit,
    onDelete: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onTogglePin: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulOscuro)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Text("Textify", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNewConversation() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape, 
                color = VerdeClaro, 
                modifier = Modifier.size(40.dp),
                border = BorderStroke(1.dp, Verde)
            ) {
                Icon(
                    Icons.Default.EditNote, 
                    contentDescription = null, 
                    tint = Verde, 
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Nueva conversación", 
                color = AzulMedio, 
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
        HorizontalDivider(color = FondoGris.copy(alpha = 0.5f))
        
        Text(
            "CONVERSACIONES", 
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            fontSize = 12.sp, 
            fontWeight = FontWeight.Bold,
            color = AzulMedio
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(conversations) { conv ->
                ConversationItem(
                    conversation = conv,
                    isSelected = conv.id == currentConversationId,
                    onClick = { onSelectConversation(conv) },
                    onDelete = { onDelete(conv.id) },
                    onRename = { id, name -> onRename(id, name) },
                    onTogglePin = { onTogglePin(conv.id) }
                )
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: ConversationEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String, String) -> Unit,
    onTogglePin: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) AzulClaro.copy(alpha = 0.1f) else Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { showMenu = true }
                )
            }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.participantName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isSelected) AzulMedio else TextoPrimario
                )
                Text(
                    text = conversation.lastMessage,
                    maxLines = 1,
                    fontSize = 13.sp,
                    color = TextoMuted
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                if (conversation.isPinned) {
                    Icon(
                        Icons.Default.PushPin, 
                        contentDescription = null, 
                        modifier = Modifier.size(14.dp), 
                        tint = AzulMedio
                    )
                }
                Text(
                    text = "Ayer", 
                    fontSize = 11.sp, 
                    color = TextoMuted
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text(if (conversation.isPinned) "Desanclar" else "Anclar") },
                leadingIcon = { Icon(Icons.Default.PushPin, null) },
                onClick = { onTogglePin(); showMenu = false }
            )
            DropdownMenuItem(
                text = { Text("Renombrar") },
                leadingIcon = { Icon(Icons.Default.Edit, null) },
                onClick = { onRename(conversation.id, "Renombrado"); showMenu = false }
            )
            DropdownMenuItem(
                text = { Text("Eliminar", color = Rojo) },
                leadingIcon = { Icon(Icons.Default.Delete, null, tint = Rojo) },
                onClick = { onDelete(); showMenu = false }
            )
        }
    }
}