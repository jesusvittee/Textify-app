package com.textify.app.ui.screens.phrases

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.textify.app.ui.components.PhraseCard
import com.textify.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhrasesScreen(
    navController: NavController,
    viewModel: PhrasesViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var nuevaFrase by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AnimatedContent(
                targetState = uiState.isSelectionMode,
                transitionSpec = {
                    slideInVertically { -it } togetherWith slideOutVertically { -it }
                },
                label = "topBarTransition"
            ) { isSelectionMode ->
                if (isSelectionMode) {
                    TopAppBar(
                        title = {
                            Text(
                                text = "${uiState.selectedPhraseIds.size} seleccionadas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color.White)
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.deleteSelectedPhrases() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar seleccionadas", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                } else {
                    TopAppBar(
                        title = {
                            BasicTextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp)
                                    .height(40.dp)
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                singleLine = true,
                                cursorBrush = SolidColor(Color.White),
                                decorationBox = { innerTextField ->
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (searchText.isEmpty()) {
                                                Text(
                                                    text = "Buscar frase...",
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                }
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !uiState.isSelectionMode,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                Box(modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding())) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = nuevaFrase,
                                onValueChange = { nuevaFrase = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { 
                                    Text(
                                        "Escribe una nueva frase...", 
                                        style = MaterialTheme.typography.bodyMedium
                                    ) 
                                },
                                shape = RoundedCornerShape(25.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            FloatingActionButton(
                                onClick = {
                                    if (nuevaFrase.isNotBlank()) {
                                        viewModel.addPhrase(nuevaFrase)
                                        nuevaFrase = ""
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Agregar")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val filteredPhrases = uiState.phrases.filter { 
                it.text.contains(searchText, ignoreCase = true) 
            }
            items(filteredPhrases, key = { it.id }) { phrase ->
                PhraseCard(
                    phrase = phrase,
                    isPlaying = uiState.playingPhraseId == phrase.id,
                    isSelected = uiState.selectedPhraseIds.contains(phrase.id),
                    isSelectionMode = uiState.isSelectionMode,
                    onPlay = { viewModel.setPlaying(phrase.id) },
                    onDelete = { viewModel.deletePhrase(phrase.id) },
                    onToggleSelection = { viewModel.toggleSelection(phrase.id) }
                )
            }
        }
    }
}
