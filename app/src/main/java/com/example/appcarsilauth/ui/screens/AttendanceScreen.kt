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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    email: String,
    viewModel: IntranetViewModel,
    idUsuario: Int,
    onLogout: () -> Unit
) {
    val asistenciaState by viewModel.asistenciaState.collectAsState()
    
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
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar(
                title = { Text("Asistencia CARSIL", fontWeight = FontWeight.Black) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // RELOJ DIGITAL PREMIUM
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 32.dp)) {
                Text(currentDate.replaceFirstChar { it.uppercase() }, color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = currentTime,
                        color = Color.Black,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-2).sp
                    )
                    Text(
                        text = seconds,
                        color = Color.Gray,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
                    )
                }
                Surface(color = Color(0xFFF1F3F4), shape = RoundedCornerShape(12.dp)) {
                    Text(email, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 12.sp, color = Color.Gray)
                }
            }

            // BOTON DE MARCACION
            val asistencia = asistenciaState
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                if (asistencia == null) {
                    Button(
                        onClick = { viewModel.registrarAsistencia(idUsuario) },
                        modifier = Modifier
                            .size(220.dp)
                            .shadow(20.dp, CircleShape),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = CircleShape
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.WatchLater, null, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("MARCAR\nINGRESO", textAlign = TextAlign.Center, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                    }
                } else if (asistencia.HoraSalida == null) {
                    Button(
                        onClick = { viewModel.registrarAsistencia(idUsuario) },
                        modifier = Modifier
                            .size(220.dp)
                            .shadow(20.dp, CircleShape),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = CircleShape
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("MARCAR\nSALIDA", textAlign = TextAlign.Center, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE8F5E9))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00C853), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Jornada Finalizada", fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Ingreso: ${asistencia.HoraEntrada}", color = Color.Gray)
                            Text("Salida: ${asistencia.HoraSalida}", color = Color.Gray)
                        }
                    }
                }
            }

            // FOOTER INFO
            Text(
                "Ubicacion verificada vía GPS para registro de asistencia.",
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
