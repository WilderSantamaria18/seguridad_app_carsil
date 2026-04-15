package com.example.appcarsilauth.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores del Sistema según el diseño SGE CARSIL (Flat & Minimalist)
object CarsilColors {
    val Primary = Color(0xFF3B49DF) // Azul corporativo
    val PrimaryDark = Color(0xFF2D38B0)
    val PrimaryLight = Color(0xFFEEF0FD)
    val Background = Color(0xFFF7F7F8) // Fondo general
    val Surface = Color(0xFFFFFFFF) // Blanco sólido
    val TextPrimary = Color(0xFF000000) // Negro principal
    val TextSecondary = Color(0xFF111111) // Negro secundario
    val Stroke = Color(0xFFE5E5E5) // Borde fino
    val Gray400 = Color(0xFF4B5563)
    val TextMuted = Color(0xFF1F2937)
    val Success = Color(0xFF10B981)
    val SuccessLight = Color(0xFFD1FAE5) // Fondo éxito
    val Warning = Color(0xFFF59E0B)
    val Danger = Color(0xFFEF4444)
    val DangerLight = Color(0xFFFEE2E2) // Fondo error
}

object CarsilShapes {
    val Full = RoundedCornerShape(100.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Small = RoundedCornerShape(8.dp) // Según guía de diseño
}