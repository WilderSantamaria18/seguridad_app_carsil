package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.appcarsilauth.ui.components.CarsilColors
import com.example.appcarsilauth.ui.components.CarsilShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    email: String,
    viewModel: IntranetViewModel,
    idUsuario: Int,
    onLogout: () -> Unit
) {
    val asistenciaMap by viewModel.asistenciaState.collectAsState()
    
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    var seconds by remember { mutableStateOf(SimpleDateFormat("ss", Locale.getDefault()).format(Date())) }
    var currentDate by remember { mutableStateOf(SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("es", "ES")).format(Date())) }

    LaunchedEffect(Unit) {
        viewModel.cargarAsistenciaHoy(idUsuario)
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            seconds = SimpleDateFormat("ss", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    Scaffold(
        containerColor = CarsilColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Asistencia", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CarsilColors.Background,
                    titleContentColor = CarsilColors.TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // RELOJ DIGITAL MINIMALISTA
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentDate.replaceFirstChar { it.uppercase() },
                    color = CarsilColors.TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = currentTime,
                        color = CarsilColors.TextPrimary,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-2).sp
                    )
                    Text(
                        text = ":$seconds",
                        color = CarsilColors.Gray400,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(bottom = 14.dp, start = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ESTADO DE ASISTENCIA
            val asistencia = asistenciaMap
            val horaEntrada = asistencia?.get("HoraEntrada") as? String
            val horaSalida = asistencia?.get("HoraSalida") as? String

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CarsilShapes.Medium,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CarsilColors.Stroke)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when {
                            asistencia == null -> "SIN REGISTRO"
                            horaSalida == null -> "EN JORNADA"
                            else -> "JORNADA FINALIZADA"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (asistencia != null && horaSalida == null) CarsilColors.Success else CarsilColors.TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (asistencia != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            TimeRecord("Entrada", horaEntrada ?: "--:--", Icons.Default.WatchLater)
                            TimeRecord("Salida", horaSalida ?: "--:--", Icons.AutoMirrored.Filled.Logout)
                        }
                    } else {
                        Text(
                            "Aún no has registrado tu ingreso el día de hoy.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = CarsilColors.TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (horaSalida == null) {
                        Button(
                            onClick = { viewModel.registrarAsistencia(idUsuario) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = CarsilShapes.Small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (asistencia == null) Color.Black else Color(0xFFD32F2F)
                            )
                        ) {
                            Icon(
                                if (asistencia == null) Icons.Default.WatchLater else Icons.AutoMirrored.Filled.Logout, 
                                null, 
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (asistencia == null) "Marcar Ingreso" else "Marcar Salida",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = CarsilShapes.Small,
                            color = Color(0xFFF1F3F4)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, null, tint = CarsilColors.Success, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Registro completado", color = CarsilColors.Success, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // INFO DE SEGURIDAD
            Surface(
                color = Color(0xFFEEEEEE),
                shape = CarsilShapes.Small,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = CarsilColors.Success, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ubicación verificada vía GPS", fontSize = 11.sp, color = CarsilColors.TextSecondary)
                }
            }
        }
    }
}

@Composable
fun TimeRecord(label: String, time: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = CarsilColors.Gray400)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = CarsilColors.TextSecondary)
        Text(time, fontSize = 18.sp, fontWeight = FontWeight.Black, color = CarsilColors.TextPrimary)
    }
}
