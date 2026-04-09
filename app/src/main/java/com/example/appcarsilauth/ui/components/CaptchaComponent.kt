package com.example.appcarsilauth.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.ui.components.CarsilColors
import com.example.appcarsilauth.ui.components.CarsilShapes
import kotlin.random.Random

@Composable
fun CaptchaComponent(
    onCaptchaGenerated: (String) -> Unit
) {
    var captchaText by remember { mutableStateOf(generateRandomCode()) }

    LaunchedEffect(captchaText) {
        onCaptchaGenerated(captchaText)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Caja del Captcha Visual
        Box(
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .clip(CarsilShapes.Medium)
                .background(Color.White) // Blanco nuclear para máximo contraste
                .border(1.dp, Color.Black, CarsilShapes.Medium)
                .clickable { captchaText = generateRandomCode() },
            contentAlignment = Alignment.Center
        ) {
            CaptchaCanvas(captchaText)
        }

        Spacer(Modifier.width(  8.dp))

        IconButton(
            onClick = { captchaText = generateRandomCode() },
            modifier = Modifier
                .size(48.dp)
                .clip(CarsilShapes.Medium)
                .background(Color.Black)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refrescar",
                tint = Color.White
            )
        }
    }
}

@Composable
fun CaptchaCanvas(text: String) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Dibujar ruido (líneas aleatorias para confundir bots)
        repeat(12) {
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(Random.nextFloat() * width, Random.nextFloat() * height),
                end = Offset(Random.nextFloat() * width, Random.nextFloat() * height),
                strokeWidth = 2f
            )
        }

        // 2. Dibujar puntos de ruido
        repeat(50) {
            drawCircle(
                color = Color.DarkGray.copy(alpha = 0.2f),
                radius = 2f,
                center = Offset(Random.nextFloat() * width, Random.nextFloat() * height)
            )
        }

        // 3. Dibujar el texto con distorsión leve (vía Native Canvas)
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 70f // Un poco más grande
                isFakeBoldText = true
                isAntiAlias = true
                strokeWidth = 2f
                style = android.graphics.Paint.Style.FILL_AND_STROKE // Relleno y borde para grosor extra
                letterSpacing = 0.1f
            }
            
            // Medir ancho total
            val textWidth = paint.measureText(text)
            var currentX = (width - textWidth) / 2
            
            text.forEach { char ->
                save()
                rotate(Random.nextInt(-10, 10).toFloat(), currentX + 10f, height / 2)
                drawText(char.toString(), currentX, height / 2 + 25f, paint)
                restore()
                // El incremento debe ser proporcional al espacio medido
                currentX += (textWidth / text.length)
            }
        }
    }
}

fun generateRandomCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Omitimos O, 0, I, 1 para evitar confusión
    return (1..6)
        .map { chars[Random.nextInt(chars.length)] }
        .joinToString("")
}
