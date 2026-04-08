package com.textify.app.ui.theme

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EsquemaClaro = lightColorScheme(
    primary = AzulOscuro,
    onPrimary = FondoClaro,
    secondary = AzulMedio,
    onSecondary = FondoClaro,
    tertiary = Verde,
    background = FondoClaro,
    surface = FondoGris,
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
    onBackground = FondoClaro,
    onSurface = FondoClaro
)

@Composable
fun TextifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
                    .isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = esquemaColores,
        typography = Typography,
        content = content
    )
}
