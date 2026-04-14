package com.example.appcarsilauth.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.ui.components.CarsilColors
import com.example.appcarsilauth.ui.components.CarsilShapes
import com.example.appcarsilauth.ui.components.CaptchaComponent
import com.example.appcarsilauth.ui.viewmodel.AuthState
import com.example.appcarsilauth.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    isBiometricEnrolledForThisUser: Boolean,
    onFingerprintClick: () -> Unit = {}
) {
    val authState by viewModel.authState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    val backgroundSolid = CarsilColors.Background

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundSolid)
                .padding(padding)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.Black.copy(alpha = 0.03f),
                    radius = 400f,
                    center = Offset(size.width * 0.9f, size.height * 0.1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                AnimatedContent(
                    targetState = viewModel.loginStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    label = "stepAnimation"
                ) { step ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (step == 1) {
                            // EL LOGO SOLO APARECE EN EL PASO 1
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.appcarsilauth.R.drawable.logo_carsil),
                                contentDescription = "Logo Carsil",
                                modifier = Modifier
                                    .size(320.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(0.dp))

                            Text(
                                text = "Gestión Empresarial Inteligente\nIngresa tu correo para comenzar.",
                                fontSize = 15.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = viewModel.email,
                                onValueChange = { viewModel.onEmailChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Correo electrónico", color = Color.Black) },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.AlternateEmail, null, tint = Color.Black) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedBorderColor = Color.Black,
                                    unfocusedBorderColor = Color.Gray,
                                    cursorColor = Color.Black,
                                    focusedLabelColor = Color.Black
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.rememberMe = !viewModel.rememberMe },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = viewModel.rememberMe,
                                    onCheckedChange = { viewModel.rememberMe = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = CarsilColors.Primary,
                                        uncheckedColor = Color.Black
                                    )
                                )
                                Text(
                                    text = "Recuérdame",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.identifyUser() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp),
                                shape = CarsilShapes.Small,
                                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CarsilColors.Primary,
                                    contentColor = Color.Black
                                )
                            ) {
                                if (authState is AuthState.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("IDENTIFICARME", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // PASO 2: BIENVENIDA (SIN LOGO)
                            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.backToStep1() },
                                        modifier = Modifier.background(Color.White, CircleShape).size(36.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(16.dp))
                                        Text("Cambiar cuenta", fontSize = 14.sp, color = Color.Black)
                                }
                                
                                Spacer(modifier = Modifier.height(40.dp))
                                
                                Text(
                                    text = "Bienvenido al sistema,",
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = viewModel.identifiedUserName,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    lineHeight = 36.sp
                                )

                                Spacer(modifier = Modifier.height(40.dp))

                                OutlinedTextField(
                                    value = viewModel.pin,
                                    onValueChange = { viewModel.onPinChange(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Ingresa tu contraseña", color = Color.Black) },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = Color.Black)
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        focusedBorderColor = Color.Black,
                                        unfocusedBorderColor = Color.Gray,
                                        cursorColor = Color.Black,
                                        focusedLabelColor = Color.Black
                                    )
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = { viewModel.preVerifyLogin() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(58.dp),
                                    shape = CarsilShapes.Small,
                                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CarsilColors.Primary,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    if (authState is AuthState.Loading && !viewModel.isCaptchaModalVisible) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text("ENTRAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(30.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isBiometricEnrolledForThisUser) Color.White else Color.Transparent)
                                        .border(
                                            1.dp, 
                                            if (isBiometricEnrolledForThisUser) Color.Black.copy(alpha = 0.1f) else Color.Transparent, 
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable(enabled = isBiometricEnrolledForThisUser) { onFingerprintClick() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Fingerprint, 
                                            null, 
                                            modifier = Modifier.size(32.dp),
                                            tint = if (isBiometricEnrolledForThisUser) Color.Black else Color.Gray.copy(alpha = 0.4f)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = if (isBiometricEnrolledForThisUser) "Acceder con Biometría" else "Huella no configurada",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (authState is AuthState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEBEB),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = (authState as AuthState.Error).message,
                                color = Color(0xFFB91C1C),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (viewModel.isCaptchaModalVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCaptchaModal() },
            title = { 
                Text(
                    "Seguridad Adicional", 
                    fontWeight = FontWeight.Bold, 
                    color = CarsilColors.TextPrimary,
                    fontSize = 18.sp
                ) 
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Resuelve el captcha para verificar tu ingreso.", 
                        color = CarsilColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    CaptchaComponent(onCaptchaGenerated = { viewModel.setGeneratedCaptcha(it) })
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = viewModel.userCaptchaInput,
                        onValueChange = { viewModel.onCaptchaInputChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Código de imagen",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        shape = CarsilShapes.Small,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CarsilColors.Primary,
                            unfocusedBorderColor = CarsilColors.Stroke,
                            focusedTextColor = CarsilColors.TextPrimary,
                            unfocusedTextColor = CarsilColors.TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.finalizeLoginWithCaptcha() },
                    shape = CarsilShapes.Small,
                    colors = ButtonDefaults.buttonColors(containerColor = CarsilColors.Primary, contentColor = Color.Black)
                ) {
                    Text("VERIFICAR")
                }
            },
            containerColor = CarsilColors.Surface,
            tonalElevation = 0.dp
        )
    }

    // ── Pantalla de Usuario Inactivo ──────────────────────────────────
    if (authState is AuthState.InactiveUser) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ícono de cuenta bloqueada
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PersonOff,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(58.dp)
                    )
                }

                Text(
                    "Acceso Denegado",
                    color = Color(0xFF1F2937),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    "Tu cuenta ha sido desactivada y no tienes\nacceso al sistema CARSIL.",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Cuadro de aviso de contacto
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF0F9FF),
                    border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            "Comunícate con el Administrador",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFF0369A1),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Para reactivar tu cuenta o consultar el motivo de la desactivación, " +
                            "contacta al Administrador del Sistema CARSIL.",
                            fontSize = 13.sp,
                            color = Color(0xFF0284C7),
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Botón para volver
                Button(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1F2937),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Volver al inicio de sesión",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // ── Pantalla de Bloqueo por intentos ─────────────────────────────
    if (authState is AuthState.LockedOut) {
        val lockedState = authState as AuthState.LockedOut
        val totalSecs = (lockedState.remainingTimeMs / 1000).toInt()
        val mins = totalSecs / 60
        val secs = totalSecs % 60

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ícono de candado
                Box(
                    modifier = androidx.compose.ui.Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = androidx.compose.ui.Modifier.size(52.dp)
                    )
                }

                Text(
                    "Cuenta Bloqueada",
                    color = Color(0xFF1F2937),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    "Has superado los 5 intentos fallidos.\nPor seguridad, tu acceso está temporalmente bloqueado.",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))

                // Contador regresivo grande
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF3F4F6),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(horizontal = 40.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tiempo restante",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            color = Color(0xFFDC2626),
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "minutos : segundos",
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF7E6),
                    border = BorderStroke(1.dp, Color(0xFFFFD591))
                ) {
                    Row(
                        modifier = androidx.compose.ui.Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            null,
                            tint = Color(0xFFB45309),
                            modifier = androidx.compose.ui.Modifier.size(18.dp)
                        )
                        Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        Text(
                            "El bloqueo se levantará automáticamente al llegar a 00:00.",
                            fontSize = 12.sp,
                            color = Color(0xFFB45309),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }


    AnimatedVisibility(
        visible = authState is AuthState.SuccessAnimation,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = CarsilShapes.Medium,
                color = CarsilColors.Surface,
                tonalElevation = 0.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(CarsilColors.Success),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "¡Acceso Concedido!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = CarsilColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Identidad verificada correctamente.\nCargando panel de gestión...",
                        fontSize = 14.sp,
                        color = CarsilColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = CarsilColors.Primary,
                        trackColor = CarsilColors.PrimaryLight
                    )
                }
            }
        }
    }
}
