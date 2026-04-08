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

// Colores del Sistema según el diseño HTML
object CarsilColors {
    val Primary = Color(0xFF000000) // Black
    val Background = Color(0xFFF8F9FA) // Light Gray Bg
    val Surface = Color(0xFFFFFFFF) // White
    val TextPrimary = Color(0xFF000000)
    val TextSecondary = Color(0xFF6C757D)
    val Stroke = Color(0xFFE9ECEF)
    val Accent = Color(0xFF000000)
    val Navy = Color(0xFF000000)
    val Gray400 = Color(0xFFADB5BD)
    val Success = Color(0xFF198754)
    val Warning = Color(0xFFFDC105)
}

object CarsilShapes {
    val Full = RoundedCornerShape(100.dp)
    val Medium = RoundedCornerShape(16.dp)
    val Small = RoundedCornerShape(12.dp)
}