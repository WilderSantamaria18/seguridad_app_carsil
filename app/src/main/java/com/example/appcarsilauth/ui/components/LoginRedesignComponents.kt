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
    val Primary = Color(0xFF3B49DF)
    val Accent = Color(0xFF7C3AED)
    val Navy = Color(0xFF0F172A)
    val PanelBg = Color(0xFF1E2A4A)
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Gray400 = Color(0xFF94A3B8)
    val AIBadgeText = Color(0xFFC4B5FD)
}

@Composable
fun AIBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CarsilColors.Accent.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = CarsilColors.AIBadgeText,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Potenciado con Inteligencia Artificial",
            color = CarsilColors.AIBadgeText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MiniKPICard(label: String, value: String, delta: String, isUp: Boolean) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp)
    ) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            text = if (isUp) "▲ $delta" else "▼ $delta",
            color = if (isUp) CarsilColors.Success else Color.Red.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LineChartSimulated() {
    Canvas(modifier = Modifier.fillMaxWidth().height(60.dp)) {
        val path = Path().apply {
            moveTo(0f, size.height * 0.8f)
            quadraticBezierTo(size.width * 0.2f, size.height * 0.6f, size.width * 0.4f, size.height * 0.7f)
            quadraticBezierTo(size.width * 0.6f, size.height * 0.8f, size.width * 0.8f, size.height * 0.3f)
            lineTo(size.width, size.height * 0.4f)
        }
        drawPath(
            path = path,
            color = CarsilColors.Accent,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

@Composable
fun DonutChartSimulated() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
        Canvas(modifier = Modifier.size(50.dp)) {
            drawArc(
                color = CarsilColors.Accent,
                startAngle = -90f,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx())
            )
            drawArc(
                color = CarsilColors.Success,
                startAngle = 150f,
                sweepAngle = 100f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx())
            )
        }
        Text("Ventas", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MiniDashboardPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniKPICard("Clientes", "1,284", "12%", true)
            MiniKPICard("Ventas", "$48.2K", "8%", true)
            MiniKPICard("Proyectos", "38", "2", false)
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .padding(12.dp)
        ) {
            Text("INGRESOS MENSUALES", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LineChartSimulated()
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DonutChartSimulated()
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(CarsilColors.Accent))
                    Spacer(Modifier.width(4.dp))
                    Text("Equipos", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(CarsilColors.Success))
                    Spacer(Modifier.width(4.dp))
                    Text("Servicios", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                }
            }
        }
    }
}
