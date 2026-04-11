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
    userId: Int,
    onGoToChangePassword: () -> Unit,
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, CarsilColors.Stroke, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = CarsilColors.Primary, modifier = Modifier.size(18.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        "Mi Perfil",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CarsilColors.TextPrimary
                    )
                }
            }
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

            // Avatar (Flat Solid Color)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(CarsilColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.ifEmpty { email }.first().uppercase(),
                    color = Color.Black,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = CarsilColors.TextPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
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
                                .background(if (isBiometricEnrolled) CarsilColors.Success else CarsilColors.Primary),
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
                                if (isBiometricEnrolled) "Activada" else "No configurada",
                                fontSize = 13.sp,
                                color = Color.Black
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
                            shape = CarsilShapes.Small,
                            color = CarsilColors.PrimaryLight
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info, 
                                    null, 
                                    tint = CarsilColors.Primary, 
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    "Tu dispositivo no cuenta con biometría activa o disponible. Para habilitar esta función, regístrala en los Ajustes de Seguridad de tu sistema operativo.",
                                    fontSize = 13.sp,
                                    color = CarsilColors.TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    } else if (isBiometricEnrolled) {
                        // Botón para DESACTIVAR
                        Button(
                            onClick = onRemoveBiometric,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = CarsilShapes.Small,
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Black
                            ),
                            border = BorderStroke(1.dp, CarsilColors.Danger)
                        ) {
                            Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DESACTIVAR HUELLA", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Botón para ACTIVAR
                        Button(
                            onClick = onEnrollBiometric,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = CarsilShapes.Small,
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CarsilColors.Primary)
                        ) {
                            Icon(Icons.Default.Fingerprint, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ACTIVAR HUELLA DACTILAR", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info adicional de seguridad
            ProfileInfoRow(Icons.Default.Badge, "Rol", roleName)
            ProfileInfoRow(Icons.Default.Shield, "Autenticación", "Activa")
            ProfileInfoRow(Icons.Default.Lock, "Sesión", "Protegida")
            ProfileInfoRow(Icons.Default.EnhancedEncryption, "Cifrado", "AES-256")

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Cambiar Contraseña
            Button(
                onClick = onGoToChangePassword,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CarsilShapes.Small,
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CarsilColors.Primary)
            ) {
                    Icon(Icons.Default.Password, null, tint = Color.Black)
                Spacer(modifier = Modifier.width(12.dp))
                    Text("CAMBIAR CONTRASEÑA", fontWeight = FontWeight.Bold, color = Color.Black)
            }

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
        shape = CarsilShapes.Small,
        color = CarsilColors.Surface,
        border = BorderStroke(1.dp, CarsilColors.Stroke)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = CarsilColors.Primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = CarsilColors.TextPrimary, modifier = Modifier.width(100.dp))
            Spacer(modifier = Modifier.weight(1f))
            Text(
                value, 
                fontSize = 12.sp, 
                color = CarsilColors.TextSecondary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
