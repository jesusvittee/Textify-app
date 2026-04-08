package com.textify.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.textify.app.domain.model.Phrase
import com.textify.app.ui.theme.*

@Composable
fun PhraseCard(
    phrase: Phrase,
    isPlaying: Boolean = false,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = phrase.text,
            color = if (isPlaying) AzulMedio else TextoPrimario,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            lineHeight = 20.sp
        )
        IconButton(onClick = onPlay) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Reproducir",
                tint = if (isPlaying) Verde else AzulOscuro
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Eliminar",
                tint = TextoMuted
            )
        }
    }
}