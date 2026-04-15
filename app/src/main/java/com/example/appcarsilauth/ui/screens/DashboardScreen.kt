package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.ui.components.CarsilColors
import com.example.appcarsilauth.ui.components.CarsilShapes
import com.example.appcarsilauth.ui.components.StatCard
import java.math.BigDecimal

@Composable
fun DashboardScreen(
    email: String = "admin@carsil.com",
    userName: String = "Administrador CARSIL",
    roleId: Int = 1,
    allowedMenus: List<String> = emptyList(),
    stats: Map<String, Int> = emptyMap(),
    recentProformas: List<Map<String, Any>> = emptyList(),
    onGoToClients: () -> Unit = {},
    onGoToProducts: () -> Unit = {},
    onGoToProforma: () -> Unit = {},
    onGoToProfile: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onRefresh: () -> Unit = {},
    isLoading: Boolean = false,
    verifiedTotalIncome: BigDecimal = BigDecimal("23824.20")
) {
    val canUseClients = if (allowedMenus.isEmpty()) roleId != 2 else allowedMenus.contains("Clientes")
    val canUseProducts = if (allowedMenus.isEmpty()) roleId != 2 else allowedMenus.contains("Productos")
    val canUseProformas = if (allowedMenus.isEmpty()) roleId != 2 else allowedMenus.contains("Proformas")

    val roleName = when (roleId) {
        1 -> "Administrador"
        2 -> "Empleado"
        3 -> "Supervisor"
        4 -> "Vendedor"
        else -> "Usuario"
    }

    Scaffold(
        containerColor = CarsilColors.Background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 28.dp, bottom = 12.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, CarsilColors.Stroke, CircleShape)
                    ) {
                        Icon(Icons.Default.Menu, "Menú", tint = CarsilColors.Primary, modifier = Modifier.size(18.dp))
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "CARSIL SGE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CarsilColors.TextPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Panel Principal",
                            fontSize = 11.sp,
                            color = CarsilColors.TextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.size(36.dp),
                            enabled = !isLoading
                        ) {
                            Icon(
                                Icons.Default.Refresh, 
                                "Actualizar", 
                                tint = CarsilColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CarsilColors.Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                userName.take(1).uppercase(),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // INDICADOR DE CARGA
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = CarsilColors.Primary,
                    trackColor = CarsilColors.PrimaryLight
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // WELCOME HEADER
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(
                        "¡Hola, $userName!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = CarsilColors.TextPrimary
                    )
                    Text(
                        "Aquí tienes el resumen de hoy",
                        fontSize = 14.sp,
                        color = CarsilColors.TextSecondary
                    )
                }

                // STATS ROW
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        val total = stats["clientes"] ?: 0
                        val nuevos = stats["clientes_30d"] ?: 0
                        val trendVal = if (total > 0) (nuevos * 100f / total) else 0f
                        StatCard(
                            label = "Total Clientes",
                            value = total.toString(),
                            subLabel = "Cartera activa",
                            icon = Icons.Default.Groups,
                            trend = if (nuevos > 0) String.format("%.1f%%", trendVal) else "0.0%",
                            isTrendUp = nuevos > 0
                        )
                    }
                    item {
                        val hoy = stats["proformas_hoy"] ?: 0
                        val ayer = stats["proformas_ayer"] ?: 0
                        val trendVal = when {
                            ayer == 0 && hoy > 0 -> 100f
                            ayer == 0 -> 0f
                            else -> ((hoy - ayer) * 100f / ayer)
                        }
                        StatCard(
                            label = "Proformas",
                            value = hoy.toString(),
                            subLabel = "Emitidas hoy",
                            icon = Icons.Default.ReceiptLong,
                            iconBgColor = CarsilColors.SuccessLight,
                            iconTintColor = CarsilColors.Success,
                            trend = String.format("%.1f%%", Math.abs(trendVal)),
                            isTrendUp = trendVal >= 0
                        )
                    }
                    item {
                        val total = stats["productos"] ?: 0
                        val nuevos = stats["productos_30d"] ?: 0
                        val trendVal = if (total > 0) (nuevos * 100f / total) else 0f
                        StatCard(
                            label = "Productos",
                            value = total.toString(),
                            subLabel = "Stock verificado",
                            icon = Icons.Default.Inventory,
                            iconBgColor = Color(0xFFF3E5F5),
                            iconTintColor = Color(0xFF8E24AA),
                            trend = if (nuevos > 0) String.format("%.1f%%", trendVal) else "0.0%",
                            isTrendUp = nuevos >= 0
                        )
                    }
                    item {
                        val total = stats["empleados"] ?: 0
                        val nuevos = stats["empleados_30d"] ?: 0
                        val trendVal = if (total > 0) (nuevos * 100f / total) else 0f
                        StatCard(
                            label = "Empleados",
                            value = total.toString(),
                            subLabel = "Personal CARSIL",
                            icon = Icons.Default.Badge,
                            iconBgColor = Color(0xFFFFF8E1),
                            iconTintColor = Color(0xFFFBC02D),
                            trend = if (nuevos > 0) String.format("%.1f%%", trendVal) else "0.0%",
                            isTrendUp = nuevos >= 0
                        )
                    }
                }

                // CHARTS SECTION
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CarsilShapes.Medium,
                        color = Color.White,
                        border = BorderStroke(1.dp, CarsilColors.Stroke)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        "Distribucion por Modulo",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CarsilColors.TextPrimary
                                    )
                                    Text(
                                        "Registros totales en tiempo real",
                                        modifier = Modifier.padding(top = 8.dp),
                                        fontSize = 11.sp,
                                        color = CarsilColors.TextSecondary
                                    )
                                }
                                Icon(Icons.Default.Analytics, null, tint = CarsilColors.Primary)
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))

                            val moduleLabels = listOf("Clientes", "Proformas", "Productos", "Empleados")
                            val moduleTotalData = listOf(
                                (stats["clientes"] ?: 0).toFloat(),
                                (stats["proformas"] ?: 0).toFloat(),
                                (stats["productos"] ?: 0).toFloat(),
                                (stats["empleados"] ?: 0).toFloat()
                            )

                            if (moduleTotalData.any { it > 0f }) {
                                com.example.appcarsilauth.ui.components.CarsilBarChart(
                                    data = moduleTotalData,
                                    labels = moduleLabels,
                                    barColor = CarsilColors.Primary,
                                    unit = "Registros"
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Sin datos de modulos aun",
                                        color = CarsilColors.TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // RECENT PROFORMAS TABLE
                    Text(
                        "Proformas Recientes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CarsilColors.TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                        shape = CarsilShapes.Medium,
                        color = Color.White,
                        border = BorderStroke(1.dp, CarsilColors.Stroke)
                    ) {
                        if (recentProformas.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay registros recientes", color = CarsilColors.TextMuted, fontSize = 13.sp)
                            }
                        } else {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                recentProformas.take(10).forEachIndexed { index, prof ->
                                    RecentProformaItem(
                                        codigo = (prof["Codigo"] ?: "#---").toString(),
                                        cliente = (prof["Cliente"] ?: "Cliente Final").toString(),
                                        monto = "S/ " + String.format("%.2f", (prof["Total"] as? Number)?.toDouble() ?: 0.0),
                                        estado = (prof["Estado"] ?: "PENDIENTE").toString().uppercase(),
                                        isLast = index == (recentProformas.take(10).size - 1)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // FOOTER
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CarsilShapes.Small,
                        color = CarsilColors.Surface,
                        border = BorderStroke(1.dp, CarsilColors.Stroke)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Panel de Gestión Seguro CARSIL SAC",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun RecentProformaItem(
    codigo: String,
    cliente: String,
    monto: String,
    estado: String,
    isLast: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = codigo, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 14.sp, 
                    color = CarsilColors.TextPrimary
                )
                Text(
                    text = cliente, 
                    fontSize = 12.sp, 
                    color = CarsilColors.TextSecondary, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = monto, 
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 14.sp, 
                    color = CarsilColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = when(estado) {
                        "APROBADA", "VENDIDA" -> CarsilColors.SuccessLight
                        "PENDIENTE" -> Color(0xFFFFF8E1)
                        else -> CarsilColors.DangerLight
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = estado,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
        
        if (!isLast) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CarsilColors.Stroke, thickness = 0.5.dp)
        }
    }
}

@Composable
fun MenuCard(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(130.dp),
        shape = CarsilShapes.Medium,
        color = CarsilColors.Surface,
        border = BorderStroke(1.dp, CarsilColors.Stroke)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CarsilColors.PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = CarsilColors.Primary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = CarsilColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Gestionar",
                fontSize = 11.sp,
                color = Color.Black
            )
        }
    }
}