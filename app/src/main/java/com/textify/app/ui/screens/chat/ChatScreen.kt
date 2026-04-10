package com.textify.app.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.textify.app.domain.model.MessageType
import com.textify.app.ui.components.ListeningIndicator
import com.textify.app.ui.components.MessageBubble
import com.textify.app.ui.screens.profile.ProfileViewModel
import com.textify.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel,
    profileViewModel: ProfileViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    var textoInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showDeleteErrorDialog by remember { mutableStateOf(false) }

    // MOSTRAR ERROR SI OCURRE
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(uiState.transcribedText) {
        if (uiState.isListening && uiState.transcribedText.isNotEmpty()) {
            textoInput = uiState.transcribedText
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.isRecording) {
        if (uiState.messages.isNotEmpty() || uiState.isRecording) {
            val lastIndex = if (uiState.isRecording) uiState.messages.size else uiState.messages.size - 1
            if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.startListening()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxHeight().width(300.dp)
            ) {
                ChatDrawerContent(
                    conversations = uiState.conversations,
                    currentConversationId = uiState.currentConversation?.id,
                    onSelectConversation = {
                        viewModel.selectConversation(it.id)
                        scope.launch { drawerState.close() }
                    },
                    onNewConversation = {
                        viewModel.createNewConversation("Textify")
                        scope.launch { drawerState.close() }
                    },
                    onDelete = { id ->
                        if (id == uiState.currentConversation?.id) showDeleteErrorDialog = true
                        else viewModel.deleteConversation(id)
                    },
                    onRename = { id, name -> viewModel.renameConversation(id, name) },
                    onTogglePin = { viewModel.togglePinConversation(it) }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = { Text("Textify", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro)
                )
            },
            bottomBar = {
                Box(modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding())) {
                    ChatBottomBar(
                        isListening = uiState.isListening,
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
                            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) viewModel.startListening() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        onConfirmAudio = {
                            viewModel.stopAndSend()
                            textoInput = ""
                        },
                        onCancelAudio = {
                            viewModel.cancelListening()
                            textoInput = ""
                        }
                    )
                }
            }
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Bottom,
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(uiState.messages) { mensaje ->
                    MessageBubble(
                        message = mensaje, 
                        fontScale = profileState.fontScale,
                        onPlayClick = { viewModel.playMessage(mensaje.text) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (uiState.isRecording) {
                    item {
                        ListeningIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    if (showDeleteErrorDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteErrorDialog = false },
            confirmButton = {
                TextButton(onClick = { showDeleteErrorDialog = false }) {
                    Text("Entendido", color = AzulMedio)
                }
            },
            title = { Text("Acción no permitida") },
            text = { Text("No puedes eliminar la conversación actual.") },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun ChatBottomBar(
    isListening: Boolean,
    isRecording: Boolean,
    textoInput: String,
    onTextoChange: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    onConfirmAudio: () -> Unit,
    onCancelAudio: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isListening) {
                IconButton(onClick = onCancelAudio) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    modifier = Modifier.weight(1f).height(48.dp)
                        .background(if (isRecording) Rojo.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.GraphicEq else Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = if (isRecording) Rojo else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRecording) "Grabando..." else "Iniciando...",
                        color = if (isRecording) Rojo else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                FloatingActionButton(
                    onClick = onConfirmAudio,
                    containerColor = Verde,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Enviar audio")
                }
            } else {
                OutlinedTextField(
                    value = textoInput,
                    onValueChange = onTextoChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje...", fontSize = 14.sp) },
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                    Icon(if (textoInput.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic, contentDescription = "Acción")
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
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Box(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).statusBarsPadding().padding(16.dp)
        ) {
            Text("Textify", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Row(
            modifier = Modifier.fillMaxWidth().clickable { onNewConversation() }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Nueva conversación", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
        
        Text(
            text = "CONVERSACIONES", 
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp),
            fontSize = 12.sp, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
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
    onTogglePin: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth().background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() }, onLongPress = { showMenu = true })
            }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = conversation.participantName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                Text(text = conversation.lastMessage, maxLines = 1, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                if (conversation.isPinned) {
                    Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                }
                Text(text = "Ayer", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text(if (conversation.isPinned) "Desanclar" else "Anclar") },
                leadingIcon = { Icon(Icons.Default.PushPin, null) },
                onClick = { onTogglePin(conversation.id); showMenu = false }
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
