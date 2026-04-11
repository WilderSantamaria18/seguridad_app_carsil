package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
    userName: String,
    roleId: Int,
    viewModel: IntranetViewModel,
    idUsuario: Int,
    onLogout: () -> Unit
) {
    val isAdmin = roleId == 1 || roleId == 3
    val asistenciaMap by viewModel.asistenciaState.collectAsState()
    val allAttendances by viewModel.allAttendances.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    var seconds by remember { mutableStateOf(SimpleDateFormat("ss", Locale.getDefault()).format(Date())) }
    var currentDate by remember { mutableStateOf(SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("es", "ES")).format(Date())) }
    var selectedDateForAdmin by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("TODOS") }

    LaunchedEffect(Unit) {
        if (!isAdmin) {
            viewModel.cargarAsistenciaHoy(idUsuario)
        }
        
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            seconds = SimpleDateFormat("ss", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    LaunchedEffect(isAdmin, selectedDateForAdmin, searchQuery, selectedStatusFilter) {
        if (isAdmin) {
            viewModel.loadAllAttendances(
                fecha = selectedDateForAdmin,
                search = searchQuery,
                estadoFiltro = selectedStatusFilter
            )
        }
    }

    Scaffold(
        containerColor = CarsilColors.Background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isAdmin) "Control de Personal" else "Mi Asistencia", 
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = {
                            viewModel.loadAllAttendances(
                                fecha = selectedDateForAdmin,
                                search = searchQuery,
                                estadoFiltro = selectedStatusFilter
                            )
                        }) {
                            Icon(Icons.Default.Refresh, null)
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = CarsilColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = CarsilColors.TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CarsilColors.Primary)
            }

            if (!isAdmin) {
                // VISTA EMPLEADO
                EmployeeAttendanceView(
                    userName = userName,
                    currentDate = currentDate,
                    currentTime = currentTime,
                    seconds = seconds,
                    asistenciaMap = asistenciaMap,
                    onMark = { viewModel.registrarAsistencia(idUsuario) }
                )
            } else {
                // VISTA ADMINISTRADOR
                AdminAttendanceView(
                    attendances = allAttendances,
                    selectedDate = selectedDateForAdmin,
                    searchQuery = searchQuery,
                    selectedFilter = selectedStatusFilter,
                    onDateChange = { 
                        selectedDateForAdmin = it
                    },
                    onSearchQueryChange = { searchQuery = it },
                    onFilterChange = { selectedStatusFilter = it }
                )
            }
        }
    }
}

@Composable
fun EmployeeAttendanceView(
    userName: String,
    currentDate: String,
    currentTime: String,
    seconds: String,
    asistenciaMap: Map<String, Any?>?,
    onMark: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // SALUDO PERSONALIZADO
        Text(
            "¡Hola, $userName!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = CarsilColors.TextPrimary
        )
        Text(
            "Registra tu jornada de hoy",
            fontSize = 14.sp,
            color = CarsilColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // RELOJ DIGITAL
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

        val horaEntrada = asistenciaMap?.get("HoraEntrada") as? String
        val horaSalida = asistenciaMap?.get("HoraSalida") as? String

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CarsilShapes.Medium,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CarsilColors.Stroke)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when {
                        asistenciaMap == null -> "SIN REGISTRO"
                        horaSalida == null -> "EN JORNADA"
                        else -> "JORNADA FINALIZADA"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (asistenciaMap != null) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        TimeRecord("Entrada", horaEntrada ?: "--:--", Icons.Default.WatchLater)
                        TimeRecord("Salida", horaSalida ?: "--:--", Icons.AutoMirrored.Filled.Logout)
                    }
                } else {
                    Text(
                        "Aún no has registrado tu ingreso el día de hoy.",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = CarsilColors.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (horaSalida == null) {
                    Button(
                        onClick = onMark,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = CarsilShapes.Small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (asistenciaMap == null) CarsilColors.Primary else CarsilColors.Danger
                        )
                    ) {
                        Icon(if (asistenciaMap == null) Icons.Default.WatchLater else Icons.AutoMirrored.Filled.Logout, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (asistenciaMap == null) "Marcar Ingreso" else "Marcar Salida", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = CarsilShapes.Small,
                        color = CarsilColors.SuccessLight
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Asistencia completada correctamente", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAttendanceView(
    attendances: List<Map<String, Any>>,
    selectedDate: String,
    searchQuery: String,
    selectedFilter: String,
    onDateChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (String) -> Unit
) {
    val filterOptions = listOf("TODOS", "EN JORNADA", "COMPLETADA", "SIN REGISTRO")
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // DatePicker entrega medianoche en UTC; usar UTC evita desfase de un dia.
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atOffset(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                        onDateChange(date.toString())
                    }
                    showDatePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Visualizando asistencia del día:", fontSize = 12.sp, color = CarsilColors.TextSecondary)
        
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = selectedDate,
            onValueChange = { },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            readOnly = true,
            label = { Text("Fecha seleccionada", fontWeight = FontWeight.Bold) },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Elegir fecha", tint = CarsilColors.Primary)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CarsilColors.Primary,
                unfocusedBorderColor = CarsilColors.Stroke,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Buscar empleado") },
            placeholder = {
                Text(
                    "Nombre, cargo o correo",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { option ->
                FilterChip(
                    selected = selectedFilter == option,
                    onClick = { onFilterChange(option) },
                    label = { Text(option, fontSize = 12.sp) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (attendances.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay coincidencias para los filtros aplicados", color = CarsilColors.TextMuted)
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(attendances) { record ->
                    AttendanceItem(record)
                }
            }
        }
    }
}

@Composable
fun AttendanceItem(record: Map<String, Any>) {
    val estado = (record["Estado"] as? String ?: "SIN REGISTRO")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CarsilShapes.Small,
        color = Color.White,
        border = BorderStroke(1.dp, CarsilColors.Stroke)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(CarsilColors.PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    (record["Empleado"] as? String)?.take(1) ?: "?",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(record["Empleado"] as? String ?: "N/A", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(record["Cargo"] as? String ?: "Colaborador", fontSize = 11.sp, color = Color.Black)
                val correo = record["Correo"] as? String ?: ""
                if (correo.isNotBlank()) {
                    Text(correo, fontSize = 10.sp, color = CarsilColors.TextMuted)
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(estado, fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WatchLater, null, modifier = Modifier.size(10.dp), tint = CarsilColors.Success)
                    Spacer(Modifier.width(4.dp))
                    Text(record["HoraEntrada"] as? String ?: "--", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(10.dp), tint = CarsilColors.Danger)
                    Spacer(Modifier.width(4.dp))
                    Text(record["HoraSalida"] as? String ?: "--", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
