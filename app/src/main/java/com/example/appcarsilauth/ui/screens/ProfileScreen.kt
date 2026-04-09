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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.ui.components.CarsilColors
import com.example.appcarsilauth.ui.components.CarsilShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    email: String,
    userName: String,
    roleId: Int,
    isBiometricEnrolled: Boolean,
    isBiometricAvailable: Boolean,
    onEnrollBiometric: () -> Unit,
    onRemoveBiometric: () -> Unit,
    onBack: () -> Unit
) {
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
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CarsilColors.Background,
                    titleContentColor = CarsilColors.TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = email.first().uppercase(),
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = CarsilColors.TextPrimary
            )

            Text(
                text = roleName,
                fontSize = 14.sp,
                color = CarsilColors.TextSecondary
            )

            Text(
                text = email,
                fontSize = 14.sp,
                color = CarsilColors.TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Sección de Seguridad
            Text(
                text = "Seguridad",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = CarsilColors.TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de Autenticación Biométrica
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CarsilShapes.Medium,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CarsilColors.Stroke)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isBiometricEnrolled) Color(0xFF00C853) else Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Huella Dactilar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = CarsilColors.TextPrimary
                            )
                            Text(
                                if (isBiometricEnrolled) "Activada y enlazada" else "No configurada",
                                fontSize = 13.sp,
                                color = if (isBiometricEnrolled) Color(0xFF00C853) else CarsilColors.TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isBiometricEnrolled)
                            "Tu huella dactilar está enlazada a esta cuenta. Puedes usarla para ingresar rápidamente sin escribir contraseña."
                        else
                            "Registra tu huella dactilar para iniciar sesión de forma rápida y segura sin necesidad de escribir tu contraseña.",
                        fontSize = 13.sp,
                        color = CarsilColors.TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!isBiometricAvailable) {
                        // Dispositivo sin biometría
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF3E0)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Tu dispositivo no tiene biometría configurada. Ve a Ajustes > Seguridad para registrar una huella.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE65100),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    } else if (isBiometricEnrolled) {
                        // Botón para DESACTIVAR
                        Button(
                            onClick = onRemoveBiometric,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Red
                            ),
                            border = BorderStroke(1.dp, Color.Red)
                        ) {
                            Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Desactivar Huella", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Botón para ACTIVAR
                        Button(
                            onClick = onEnrollBiometric,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Icon(Icons.Default.Fingerprint, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Activar Huella Dactilar", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info adicional de seguridad
            ProfileInfoRow(Icons.Default.Shield, "Autenticación", "JWT + CAPTCHA + Biometría")
            ProfileInfoRow(Icons.Default.Lock, "Sesión", "Expira por inactividad (10 min)")
            ProfileInfoRow(Icons.Default.Security, "Cifrado", "AES-256 + HMAC-SHA256")

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CarsilColors.Stroke)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.Black, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = CarsilColors.TextPrimary)
            Spacer(modifier = Modifier.weight(1f))
            Text(value, fontSize = 12.sp, color = CarsilColors.TextSecondary)
        }
    }
}
