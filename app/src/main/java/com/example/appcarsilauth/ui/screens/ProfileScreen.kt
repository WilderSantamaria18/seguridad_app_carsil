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

    // Los empleados (roleId == 2) tienen perfil de solo lectura
    val isEmployeeReadOnly = roleId == 2

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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Volver",
                            tint = CarsilColors.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        "Mi Perfil",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CarsilColors.TextPrimary
                    )

                    // Badge "Solo lectura" en el header para empleados
                    if (isEmployeeReadOnly) {
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            color = Color(0xFFFFF3CD),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Solo lectura",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Avatar ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEmployeeReadOnly) Color(0xFF6366F1) else CarsilColors.Primary
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.ifEmpty { email }.first().uppercase(),
                    color = if (isEmployeeReadOnly) Color.White else Color.Black,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = userName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = CarsilColors.TextPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            // Badge de rol debajo del nombre
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = if (isEmployeeReadOnly) Color(0xFFEDE9FE) else CarsilColors.PrimaryLight,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Badge,
                        null,
                        tint = if (isEmployeeReadOnly) Color(0xFF6366F1) else CarsilColors.Primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        roleName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEmployeeReadOnly) Color(0xFF6366F1) else CarsilColors.Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Banner de aviso solo para empleados ────────────────────
            if (isEmployeeReadOnly) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFF7E6),
                    border = BorderStroke(1.dp, Color(0xFFFFD591))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Perfil de solo lectura",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color(0xFF92400E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Solo el Administrador puede modificar tus datos personales, " +
                                "cambiar tu contraseña o actualizar tu información de cuenta.",
                                fontSize = 12.sp,
                                color = Color(0xFFB45309),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            // ── Datos de la Cuenta (todos los roles) ───────────────────
            ProfileSectionTitle("Datos de la Cuenta")
            Spacer(modifier = Modifier.height(10.dp))

            ProfileInfoRow(Icons.Default.Email, "Correo", email)
            ProfileInfoRow(Icons.Default.Badge, "Rol", roleName)

            Spacer(modifier = Modifier.height(24.dp))

            // ── Sección Seguridad ──────────────────────────────────────
            ProfileSectionTitle("Seguridad")
            Spacer(modifier = Modifier.height(10.dp))

            if (!isEmployeeReadOnly) {
                // ADMIN / SUPERVISOR: tarjeta de huella completa
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
                                    .background(
                                        if (isBiometricEnrolled) CarsilColors.Success
                                        else CarsilColors.Primary
                                    ),
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
                                "Tu huella dactilar está enlazada. Puedes usarla para ingresar rápidamente."
                            else
                                "Registra tu huella dactilar para iniciar sesión de forma rápida y segura.",
                            fontSize = 13.sp,
                            color = CarsilColors.TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        when {
                            !isBiometricAvailable -> {
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
                                            "Tu dispositivo no cuenta con biometría activa o disponible.",
                                            fontSize = 13.sp,
                                            color = CarsilColors.TextSecondary,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                            isBiometricEnrolled -> {
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
                            }
                            else -> {
                                Button(
                                    onClick = onEnrollBiometric,
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = CarsilShapes.Small,
                                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CarsilColors.Primary)
                                ) {
                                    Icon(
                                        Icons.Default.Fingerprint,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "ACTIVAR HUELLA DACTILAR",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                ProfileInfoRow(Icons.Default.Shield, "Autenticación", "Activa")
                ProfileInfoRow(Icons.Default.Lock, "Sesión", "Protegida")
                ProfileInfoRow(Icons.Default.EnhancedEncryption, "Cifrado", "AES-256")


                Spacer(modifier = Modifier.height(24.dp))

            } else {
                // EMPLEADO: filas de seguridad en lectura
                ProfileInfoRow(Icons.Default.Shield, "Autenticación", "Activa")
                ProfileInfoRow(Icons.Default.Lock, "Sesión", "Protegida")
                ProfileInfoRow(Icons.Default.EnhancedEncryption, "Cifrado", "AES-256")

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Botón Cambiar Contraseña (todos los roles, deshabilitado para empleado) ──
            Button(
                onClick = if (!isEmployeeReadOnly) onGoToChangePassword else { {} },
                enabled = !isEmployeeReadOnly,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CarsilShapes.Small,
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CarsilColors.Primary,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFFE5E7EB),
                    disabledContentColor = Color(0xFF9CA3AF)
                )
            ) {
                Icon(
                    if (isEmployeeReadOnly) Icons.Default.LockPerson else Icons.Default.Password,
                    null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isEmployeeReadOnly)
                        "CAMBIAR CONTRASEÑA  •  Solo el Administrador"
                    else
                        "CAMBIAR CONTRASEÑA",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        color = CarsilColors.TextSecondary,
        letterSpacing = 0.8.sp,
        modifier = Modifier.fillMaxWidth()
    )
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                tint = CarsilColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = CarsilColors.TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    value,
                    fontSize = 14.sp,
                    color = CarsilColors.TextPrimary,
                    fontWeight = FontWeight.Medium,
                    softWrap = true,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Visible
                )
            }
        }
    }
}
