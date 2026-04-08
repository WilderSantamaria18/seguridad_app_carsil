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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import com.example.appcarsilauth.ui.components.CaptchaComponent
import com.example.appcarsilauth.ui.components.CarsilColors
import com.example.appcarsilauth.ui.components.CarsilShapes
import com.example.appcarsilauth.ui.viewmodel.AuthState
import com.example.appcarsilauth.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val authState by viewModel.authState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = CarsilColors.Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Bienvenido de vuelta",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = CarsilColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Ingresa tu correo y contraseña para acceder al sistema.",
                    fontSize = 14.sp,
                    color = CarsilColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Alerta de Error
                if (authState is AuthState.Error) {
                    Text(
                        (authState as AuthState.Error).message,
                        color = Color.Red,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Campos de Entrada
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Correo electrónico",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = CarsilColors.TextPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("usuario@carsil.com", color = CarsilColors.Gray400) },
                        shape = CarsilShapes.Medium,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            unfocusedContainerColor = CarsilColors.Surface,
                            focusedContainerColor = CarsilColors.Surface,
                            unfocusedBorderColor = CarsilColors.Stroke,
                            focusedBorderColor = CarsilColors.TextPrimary,
                            cursorColor = CarsilColors.TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Contraseña",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = CarsilColors.TextPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = viewModel.pin,
                        onValueChange = { viewModel.onPinChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("••••••••", color = CarsilColors.Gray400) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = CarsilColors.Gray400)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = CarsilShapes.Medium,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            unfocusedContainerColor = CarsilColors.Surface,
                            focusedContainerColor = CarsilColors.Surface,
                            unfocusedBorderColor = CarsilColors.Stroke,
                            focusedBorderColor = CarsilColors.TextPrimary,
                            cursorColor = CarsilColors.TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = viewModel.rememberMe,
                        onCheckedChange = { viewModel.rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.Black,
                            uncheckedColor = Color.Gray,
                            checkmarkColor = Color.White
                        )
                    )
                    Text(
                        "Recordarme",
                        fontSize = 14.sp,
                        color = CarsilColors.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón Principal - Iniciar Sesión (Paso 1)
                Button(
                    onClick = { viewModel.preVerifyLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = CarsilShapes.Full,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    if (authState is AuthState.Loading && !viewModel.isCaptchaModalVisible) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Iniciar sesión", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal de Captcha (Paso 2)
    if (viewModel.isCaptchaModalVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCaptchaModal() },
            title = { Text("Verificación de Seguridad", color = Color.Black, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Resuelve el captcha para continuar. Intento ${viewModel.captchaAttemptsInModal} de 5.",
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    CaptchaComponent(onCaptchaGenerated = { viewModel.setGeneratedCaptcha(it) })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.userCaptchaInput,
                        onValueChange = { viewModel.onCaptchaInputChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Escribe el código") },
                        shape = CarsilShapes.Medium,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color.Black,
                            cursorColor = Color.Black
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.finalizeLoginWithCaptcha() },
                    shape = CarsilShapes.Full,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text("Verificar e Ingresar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCaptchaModal() }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = CarsilShapes.Medium
        )
    }

    // Overlay de Bloqueo Temporal
    if (authState is AuthState.LockedOut) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("CUENTA BLOQUEADA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                val state = authState as AuthState.LockedOut
                val min = (state.remainingTimeMs / 1000) / 60
                val sec = (state.remainingTimeMs / 1000) % 60
                Text(
                    text = String.format("%02d:%02d", min, sec),
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }

    // PANTALLA DE ÉXITO (ANIMACIÓN)
    AnimatedVisibility(
        visible = authState is AuthState.SuccessAnimation,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            // Card Blanca Curva al fondo (Estilo de la imagen)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f),
                shape = RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp),
                color = Color.White
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Círculo Verde con Check (Simulando Confeti con Canvas)
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                        Canvas(modifier = Modifier.size(140.dp)) {
                            // Puntos de Confeti estáticos de colores
                            drawCircle(Color(0xFFFFC107), 4f, Offset(20f, 30f))
                            drawCircle(Color(0xFFE91E63), 5f, Offset(110f, 10f))
                            drawCircle(Color(0xFF2196F3), 4f, Offset(130f, 60f))
                            drawCircle(Color(0xFF4CAF50), 3f, Offset(10f, 90f))
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00C853)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "¡Acceso Exitoso!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Tu identidad ha sido verificada.\nRedirigiendo al inicio...",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // Cargador sutil
                    LinearProgressIndicator(
                        modifier = Modifier.width(100.dp).clip(CircleShape),
                        color = Color.Black,
                        trackColor = Color.LightGray
                    )
                }
            }
        }
    }
}
