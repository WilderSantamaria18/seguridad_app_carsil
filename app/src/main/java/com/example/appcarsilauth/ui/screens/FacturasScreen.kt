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
fun FacturasScreen(
    viewModel: IntranetViewModel,
    idUsuario: Int,
    onBack: () -> Unit
) {
    val facturas by viewModel.facturas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var estadoFiltro by remember { mutableStateOf("TODOS") }
    val context = LocalContext.current

    val estadoOpciones = listOf("TODOS", "PENDIENTE", "PAGADA", "ANULADA")

    LaunchedEffect(searchQuery, estadoFiltro) {
        viewModel.loadAllFacturas(searchQuery, estadoFiltro)
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            Column(Modifier.background(Color.White)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Historial de Facturas",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Text(
                                "Comprobantes con código QR",
                                fontSize = 11.sp,
                                color = Color.Black
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = Color(0xFF6366F1),
                    trackColor = Color(0xFFE0E7FF)
                )
            }

            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = {
                    Text(
                        "Buscar por código o cliente...",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF6366F1)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray)
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFFEEEEEE),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            // Filtros de estado en chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                estadoOpciones.forEach { opcion ->
                    val isSelected = estadoFiltro == opcion
                    FilterChip(
                        selected = isSelected,
                        onClick = { estadoFiltro = opcion },
                        label = {
                            Text(
                                opcion,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6366F1),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color.Black
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color(0xFF6366F1) else Color(0xFFE0E0E0),
                            selectedBorderColor = Color(0xFF6366F1)
                        )
                    )
                }
            }

            if (facturas.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Receipt,
                            null,
                            modifier = Modifier.size(72.dp),
                            tint = Color(0xFFE0E0E0)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No se encontraron facturas",
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Text(
                            "Prueba cambiando el filtro o la búsqueda",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Resultados: ${facturas.size} facturas encontradas",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(facturas) { factura ->
                        FacturaItem(
                            factura = factura,
                            onDownloadPdf = { id -> viewModel.descargarPdfFactura(context, id) }
                        )
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun FacturaItem(
    factura: Map<String, Any>,
    onDownloadPdf: (Int) -> Unit
) {
    val idFact = factura["IdFactura"] as? Int ?: 0
    val estado = (factura["Estado"] as? String ?: "PENDIENTE").uppercase()

    val (statusColor, statusBg) = when (estado) {
        "PAGADA" -> Pair(Color(0xFF16A34A), Color(0xFFDCFCE7))
        "PENDIENTE" -> Pair(Color(0xFFD97706), Color(0xFFFEF3C7))
        "ANULADA" -> Pair(Color(0xFFDC2626), Color(0xFFFEE2E2))
        else -> Pair(Color.Gray, Color(0xFFF3F4F6))
    }

    val statusIcon = when (estado) {
        "PAGADA" -> Icons.Default.CheckCircle
        "PENDIENTE" -> Icons.Default.HourglassTop
        "ANULADA" -> Icons.Default.Cancel
        else -> Icons.Default.Info
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: Código + Badge Estado + Icono QR
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFEDE9FE),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            factura["Codigo"] as? String ?: "---",
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            fontSize = 15.sp
                        )
                        // Indicador QR
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.QrCode2,
                                null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                "Incluye QR",
                                fontSize = 9.sp,
                                color = Color(0xFF6366F1),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            statusIcon,
                            null,
                            tint = statusColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            estado,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Cliente
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Business,
                    null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    factura["Cliente"] as? String ?: "Sin cliente",
                    fontSize = 13.sp,
                    color = Color.Black,
                    maxLines = 1,
                    fontWeight = FontWeight.Medium
                )
            }

            // Forma de pago
            val formaPago = factura["FormaPago"] as? String ?: ""
            if (formaPago.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CreditCard,
                        null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        formaPago,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5))
            Spacer(Modifier.height(12.dp))

            // Pie: Fecha + Vencimiento + Total + Botón PDF
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "EMISIÓN",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        factura["FechaEmision"] as? String ?: "---",
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                    val venc = factura["FechaVencimiento"] as? String
                    if (!venc.isNullOrEmpty()) {
                        Text(
                            "Vence: $venc",
                            fontSize = 10.sp,
                            color = if (estado == "PENDIENTE") Color(0xFFD97706) else Color.Gray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        val total = factura["Total"] as? Double ?: 0.0
                        Text(
                            "TOTAL",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "S/ ${"%.2f".format(total)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color(0xFF6366F1)
                        )
                    }

                    // Botón descargar PDF con QR
                    IconButton(
                        onClick = { onDownloadPdf(idFact) },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF6366F1).copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = "Descargar Factura PDF",
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
