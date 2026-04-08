package com.textify.app.ui.screens.phrases

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
        containerColor = FondoClaro,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                            .height(44.dp),
                        placeholder = { Text("Buscar frase...", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulOscuro
                )
            )
        },
        bottomBar = {
            // Aplicamos el padding para que no quede detrás de la barra de navegación
            Box(modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding())) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
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
                            placeholder = { Text("Escribe una nueva frase...", fontSize = 14.sp) },
                            shape = RoundedCornerShape(25.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FondoGris,
                                unfocusedBorderColor = FondoGris,
                                focusedContainerColor = Color(0xFFF0F2F5).copy(alpha = 0.5f),
                                unfocusedContainerColor = Color(0xFFF0F2F5).copy(alpha = 0.5f)
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        FloatingActionButton(
                            onClick = {
                                if (nuevaFrase.isNotBlank()) {
                                    viewModel.addPhrase(nuevaFrase)
                                    nuevaFrase = ""
                                }
                            },
                            containerColor = Verde,
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
            items(filteredPhrases) { phrase ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, AzulMedio.copy(alpha = 0.1f))
                ) {
                    PhraseCard(
                        phrase = phrase,
                        isPlaying = uiState.playingPhraseId == phrase.id,
                        onPlay = { viewModel.setPlaying(phrase.id) },
                        onDelete = { viewModel.deletePhrase(phrase.id) }
                    )
                }
            }
        }
    }
}