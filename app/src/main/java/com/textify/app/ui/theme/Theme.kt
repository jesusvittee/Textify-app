package com.textify.app.ui.theme

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat

private val EsquemaClaro = lightColorScheme(
    primary = AzulOscuro,
    onPrimary = Color.White,
    secondary = AzulMedio,
    onSecondary = Color.White,
    tertiary = Verde,
    background = FondoClaro,
    surface = Color.White,
    onBackground = TextoPrimario,
    onSurface = TextoPrimario
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulClaro,
    onPrimary = FondoOscuro,
    secondary = AzulMedio,
    onSecondary = FondoOscuro,
    tertiary = Verde,
    background = FondoOscuro,
    surface = SuperficieOscura,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun TextifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val esquemaColores = if (darkTheme) EsquemaOscuro else EsquemaClaro

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var context = view.context
            while (context is ContextWrapper) {
                if (context is Activity) break
                context = context.baseContext
            }
            val window = (context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = esquemaColores.primary.toArgb()
                WindowCompat.getInsetsController(window, view)
                    .isAppearanceLightStatusBars = false
            }
        }
    }

    val currentDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density = currentDensity.density, fontScale = fontScale)
    ) {
        MaterialTheme(
            colorScheme = esquemaColores,
            typography = Typography,
            content = content
        )
    }
}
