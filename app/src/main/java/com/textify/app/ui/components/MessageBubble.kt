package com.textify.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.textify.app.domain.model.Message
import com.textify.app.ui.theme.AzulMedio

@Composable
fun MessageBubble(
    message: Message, 
    fontScale: Float = 1.0f,
    onPlayClick: () -> Unit = {}
) {
    val arrangement = if (message.isOwn) Arrangement.End else Arrangement.Start
    val alignment = if (message.isOwn) Alignment.End else Alignment.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayClick() }, // Reproducir al tocar la burbuja
        horizontalArrangement = arrangement
    ) {
        Column(
            horizontalAlignment = alignment
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomEnd = if (message.isOwn) 2.dp else 12.dp,
                            bottomStart = if (message.isOwn) 12.dp else 2.dp
                        )
                    )
                    .background(
                        if (message.isOwn) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .widthIn(max = 280.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.weight(1f, fill = false),
                        color = if (message.isOwn) Color.White else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = (16 * fontScale).sp
                        )
                    )
                    
                    // Botón de Play dentro del mensaje
                    IconButton(
                        onClick = onPlayClick,
                        modifier = Modifier.size(24.dp).padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Reproducir",
                            tint = if (message.isOwn) Color.White.copy(alpha = 0.7f) else AzulMedio,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListeningIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 2.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            DotsAnimation()
        }
    }
}

@Composable
fun DotsAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600
                0.2f at 0
                1f at 200
                0.2f at 400
            }
        ),
        label = "dot1"
    )
    
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600
                0.2f at 100
                1f at 300
                0.2f at 500
            }
        ),
        label = "dot2"
    )
    
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600
                0.2f at 200
                1f at 400
                0.2f at 600
            }
        ),
        label = "dot3"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Dot(dot1Alpha)
        Spacer(Modifier.width(4.dp))
        Dot(dot2Alpha)
        Spacer(Modifier.width(4.dp))
        Dot(dot3Alpha)
    }
}

@Composable
fun Dot(alpha: Float) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
    )
}
