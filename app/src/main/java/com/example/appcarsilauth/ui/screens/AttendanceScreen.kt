package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    
    // Reloj Digital
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())) }
    var currentDate by remember { mutableStateOf(SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("es", "ES")).format(Date())) }

    LaunchedEffect(Unit) {
        viewModel.cargarAsistenciaHoy(idUsuario)
        while (true) {
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portal de Asistencia") },
                navigationIcon = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ArrowBack, "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F2027),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF203A43)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(currentDate.capitalize(Locale.getDefault()), color = Color.White.copy(alpha = 0.8f), fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Sesión: $email", color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(48.dp))

            // Lógica de marcado
            val asistencia = asistenciaState
            
            if (asistencia == null) {
                Button(
                    onClick = { viewModel.registrarAsistencia(idUsuario) },
                    modifier = Modifier.size(200.dp).shadow(12.dp, RoundedCornerShape(100.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text("MARCAR\nINGRESO", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            } else if (asistencia.HoraSalida == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ingreso registrado a las: ${asistencia.HoraEntrada}", color = Color.White)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.registrarAsistencia(idUsuario) },
                        modifier = Modifier.size(200.dp).shadow(12.dp, RoundedCornerShape(100.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text("MARCAR\nSALIDA", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).padding(32.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, "Completado", tint = Color(0xFF69F0AE), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Jornada Completada", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Entrada: ${asistencia.HoraEntrada}", color = Color.White.copy(alpha = 0.8f))
                    Text("Salida: ${asistencia.HoraSalida}", color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}
