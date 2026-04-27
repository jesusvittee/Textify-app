package com.textify.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.textify.app.R
import com.textify.app.ui.theme.LogoBlue
import com.textify.app.ui.theme.TextifyTheme

@Composable
fun TextifyLogo(
    modifier: Modifier = Modifier,
    size: Dp = 200.dp
) {
    val logoDescription = stringResource(id = R.string.textify_logo_description)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(LogoBlue)
            .semantics { contentDescription = logoDescription },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "t",
            color = Color.White,
            fontSize = (size.value * 0.65).sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TextifyLogoPreview() {
    TextifyTheme {
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            TextifyLogo()
        }
    }
}
