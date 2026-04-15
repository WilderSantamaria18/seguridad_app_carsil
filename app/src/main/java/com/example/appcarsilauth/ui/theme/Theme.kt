package com.example.appcarsilauth.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.appcarsilauth.ui.components.CarsilColors

private val DarkColorScheme = darkColorScheme(
    primary = CarsilColors.Primary,
    onPrimary = CarsilColors.TextPrimary,
    secondary = CarsilColors.PrimaryDark,
    onSecondary = CarsilColors.TextPrimary,
    background = CarsilColors.Background,
    onBackground = CarsilColors.TextPrimary,
    surface = CarsilColors.Surface,
    onSurface = CarsilColors.TextPrimary,
    primaryContainer = CarsilColors.PrimaryLight,
    onPrimaryContainer = CarsilColors.TextPrimary,
    error = CarsilColors.Danger,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CarsilColors.Primary,
    onPrimary = CarsilColors.TextPrimary,
    secondary = CarsilColors.PrimaryDark,
    onSecondary = CarsilColors.TextPrimary,
    background = CarsilColors.Background,
    onBackground = CarsilColors.TextPrimary,
    surface = CarsilColors.Surface,
    onSurface = CarsilColors.TextPrimary,
    primaryContainer = CarsilColors.PrimaryLight,
    onPrimaryContainer = CarsilColors.TextPrimary,
    error = CarsilColors.Danger,
    onError = Color.White
)

@Composable
fun AppCarsilAuthTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}