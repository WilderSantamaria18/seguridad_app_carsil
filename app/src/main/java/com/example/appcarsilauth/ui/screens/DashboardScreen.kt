package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onLogout: () -> Unit = {},
    verifiedTotalIncome: java.math.BigDecimal = java.math.BigDecimal("23824.20")
) {
    val canUseClients = if (allowedMenus.isEmpty()) roleId != 2 else allowedMenus.contains("Clientes")
    val canUseProducts = if (allowedMenus.isEmpty()) roleId != 2 else allowedMenus.contains("Productos")
    val canUseProformas = if (allowedMenus.isEmpty()) roleId != 2 else allowedMenus.contains("Proformas")

    Scaffold(
        containerColor = CarsilColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Dashboard",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = CarsilColors.TextPrimary
            )
            
            Text(
                text = "Sesión activa: $email",
                fontSize = 14.sp,
                color = CarsilColors.TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Financial Metrics (Administrador / Supervisor)
            if (roleId == 1 || roleId == 3) {
                FinancialCardMinimal(verifiedTotalIncome)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Operaciones",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = CarsilColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Module Navigation Blocks
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (canUseClients) {
                    OperationButton("Gestión de Clientes", Icons.Default.Person, onGoToClients)
                }
                if (canUseProducts) {
                    OperationButton("Inventario de Productos", Icons.Default.Build, onGoToProducts)
                }
                if (canUseProformas) {
                    OperationButton("Proformas y Ventas", Icons.Default.List, onGoToProforma)
                }
                
                if (!canUseClients && !canUseProducts && !canUseProformas) {
                    Text(
                        "No tienes módulos operativos asignados.",
                        color = CarsilColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Logout Pill Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CarsilShapes.Full,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, CarsilColors.Stroke)
            ) {
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FinancialCardMinimal(total: java.math.BigDecimal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CarsilShapes.Medium,
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "INGRESOS TOTALES",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "S/ ${total.toPlainString()}",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Calculado según ventas verificadas",
                color = Color.Green.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun OperationButton(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = CarsilShapes.Medium,
        color = Color.White,
        border = BorderStroke(1.dp, CarsilColors.Stroke)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CarsilShapes.Small)
                    .background(Color.Black),   
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CarsilColors.TextPrimary)
        }
    }
}
