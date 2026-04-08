package com.textify.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.textify.app.domain.model.Message
import com.textify.app.ui.theme.*
import com.textify.app.utils.toFormattedTime

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isOwn)
            Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (message.isOwn)
                Alignment.End else Alignment.Start
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
                        if (message.isOwn) BurbujaSordo else BurbujaOyente
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .widthIn(max = 260.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (message.isOwn) AzulOscuro else TextoPrimario,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message.timestamp.toFormattedTime(),
                color = TextoMuted,
                fontSize = 9.sp
            )
        }
    }
}