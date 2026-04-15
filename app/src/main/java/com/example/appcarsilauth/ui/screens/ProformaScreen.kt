package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.ui.components.CarsilColors
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProformaScreen(
    viewModel: IntranetViewModel,
    idUsuario: Int,
    onBack: () -> Unit
) {
    val proformas by viewModel.proformas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(searchQuery) {
        viewModel.loadAllProformas(searchQuery)
    }

    Scaffold(
        containerColor = CarsilColors.Background,
        topBar = {
            Column(Modifier.background(CarsilColors.Surface)) {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                "Historial de Proformas",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = CarsilColors.TextPrimary
                            )
                            Text(
                                "Consulta y descarga de documentos",
                                fontSize = 11.sp,
                                color = CarsilColors.TextMuted
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = CarsilColors.TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CarsilColors.Surface)
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = CarsilColors.Primary,
                    trackColor = CarsilColors.PrimaryLight
                )
            }

            // Barra de búsqueda premium
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = {
                    Text(
                        "Buscar por código o cliente...",
                        fontSize = 14.sp,
                        color = CarsilColors.TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = CarsilColors.Primary) },
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CarsilColors.TextPrimary,
                    unfocusedTextColor = CarsilColors.TextPrimary,
                    focusedBorderColor = CarsilColors.Primary,
                    unfocusedBorderColor = CarsilColors.Stroke,
                    unfocusedContainerColor = CarsilColors.Surface,
                    focusedContainerColor = CarsilColors.Surface
                )
            )

            if (proformas.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = CarsilColors.Stroke)
                        Spacer(Modifier.height(16.dp))
                        Text("No se encontraron proformas", color = CarsilColors.TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { 
                        Text(
                            "Resultados: ${proformas.size} documentos", 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold,
                            color = CarsilColors.TextMuted,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) 
                    }
                    
                    items(proformas) { proforma ->
                        ProformaItem(
                            proforma = proforma,
                            onDownload = { id -> viewModel.descargarPdfProforma(context, id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProformaItem(
    proforma: Map<String, Any>,
    onDownload: (Int) -> Unit
) {
    val idProf = proforma["IdProforma"] as? Int ?: 0
    val estado = (proforma["Estado"] as? String ?: "PENDIENTE").uppercase()
    
    val (statusColor, statusBg) = when(estado) {
        "VENDIDA", "APROBADA" -> Pair(Color(0xFF166534), Color(0xFFDCFCE7))
        "PENDIENTE" -> Pair(Color(0xFF92400E), Color(0xFFFEF3C7))
        "ANULADA", "RECHAZADA" -> Pair(Color(0xFF991B1B), Color(0xFFFEE2E2))
        else -> Pair(CarsilColors.TextMuted, Color(0xFFF3F4F6))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CarsilColors.Surface,
        border = BorderStroke(1.dp, CarsilColors.Stroke),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    proforma["Codigo"] as? String ?: "---", 
                    fontWeight = FontWeight.Black, 
                    color = Color.Black,
                    fontSize = 15.sp
                )
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        estado, 
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), 
                        fontSize = 9.sp, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = statusColor
                    )
                }
            }
            
            Spacer(Modifier.height(4.dp))
            Text(
                proforma["Cliente"] as? String ?: "Sin cliente", 
                fontSize = 13.sp, 
                color = Color.Black,
                maxLines = 1
            )
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = CarsilColors.Stroke)
            Spacer(Modifier.height(12.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("FECHA EMISIÓN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(proforma["Fecha"] as? String ?: "---", fontSize = 12.sp, color = Color.Black)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 12.dp)) {
                        val total = proforma["Total"] as? Double ?: 0.0
                        Text("TOTAL DOCUMENTO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("S/ ${"%.2f".format(total)}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
                    }
                    
                    IconButton(
                        onClick = { onDownload(idProf) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(CarsilColors.Primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf, 
                            contentDescription = "Descargar PDF",
                            tint = CarsilColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
