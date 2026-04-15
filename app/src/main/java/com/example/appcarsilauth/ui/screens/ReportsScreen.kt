package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.ui.components.*
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: IntranetViewModel,
    onBack: () -> Unit = {}
) {
    val kpis by viewModel.reportKPIs.collectAsState()
    val proformasByState by viewModel.proformasByState.collectAsState()
    val topClients by viewModel.topClients.collectAsState()
    val salesByMonth by viewModel.salesByMonth.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "PE")).apply {
            minimumFractionDigits = 2
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadReportData()
    }

    Scaffold(
        containerColor = CarsilColors.Background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarsilColors.Surface)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 24.dp, bottom = 12.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, CarsilColors.Stroke, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = CarsilColors.Primary, modifier = Modifier.size(18.dp))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "REPORTES ANALÍTICOS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = CarsilColors.TextPrimary,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            "Dashboard Operativo",
                            fontSize = 10.sp,
                            color = CarsilColors.TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = { viewModel.loadReportData() },
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, CarsilColors.Stroke, CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, "Actualizar", tint = CarsilColors.Primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = CarsilColors.Primary,
                    trackColor = CarsilColors.PrimaryLight
                )
            }

            if (isLoading && kpis.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = CarsilColors.Primary, strokeWidth = 2.dp, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Analizando datos...", fontSize = 13.sp, color = CarsilColors.TextMuted, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    // ===== SECCIÓN 1: KPIs RESUMEN =====
                    ReportKPISection(kpis, currencyFormat)

                    Spacer(modifier = Modifier.height(24.dp))

                    // ===== SECCIÓN 2: ESTADO DE PROFORMAS (DOUGHNUT) =====
                    ReportCard(
                        title = "Distribución de Proformas",
                        subtitle = "Por estado actual en sistema",
                        icon = Icons.Default.DonutLarge
                    ) {
                        if (proformasByState.isNotEmpty()) {
                            val chartData = proformasByState.map { 
                                (it["estado"] as? String ?: "—") to ((it["cantidad"] as? Int) ?: 0)
                            }
                            val colors = proformasByState.map { getStateColor(it["estado"] as? String ?: "") }
                            val total = chartData.sumOf { it.second }

                            CarsilDoughnutChart(
                                data = chartData,
                                colors = colors,
                                centerLabel = "Total",
                                centerValue = "$total",
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            EmptyReportState("Sin datos de proformas")
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ===== SECCIÓN 3: ACTIVIDAD MENSUAL (BARS) =====
                    ReportCard(
                        title = "Ventas Mensuales",
                        subtitle = "Ingresos proformados (12 meses)",
                        icon = Icons.Default.StackedBarChart
                    ) {
                        if (salesByMonth.isNotEmpty()) {
                            val values = salesByMonth.map { ((it["total"] as? Double) ?: 0.0).toFloat() }
                            val labels = salesByMonth.map {
                                val mes = it["mes"] as? String ?: ""
                                if (mes.length >= 7) {
                                    val monthNum = mes.substring(5, 7).toIntOrNull() ?: 1
                                    getMonthAbbr(monthNum)
                                } else mes
                            }
                            
                            Column {
                                val totalVentas = values.sum()
                                Text(
                                    "Total: ${currencyFormat.format(totalVentas.toDouble())}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CarsilColors.TextPrimary,
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                CarsilBarChart(
                                    data = values,
                                    labels = labels,
                                    barColor = CarsilColors.Primary,
                                    showYAxis = true
                                )
                            }
                        } else {
                            EmptyReportState("Sin datos de ventas")
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ===== SECCIÓN 4: TOP CLIENTES =====
                    ReportCard(
                        title = "Top 7 Clientes",
                        subtitle = "Por volumen de negocio",
                        icon = Icons.Default.Star
                    ) {
                        if (topClients.isNotEmpty()) {
                            val totalMonto = topClients.sumOf { (it["monto"] as? Number)?.toDouble() ?: 0.0 }
                            val clientColors = listOf(
                                CarsilColors.Primary,
                                CarsilColors.PrimaryDark,
                                CarsilColors.Success,
                                CarsilColors.Warning,
                                CarsilColors.Danger,
                                CarsilColors.TextMuted,
                                Color(0xFF111111)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                topClients.forEachIndexed { index, client ->
                                    val nombre = client["cliente"] as? String ?: "—"
                                    val monto = (client["monto"] as? Number)?.toDouble() ?: 0.0
                                    val pct = if (totalMonto > 0) (monto / totalMonto * 100).toInt() else 0
                                    
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Box(modifier = Modifier.size(6.dp).background(clientColors[index % clientColors.size], CircleShape))
                                                Spacer(Modifier.width(8.dp))
                                                Text(nombre, fontSize = 12.sp, color = CarsilColors.TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            Text(currencyFormat.format(monto), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CarsilColors.TextPrimary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(CarsilColors.Stroke, CircleShape)) {
                                            Box(modifier = Modifier.fillMaxWidth(pct/100f).fillMaxHeight().background(clientColors[index % clientColors.size], CircleShape))
                                        }
                                    }
                                }
                            }
                        } else {
                            EmptyReportState("Sin datos de clientes")
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

// ===== SUBCOMPONENTES =====

@Composable
private fun ReportKPISection(kpis: Map<String, Any>, fmt: NumberFormat) {
    val totalIngresos = (kpis["totalIngresos"] as? Double) ?: 0.0
    val totalProformas = (kpis["totalProformas"] as? Int) ?: 0
    val vendidas = (kpis["proformasVendidas"] as? Int) ?: 0

    // featured card
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CarsilColors.Stroke)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(CarsilColors.PrimaryLight, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Payments, null, tint = CarsilColors.Primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("INGRESOS TOTALES", fontSize = 10.sp, fontWeight = FontWeight.Black, color = CarsilColors.TextMuted, letterSpacing = 0.5.sp)
                        Text(fmt.format(totalIngresos), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = CarsilColors.TextPrimary, letterSpacing = (-1).sp)
                    }
                }
                
                // Badge for vendidas
                Surface(
                    color = CarsilColors.Success.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "$vendidas VENDIDAS",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    MiniKPICard(
        label = "PROFORMAS TOTALES",
        value = "$totalProformas",
        sub = "$vendidas cerradas",
        color = CarsilColors.Primary,
        icon = Icons.Default.Description,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun MiniKPICard(
    label: String,
    value: String,
    sub: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CarsilColors.Stroke)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black, color = CarsilColors.TextMuted)
                Icon(icon, null, tint = color.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = CarsilColors.TextPrimary)
            Text(sub, fontSize = 10.sp, color = CarsilColors.TextMuted, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ReportCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CarsilColors.Stroke)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = CarsilColors.Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CarsilColors.TextPrimary)
                    Text(subtitle, fontSize = 10.sp, color = CarsilColors.TextMuted)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
private fun EmptyReportState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Analytics, null, tint = CarsilColors.Stroke, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text(message, fontSize = 12.sp, color = CarsilColors.TextMuted)
        }
    }
}

private fun getStateColor(estado: String): Color = when (estado.uppercase().trim()) {
    "PENDIENTE" -> CarsilColors.Warning
    "APROBADA" -> CarsilColors.Primary
    "VENDIDA" -> CarsilColors.Success
    "VENCIDA" -> CarsilColors.Danger
    "ANULADA" -> CarsilColors.TextMuted
    "RECHAZADA" -> CarsilColors.PrimaryDark
    else -> CarsilColors.Gray400
}

private fun getMonthAbbr(month: Int): String = when (month) {
    1 -> "Ene"
    2 -> "Feb"
    3 -> "Mar"
    4 -> "Abr"
    5 -> "May"
    6 -> "Jun"
    7 -> "Jul"
    8 -> "Ago"
    9 -> "Sep"
    10 -> "Oct"
    11 -> "Nov"
    12 -> "Dic"
    else -> "?"
}
