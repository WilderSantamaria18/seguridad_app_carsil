package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
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
import java.math.BigDecimal

@Composable
fun DashboardScreen(
    email: String = "admin@carsil.com",
    roleId: Int = 1,
    onGoToProforma: () -> Unit = {},
    onGoToInventory: () -> Unit = {},
    onLogout: () -> Unit = {},
    verifiedTotalIncome: BigDecimal = BigDecimal("23824.20")
) {
    val roleName = when(roleId) {
        1 -> "ADMINISTRADOR"
        2 -> "EMPLEADO"
        3 -> "SUPERVISOR"
        4 -> "VENDEDOR"
        else -> "DESCONOCIDO"
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFece9e6), Color(0xFFffffff))
                )
            )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Dashboard",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2C5364),
                modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
            )

            // RBAC Verification - Only show financial metrics to Management/Administrators
            if (roleId == 1 || roleId == 3) { // 1: Administrador, 3: Supervisor
                FinancialCard(verifiedTotalIncome)
            } else {
                Text(
                    text = "No tienes los permisos necesarios para ver las métricas financieras (CARSIL-POL-LEY).",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Tus Módulos",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ModuleCard("Proformas", Icons.Default.List, Color(0xFF4FC3F7))
                ModuleCard("Catálogo", Icons.Default.Build, Color(0xFF69F0AE))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sesión activa de: $email",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Intranet Proforma & Inventory Module Button (Only non-employees can do this)
            if (roleId != 2) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onGoToProforma,
                        modifier = Modifier.weight(1f).height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
                    ) {
                        Text("PROFORMAS", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    
                    // Nuevo Módulo de Inventario
                    Button(
                        onClick = { onGoToInventory() },
                        modifier = Modifier.weight(1f).height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B49DF))
                    ) {
                        Text("INVENTARIO", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logout Button
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FinancialCard(total: BigDecimal) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF).copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "INGRESOS VERIFICADOS (S/)",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "S/ ${total.toPlainString()}",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Solo considerando ventas en estado 'COMPLETADA' con precisión BigDecimal.",
                color = Color(0xFF69F0AE),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ModuleCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color) {
    Card(
        modifier = Modifier
            .size(120.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF2C5364))
        }
    }
}
