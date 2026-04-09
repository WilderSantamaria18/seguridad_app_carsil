package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import java.math.BigDecimal

@Composable
fun DashboardScreen(
    email: String = "admin@carsil.com",
    roleId: Int = 1,
    allowedMenus: List<String> = emptyList(),
    onGoToClients: () -> Unit = {},
    onGoToProducts: () -> Unit = {},
    onGoToProforma: () -> Unit = {},
    onGoToProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
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
        containerColor = Color(0xFFF8F9FA) // Fondo gris extra claro muy elegante
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // HEADER CORPORATIVO
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = email.first().uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ERP CARSIL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = roleName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
                
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, "Salir", tint = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // TARJETA DE INGRESOS (SOLO PARA ADMIN/SUP)
            if (roleId == 1 || roleId == 3) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "INGRESOS VERIFICADOS",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Icon(Icons.Default.TrendingUp, null, tint = Color(0xFF00C853), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "S/ ${verifiedTotalIncome.toPlainString()}",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Actualizado ahora",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Modulos de Gestion",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // GRILLA DE MODULOS
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Fila 1
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (canUseClients) {
                        MenuCard("Clientes", Icons.Default.Groups, onGoToClients, Modifier.weight(1f))
                    }
                    if (canUseProducts) {
                        MenuCard("Inventario", Icons.Default.Inventory, onGoToProducts, Modifier.weight(1f))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Fila 2
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (canUseProformas) {
                        MenuCard("Proformas", Icons.Default.ReceiptLong, onGoToProforma, Modifier.weight(1f))
                    }
                    // Siempre visible: Perfil
                    MenuCard("Mi Cuenta", Icons.Default.AccountCircle, onGoToProfile, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                // Info de Pie
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Sistema CARSIL v4.0.2 - Acceso Seguro",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun MenuCard(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        shadowElevation = 2.dp
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
                    .background(Color(0xFFF1F3F4)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.Black, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Gestionar",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
